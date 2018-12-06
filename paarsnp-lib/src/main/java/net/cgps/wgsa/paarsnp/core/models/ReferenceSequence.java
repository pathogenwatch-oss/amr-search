package net.cgps.wgsa.paarsnp.core.models;

import net.cgps.wgsa.paarsnp.core.models.variants.TranscribedVariant;
import net.cgps.wgsa.paarsnp.core.models.variants.VariantParser;
import net.cgps.wgsa.paarsnp.core.models.variants.implementations.Frameshift;
import net.cgps.wgsa.paarsnp.core.models.variants.implementations.PrematureStop;
import net.cgps.wgsa.paarsnp.core.models.variants.implementations.ResistanceMutation;

import java.util.*;

public class ReferenceSequence {

  private final String name;
  private final int length;
  private final float pid;
  private final float coverage;
  private final Collection<TranscribedVariant> transcribedVariants;

  @SuppressWarnings("unused")
  private ReferenceSequence() {
    this("", 0, 0.0f, 0.0f);
  }

  public ReferenceSequence(final String name, final int length, final float pid, final float coverage) {

    this.name = name;
    this.length = length;
    this.pid = pid;
    this.coverage = coverage;
    this.transcribedVariants = new HashSet<>(100);
  }

  public Collection<TranscribedVariant> getTranscribedVariants() {

    return this.transcribedVariants;
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
    return Objects.hash(this.name, this.pid, this.coverage, this.transcribedVariants);
  }

  public void addVariants(final Collection<String> newVariants) {

    final VariantParser variantParser = new VariantParser();

    final List<TranscribedVariant> mappedVariants = new ArrayList<>(500);

    for (final String newVariant : newVariants) {
      if ("truncated".equals(newVariant.toLowerCase())) {
        mappedVariants.add(new PrematureStop(this.length));
      } else if ("frameshift".equals(newVariant.toLowerCase())) {
        mappedVariants.add(new Frameshift());
      } else {
        final Map.Entry<Integer, Map.Entry<Character, Character>> mutation = variantParser.apply(newVariant);
        mappedVariants.add(ResistanceMutation.build(newVariant, mutation, this.length));
      }
    }

    this.transcribedVariants.addAll(mappedVariants);
  }
}
