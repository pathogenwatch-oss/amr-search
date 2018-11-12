package net.cgps.wgsa.paarsnp.core.snpar;

import net.cgps.wgsa.paarsnp.core.formats.*;
import net.cgps.wgsa.paarsnp.core.lib.blast.BlastMatch;
import net.cgps.wgsa.paarsnp.core.lib.blast.MutationBuilder;
import net.cgps.wgsa.paarsnp.core.lib.blast.SequenceProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ProcessVariants implements Function<BlastMatch, SnparMatchData> {

  private final Logger logger = LoggerFactory.getLogger(ProcessVariants.class);

  private final Snpar snparLibrary;

  public ProcessVariants(final Snpar snparLibrary) {

    this.snparLibrary = snparLibrary;
  }

  @Override
  public SnparMatchData apply(final BlastMatch mutationSearchResult) {

    final ReferenceSequence referenceSequence = this.snparLibrary.getGenes().get(mutationSearchResult.getBlastSearchStatistics().getLibrarySequenceId());
    final Map<Integer, Collection<Mutation>> mutations = new SequenceProcessor(mutationSearchResult.getReferenceMatchSequence(), mutationSearchResult.getBlastSearchStatistics().getLibrarySequenceStart(), mutationSearchResult.getBlastSearchStatistics().getStrand(), mutationSearchResult.getForwardQuerySequence(), mutationSearchResult.getBlastSearchStatistics().getQuerySequenceStart(), new MutationBuilder()).call();

    final CodonMap codonMap = new CodonMapper().apply(mutationSearchResult);

    final Collection<ResistanceMutationMatch> resistanceMutations = referenceSequence
        .getMappedVariants()
        .stream()
        .peek(mutation -> this.logger.debug("Resistance mutation {}", mutation.getName()))
        .filter(mutation ->
            mutationSearchResult.containsPosition(mutation.getReferenceLocation())
                && mutation.getType() == ResistanceMutation.TYPE.DNA || mutationSearchResult.containsPosition(mutation.getReferenceLocation() + 2))
        .peek(mutation -> this.logger.debug("Mutation {} in range", mutation.getName()))
        .peek(mutation -> this.logger.debug("Mutation {} in range", mutation.getName()))
        .filter(resistanceMutation -> {
          switch (resistanceMutation.getType()) {
            case DNA:

              return mutations.containsKey(resistanceMutation.getReferenceLocation())
                  && mutations.get(resistanceMutation.getReferenceLocation())
                  .stream()
                  .anyMatch(queryMutation -> queryMutation.getMutationSequence() == resistanceMutation.getMutationSequence());
            case AA:
              return resistanceMutation.getMutationSequence() == codonMap.get(resistanceMutation.getAaLocation());
            default:
              return false;
          }
        })
        .map(resistanceMutation -> {
          switch (resistanceMutation.getType()) {
            case DNA:
              return new ResistanceMutationMatch(
                  resistanceMutation,
                  mutations.get(resistanceMutation.getReferenceLocation())
                      .stream()
                      .filter(queryMutation -> queryMutation.getMutationSequence() == resistanceMutation.getMutationSequence())
                      .collect(Collectors.toList()));
            case AA:
            default:
              return new ResistanceMutationMatch(
                  resistanceMutation,
                  Stream.of(resistanceMutation.getReferenceLocation(), resistanceMutation.getReferenceLocation() + 1, resistanceMutation.getReferenceLocation() + 2)
                      .filter(index -> mutations.keySet().contains(index))
                      .map(mutations::get)
                      .flatMap(Collection::stream)
                      .collect(Collectors.toList()));
          }
        })
        .collect(Collectors.toList());

    return new SnparMatchData(mutationSearchResult.getBlastSearchStatistics(), resistanceMutations);
  }
}
