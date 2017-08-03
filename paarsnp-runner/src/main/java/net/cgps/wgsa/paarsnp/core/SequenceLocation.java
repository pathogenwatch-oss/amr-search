package net.cgps.wgsa.paarsnp.core;

/**
 * Created by David Alonso Garcia on 30/03/15.
 */
public class SequenceLocation {

  private final int sequenceIndex;
  private final String sequenceIdentifier;
  private final char nt;

  public SequenceLocation(final int sequenceIndex, final String sequenceIdentifier, final char nt) {

    this.sequenceIndex = sequenceIndex;
    this.sequenceIdentifier = sequenceIdentifier;
    this.nt = nt;
  }

  public int getSequenceIndex() {

    return this.sequenceIndex;
  }

  public String getSequenceIdentifier() {

    return this.sequenceIdentifier;
  }

  public char getNt() {

    return this.nt;
  }
}
