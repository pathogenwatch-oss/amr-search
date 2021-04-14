package net.cgps.wgsa.paarsnp.output;

import net.cgps.wgsa.paarsnp.core.lib.AbstractJsonnable;

import java.util.Objects;

public class Determinant extends AbstractJsonnable {

  private final String name;
  private final DeterminantClass resistanceEffect;

  private Determinant() {
    this("", DeterminantClass.RESISTANCE);
  }

  public Determinant(final String name, final DeterminantClass resistanceEffect) {
    this.name = name;
    this.resistanceEffect = resistanceEffect;
  }

  public String getName() {
    return name;
  }

  public DeterminantClass getResistanceEffect() {
    return resistanceEffect;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    final Determinant that = (Determinant) o;
    return Objects.equals(name, that.name) && resistanceEffect == that.resistanceEffect;
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, resistanceEffect);
  }
}
