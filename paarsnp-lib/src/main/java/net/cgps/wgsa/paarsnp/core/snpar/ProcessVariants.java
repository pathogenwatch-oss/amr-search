package net.cgps.wgsa.paarsnp.core.snpar;

import net.cgps.wgsa.paarsnp.core.lib.DnaSequence;
import net.cgps.wgsa.paarsnp.core.lib.SequenceType;
import net.cgps.wgsa.paarsnp.core.lib.blast.BlastMatch;
import net.cgps.wgsa.paarsnp.core.lib.blast.MutationSearchMatch;
import net.cgps.wgsa.paarsnp.core.snpar.json.Mutation;
import net.cgps.wgsa.paarsnp.core.snpar.json.SnpResistanceElement;
import net.cgps.wgsa.paarsnp.core.snpar.json.SnparLibrary;
import net.cgps.wgsa.paarsnp.core.snpar.json.SnparMatchData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ProcessVariants implements Function<BlastMatch, SnparMatchData> {

  private final Logger logger = LoggerFactory.getLogger(ProcessVariants.class);

  private final SnparLibrary snparLibrary;

  public ProcessVariants(final SnparLibrary snparLibrary) {

    this.snparLibrary = snparLibrary;
  }

  @Override
  public SnparMatchData apply(final BlastMatch mutationSearchResult) {

    @SuppressWarnings("OptionalGetWithoutIsPresent") final SnparReferenceSequence snparReferenceSequence = this.snparLibrary.getSequence(mutationSearchResult.getBlastSearchStatistics().getLibrarySequenceId()).get(); // We know it's there.

    final Collection<SnpResistanceElement> snpResistanceElements = new ArrayList<>(100);

    snparReferenceSequence
        .getResistanceMutations()
        .stream()
        .peek(mutation -> this.logger.debug("Resistance mutation {}", mutation.getName()))
        .filter(resistanceMutation -> resistanceMutation.getRepSequenceLocation() > mutationSearchResult.getBlastSearchStatistics().getLibrarySequenceStart())
        .filter(resistanceMutation -> resistanceMutation.getRepSequenceLocation() + 3 < mutationSearchResult.getBlastSearchStatistics().getLibrarySequenceStop())
        .peek(mutation -> this.logger.debug("Mutation {} in range", mutation.getName()))
        .forEach(mutation -> {

              // Generate a reference to query index
              this.logger.debug("mutationId={} refId={} repLocation={} repStart={} queryStart={} reverse={}", mutation.getName(), snparReferenceSequence.getSequenceId(), mutation.getRepSequenceLocation(), mutationSearchResult.getBlastSearchStatistics().getLibrarySequenceStart(), mutationSearchResult.getBlastSearchStatistics().isReversed());

              if (SequenceType.PROTEIN == mutation.getSequenceType()) {

                // Go through the mutations and gather those that overlap the reference region

                final List<Mutation> overlappingMutations = mutationSearchResult
                    .getMutations()
                    .stream()
                    .peek(queryMutation -> this.logger.trace("Testing {} {} v {}", queryMutation.getReferenceLocation(), queryMutation.getMutationSequence(), mutation.getRepSequenceLocation()))
                    // Start of query mutation <= codon end && end of query mutation >= codon start
                    .filter(queryMutation -> (queryMutation.getReferenceLocation() <= mutation.getRepSequenceLocation() + 2) && (queryMutation.getReferenceLocation() + queryMutation.getMutationSequence().length() - 1 >= mutation.getRepSequenceLocation()))
                    .peek(queryMutation -> this.logger.debug("found mutation {} {}", queryMutation.getReferenceLocation(), queryMutation.getMutationSequence()))
                    .collect(Collectors.toList());

                // Generate the new codon and check the amino acid.
                if (!overlappingMutations.isEmpty()) {

                  this.logger.debug("Codon boundary substring indexes = {} - {} (seq boundaries = {} - {}", mutation.getRepSequenceLocation() - 1, mutation.getRepSequenceLocation() + 2, mutationSearchResult.getBlastSearchStatistics().getLibrarySequenceStart(), mutationSearchResult.getBlastSearchStatistics().getLibrarySequenceStop());

                  String codon = snparReferenceSequence.getSequence().substring(mutation.getRepSequenceLocation() - 1, mutation.getRepSequenceLocation() + 2);

                  for (final Mutation queryMutation : overlappingMutations) {

                    final int codonRelativeLocation = queryMutation.getReferenceLocation() - mutation.getRepSequenceLocation() + 1;

                    this.logger.debug("{} Codon relative position = {} mutant={} currentSeq = {}", queryMutation.getReferenceLocation(), codonRelativeLocation, queryMutation.getMutationSequence(), codon);

                    // Need to substring the mutation to the correct length (e.g. multi-nucleotide substitution)
                    // if codon position = -1  then 2 nts to remove
                    // if codon position = 1 then 0 nts to remove
                    // if codon position = 2 then 0 nts to remove
                    final int trimStart = codonRelativeLocation < 1 ? 1 - codonRelativeLocation : 0;

                    final int codonPosition = codonRelativeLocation < 1 ? 1 : codonRelativeLocation;

                    this.logger.debug("nts to trim={} codonPosition={}", trimStart, codonPosition);

                    final String trimmedMutation = queryMutation.getMutationSequence().substring(trimStart);

                    final String mutationSequence = codonRelativeLocation < 1 ? trimmedMutation : queryMutation.getMutationSequence();

                    codon = DnaSequence.mutateSequence(codonPosition, MutationType.S, mutationSequence, codon);

                    codon = 3 < codon.length() ? codon.substring(0, 3) : codon;
                    this.logger.debug("{}", codon);
                  }

                  final char aminoAcid = this.convertCodon(codon);

                  this.logger.debug("refId={} mutationName={} codon={} aminoAcid={} mutationSeq={}", snparReferenceSequence.getSequenceId(), mutation.getName(), codon, String.valueOf(aminoAcid), mutation.getMutationSequence());

                  if (mutation.getMutationSequence().equals(String.valueOf(aminoAcid))) {
                    this.logger.debug("Added");
                    snpResistanceElements.add(new SnpResistanceElement(mutation, overlappingMutations));
                  }
                }
              } else {

                // Go through mutations in sequence and see if any match location & type, must allow of multi-nt substitutions.
                final Optional<Mutation> mutationOptional = mutationSearchResult
                    .getMutations()
                    .stream()
                    // .filter(queryMutation -> MutationType.S == queryMutation.getMutationType())
                    .filter(queryMutation -> queryMutation.getReferenceLocation() <= mutation.getRepSequenceLocation() && queryMutation.getReferenceLocation() + queryMutation.getMutationSequence().length() - 1 >= mutation.getRepSequenceLocation())
                    .filter(queryMutation -> {

// If length 1 && the same sequence, all good. Else substring...
                      return (1 == queryMutation.getMutationSequence().length() && queryMutation.getMutationSequence().equals(mutation.getMutationSequence())) || mutation.getMutationSequence().equals(queryMutation.getMutationSequence().substring(mutation.getRepSequenceLocation() - queryMutation.getReferenceLocation()));
                    })
                    .findFirst();

                // ncRNA sequence (or DNA level alteration)
                if (mutationOptional.isPresent()) {
                  this.logger.debug("Added");
                  snpResistanceElements.add(new SnpResistanceElement(mutation, Collections.singleton(mutationOptional.get())));
                }
              }
            }
        );

    // Add the collated match and resistance data.
    return new SnparMatchData(mutationSearchResult.getBlastSearchStatistics(), snpResistanceElements, mutationSearchResult.getMutations());
  }


  private char convertCodon(final String codon) {

    return DnaSequence.translateCodon(codon).orElse('0');
  }
}
