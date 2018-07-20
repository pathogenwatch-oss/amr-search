package net.cgps.wgsa.paarsnp.core.lib;

import net.cgps.wgsa.paarsnp.core.lib.blast.BlastMatch;

import java.util.function.Predicate;

public class SimpleBlastMatchFilter implements Predicate<BlastMatch> {
  private double coverageThreshold;

  public SimpleBlastMatchFilter(double coverageThreshold) {
    this.coverageThreshold = coverageThreshold;
  }

  @Override
  public boolean test(final BlastMatch match) {
    return this.coverageThreshold < match.calculateCoverage();
  }
}
