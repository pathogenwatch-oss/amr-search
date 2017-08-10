package net.cgps.wgsa.paarsnp.core.snpar.json;

import net.cgps.wgsa.paarsnp.core.lib.json.AbstractJsonnable;

import java.util.ArrayList;
import java.util.Collection;

public class SnpResistanceElement extends AbstractJsonnable {

  private final ResistanceMutation resistanceMutation;
  private final Collection<Mutation> causalMutations;

  @SuppressWarnings("unused")
  private SnpResistanceElement() {

    this(null, new ArrayList<>(0));
  }

  public SnpResistanceElement(final ResistanceMutation resistanceMutation, final Collection<Mutation> causalMutations) {

    this.resistanceMutation = resistanceMutation;
    this.causalMutations = new ArrayList<>(causalMutations);
  }

  public final ResistanceMutation getResistanceMutation() {

    return this.resistanceMutation;
  }

  public final Collection<Mutation> getCausalMutations() {

    return this.causalMutations;
  }
}
