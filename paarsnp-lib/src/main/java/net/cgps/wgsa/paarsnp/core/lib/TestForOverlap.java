package net.cgps.wgsa.paarsnp.core.lib;


import net.cgps.wgsa.paarsnp.core.lib.blast.BlastMatch;

import java.util.function.BiPredicate;

public class TestForOverlap implements BiPredicate<BlastMatch, BlastMatch> {


  private final int overlapThreshold;

  @Override
  public boolean test(final BlastMatch match1, final BlastMatch match2) {

    // The query coordinates are never reversed.
    final int queryStart1 = match1.getBlastSearchStatistics().getQuerySequenceStart();
    final int queryStop1 = match1.getBlastSearchStatistics().getQuerySequenceStop();

    final int queryStart2 = match2.getBlastSearchStatistics().getQuerySequenceStart();
    final int queryStop2 = match2.getBlastSearchStatistics().getQuerySequenceStop();

    return overlapCheck(queryStart1, queryStop1, queryStart2, queryStop2, overlapThreshold);
  }

  TestForOverlap(final int overlapThreshold) {

    this.overlapThreshold = overlapThreshold;
  }

  static boolean overlapCheck(int queryStart1, int queryStop1, int queryStart2, int queryStop2, int overlapThreshold) {
    return queryStop2 >= queryStart1 + overlapThreshold && queryStop1 >= queryStart2 + overlapThreshold;

  }
}