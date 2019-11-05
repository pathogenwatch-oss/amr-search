package net.cgps.wgsa.paarsnp.core.models;

import net.cgps.wgsa.paarsnp.core.models.variants.Variant;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

public class ReferenceSequence {

  private final String name;
  private final int length;
  private final float pid;
  private final float coverage;
  private final Collection<Variant> variants;

  @SuppressWarnings("unused")
  private ReferenceSequence() {
    this("", 0, 0.0f, 0.0f);
  }

  public ReferenceSequence(final String name, final int length, final float pid, final float coverage) {

    this.name = name;
    this.length = length;
    this.pid = pid;
    this.coverage = coverage;
    this.variants = new HashSet<>(100);
  }

  public Collection<Variant> getVariants() {

    return this.variants;
  }

  public String getName() {

    return this.name;
  }

  @SuppressWarnings("unused")
  public int getLength() {
    return this.length;
  }

  public float getPid() {

    return this.pid;
  }

  public float getCoverage() {
    return this.coverage;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || this.getClass() != o.getClass()) return false;
    final ReferenceSequence that = (ReferenceSequence) o;
    return Float.compare(that.pid, this.pid) == 0 &&
        Float.compare(that.coverage, this.coverage) == 0 &&
        Objects.equals(this.name, that.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.name, this.pid, this.coverage, this.variants);
  }

  public void addVariants(final Collection<Variant> newVariants) {
    this.variants.addAll(newVariants);
  }
}
