package net.cgps.wgsa.paarsnp.core.lib;

import net.cgps.wgsa.paarsnp.core.lib.blast.BlastMatch;

import java.util.*;
import java.util.function.Function;

public class OverlapRemover implements Function<List<BlastMatch>, Collection<BlastMatch>> {

  private final Comparator<BlastMatch> matchComparer = Comparator
      .comparingDouble(BlastMatch::getPercentIdentity).reversed() // Highest PID first
      .thenComparing(BlastMatch::getLibrarySequenceId)
      .thenComparing(BlastMatch::getQuerySequenceId)
      .thenComparingDouble(BlastMatch::getQuerySequenceStart);

  private final TestForOverlap testOverlap;

  public OverlapRemover(final int allowedOverlap) {

    this.testOverlap = new TestForOverlap(allowedOverlap);
  }

  @Override
  public Collection<BlastMatch> apply(final List<BlastMatch> matchList) {

    // Complete matches checked against themselves for overlaps
    if (matchList.isEmpty()) {
      return matchList;
    }

    matchList.sort(matchComparer);

    final List<BlastMatch> filtered = new ArrayList<>(matchList.size());
    filtered.add(matchList.get(0)); // Add the best hit.

    // Remove overlaps, keeping best first and removing subsequent overlapping matches.
    for (int i = 1; i < matchList.size(); i++) {
      boolean keep = true;
      for (final BlastMatch alreadySelected : filtered) {
        if (this.testOverlap.test(alreadySelected, matchList.get(i))) {
          keep = false;
          break;
        }
      }
      if (keep) {
        filtered.add(matchList.get(i));
      }
    }
    return filtered;
  }
}
