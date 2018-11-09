package net.cgps.wgsa.paarsnp.core.snpar.json;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;

public class SnparReferenceSequence {

  private final String name;
  private final float pid;
  private final float coverage;
  private final Collection<String> variants;
  private final Collection<ResistanceMutation> mappedVariants;

  private SnparReferenceSequence() {
    this("", 0.0f, 0.0f, Collections.emptyList());
  }

  public SnparReferenceSequence(final String name, final float pid, final float coverage, final Collection<String> variants) {

    this.name = name;
    this.pid = pid;
    this.coverage = coverage;
    this.variants = variants;
    this.mappedVariants = this.mapVariants(variants);
  }

  private Collection<ResistanceMutation> mapVariants(final Collection<String> variants) {

    return variants.stream().map(ResistanceMutation.parseVariant()).collect(Collectors.toList());
  }

  public Collection<ResistanceMutation> getMappedVariants() {

    return this.mappedVariants;
  }

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
    final SnparReferenceSequence that = (SnparReferenceSequence) o;
    return Float.compare(that.pid, this.pid) == 0 &&
        Float.compare(that.coverage, this.coverage) == 0 &&
        Objects.equals(this.name, that.name) &&
        Objects.equals(this.variants, that.variants);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.name, this.pid, this.coverage, this.variants, this.mappedVariants);
  }
}
