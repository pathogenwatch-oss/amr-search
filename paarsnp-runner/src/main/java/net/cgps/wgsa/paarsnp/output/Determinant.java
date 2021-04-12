package net.cgps.wgsa.paarsnp.output;

import net.cgps.wgsa.paarsnp.core.lib.AbstractJsonnable;

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

  private String getName() {
    return name;
  }

  private DeterminantClass getResistanceEffect() {
    return resistanceEffect;
  }
}
