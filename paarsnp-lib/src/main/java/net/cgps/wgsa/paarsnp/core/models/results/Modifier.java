package net.cgps.wgsa.paarsnp.core.models.results;

import net.cgps.wgsa.paarsnp.core.lib.AbstractJsonnable;
import net.cgps.wgsa.paarsnp.core.models.ElementEffect;

import java.util.*;
import java.util.stream.Collectors;

public class Modifier extends AbstractJsonnable implements HasVariants {

  private final String gene;
  private final Set<String> variants;
  private final ElementEffect effect;

  @SuppressWarnings("unused")
  private Modifier() {
    this("", Collections.emptyList(), ElementEffect.INDUCED);
  }

  public Modifier(final String gene, final Collection<String> variants, final ElementEffect effect) {
    this.gene = gene;
    this.variants = new HashSet<>(variants);
    this.effect = effect;
  }

  public String getGene() {
    return this.gene;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    final Modifier modifier = (Modifier) o;
    return Objects.equals(this.gene, modifier.gene) &&
        Objects.equals(this.variants, modifier.variants) &&
        this.effect == modifier.effect;
  }

  public ElementEffect getEffect() {
    return this.effect;
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.gene, this.variants, this.effect);
  }

  public String toName() {
    return this.gene + (this.getVariants().isEmpty() ? "" : "_" + this.getVariants().stream().sorted().collect(Collectors.joining("_")));
  }

  public Set<String> getVariants() {
    return this.variants;
  }
}
