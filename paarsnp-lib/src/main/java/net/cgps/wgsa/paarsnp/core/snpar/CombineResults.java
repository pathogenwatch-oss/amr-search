package net.cgps.wgsa.paarsnp.core.snpar;

import net.cgps.wgsa.paarsnp.core.lib.blast.BlastMatch;
import net.cgps.wgsa.paarsnp.core.models.Phenotype;
import net.cgps.wgsa.paarsnp.core.models.ProcessedMatch;
import net.cgps.wgsa.paarsnp.core.models.ResistanceSet;
import net.cgps.wgsa.paarsnp.core.models.results.HasVariants;
import net.cgps.wgsa.paarsnp.core.models.results.MatchJson;
import net.cgps.wgsa.paarsnp.core.models.results.SearchResult;
import net.cgps.wgsa.paarsnp.core.models.results.SetResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class CombineResults implements Collector<BlastMatch, List<ProcessedMatch>, SearchResult> {

  private final Logger logger = LoggerFactory.getLogger(CombineResults.class);
  private final Collection<ResistanceSet> resistanceSets;
  private final ProcessMatches processMatches;

  public CombineResults(final Collection<ResistanceSet> resistanceSets, ProcessMatches processMatches) {
    this.resistanceSets = resistanceSets;
    this.processMatches = processMatches;
  }

  @Override
  public Supplier<List<ProcessedMatch>> supplier() {
    return ArrayList::new;
  }

  @Override
  public BiConsumer<List<ProcessedMatch>, BlastMatch> accumulator() {

    // First process all the BLAST matches and assign the resistance mutations
    return (list, match) -> list.add(this.processMatches.apply(match));
  }

  @Override
  public BinaryOperator<List<ProcessedMatch>> combiner() {
    return (a, b) -> {
      a.addAll(b);
      return a;
    };
  }

  @Override
  public Function<List<ProcessedMatch>, SearchResult> finisher() {

    return (selectedMatches) -> {

      this.logger.debug("Found {} PAARSNP resistance matches.", selectedMatches.size());

      // Group matches by library sequence ID for easy look up.
      final Map<String, List<ProcessedMatch>> matches = selectedMatches
          .stream()
          .collect(Collectors.groupingBy(match -> match.getSearchStatistics().getRefId()));

      final Collection<SetResult> setResults = this.resistanceSets
          .stream()
          .map(set -> new SetResult(
                  set.getMembers()
                      .stream()
                      .filter(member -> this.checkPresence(matches).test(member))
                      .collect(Collectors.toList()),
                  set.getPhenotypes()
                      .stream()
                      .map(Phenotype::getModifiers)
                      .flatMap(Collection::stream)
                      .filter(member -> this.checkPresence(matches).test(member))
                      .collect(Collectors.toList()),
                  set
              )
          )
          .collect(Collectors.toList());

      // Finally generate the result document.
      return new SearchResult(
          setResults,
          selectedMatches
              .stream()
              .map(ProcessedMatch::getSearchStatistics)
              .collect(Collectors.toList())
      );
    };
  }

  private <V extends HasVariants> Predicate<V> checkPresence(final Map<String, List<ProcessedMatch>> matches) {
    return (query) -> {
      if (matches.containsKey(query.getGene())) {
        return this.testPresence(query, matches.get(query.getGene())).isPresent();
      } else {
        return false;
      }
    };
  }

  private <V extends HasVariants> Optional<V> testPresence(final V query, final List<ProcessedMatch> matches) {
    final Optional<V> queryResult;
    if (query.getVariants().isEmpty()) {
      queryResult = Optional.of(query);
    } else {

      final Set<String> variants = findMembers(query.getVariants(), matches);

      if (variants.size() == query.getVariants().size()) {
        queryResult = Optional.of(query);
      } else {
        queryResult = Optional.empty();
      }
    }
    return queryResult;
  }

  private Set<String> findMembers(final Set<String> variants, final List<ProcessedMatch> queryMatches) {
    final var extractResistanceVariants = new ExtractResistanceVariants(variants);
    return queryMatches
        .stream()
        .map(extractResistanceVariants)
        .max(Comparator.comparingInt(Collection::size))
        .orElse(Collections.emptySet());
  }

  @Override
  public Set<Characteristics> characteristics() {
    return Collections.singleton(Characteristics.UNORDERED);
  }
}
