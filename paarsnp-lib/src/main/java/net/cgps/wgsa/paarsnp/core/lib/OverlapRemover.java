package net.cgps.wgsa.paarsnp.core.lib;

import net.cgps.wgsa.paarsnp.core.lib.blast.BlastMatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.function.Function;

public class OverlapRemover implements Function<List<BlastMatch>, Collection<BlastMatch>> {

  private final Logger logger = LoggerFactory.getLogger(OverlapRemover.class);

  private final int allowedOverlap;

  public OverlapRemover(final int allowedOverlap) {

    this.allowedOverlap = allowedOverlap;
  }

  @Override
  public Collection<BlastMatch> apply(final List<BlastMatch> possiblyOverlappingMatches) {

    final Collection<BlastMatch> selectedMatches = new ArrayList<>(possiblyOverlappingMatches.size());
    final Collection<BlastMatch> rejected = new HashSet<>(possiblyOverlappingMatches.size()); // Mark matches for rejection

    // Check matches for overlaps.
    for (int i = 0; i < possiblyOverlappingMatches.size(); i++) {

      // Skip if already has been rejected.
      if (rejected.contains(possiblyOverlappingMatches.get(i))) {
        continue;
      }

      final BlastMatch firstBlastMatch = possiblyOverlappingMatches.get(i);

      for (int j = i + 1; j < possiblyOverlappingMatches.size(); j++) {

        final BlastMatch secondBlastMatch = possiblyOverlappingMatches.get(j);

        this.logger.debug("Comparing id:{} start:{} stop:{} rid:{} rstart:{} rstop:{} with id:{} start:{} stop:{} rid:{} rstart:{} rstop:{}", firstBlastMatch.getQuerySequenceId(), firstBlastMatch.getQuerySequenceStart(), firstBlastMatch.getQuerySequenceStop(), firstBlastMatch.getLibrarySequenceId(), firstBlastMatch.getLibrarySequenceStart(), firstBlastMatch.getLibrarySequenceStop(), secondBlastMatch.getQuerySequenceId(), secondBlastMatch.getQuerySequenceStart(), secondBlastMatch.getQuerySequenceStop(), secondBlastMatch.getLibrarySequenceId(), secondBlastMatch.getLibrarySequenceStart(), secondBlastMatch.getLibrarySequenceStop());

        if (firstBlastMatch.getQuerySequenceId().equals(secondBlastMatch.getQuerySequenceId())
            &&
            BlastMatch.significantOverlap(firstBlastMatch.getQuerySequenceStart(), firstBlastMatch.getQuerySequenceStop(), !firstBlastMatch.isReversed(), secondBlastMatch.getQuerySequenceStart(), secondBlastMatch.getQuerySequenceStop(), !secondBlastMatch.isReversed(), this.allowedOverlap)) {

          final BlastMatch reject = this.selectReject(firstBlastMatch, secondBlastMatch);

          this.logger.debug("Removing due to overlap id:{} pid:{}, start:{}, stop:{}", reject.getLibrarySequenceId(), reject.getPercentIdentity(), reject.getLibrarySequenceStart(), reject.getLibrarySequenceStop());

          rejected.add(reject); // Note the rejected one for skipping down the line
        }
      }

      // If it hasn't yet been rejected, keep it.
      if (!rejected.contains(possiblyOverlappingMatches.get(i))) {
        selectedMatches.add(possiblyOverlappingMatches.get(i));
      }
    }

    return selectedMatches;
  }


  private BlastMatch selectReject(final BlastMatch blastMatchA, final BlastMatch blastMatchB) {

    return blastMatchA.getPercentIdentity() > blastMatchB.getPercentIdentity() ? blastMatchB : blastMatchA;
  }

}
