package net.cgps.wgsa.paarsnp.core.lib.blast;

import net.cgps.wgsa.paarsnp.core.lib.AbstractJsonnable;
import net.cgps.wgsa.paarsnp.core.lib.utils.DnaSequence;

public class BlastSearchStatistics extends AbstractJsonnable {

  private final String librarySequenceId;
  private final int librarySequenceStart;
  private final String querySequenceId;
  private final int querySequenceStart;
  private final double percentIdentity;
  private final double evalue;
  private final DnaSequence.Strand strand;
  private final boolean reversed;
  private final int librarySequenceStop;
  private final int querySequenceStop;
  private final int librarySequenceLength;

  @SuppressWarnings("unused")
  private BlastSearchStatistics() {

    this("", 0, 0, 0, "", 0, 0, 0.0, 0.0, DnaSequence.Strand.FORWARD);
  }

  public BlastSearchStatistics(final String librarySequenceId, final int librarySequenceStart, final int librarySequenceStop, final int librarySequenceLength, final String querySequenceId, final int querySequenceStart, final int querySequenceStop, final double evalue, final double percentIdentity, final DnaSequence.Strand strand) {

    this.librarySequenceId = librarySequenceId;
    this.querySequenceId = querySequenceId;
    this.querySequenceStart = querySequenceStart;
    this.percentIdentity = percentIdentity;
    this.evalue = evalue;
    this.strand = strand;
    this.querySequenceStop = querySequenceStop;
    this.librarySequenceLength = librarySequenceLength;

    if (DnaSequence.Strand.FORWARD == strand) {
      this.librarySequenceStart = librarySequenceStart;
      this.librarySequenceStop = librarySequenceStop;
      this.reversed = false;
    } else {
      this.librarySequenceStart = librarySequenceStop;
      this.librarySequenceStop = librarySequenceStart;
      this.reversed = true;
    }
  }

  public String getLibrarySequenceId() {

    return this.librarySequenceId;
  }

  public int getLibrarySequenceStart() {

    return this.librarySequenceStart;
  }

  public int getLibrarySequenceStop() {

    return this.librarySequenceStop;
  }

  public int getLibrarySequenceLength() {

    return this.librarySequenceLength;
  }

  public String getQuerySequenceId() {

    return this.querySequenceId;
  }

  public int getQuerySequenceStart() {

    return this.querySequenceStart;
  }

  public int getQuerySequenceStop() {

    return this.querySequenceStop;
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
        "librarySequenceId='" + this.librarySequenceId + '\'' +
        ", librarySequenceStart=" + this.librarySequenceStart +
        ", querySequenceId='" + this.querySequenceId + '\'' +
        ", querySequenceStart=" + this.querySequenceStart +
        ", percentIdentity=" + this.percentIdentity +
        ", evalue=" + this.evalue +
        ", strand=" + this.strand +
        ", librarySequenceStop=" + this.librarySequenceStop +
        ", querySequenceStop=" + this.querySequenceStop +
        ", librarySequenceLength=" + this.librarySequenceLength +
        '}';
  }

  @SuppressWarnings("unused")
  @Deprecated
  // This method is preserved so that the output JSON still works with the current pathogenwatch wrapper.
  public boolean isReversed() {
    return this.reversed;
  }
}
