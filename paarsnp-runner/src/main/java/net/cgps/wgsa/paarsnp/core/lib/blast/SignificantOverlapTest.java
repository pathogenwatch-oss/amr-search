package net.cgps.wgsa.paarsnp.core.lib.blast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

public class SignificantOverlapTest implements BiFunction<BlastMatch, BlastMatch, Boolean> {

  private final short overlapThreshold;

  public SignificantOverlapTest(final short threshold) {

    this.overlapThreshold = threshold;
  }

  @Override
  public Boolean apply(final BlastMatch match1, final BlastMatch match2) {

    if (!match1.getQuerySequenceId().equals(match2.getQuerySequenceId())) {
      return false;
    }

    // Check that the matches are on the same strand
    if (match1.isReversed() == match2.isReversed()) {
      return false;
    }

    final List<Integer> startStop1 = new ArrayList<>();
    startStop1.add(match1.getQuerySequenceStart());
    startStop1.add(match1.getQuerySequenceStop());

    Collections.sort(startStop1);

    final List<Integer> startStop2 = new ArrayList<>();
    startStop2.add(match2.getQuerySequenceStart());
    startStop2.add(match2.getQuerySequenceStop());

    Collections.sort(startStop2);

    final int overlap;

    if (startStop1.get(0) <= startStop2.get(0) && startStop2.get(1) <= startStop1.get(1)) {
      // Check if one is inside the other

      overlap = startStop2.get(1) - startStop2.get(0) + 1;

    } else if (startStop2.get(0) <= startStop1.get(0) && startStop1.get(1) <= startStop2.get(1)) {
      // Check if the other inside the one

      overlap = startStop1.get(1) - startStop1.get(0) + 1;

    } else if ((startStop1.get(0) <= startStop2.get(1)) && (startStop1.get(1) >= startStop2.get(0))) {
      // Check if they overlap

      overlap = startStop1.get(0) > startStop2.get(0) ?
                (startStop2.get(1) - startStop1.get(0) + 1) :
                (startStop1.get(1) - startStop2.get(0) + 1);
    } else {
      overlap = 0;
    }

    return overlap > this.overlapThreshold;
  }
}
