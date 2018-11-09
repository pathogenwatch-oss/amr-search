package net.cgps.wgsa.paarsnp.core.lib.blast;

import net.cgps.wgsa.paarsnp.core.lib.utils.DnaSequence;

public class BlastMatch {

  // Don't use a char[] here as the internal elements aren't immutable.
  private final BlastSearchStatistics blastSearchStatistics;
  private final String referenceMatchSequence;
  private final String forwardMatchSequence;

  public BlastMatch(final BlastSearchStatistics blastSearchStatistics, final String queryMatchSequence, final String referenceMatchSequence) {
    this.blastSearchStatistics = blastSearchStatistics;
    this.referenceMatchSequence = this.buildMatchSequence(referenceMatchSequence, blastSearchStatistics.getStrand());
    this.forwardMatchSequence = this.buildMatchSequence(queryMatchSequence, blastSearchStatistics.getStrand());
  }

  public float calculateCoverage() {
    return (((float) this.blastSearchStatistics.getLibrarySequenceStop() - this.blastSearchStatistics.getLibrarySequenceStart() + 1.0f)
        / (float) this.blastSearchStatistics.getLibrarySequenceLength())
        * 100.0f;
  }

  public boolean containsPosition(final int position) {
    return this.getBlastSearchStatistics().getLibrarySequenceStart() <= position && position <= this.getBlastSearchStatistics().getLibrarySequenceStop();
  }

  public final BlastSearchStatistics getBlastSearchStatistics() {
    return this.blastSearchStatistics;
  }

  /**
   * Converts the query match sequence to blast to the orientation of the reference gene.
   *
   * @param sequence - input sequence
   * @param strand   - blast match starnd
   * @return correctly orientated query sequence.
   */
  // Should be external really.
  private String buildMatchSequence(final String sequence, final DnaSequence.Strand strand) {

    if (DnaSequence.Strand.FORWARD == strand) {
      return sequence;
    } else {
      return DnaSequence.reverseTranscribe(sequence);
    }
  }

  public final String getForwardQuerySequence() {

    return this.forwardMatchSequence;
  }

  public String getReferenceMatchSequence() {
    return this.referenceMatchSequence;
  }

  @Override
  public String toString() {
    return "BlastMatch{" +
        "blastSearchStatistics=" + this.blastSearchStatistics +
        '}';
  }
}
