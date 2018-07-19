package net.cgps.wgsa.paarsnp.core.lib.blast;

import net.cgps.wgsa.paarsnp.core.paar.PaarLibrary;
import net.cgps.wgsa.paarsnp.core.paar.ResistanceGene;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Predicate;

public class PaarMatchFilter implements Predicate<BlastMatch> {

  private final Logger logger = LoggerFactory.getLogger(PaarMatchFilter.class);
  private final PaarLibrary paarLibrary;

  public PaarMatchFilter(final PaarLibrary paarLibrary) {

    this.paarLibrary = paarLibrary;
  }

  @Override
  public boolean test(final BlastMatch match) {

    final ResistanceGene resistanceGene = this.paarLibrary.getPaarGene(match.getReferenceMatchSequence());

    // Filter according to thresholds. The second threshold allows very v high similarity matches that have been badly assembled.
    final BlastSearchStatistics blastSearchStatistics = match.getBlastSearchStatistics();

    final double coverage = ((double) blastSearchStatistics.getSubjectMatchLength() / (double) blastSearchStatistics.getLibrarySequenceLength()) * 100;

    this.logger.debug("Filter parameters: name={} matchId={} matchCov={} idThreshold={} covThreshold={}",
        resistanceGene.getFamilyName(), blastSearchStatistics.getPercentIdentity(), coverage, resistanceGene.getSimilarityThreshold(), resistanceGene.getLengthThreshold());

    final boolean lengthThreshold = blastSearchStatistics.getPercentIdentity() > resistanceGene.getSimilarityThreshold();
    final boolean coverageThreshold = coverage > (double) resistanceGene.getLengthThreshold();
    final boolean standardThreshold = lengthThreshold && coverageThreshold;

    this.logger.debug("name={} lengthThreshold={} coverageThreshold={} standardThreshold={}", resistanceGene.getFamilyName(), lengthThreshold, coverageThreshold, standardThreshold);

    if (standardThreshold) {
      return true;
    } else {

      final boolean fragmentThreshold = (blastSearchStatistics.getPercentIdentity() > 95.0) && (coverage > 40);

      this.logger.debug("name={} fragmentThreshold={}", resistanceGene.getFamilyName(), fragmentThreshold);

      return fragmentThreshold;
    }
  }
}
