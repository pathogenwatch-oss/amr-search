package net.cgps.wgsa.paarsnp.core.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class ResistanceMutationMatch {

  private final String name;
  private final List<Location> causalMutations;

  @SuppressWarnings("unused")
  private ResistanceMutationMatch() {
    this(null, null);
  }

  public ResistanceMutationMatch(final String variantName, final Collection<Location> causalMutations) {

    this.name = variantName;
    this.causalMutations = new ArrayList<>(causalMutations);
  }

  public String getName() {
    return this.name;
  }

  public List<Location> getLocations() {
    return this.causalMutations;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    final ResistanceMutationMatch that = (ResistanceMutationMatch) o;
    return Objects.equals(name, that.name) &&
        Objects.equals(causalMutations, that.causalMutations);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, causalMutations);
  }

  @Override
  public String toString() {
    return "ResistanceMutationMatch{" +
        "resistanceMutation=" + name +
        ", causalMutations=" + causalMutations +
        '}';
  }
}
