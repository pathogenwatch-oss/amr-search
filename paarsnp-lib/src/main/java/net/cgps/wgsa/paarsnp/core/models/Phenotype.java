package net.cgps.wgsa.paarsnp.core.models;

import net.cgps.wgsa.paarsnp.core.lib.AbstractJsonnable;
import net.cgps.wgsa.paarsnp.core.models.results.Modifier;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Phenotype extends AbstractJsonnable {

  private final PhenotypeEffect effect;
  private final List<String> profile; // agent keys
  private final List<Modifier> modifiers;

  private Phenotype() {
    this(PhenotypeEffect.RESISTANT, Collections.emptyList(), Collections.emptyList());
  }

  public Phenotype(final PhenotypeEffect effect, final List<String> profile, final List<Modifier> modifiers) {
    this.effect = effect;
    this.profile = profile;
    this.modifiers = modifiers;
  }

  public PhenotypeEffect getEffect() {
    return this.effect;
  }

  public List<String> getProfile() {
    return this.profile;
  }

  public List<Modifier> getModifiers() {
    return this.modifiers;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || this.getClass() != o.getClass()) return false;
    final Phenotype phenotype = (Phenotype) o;
    return this.effect == phenotype.effect &&
        Objects.equals(this.profile, phenotype.profile) &&
        Objects.equals(this.modifiers, phenotype.modifiers);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.effect, this.profile, this.modifiers);
  }

  @Override
  public String toString() {
    return this.toJson();
  }
}
