package net.cgps.wgsa.paarsnp.core.snpar;

import net.cgps.wgsa.paarsnp.core.lib.blast.BlastMatch;
import net.cgps.wgsa.paarsnp.core.models.Phenotype;
import net.cgps.wgsa.paarsnp.core.models.ProcessedMatch;
import net.cgps.wgsa.paarsnp.core.models.ResistanceSet;
import net.cgps.wgsa.paarsnp.core.models.SetMember;
import net.cgps.wgsa.paarsnp.core.models.results.MatchJson;
import net.cgps.wgsa.paarsnp.core.models.results.Modifier;
import net.cgps.wgsa.paarsnp.core.models.results.SearchResult;
import net.cgps.wgsa.paarsnp.core.models.results.SetResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
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
          .collect(Collectors.groupingBy(match -> match.getSearchStatistics().getLibrarySequenceId()));

      final Collection<SetResult> setResults = this.resistanceSets
          .stream()
          .map(set -> new SetResult(
                  set.getMembers()
                      .stream()
                      .filter(member -> matches.containsKey(member.getGene()))
                      .map(member -> {
                        // Just return the gene name if it's presence-absence
                        // Otherwise the list of variants
                        if (member.getVariants().isEmpty()) {
                          return Optional.of(member);
                        }

                        final var extractResistanceVariants = new ExtractResistanceVariants(member.getVariants());

                        // NB We should only consider SNPs from a single copy of a gene, so here we are going to select the
                        // copy with the most coverage of the set
                        final Set<String> variants = matches.get(member.getGene())
                            .stream()
                            .map(extractResistanceVariants)
                            .max(Comparator.comparingInt(Collection::size))
                            .orElse(Collections.emptySet());

                        final SetMember variantsMember = new SetMember(member.getGene(), variants);
                        final Optional<SetMember> resultMember;

                        if (variantsMember.getVariants().isEmpty()) {
                          resultMember = Optional.empty();
                        } else {
                          resultMember = Optional.of(variantsMember);
                        }
                        return resultMember;
                      })
                      .filter(Optional::isPresent)
                      .map(Optional::get)
                      .collect(Collectors.toList()),
                  set.getPhenotypes()
                      .stream()
                      .map(Phenotype::getModifiers)
                      .flatMap(Collection::stream)
                      .filter(modifier -> matches.containsKey(modifier.getGene()))
                      .map(modifier -> {
                        // Just return the gene name if it's presence-absence
                        // Otherwise the list of variants
                        final Optional<Modifier> modifierPresent;
                        if (modifier.getVariants().isEmpty()) {
                          modifierPresent = Optional.of(modifier);
                        } else {

                          final var variantExtractor = new ExtractResistanceVariants(modifier.getVariants());

                          final Set<String> variants = matches.get(modifier.getGene())
                              .stream()
                              .map(variantExtractor)
                              .max(Comparator.comparingInt(Collection::size))
                              .orElse(Collections.emptySet());

                          if (variants.size() == modifier.getVariants().size()) {
                            modifierPresent = Optional.of(modifier);
                          } else {
                            modifierPresent = Optional.empty();
                          }
                        }
                        return modifierPresent;
                      })
                      .filter(Optional::isPresent)
                      .map(Optional::get)
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
