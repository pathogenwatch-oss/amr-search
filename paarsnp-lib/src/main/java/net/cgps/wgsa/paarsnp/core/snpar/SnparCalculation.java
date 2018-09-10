package net.cgps.wgsa.paarsnp.core.snpar;

import net.cgps.wgsa.paarsnp.core.lib.blast.BlastMatch;
import net.cgps.wgsa.paarsnp.core.lib.json.ResistanceSet;
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
      this.logger.debug("Found {} resistance matches.", selectedMatches.size());

      // Need to now account for multi-gene snpar sets
      final Map<String, List<SnparMatchData>> matches = selectedMatches
          .stream()
          .collect(Collectors.groupingBy(match -> match.getSearchStatistics().getLibrarySequenceId()));

      final Collection<ResistanceSet> completedSets = new HashSet<>(10);
      final Collection<ResistanceSet> partialSets = new HashSet<>(10);
      final Set<String> seenMutationNames = new HashSet<>(50);

      this.snparLibrary.getSets().values()
          .forEach(set -> {
            final boolean complete = set.getMembers().size() == (int) set.getMembers().stream()
                .filter(setMember -> matches.keySet().contains(setMember.getGene()))
                // Check that all mutations are present
                .filter(setMember -> matches.get(setMember.getGene())
                    .stream()
                    .anyMatch(match -> setMember.getVariants().size() == setMember.getVariants()
                        .stream()
                        .filter(variant -> match.getSnpResistanceElements()
                            .stream()
                            .map(ResistanceMutation::getName)
                            .anyMatch(name -> name.equals(variant)))
                        // Since we check for the presence of every member we can add observed ones here.
                        .peek(seenMutationNames::add)
                        .count())
                )
                .count();
            if (complete) {
              completedSets.add(set);
            } else {
              final boolean partial = set.getMembers().stream()
                  .filter(setMember -> matches.keySet().contains(setMember.getGene()))
                  .anyMatch(setMember -> matches.get(setMember.getGene())
                      .stream()
                      .anyMatch(match -> setMember.getVariants()
                          .stream()
                          .anyMatch(variant -> match.getSnpResistanceElements()
                              .stream()
                              .map(ResistanceMutation::getName)
                              .anyMatch(name -> name.equals(variant))
                          )
                      ));
              if (partial) {
                partialSets.add(set);
              }
            }
          });

      // Finally generate the result document.
      return new SnparResult(
          seenMutationNames,
          completedSets,
          partialSets,
          selectedMatches
              .stream()
              .map(match -> new MatchJson(
                  match.getSearchStatistics(),
                  match.getSnpResistanceElements()
                      .stream()
                      .map(ResistanceMutation::getName)
                      .collect(Collectors.toList())))
              .collect(Collectors.toList())
      );
    };


  }

  @Override
  public Set<Characteristics> characteristics() {
    return Collections.singleton(Characteristics.UNORDERED);
  }
}
