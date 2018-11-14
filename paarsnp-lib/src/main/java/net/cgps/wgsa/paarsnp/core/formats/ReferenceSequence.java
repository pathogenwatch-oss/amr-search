package net.cgps.wgsa.paarsnp.core.formats;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.stream.Collectors;

public class ReferenceSequence {

  private final String name;
  private final float pid;
  private final float coverage;
  private final Collection<String> variants;
  private final Collection<Variant> mappedVariants;

  @SuppressWarnings("unused")
  private ReferenceSequence() {
    this("", 0.0f, 0.0f);
  }

  public ReferenceSequence(final String name, final float pid, final float coverage) {

    this.name = name;
    this.pid = pid;
    this.coverage = coverage;
    this.variants = new HashSet<>(100);
    this.mappedVariants = new HashSet<>(100);
  }

  public Collection<Variant> getMappedVariants() {

    return this.mappedVariants;
  }

  @SuppressWarnings("unused")
  public Collection<String> getVariants() {
    return this.variants;
  }

  public String getName() {

    return this.name;
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
        Objects.equals(this.name, that.name) &&
        Objects.equals(this.variants, that.variants);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.name, this.pid, this.coverage, this.variants, this.mappedVariants);
  }

  public void addVariants(final Collection<String> newVariants) {
    this.variants.addAll(newVariants);
    this.mappedVariants.addAll(newVariants.stream().map(ResistanceMutation.parseVariant()).collect(Collectors.toList()));
  }
}
