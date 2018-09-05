package net.cgps.wgsa.paarsnp.core.lib.json;

import net.cgps.wgsa.paarsnp.core.lib.ElementEffect;

public class Modifier {

  private final String name;
  private final ElementEffect effect;

  public Modifier(final String name, final ElementEffect effect) {
    this.name = name;
    this.effect = effect;
  }

  private String getName() {
    return this.name;
  }

  private ElementEffect getEffect() {
    return this.effect;
  }
}
