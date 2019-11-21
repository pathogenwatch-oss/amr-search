package net.cgps.wgsa.paarsnp.core.snpar.codonmapping;

public enum FRAME {
  ONE(0, 0), TWO(1, 2), THREE(2, 1);

  private final int index;
  private final int codonOffset;

  FRAME(final int index, final int codonOffset) {
    this.index = index;
    this.codonOffset = codonOffset;
  }

  public static FRAME toFrame(final int referencePosition) {
    return FRAME.values()[((referencePosition - 1) % 3)];
  }

  public int getCodonOffset() {
    return this.codonOffset;
  }

  public int getIndex() {
    return this.index;
  }
}
