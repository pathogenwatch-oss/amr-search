package net.cgps.wgsa.paarsnp.core.lib.blast;

import net.cgps.wgsa.paarsnp.core.lib.json.AbstractJsonnable;

import java.util.function.Function;

public class BlastSearchStatistics extends AbstractJsonnable {

  // Don't use a char[] here as the internal elements aren't immutable.
  private final String librarySequenceId;
  private final int librarySequenceStart;
  private final String querySequenceId;
  private final int querySequenceStart;
  private final double percentIdentity;
  private final double evalue;
  private final boolean reversed;
  private final int librarySequenceStop;
  private final int querySequenceStop;
  private final int librarySequenceLength;

  @SuppressWarnings("unused")
  private BlastSearchStatistics() {

    this("", 0, "", 0, 0.0, 0.0, false, 0, 0, 0);
  }

  public BlastSearchStatistics(final String librarySequenceId, final int librarySequenceStart, final String querySequenceId, final int querySequenceStart, final double percentIdentity, final double evalue, final boolean reversed, final int librarySequenceStop, final int querySequenceStop, final int librarySequenceLength) {

    this.librarySequenceId = librarySequenceId;
    this.querySequenceId = querySequenceId;
    this.querySequenceStart = querySequenceStart;
    this.percentIdentity = percentIdentity;
    this.evalue = evalue;
    this.reversed = reversed;
    this.querySequenceStop = querySequenceStop;
    this.librarySequenceLength = librarySequenceLength;

    if (reversed) {
      this.librarySequenceStart = librarySequenceStop;
      this.librarySequenceStop = librarySequenceStart;
    } else {
      this.librarySequenceStart = librarySequenceStart;
      this.librarySequenceStop = librarySequenceStop;
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

  public double getEvalue() {

    return this.evalue;
  }

  public boolean isReversed() {

    return this.reversed;
  }

  public String format(final Function<BlastSearchStatistics, char[]> blastSnpMatchFormatter) {

    return new String(blastSnpMatchFormatter.apply(this));
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
        ", reversed=" + this.reversed +
        ", librarySequenceStop=" + this.librarySequenceStop +
        ", querySequenceStop=" + this.querySequenceStop +
        ", librarySequenceLength=" + this.librarySequenceLength +
        '}';
  }
}
