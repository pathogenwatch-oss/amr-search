package net.cgps.wgsa.paarsnp.core.models;

import java.util.Objects;

public class Location {

  private final int queryLocation;
  private final int referenceLocation;

  @SuppressWarnings("unused")
  private Location() {
    this(0, 0);
  }

  public Location(final int queryLocation, final int referenceLocation) {
    this.queryLocation = queryLocation;
    this.referenceLocation = referenceLocation;
  }

  public int getQueryLocation() {
    return this.queryLocation;
  }

  public int getReferenceLocation() {
    return this.referenceLocation;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    final Location location = (Location) o;
    return queryLocation == location.queryLocation &&
        referenceLocation == location.referenceLocation;
  }

  @Override
  public int hashCode() {
    return Objects.hash(queryLocation, referenceLocation);
  }
}
