package net.cgps.wgsa.paarsnp.core.lib;

import net.cgps.wgsa.paarsnp.core.lib.blast.BlastMatch;
import net.cgps.wgsa.paarsnp.core.models.Mechanisms;
import net.cgps.wgsa.paarsnp.core.models.Paar;
import net.cgps.wgsa.paarsnp.core.models.ReferenceSequence;

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

  public static FilterByIndividualThresholds build(final Paar paarLibrary) {
    return new FilterByIndividualThresholds(
        paarLibrary
            .getGenes()
            .values()
            .stream()
            .collect(Collectors.toMap(ReferenceSequence::getName, ReferenceSequence::getPid)),
        paarLibrary
            .getGenes()
            .values()
            .stream()
            .collect(Collectors.toMap(ReferenceSequence::getName, ReferenceSequence::getCoverage)));
  }

  public static FilterByIndividualThresholds build(final Mechanisms mechanismsLibrary) {
    return new FilterByIndividualThresholds(
        mechanismsLibrary.getGenes()
            .values()
            .stream()
            .collect(Collectors.toMap(ReferenceSequence::getName, ReferenceSequence::getPid)),
        mechanismsLibrary.getGenes()
            .values()
            .stream()
            .collect(Collectors.toMap(ReferenceSequence::getName, ReferenceSequence::getCoverage))
    );
  }

  @Override
  public boolean test(final BlastMatch match) {

    return this.coverageThresholds.get(match.getBlastSearchStatistics().getLibrarySequenceId()) <= match.calculateCoverage()
        &&
        this.pidThresholds.get(match.getBlastSearchStatistics().getLibrarySequenceId()) <= match.getBlastSearchStatistics().getPercentIdentity();
  }
}