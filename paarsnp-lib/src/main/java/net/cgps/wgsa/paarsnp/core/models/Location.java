package net.cgps.wgsa.paarsnp.core.models;

import java.util.Objects;

public class Location {

  private final int queryIndex;
  private final int referenceIndex;

  @SuppressWarnings("unused")
  private Location() {
    this(0, 0);
  }

  public Location(final int queryLocation, final int referenceLocation) {
    this.queryIndex = queryLocation;
    this.referenceIndex = referenceLocation;
  }

  public int getQueryIndex() {
    return this.queryIndex;
  }

  public int getReferenceIndex() {
    return this.referenceIndex;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    final Location location = (Location) o;
    return queryIndex == location.queryIndex &&
        referenceIndex == location.referenceIndex;
  }

  @Override
  public int hashCode() {
    return Objects.hash(queryIndex, referenceIndex);
  }
}
