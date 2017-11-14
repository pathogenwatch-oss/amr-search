package net.cgps.wgsa.paarsnp.core.snpar;

import net.cgps.wgsa.paarsnp.core.lib.SetAggregator;
import net.cgps.wgsa.paarsnp.core.lib.blast.MutationSearchResult;
import net.cgps.wgsa.paarsnp.core.snpar.json.SnparLibrary;
import net.cgps.wgsa.paarsnp.core.snpar.json.SnparMatchData;
import net.cgps.wgsa.paarsnp.core.snpar.json.SnparResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SnparCalculation implements Function<Stream<MutationSearchResult>, SnparResult> {

  private final Logger logger = LoggerFactory.getLogger(SnparCalculation.class);
  private final SnparLibrary snparLibrary;

  public SnparCalculation(final SnparLibrary snparLibrary) {

    this.snparLibrary = snparLibrary;
  }

  public SnparResult apply(final Stream<MutationSearchResult> blastMatches) {

    final Function<MutationSearchResult, SnparMatchData> processSnparMatchFunction = new ProcessSnparMatchFunction(this.snparLibrary);

    // First process all the BLAST matches and assign the resistance mutations
    final Collection<SnparMatchData> snparMatchDatas = blastMatches.filter(match -> {
                                                                             // Error check, skipping matches without a reference in the library. Flash a warning.
                                                                             final Optional<SnparReferenceSequence> mutationReferenceSequence = this.snparLibrary.getSequence(match.getBlastSearchStatistics().getLibrarySequenceId());
                                                                             if (!mutationReferenceSequence.isPresent()) {
                                                                               this.logger.error("Sequence {} in Arsnp BLAST library, but not in couchbase.", match.getBlastSearchStatistics().getLibrarySequenceId());
                                                                             }
                                                                             return mutationReferenceSequence.isPresent();
                                                                           }
                                                                          )
                                                                   .filter(match -> {
                                                                             final double coverage = (((double) match.getBlastSearchStatistics().getLibrarySequenceStop() - match.getBlastSearchStatistics().getLibrarySequenceStart() + 1) / (double) match.getBlastSearchStatistics().getLibrarySequenceLength()) * 100;
                                                                             return coverage > 60;
                                                                           }
                                                                          )
                                                                   .map(processSnparMatchFunction)
                                                                   .peek(snparMatchData -> this.logger.debug("snparMatchData={}", snparMatchData))
                                                                   .collect(Collectors.toList());

    this.logger.debug("Found {} resistance matches.", snparMatchDatas.size());

    // Next identify the resistance sets (and classify as complete or not) for each resistance gene.
    final ProcessSnparMatchData processSnparMatchData = new ProcessSnparMatchData(this.snparLibrary.getResistanceSets().values());

    final Collection<ProcessSnparMatchData.ProcessedSets> processedSets = snparMatchDatas
        .stream()
        .map(processSnparMatchData)
        .collect(Collectors.toList());

    // Process the matched snps into complete & partially-complete resistance sets.
    final ProcessSnparMatchData.ProcessedSets aggregateSets = new SetAggregator().apply(processedSets);

    // Finally aggregate the sets and return the result document.
    return new SnparResult(aggregateSets.getSeenIds(), aggregateSets.getCompleteSets(), aggregateSets.getPartialSets(), snparMatchDatas);
  }
}
