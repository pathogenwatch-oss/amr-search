package net.cgps.wgsa.paarsnp.core.snpar;

import net.cgps.wgsa.paarsnp.core.lib.SequenceType;
import net.cgps.wgsa.paarsnp.core.lib.blast.BlastMatch;
import net.cgps.wgsa.paarsnp.core.lib.utils.DnaSequence;
import net.cgps.wgsa.paarsnp.core.snpar.json.*;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ProcessVariants implements Function<BlastMatch, SnparMatchData> {

  private final Logger logger = LoggerFactory.getLogger(ProcessVariants.class);

  private final SnparLibrary snparLibrary;

  public ProcessVariants(final SnparLibrary snparLibrary) {

    this.snparLibrary = snparLibrary;
  }

  @Override
  public SnparMatchData apply(final BlastMatch mutationSearchResult) {

    final SnparReferenceSequence snparReferenceSequence = this.snparLibrary.getSequence(mutationSearchResult.getBlastSearchStatistics().getLibrarySequenceId());

    if (SequenceType.PROTEIN == snparReferenceSequence.getSequenceType()) {

      final Collection<SnpResistanceElement> snpResistanceElements = snparReferenceSequence
          .getResistanceMutations()
          .stream()
          .peek(mutation -> this.logger.debug("Resistance mutation {}", mutation.getName()))
          // First check that the mutation lands within the matched region
          .filter(checkBounds(mutationSearchResult))
          .peek(mutation -> this.logger.debug("Mutation {} in range", mutation.getName()))
          // Check if the amino acid matches
          .filter(resistanceMutation -> {
            int mutationIndex = resistanceMutation.getRepLocation() - mutationSearchResult.getBlastSearchStatistics().getLibrarySequenceStart();
            final String codon = mutationSearchResult.getForwardRefMatchSequence().substring(mutationIndex, mutationIndex + 3);
            return resistanceMutation.getMutationSequence() == DnaSequence.translateCodon(codon).orElse('X');
          })
          .map(mutation -> {

            // Go through the mutations and gather those that overlap the reference codon
            final List<Mutation> overlappingMutations = Stream.of(mutation.getRepLocation(), mutation.getRepLocation() + 1, mutation.getRepLocation() + 2)
                .filter(location -> mutationSearchResult.getMutations().containsKey(location))
                .map(location -> mutationSearchResult.getMutations().get(location))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

            return new SnpResistanceElement(mutation, overlappingMutations);
          })
          .collect(Collectors.toList());

      return new SnparMatchData(mutationSearchResult.getBlastSearchStatistics(), snpResistanceElements);

    } else {
      final Collection<SnpResistanceElement> snpResistanceElements = snparReferenceSequence
          .getResistanceMutations()
          .stream()
          .peek(resistanceMutation -> this.logger.debug("Resistance mutation {}", resistanceMutation.getName()))
          // First check that the mutation lands within the matched region
          .filter(resistanceMutation -> resistanceMutation.getRepLocation() >= mutationSearchResult.getBlastSearchStatistics().getLibrarySequenceStart()
              && resistanceMutation.getRepLocation() <= mutationSearchResult.getBlastSearchStatistics().getLibrarySequenceStop())
          .peek(resistanceMutation -> this.logger.debug("Mutation {} in range", resistanceMutation.getName()))
          // Then check if there is a mutation at that location
          .filter(resistanceMutation -> mutationSearchResult.getMutations().containsKey(resistanceMutation.getRepLocation()))
          .map(resistanceMutation -> new ImmutablePair<>(resistanceMutation, mutationSearchResult.getMutations().get(resistanceMutation.getRepLocation())
              .stream()
              .filter(testMutation -> resistanceMutation.getMutationSequence() == testMutation.getMutationSequence())
              .findFirst())
          )
          .filter(pair -> pair.getRight().isPresent())
          .map(resistanceMutation -> new SnpResistanceElement(resistanceMutation.getLeft(), Collections.singleton(resistanceMutation.getRight().get()))
          )
          .collect(Collectors.toList());

      return new SnparMatchData(mutationSearchResult.getBlastSearchStatistics(), snpResistanceElements);
    }
  }

  public Predicate<ResistanceMutation> checkBounds(final BlastMatch mutationSearchResult) {
    return resistanceMutation -> resistanceMutation.getRepLocation() >= mutationSearchResult.getBlastSearchStatistics().getLibrarySequenceStart()
        && resistanceMutation.getRepLocation() + 2 <= mutationSearchResult.getBlastSearchStatistics().getLibrarySequenceStop();
  }
}
