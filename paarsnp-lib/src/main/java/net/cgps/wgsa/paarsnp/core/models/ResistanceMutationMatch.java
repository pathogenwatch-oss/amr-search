package net.cgps.wgsa.paarsnp.core.models;

import net.cgps.wgsa.paarsnp.core.models.variants.Variant;

import java.util.Collection;
import java.util.Objects;

public class ResistanceMutationMatch {

  private final Variant resistanceMutation;
  private final Collection<Location> causalMutations;

  @SuppressWarnings("unused")
  private ResistanceMutationMatch() {
    this(null, null);
  }

  public ResistanceMutationMatch(final Variant resistanceMutation, final Collection<Location> causalMutations) {

    this.resistanceMutation = resistanceMutation;
    this.causalMutations = causalMutations;
  }

  public Variant getResistanceMutation() {
    return this.resistanceMutation;
  }

  public Collection<Location> getLocations() {
    return this.causalMutations;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    final ResistanceMutationMatch that = (ResistanceMutationMatch) o;
    return Objects.equals(resistanceMutation, that.resistanceMutation) &&
        Objects.equals(causalMutations, that.causalMutations);
  }

  @Override
  public int hashCode() {
    return Objects.hash(resistanceMutation, causalMutations);
  }

  @Override
  public String toString() {
    return "ResistanceMutationMatch{" +
        "resistanceMutation=" + resistanceMutation +
        ", causalMutations=" + causalMutations +
        '}';
  }
}
