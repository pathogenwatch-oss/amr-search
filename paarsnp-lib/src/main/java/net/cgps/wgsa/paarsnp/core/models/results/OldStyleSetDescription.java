package net.cgps.wgsa.paarsnp.core.models.results;

import net.cgps.wgsa.paarsnp.core.lib.AbstractJsonnable;
import net.cgps.wgsa.paarsnp.core.models.ElementEffect;
import net.cgps.wgsa.paarsnp.core.models.PhenotypeEffect;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class OldStyleSetDescription extends AbstractJsonnable {

  private final PhenotypeEffect effect;
  private final String resistanceSetName;
  private final Collection<String> agents;
  private final Collection<String> elementIds;
  private final Map<String, ElementEffect> modifiers;

  @SuppressWarnings("unused")
  private OldStyleSetDescription() {
    this(PhenotypeEffect.RESISTANT, "", Collections.emptyList(), Collections.emptyList(), Collections.emptyMap());
  }

  public OldStyleSetDescription(final PhenotypeEffect effect, final String resistanceSetName, final Collection<String> agents, final Collection<String> elementIds, final Map<String, ElementEffect> modifiers) {
    this.effect = effect;
    this.resistanceSetName = resistanceSetName;
    this.agents = agents;
    this.elementIds = elementIds;
    this.modifiers = modifiers;
  }

  public String getResistanceSetName() {
    return this.resistanceSetName;
  }

  public Collection<String> getAgents() {
    return this.agents;
  }

  public Collection<String> getElementIds() {
    return this.elementIds;
  }

  public Map<String, ElementEffect> getModifiers() {
    return this.modifiers;
  }

  public PhenotypeEffect getEffect() {
    return this.effect;
  }
}
