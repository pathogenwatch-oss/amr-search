package net.cgps.wgsa.paarsnp.core.snpar.json;


import net.cgps.wgsa.paarsnp.core.lib.json.AbstractJsonnable;
import net.cgps.wgsa.paarsnp.core.snpar.MutationType;

public class Mutation extends AbstractJsonnable {

  private final String referenceId;
  private final String originalSequence;
  private final int referenceLocation;
  private final String mutationSequence;
  private final MutationType mutationType;
  private final int queryLocation;

  @SuppressWarnings("unused")
  private Mutation() {

    this(MutationType.S, 0, "", "", "", 0);
  }

  public Mutation(final MutationType mutationType, final int queryLocation, final String mutationSequence, final String originalSequence, final String referenceId, final int referenceLocation) {

    this.mutationType = mutationType;
    this.queryLocation = queryLocation;
    this.mutationSequence = mutationSequence;
    this.originalSequence = originalSequence;
    this.referenceId = referenceId;
    this.referenceLocation = referenceLocation;
  }

  public String getReferenceId() {

    return this.referenceId;
  }

  public String getOriginalSequence() {

    return this.originalSequence;
  }

  public int getReferenceLocation() {

    return this.referenceLocation;
  }

  public String getMutationSequence() {

    return this.mutationSequence;
  }

  public MutationType getMutationType() {

    return this.mutationType;
  }

  public int getQueryLocation() {

    return this.queryLocation;
  }
}
