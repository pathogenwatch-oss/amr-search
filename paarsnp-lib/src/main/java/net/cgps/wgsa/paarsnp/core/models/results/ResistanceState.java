package net.cgps.wgsa.paarsnp.core.models.results;


public enum ResistanceState {

  RESISTANT(1),
  INDUCIBLE(2),
  INTERMEDIATE(3),
  NOT_FOUND(4);

  private final int rank;
  ResistanceState(final int rank) {

    this.rank = rank;
  }

  public int getRank() {
    return this.rank;
  }
}
