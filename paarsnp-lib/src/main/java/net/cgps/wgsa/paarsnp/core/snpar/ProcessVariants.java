package net.cgps.wgsa.paarsnp.core.snpar;

import net.cgps.wgsa.paarsnp.core.lib.blast.Mutation;
import net.cgps.wgsa.paarsnp.core.lib.utils.DnaSequence;
import net.cgps.wgsa.paarsnp.core.models.*;
import net.cgps.wgsa.paarsnp.core.lib.blast.BlastMatch;
import net.cgps.wgsa.paarsnp.core.lib.blast.MutationBuilder;
import net.cgps.wgsa.paarsnp.core.lib.blast.SequenceProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ProcessVariants implements Function<BlastMatch, SnparMatchData> {

  private final Logger logger = LoggerFactory.getLogger(ProcessVariants.class);

  private final Snpar snparLibrary;
  private final PromoterFetcher promoterFetcher;

  public ProcessVariants(final Snpar snparLibrary, final PromoterFetcher promoterFetcher) {

    this.snparLibrary = snparLibrary;
    this.promoterFetcher = promoterFetcher;
  }

  @Override
  public SnparMatchData apply(final BlastMatch match) {

    final ReferenceSequence referenceSequence = this.snparLibrary.getGenes().get(match.getBlastSearchStatistics().getLibrarySequenceId());
    final Map<Integer, Collection<Mutation>> mutations = new SequenceProcessor(match.getReferenceMatchSequence(), match.getBlastSearchStatistics().getLibrarySequenceStart(), match.getBlastSearchStatistics().getStrand(), match.getForwardQuerySequence(), match.getBlastSearchStatistics().getQuerySequenceStart(), new MutationBuilder()).call();

    final CodonMap codonMap = new CodonMapper().apply(match);

    final Collection<ResistanceMutationMatch> resistanceMutations = referenceSequence
        .getTranscribedVariants()
        .stream()
        .peek(mutation -> this.logger.debug("Testing resistance mutation {}", mutation.getName()))
        .filter(mutation -> mutation.isWithinBoundaries(
            match.getBlastSearchStatistics().getLibrarySequenceStart(),
            match.getBlastSearchStatistics().getLibrarySequenceStop()))
        .filter(resistanceMutation -> resistanceMutation.isPresent(mutations, codonMap))
        .map(resistanceMutation -> resistanceMutation.buildMatch(mutations, codonMap))
        .collect(Collectors.toList());

    if (!referenceSequence.getPromoterVariants().isEmpty()) {

      final Optional<String> promoterQuery = this.promoterFetcher.apply(match.getBlastSearchStatistics());

      if (promoterQuery.isPresent()) {
        final String promoterSequence = promoterQuery.get();
        final Collection<ResistanceMutationMatch> promoterMutations = referenceSequence
            .getPromoterVariants()
            .stream()
            .peek(mutation -> this.logger.debug("Testing promoter mutation {}", mutation.getName()))
            .filter(mutation -> mutation.isWithinBoundaries(-1, promoterSequence.length() * -1))
            .filter(mutation -> mutation.isPresent(promoterSequence, match.getBlastSearchStatistics()))
            .map(referenceMutation -> {
              final String directionMarker;
              if (DnaSequence.Strand.FORWARD == match.getBlastSearchStatistics().getStrand()) {
                directionMarker = "+";
              } else {
                directionMarker = "-";
              }
              return referenceMutation.buildMatch(directionMarker, match.getBlastSearchStatistics());
            })
            .collect(Collectors.toList());

        resistanceMutations.addAll(promoterMutations);
      }
    }
    return new SnparMatchData(match.getBlastSearchStatistics(), resistanceMutations);
  }
}
