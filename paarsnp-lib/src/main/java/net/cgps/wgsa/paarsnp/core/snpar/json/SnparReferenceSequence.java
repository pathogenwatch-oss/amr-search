package net.cgps.wgsa.paarsnp.core.snpar.json;

import net.cgps.wgsa.paarsnp.core.lib.SequenceType;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;

public class SnparReferenceSequence {

  private final String name;
  private final SequenceType type;
  private final float pid;
  private final float coverage;
  private final Collection<String> variants;
  private final Collection<ResistanceMutation> mappedVariants;

  private SnparReferenceSequence() {
    this("", SequenceType.PROTEIN, 0.0f, 0.0f, Collections.emptyList());
  }

  public SnparReferenceSequence(final String name, final SequenceType type, final float pid, final float coverage, final Collection<String> variants) {

    this.name = name;
    this.type = type;
    this.pid = pid;
    this.coverage = coverage;
    this.variants = variants;
    this.mappedVariants = this.mapVariants(variants, type);
  }

  private Collection<ResistanceMutation> mapVariants(final Collection<String> variants, final SequenceType type) {

    switch (type) {
      case DNA:
        return variants.stream().map(ResistanceMutation.parseSnp()).collect(Collectors.toList());
      case PROTEIN:
      default:
        return variants.stream().map(ResistanceMutation.parseAaVariant()).collect(Collectors.toList());
    }
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

  public SequenceType getType() {

    return this.type;
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
        this.type == that.type &&
        Objects.equals(this.variants, that.variants);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.name, this.type, this.pid, this.coverage, this.variants, this.mappedVariants);
  }
}
