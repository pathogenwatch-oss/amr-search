package net.cgps.wgsa.paarsnp.core.snpar.json;


import net.cgps.wgsa.paarsnp.core.lib.json.AbstractJsonnable;

public class Mutation extends AbstractJsonnable {

  private final char originalSequence;
  private final int referenceLocation;
  private final char mutationSequence;
  private final MutationType mutationType;
  private final int queryLocation;

  @SuppressWarnings("unused")
  private Mutation() {

    this(MutationType.S, 0, 'a', 'a', 0);
  }

  public Mutation(final MutationType mutationType, final int queryLocation, final char mutationSequence, final char originalSequence, final int referenceLocation) {

    this.mutationType = mutationType;
    this.queryLocation = queryLocation;
    this.mutationSequence = mutationSequence;
    this.originalSequence = originalSequence;
    this.referenceLocation = referenceLocation;
  }

  public char getOriginalSequence() {

    return this.originalSequence;
  }

  public int getReferenceLocation() {

    return this.referenceLocation;
  }

  public char getMutationSequence() {

    return this.mutationSequence;
  }

  public MutationType getMutationType() {

    return this.mutationType;
  }

  public int getQueryLocation() {

    return this.queryLocation;
  }

  public enum MutationType {

    S, I, D
  }
}
