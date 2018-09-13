package net.cgps.wgsa.paarsnp.core.paar;


import net.cgps.wgsa.paarsnp.core.lib.blast.BlastMatch;
import net.cgps.wgsa.paarsnp.core.lib.blast.BlastSearchStatistics;
import net.cgps.wgsa.paarsnp.core.lib.json.ResistanceSet;
import net.cgps.wgsa.paarsnp.core.paar.json.Paar;
import net.cgps.wgsa.paarsnp.core.snpar.json.SetMember;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PaarCalculation implements Collector<BlastMatch, Collection<BlastMatch>, PaarResult> {

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

      // 1. Gather seen identifiers into Set<String>
      // 2. For each resistance set check if complete/partial/modified

      final Map<String, List<BlastSearchStatistics>> matches = selectedMatches
          .stream()
          .map(BlastMatch::getBlastSearchStatistics)
          .collect(Collectors.groupingBy(BlastSearchStatistics::getLibrarySequenceId));

      final Collection<ResistanceSet> completedSets = new HashSet<>(10);
      final Collection<ResistanceSet> partialSets = new HashSet<>(10);

      this.paarLibrary.getSets().values()
          .forEach(set -> {
            if (matches.keySet().containsAll(set.getMembers().stream().map(SetMember::getGene).collect(Collectors.toList()))) {
              completedSets.add(set);
            } else if (set.getMembers().stream().anyMatch(member -> matches.keySet().contains(member.getGene()))) {
              partialSets.add(set);
            }
          });

      return new PaarResult(
          completedSets,
          partialSets,
          matches,
          Stream.concat(completedSets.stream(), partialSets.stream()).map(ResistanceSet::getName).collect(Collectors.toList()));
    };
  }

  @Override
  public Set<Characteristics> characteristics() {
    return Collections.singleton(Characteristics.UNORDERED);
  }
}
