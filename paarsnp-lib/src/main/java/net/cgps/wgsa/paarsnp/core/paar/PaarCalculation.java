package net.cgps.wgsa.paarsnp.core.paar;


import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.SetMultimap;
import net.cgps.wgsa.paarsnp.core.lib.ElementEffect;
import net.cgps.wgsa.paarsnp.core.lib.blast.BlastMatch;
import net.cgps.wgsa.paarsnp.core.lib.json.ResistanceSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PaarCalculation implements Collector<BlastMatch, Collection<BlastMatch>, PaarResult> {

  private final Logger logger = LoggerFactory.getLogger(PaarCalculation.class);
  private final PaarLibrary paarLibrary;

  public PaarCalculation(final PaarLibrary paarLibrary) {

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
      final Multimap<String, BlastMatch> matches = HashMultimap.create();
      final Collection<ResistanceSet> completedSets = new HashSet<>(10);
      final Collection<ResistanceSet> partialSets = new HashSet<>(10);
      final Map<String, ElementEffect> modifiedSets = new HashMap<>(); // setId -> effect

      // used to work out which are complete
      // Need to use set to keep track since potentially >1 hit to each family, meaning that a single family match could be counted
      // twice against the set score.
      final SetMultimap<String, ResistanceGene> setCounts = HashMultimap.create();

      selectedMatches
          .stream()
          .map(match ->
              new AbstractMap.SimpleImmutableEntry<>(match, this.paarLibrary.getPaarGene(match.getBlastSearchStatistics().getLibrarySequenceId())))
          // Check that the gene hasn't already been dealt with?
          // NB this does rule out looking at additive effects as it removes duplicates.
          .forEach(matchToGeneEntry -> {
                // Add for each set the gene may belong to.
                if (ElementEffect.RESISTANCE == matchToGeneEntry.getValue().getEffect()) {

                  this.logger.debug("Adding {} to sets {}", matchToGeneEntry.getValue().getFamilyName(), matchToGeneEntry.getValue().getResistanceSetNames().stream().collect(Collectors.joining(",")));

                  // For each set the element is in, add the element to the set count hash.
                  matchToGeneEntry.getValue().getResistanceSetNames().forEach(setName -> setCounts.put(setName, matchToGeneEntry.getValue()));

                } else {
                  // It's a modifier so work out which sets are modified and how, store as hash.
                  this.logger.debug("{} modifying sets {}", matchToGeneEntry.getValue().getFamilyName(), matchToGeneEntry.getValue().getResistanceSetNames().stream().collect(Collectors.joining(",")));

                  matchToGeneEntry.getValue().getResistanceSetNames().forEach(setName -> modifiedSets.put(setName, this.paarLibrary.getSetById(setName).getModifiers().get(matchToGeneEntry.getValue().getFamilyName())));
                }

                matches.put(matchToGeneEntry.getValue().getFamilyName(), matchToGeneEntry.getKey());
              }
          );

      // Compare the sizes of observed vs expected sizes for each set and add to the appropriate data obj.
      setCounts
          .keySet()
          .forEach(setId -> {
                this.logger.debug("{} found {} out of {}", setId, setCounts.get(setId).size(), this.paarLibrary.getPaarGeneSet(setId).size());
                if (setCounts.get(setId).size() == this.paarLibrary.getPaarGeneSet(setId).size()) {
                  completedSets.add(this.paarLibrary.getSetById(setId));
                } else {
                  partialSets.add(this.paarLibrary.getSetById(setId));
                }
              }
          );

      return new PaarResult(completedSets, partialSets, modifiedSets, matches.asMap(), Stream.concat(completedSets.stream(), partialSets.stream()).map(ResistanceSet::getResistanceSetName).collect(Collectors.toList()));
    };
  }

  @Override
  public Set<Characteristics> characteristics() {
    return Collections.singleton(Characteristics.UNORDERED);
  }
}
