package net.cgps.wgsa.paarsnp.core.snpar;

import net.cgps.wgsa.paarsnp.core.lib.SetResult;
import net.cgps.wgsa.paarsnp.core.lib.blast.BlastMatch;
import net.cgps.wgsa.paarsnp.core.lib.json.Modifier;
import net.cgps.wgsa.paarsnp.core.lib.json.Phenotype;
import net.cgps.wgsa.paarsnp.core.snpar.json.*;
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
  private final Snpar snparLibrary;
  private final ProcessVariants processVariants;

  public SnparCalculation(final Snpar snparLibrary, ProcessVariants processVariants) {

    this.snparLibrary = snparLibrary;
    this.processVariants = processVariants;
  }

  @Override
  public Supplier<List<SnparMatchData>> supplier() {
    return ArrayList::new;
  }

  @Override
  public BiConsumer<List<SnparMatchData>, BlastMatch> accumulator() {

    // First process all the BLAST matches and assign the resistance mutations
    return (list, match) -> list.add(this.processVariants.apply(match));
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

    return (selectedMatches) -> {

      this.logger.debug("Found {} SNPAR resistance matches.", selectedMatches.size());

      // Need to now account for multi-gene snpar sets
      final Map<String, List<SnparMatchData>> matches = selectedMatches
          .stream()
          .collect(Collectors.groupingBy(match -> match.getSearchStatistics().getLibrarySequenceId()));

      final Collection<SetResult> setResults = this.snparLibrary.getSets().values()
          .stream()
          .map(set -> new SetResult(
              set.getMembers()
                  .stream()
                  .filter(member -> matches.keySet().contains(member.getGene()))
                  .map(member -> {
                    // NB We should only consider SNPS from a single copy of a gene, so here we are going to select the
                    // copy with the most coverage of the set
                    return matches.get(member.getGene())
                        .stream()
                        .map(match -> match.getSnpResistanceElements()
                            .stream()
                            .filter(mutation -> member.getVariants().contains(mutation.getResistanceMutation().getName()))
                            .map(ResistanceMutationMatch::getResistanceMutation)
                            .map(ResistanceMutation::getName)
                            .map(snp -> member.getGene() + "_" + snp)
                            .collect(Collectors.toList()))
                        .max(Comparator.comparingInt(Collection::size))
                        .orElse(Collections.emptyList());
                  })
                  .flatMap(Collection::stream)
                  .collect(Collectors.toSet()),
              set.getPhenotypes()
                  .stream()
                  .map(Phenotype::getModifiers)
                  .flatMap(Collection::stream)
                  .filter(modifier -> matches.keySet().contains(modifier.getName()))
                  .map(Modifier::getName)
                  .collect(Collectors.toList()),
              set
          ))
          .collect(Collectors.toList());

      // Finally generate the result document.
      return new SnparResult(
          setResults,
          selectedMatches
              .stream()
              .map(match -> new MatchJson(match.getSearchStatistics(), match.getSnpResistanceElements()))
              .collect(Collectors.toList())
      );
    };
  }

  @Override
  public Set<Characteristics> characteristics() {
    return Collections.singleton(Characteristics.UNORDERED);
  }
}
