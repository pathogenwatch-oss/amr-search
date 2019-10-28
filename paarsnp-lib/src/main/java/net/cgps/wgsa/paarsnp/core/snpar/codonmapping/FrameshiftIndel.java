package net.cgps.wgsa.paarsnp.core.snpar.codonmapping;

public class FrameshiftIndel {

  private final int alignmentLocation;
  private final int length;
  private final int shift;

  public FrameshiftIndel(final int alignmentLocation, final int length, final int shift) {
    this.alignmentLocation = alignmentLocation;
    this.length = length;
    this.shift = shift;
  }

  public int getAlignmentLocation() {
    return this.alignmentLocation;
  }

  public int getLength() {
    return this.length;
  }

  public int getShift() {
    return this.shift;
  }
}
