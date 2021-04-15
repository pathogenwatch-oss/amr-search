package net.cgps.wgsa.paarsnp.output;

import com.fasterxml.jackson.annotation.JsonInclude;
import net.cgps.wgsa.paarsnp.core.lib.AbstractJsonnable;

import java.util.Objects;

public class Determinant extends AbstractJsonnable {

  private final String gene;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private final String variant;
  private final DeterminantClass resistanceEffect;

  private Determinant() {
    this("", null, DeterminantClass.RESISTANCE);
  }

  public Determinant(final String gene, final String variant, final DeterminantClass resistanceEffect) {
    this.gene = gene;
    this.variant = variant;
    this.resistanceEffect = resistanceEffect;
  }

  public String getGene() {
    return gene;
  }

  public String getVariant() {
    return variant;
  }

  public DeterminantClass getResistanceEffect() {
    return resistanceEffect;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    final Determinant that = (Determinant) o;
    return Objects.equals(gene, that.gene) && Objects.equals(variant, that.variant) && resistanceEffect == that.resistanceEffect;
  }

  @Override
  public int hashCode() {
    return Objects.hash(gene, variant, resistanceEffect);
  }
}
