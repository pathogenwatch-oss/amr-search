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

    if (!this.paarLibrary.getPaarGene(match.getLibrarySequenceId()).isPresent()) {
      this.logger.error("{} not found in PAAR library", match.getLibrarySequenceId());
      throw new RuntimeException("Sequences in blast library that are not in the PAAR library for " + this.paarLibrary.getSpeciesId() + ".");
    }

    final ResistanceGene resistanceGene = this.paarLibrary.getPaarGene(match.getLibrarySequenceId()).get();

    // Filter according to thresholds. The second threshold allows very v high similarity matches that have been badly assembled.
    final double coverage = ((double) match.getSubjectMatchLength() / (double) resistanceGene.getLength()) * 100;

    this.logger.debug("Filter parameters: name={} matchId={} matchCov={} idThreshold={} covThreshold={}", resistanceGene.getFamilyName(), match.getPercentIdentity(), coverage, resistanceGene.getSimilarityThreshold(), resistanceGene.getLengthThreshold());

    final boolean lengthThreshold = match.getPercentIdentity() > resistanceGene.getSimilarityThreshold();
    final boolean coverageThreshold = coverage > (double) resistanceGene.getLengthThreshold();
    final boolean standardThreshold = lengthThreshold && coverageThreshold;

    this.logger.debug("name={} lengthThreshold={} coverageThreshold={} standardThreshold={}", resistanceGene.getFamilyName(), lengthThreshold, coverageThreshold, standardThreshold);

    if (standardThreshold) {
      return true;
    } else {

      final boolean fragmentThreshold = (match.getPercentIdentity() > 95.0) && (coverage > 40);

      this.logger.debug("name={} fragmentThreshold={}", resistanceGene.getFamilyName(), fragmentThreshold);

      return fragmentThreshold;
    }
  }
}
