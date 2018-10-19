package net.cgps.wgsa.paarsnp.core.snpar.json;

import java.util.Collection;

public class ResistanceMutationMatch {

  private final ResistanceMutation resistanceMutation;
  private final Collection<Mutation> causalMutations;

  @SuppressWarnings("unused")
  private ResistanceMutationMatch() {
    this(null, null);
  }

  public ResistanceMutationMatch(final ResistanceMutation resistanceMutation, final Collection<Mutation> causalMutations) {

    this.resistanceMutation = resistanceMutation;
    this.causalMutations = causalMutations;
  }

  public ResistanceMutation getResistanceMutation() {
    return this.resistanceMutation;
  }

  public Collection<Mutation> getCausalMutations() {
    return this.causalMutations;
  }
}
