package net.cgps.wgsa.paarsnp.core.paar;


import net.cgps.wgsa.paarsnp.core.models.results.SetResult;
import net.cgps.wgsa.paarsnp.core.lib.blast.BlastMatch;
import net.cgps.wgsa.paarsnp.core.lib.blast.BlastSearchStatistics;
import net.cgps.wgsa.paarsnp.core.models.results.Modifier;
import net.cgps.wgsa.paarsnp.core.models.Phenotype;
import net.cgps.wgsa.paarsnp.core.models.Paar;
import net.cgps.wgsa.paarsnp.core.models.PaarResult;
import net.cgps.wgsa.paarsnp.core.models.SetMember;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class PaarCalculation implements Collector<BlastMatch, Collection<BlastMatch>, PaarResult> {

  private final Logger logger = LoggerFactory.getLogger(PaarCalculation.class);
  private final Paar paarLibrary;

  public PaarCalculation(final Paar paarLibrary) {

    this.paarLibrary = paarLibrary;
  }


  @Override
  public Supplier<Collection<BlastMatch>> supplier() {
    return ArrayList::new;
  }

  @Override
  public BiConsumer<Collection<BlastMatch>, BlastMatch> accumulator() {
    return Collection::add;
  }

  @Override
  public BinaryOperator<Collection<BlastMatch>> combiner() {
    return (a, b) -> {
      a.addAll(b);
      return a;
    };
  }

  @Override
  public Function<Collection<BlastMatch>, PaarResult> finisher() {

    return selectedMatches -> {  // Result data structures.

      this.logger.debug("Found {} PAAR resistance matches.", selectedMatches.size());

      // 1. Gather seen identifiers into Set<String>
      // 2. For each resistance set check if complete/partial

      final Map<String, List<BlastSearchStatistics>> matches = selectedMatches
          .stream()
          .map(BlastMatch::getBlastSearchStatistics)
          .collect(Collectors.groupingBy(BlastSearchStatistics::getLibrarySequenceId));

      final Collection<SetResult> setResults = this.paarLibrary.getSets().values()
          .stream()
          .map(set -> new SetResult(
              set.getMembers()
                  .stream()
                  .map(SetMember::getGene)
                  .filter(member -> matches.keySet().contains(member))
                  .collect(Collectors.toList()),
              set.getPhenotypes()
                  .stream()
                  .map(Phenotype::getModifiers)
                  .flatMap(Collection::stream)
                  .map(Modifier::getName)
                  .filter(modifier -> matches.keySet().contains(modifier))
                  .collect(Collectors.toList()), set))
          .collect(Collectors.toList());

      return new PaarResult(
          setResults,
          matches,
          setResults.stream().map(SetResult::getFoundMembers).flatMap(Collection::stream).collect(Collectors.toSet()));
    };
  }

  @Override
  public Set<Characteristics> characteristics() {
    return Collections.singleton(Characteristics.UNORDERED);
  }
}
