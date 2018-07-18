package net.cgps.wgsa.paarsnp.core.lib;


import net.cgps.wgsa.paarsnp.core.lib.blast.BlastMatch;

import java.util.function.BiPredicate;

public class TestForOverlap implements BiPredicate<BlastMatch, BlastMatch> {


    private final int overlapThreshold;

    public TestForOverlap(final int overlapThreshold) {

        this.overlapThreshold = overlapThreshold;
    }

    @Override
    public boolean test(final BlastMatch match1, final BlastMatch match2) {

        if (match1.isReversed() != match2.isReversed()) {
            return false;
        }

        // The query coordinates are never reversed.
        final int queryStart1 = match1.getQuerySequenceStart();
        final int queryStop1 = match1.getQuerySequenceStop();

        final int queryStart2 = match2.getQuerySequenceStart();
        final int queryStop2 = match2.getQuerySequenceStop();

        if (queryStop2 < queryStart1 + this.overlapThreshold || queryStop1 < queryStart2 + this.overlapThreshold) {

            return false;
            // No overlap and most common case
        } else if ((queryStart1 <= queryStart2) && (queryStop1 >= queryStop2)) {
            return true; // complete encapsulation
        } else if ((queryStart2 <= queryStart1) && (queryStop2 >= queryStop1)) {

            return true; // complete encapsulation
        } else if ((queryStart1 <= queryStart2) && (queryStart2 <= queryStop1 - this.overlapThreshold)) {

            return true;
        } else return (queryStart2 <= queryStart1) && (queryStart1 <= queryStop2 - this.overlapThreshold);
    }
}