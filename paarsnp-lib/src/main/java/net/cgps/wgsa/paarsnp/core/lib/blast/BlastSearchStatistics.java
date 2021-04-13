package net.cgps.wgsa.paarsnp.core.lib.blast;

import net.cgps.wgsa.paarsnp.core.lib.AbstractJsonnable;
import net.cgps.wgsa.paarsnp.core.lib.utils.DnaSequence;

public class BlastSearchStatistics extends AbstractJsonnable {

  private final String referenceId;
  private final int referenceStart;
  private final String queryId;
  private final int queryStart;
  private final double percentIdentity;
  private final double evalue;
  private final DnaSequence.Strand strand;
  private final boolean reversed;
  private final int referenceStop;
  private final int queryStop;
  private final int referenceLength;

  @SuppressWarnings("unused")
  private BlastSearchStatistics() {

    this("", 0, 0, 0, "", 0, 0, 0.0, 0.0, DnaSequence.Strand.FORWARD);
  }

  public BlastSearchStatistics(final String referenceId, final int referenceStart, final int referenceStop, final int referenceLength, final String querySequenceId, final int querySequenceStart, final int querySequenceStop, final double evalue, final double percentIdentity, final DnaSequence.Strand strand) {

    this.referenceId = referenceId;
    this.queryId = querySequenceId;
    this.queryStart = querySequenceStart;
    this.percentIdentity = (double) Math.round(percentIdentity * 100) / 100;
    this.evalue = evalue;
    this.strand = strand;
    this.queryStop = querySequenceStop;
    this.referenceLength = referenceLength;

    if (DnaSequence.Strand.FORWARD == strand) {
      this.referenceStart = referenceStart;
      this.referenceStop = referenceStop;
      this.reversed = false;
    } else {
      this.referenceStart = referenceStop;
      this.referenceStop = referenceStart;
      this.reversed = true;
    }
  }

  public String getReferenceId() {

    return this.referenceId;
  }

  public int getReferenceStart() {

    return this.referenceStart;
  }

  public int getReferenceStop() {

    return this.referenceStop;
  }

  public int getReferenceLength() {

    return this.referenceLength;
  }

  public String getQueryId() {

    return this.queryId;
  }

  public int getQueryStart() {

    return this.queryStart;
  }

  public int getQueryStop() {

    return this.queryStop;
  }

  public double getPercentIdentity() {

    return this.percentIdentity;
  }

  @SuppressWarnings("unused")
  public double getEvalue() {

    return this.evalue;
  }

  public DnaSequence.Strand getStrand() {

    return this.strand;
  }

  @Override
  public String toString() {

    return "BlastSearchStatistics{" +
        "librarySequenceId='" + this.referenceId + '\'' +
        ", librarySequenceStart=" + this.referenceStart +
        ", querySequenceId='" + this.queryId + '\'' +
        ", querySequenceStart=" + this.queryStart +
        ", percentIdentity=" + this.percentIdentity +
        ", evalue=" + this.evalue +
        ", strand=" + this.strand +
        ", librarySequenceStop=" + this.referenceStop +
        ", querySequenceStop=" + this.queryStop +
        ", librarySequenceLength=" + this.referenceLength +
        '}';
  }

  @SuppressWarnings("unused")
  @Deprecated
  // This method is preserved so that the output JSON still works with the current pathogenwatch wrapper.
  public boolean isReversed() {
    return this.reversed;
  }
}
