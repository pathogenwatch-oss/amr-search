package net.cgps.wgsa.paarsnp.core.lib;

import net.cgps.wgsa.paarsnp.core.lib.blast.BlastMatch;
import net.cgps.wgsa.paarsnp.core.paar.json.PaarLibrary;

import java.util.AbstractMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class FilterByIndividualThresholds implements Predicate<BlastMatch> {
  private final Map<String, Float> pidThresholds;
  private final Map<String, Float> coverageThresholds;

  public FilterByIndividualThresholds(final Map<String, Float> pidThresholds, final Map<String, Float> coverageThresholds) {
    this.pidThresholds = pidThresholds;
    this.coverageThresholds = coverageThresholds;
  }

  public static FilterByIndividualThresholds build(final PaarLibrary paarLibrary) {
    return new FilterByIndividualThresholds(
        paarLibrary
            .getResistanceGenes()
            .values()
            .stream()
            .map(gene -> new AbstractMap.SimpleImmutableEntry<>(gene.getFamilyName(), gene.getSimilarityThreshold()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)),
        paarLibrary
            .getResistanceGenes()
            .values()
            .stream()
            .map(gene -> new AbstractMap.SimpleImmutableEntry<>(gene.getFamilyName(), gene.getLengthThreshold()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
  }

  @Override
  public boolean test(final BlastMatch match) {

    return this.coverageThresholds.get(match.getBlastSearchStatistics().getLibrarySequenceId()) <= match.calculateCoverage()
        &&
        this.pidThresholds.get(match.getBlastSearchStatistics().getLibrarySequenceId()) <= match.getBlastSearchStatistics().getPercentIdentity();
  }
}