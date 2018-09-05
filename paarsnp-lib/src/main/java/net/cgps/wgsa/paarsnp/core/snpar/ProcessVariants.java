package net.cgps.wgsa.paarsnp.core.snpar;

import net.cgps.wgsa.paarsnp.core.lib.SequenceType;
import net.cgps.wgsa.paarsnp.core.lib.blast.BlastMatch;
import net.cgps.wgsa.paarsnp.core.lib.blast.MutationBuilder;
import net.cgps.wgsa.paarsnp.core.lib.blast.SequenceProcessor;
import net.cgps.wgsa.paarsnp.core.snpar.json.*;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ProcessVariants implements Function<BlastMatch, SnparMatchData> {

  private final Logger logger = LoggerFactory.getLogger(ProcessVariants.class);

  private final Snpar snparLibrary;

  public ProcessVariants(final Snpar snparLibrary) {

    this.snparLibrary = snparLibrary;
  }

  @Override
  public SnparMatchData apply(final BlastMatch mutationSearchResult) {

    final SnparReferenceSequence snparReferenceSequence = this.snparLibrary.getSequence(mutationSearchResult.getBlastSearchStatistics().getLibrarySequenceId());

    if (SequenceType.PROTEIN == snparReferenceSequence.getType()) {

      final CodonMap codonMap = new CodonMapper().apply(mutationSearchResult);

      final Collection<ResistanceMutation> resistanceMutations = snparReferenceSequence
          .getResistanceMutations()
          .stream()
          .peek(mutation -> this.logger.debug("Resistance mutation {}", mutation.getName()))
          // First check that the mutation lands within the matched region
          .filter(checkBounds(mutationSearchResult))
          .peek(mutation -> this.logger.debug("Mutation {} in range", mutation.getName()))
          // Check if the amino acid matches
          .filter(resistanceMutation -> resistanceMutation.getMutationSequence() == codonMap.get(resistanceMutation.getAaLocation()))
          .collect(Collectors.toList());

      return new SnparMatchData(mutationSearchResult.getBlastSearchStatistics(), resistanceMutations);

    } else {

      final Map<Integer, Collection<Mutation>> mutations = new SequenceProcessor(mutationSearchResult.getReferenceMatchSequence(), mutationSearchResult.getBlastSearchStatistics().getLibrarySequenceStart(), mutationSearchResult.getBlastSearchStatistics().getStrand(), mutationSearchResult.getForwardQuerySequence(), mutationSearchResult.getBlastSearchStatistics().getQuerySequenceStart(), new MutationBuilder()).call();

      final Collection<ResistanceMutation> snpResistanceElements = snparReferenceSequence
          .getResistanceMutations()
          .stream()
          .peek(resistanceMutation -> this.logger.debug("Resistance mutation {}", resistanceMutation.getName()))
          // First check that the mutation lands within the matched region
          .filter(resistanceMutation -> resistanceMutation.getRepLocation() >= mutationSearchResult.getBlastSearchStatistics().getLibrarySequenceStart()
              && resistanceMutation.getRepLocation() <= mutationSearchResult.getBlastSearchStatistics().getLibrarySequenceStop())
          .peek(resistanceMutation -> this.logger.debug("Mutation {} in range", resistanceMutation.getName()))
          // Then check if there is a mutation at that location
          .filter(resistanceMutation -> mutations.containsKey(resistanceMutation.getRepLocation()))
          .map(resistanceMutation -> new ImmutablePair<>(resistanceMutation, mutations.get(resistanceMutation.getRepLocation())
              .stream()
              .filter(testMutation -> resistanceMutation.getMutationSequence() == testMutation.getMutationSequence())
              .findFirst())
          )
          .filter(pair -> pair.getRight().isPresent())
          .map(Map.Entry::getKey)
          .collect(Collectors.toUnmodifiableSet());

      return new SnparMatchData(mutationSearchResult.getBlastSearchStatistics(), snpResistanceElements);
    }
  }

  private Predicate<ResistanceMutation> checkBounds(final BlastMatch mutationSearchResult) {
    return resistanceMutation -> resistanceMutation.getRepLocation() >= mutationSearchResult.getBlastSearchStatistics().getLibrarySequenceStart()
        && resistanceMutation.getRepLocation() + 2 <= mutationSearchResult.getBlastSearchStatistics().getLibrarySequenceStop();
  }
}
