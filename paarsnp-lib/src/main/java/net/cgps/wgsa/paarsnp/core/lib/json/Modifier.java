package net.cgps.wgsa.paarsnp.core.lib.json;

import net.cgps.wgsa.paarsnp.core.lib.ElementEffect;

public class Modifier {

  private final String name;
  private final ElementEffect effect;

  public Modifier(final String name, final ElementEffect effect) {
    this.name = name;
    this.effect = effect;
  }

  public String getName() {
    return this.name;
  }

  public ElementEffect getEffect() {
    return this.effect;
  }
}
