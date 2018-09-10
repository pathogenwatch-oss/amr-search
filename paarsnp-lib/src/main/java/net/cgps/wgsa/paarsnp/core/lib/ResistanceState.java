package net.cgps.wgsa.paarsnp.core.lib;


public enum ResistanceState {

  INDUCIBLE(2), RESISTANT(1), NOT_FOUND(4), INTERMEDIATE(3);

  private final int rank;
  ResistanceState(final int rank) {

    this.rank = rank;
  }

  public int getRank() {
    return this.rank;
  }
}
