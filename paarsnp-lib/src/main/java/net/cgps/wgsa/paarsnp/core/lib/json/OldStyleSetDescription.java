package net.cgps.wgsa.paarsnp.core.lib.json;

import net.cgps.wgsa.paarsnp.core.lib.ElementEffect;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class OldStyleSetDescription extends AbstractJsonnable {

  private final String resistanceSetName;
  private final Collection<String> agents;
  private final Collection<String> elementIds;
  private final Map<String, ElementEffect> modifiers;

  @SuppressWarnings("unused")
  private OldStyleSetDescription() {
    this("", Collections.emptyList(), Collections.emptyList(), Collections.emptyMap());
  }

  public OldStyleSetDescription(final String resistanceSetName, final Collection<String> agents, final Collection<String> elementIds, final Map<String, ElementEffect> modifiers) {
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
}
