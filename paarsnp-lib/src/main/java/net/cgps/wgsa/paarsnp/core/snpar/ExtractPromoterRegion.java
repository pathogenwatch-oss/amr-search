package net.cgps.wgsa.paarsnp.core.snpar;

import net.cgps.wgsa.paarsnp.core.lib.blast.BlastSearchStatistics;
import net.cgps.wgsa.paarsnp.core.lib.utils.DnaSequence;

import java.util.Optional;
import java.util.function.BiFunction;

public class ExtractPromoterRegion implements BiFunction<BlastSearchStatistics, String, Optional<String>> {
  @Override
  public Optional<String> apply(final BlastSearchStatistics blastSearchStatistics, final String contigSequence) {

    final Optional<String> promoterSequence;
    if (DnaSequence.Strand.FORWARD == blastSearchStatistics.getStrand()) {

      if (1 == blastSearchStatistics.getQuerySequenceStart()) {
        promoterSequence = Optional.empty();
      } else {
        final int startSite = blastSearchStatistics.getQuerySequenceStart() < 60?
                              1 :
                              blastSearchStatistics.getQuerySequenceStart() - 60;

        promoterSequence = Optional.of(contigSequence.substring(startSite - 1, blastSearchStatistics.getQuerySequenceStart() - 1));
      }
    } else {
      if (contigSequence.length() == blastSearchStatistics.getQuerySequenceStop()) {
        promoterSequence = Optional.empty();
      } else {
        final int startSite = 60 < contigSequence.length() - blastSearchStatistics.getQuerySequenceStop() ?
                              contigSequence.length() :
                              blastSearchStatistics.getQuerySequenceStop() + 60;
        promoterSequence = Optional.of(DnaSequence.complement(contigSequence.substring(blastSearchStatistics.getLibrarySequenceStop(), startSite)));
      }
    }
    return promoterSequence;
  }
}
