package net.cgps.wgsa.paarsnp.core.lib.json;

import net.cgps.wgsa.paarsnp.core.lib.PhenotypeEffect;

import java.util.List;

public class Phenotype {

  private final PhenotypeEffect effect;
  private final List<String> profile;
  private final List<Modifier> modifiers;

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
}
