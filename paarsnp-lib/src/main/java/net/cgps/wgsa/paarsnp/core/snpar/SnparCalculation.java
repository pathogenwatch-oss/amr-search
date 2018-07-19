package net.cgps.wgsa.paarsnp.core.snpar;

import net.cgps.wgsa.paarsnp.core.lib.SetAggregator;
import net.cgps.wgsa.paarsnp.core.lib.blast.BlastMatch;
import net.cgps.wgsa.paarsnp.core.snpar.json.SnparLibrary;
import net.cgps.wgsa.paarsnp.core.snpar.json.SnparMatchData;
import net.cgps.wgsa.paarsnp.core.snpar.json.SnparResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class SnparCalculation implements Collector<BlastMatch, List<SnparMatchData>, SnparResult> {

  private final Logger logger = LoggerFactory.getLogger(SnparCalculation.class);
  private final SnparLibrary snparLibrary;
  private final ProcessVariants processVariants;

  public SnparCalculation(final SnparLibrary snparLibrary, ProcessVariants processVariants) {

    this.snparLibrary = snparLibrary;
    this.processVariants = processVariants;
  }

  @Override
  public Supplier<List<SnparMatchData>> supplier() {
    return ArrayList::new;
  }

  @Override
  public BiConsumer<List<SnparMatchData>, BlastMatch> accumulator() {

    return (list, match) -> {
      list.add(this.processVariants.apply(match));
    };
  }

  @Override
  public BinaryOperator<List<SnparMatchData>> combiner() {
    return (a, b) -> {
      a.addAll(b);
      return a;
    };
  }

  @Override
  public Function<List<SnparMatchData>, SnparResult> finisher() {

    return (snparMatchDatas) -> {
      // First process all the BLAST matches and assign the resistance mutations
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
    };
  }

  @Override
  public Set<Characteristics> characteristics() {
    return Collections.singleton(Characteristics.UNORDERED);
  }
}
