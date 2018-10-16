package net.cgps.wgsa.paarsnp.core.lib.json;

import net.cgps.wgsa.paarsnp.core.lib.ElementEffect;

import java.util.Objects;

public class Modifier extends AbstractJsonnable {

  private final String name;
  private final ElementEffect effect;

  @SuppressWarnings("unused")
  private Modifier() {
    this("", ElementEffect.MODIFIES_INDUCED);
  }

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

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || this.getClass() != o.getClass()) return false;
    final Modifier modifier = (Modifier) o;
    return Objects.equals(this.name, modifier.name) &&
        this.effect == modifier.effect;
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.name, this.effect);
  }
}
