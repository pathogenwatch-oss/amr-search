package net.cgps.wgsa.paarsnp.core.snpar.codonmapping;

public enum FRAME {
  ONE(0), TWO(2), THREE(1);

  final int offset;

  FRAME(int offset) {
    this.offset = offset;
  }

  public static FRAME toFrame(final int referencePosition) {
    return FRAME.values()[((referencePosition - 1) % 3)];
  }

  public int getOffset() {
    return this.offset;
  }
}
