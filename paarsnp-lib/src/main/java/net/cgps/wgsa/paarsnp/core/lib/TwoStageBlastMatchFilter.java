package net.cgps.wgsa.paarsnp.core.lib;

import net.cgps.wgsa.paarsnp.core.lib.blast.BlastMatch;

import java.util.function.Predicate;

public class TwoStageBlastMatchFilter implements Predicate<BlastMatch> {

  private double coverageThreshold;

  public TwoStageBlastMatchFilter(double coverageThreshold) {
    this.coverageThreshold = coverageThreshold;
  }

  @Override
  public boolean test(BlastMatch match) {

    final double coverage = match.calculateCoverage();
    return this.coverageThreshold < coverage || ((match.getBlastSearchStatistics().getPercentIdentity() > 95.0) && (coverage > 40));
  }
}
