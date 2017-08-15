package net.cgps.wgsa.paarsnp.core.lib.json;

import com.fasterxml.jackson.annotation.JsonIgnore;
import net.cgps.wgsa.paarsnp.core.lib.ElementEffect;
import net.cgps.wgsa.paarsnp.core.lib.SetResistanceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ResistanceSet extends AbstractJsonnable {

  @JsonIgnore
  private final Logger logger = LoggerFactory.getLogger(ResistanceSet.class);

  private final SetResistanceType effect;
  private final String resistanceSetName;
  private final Set<String> agents;
  private final Collection<String> elementIds;
  private final Map<String, ElementEffect> modifiers;

  @SuppressWarnings("unused")
  private ResistanceSet() {

    this("", SetResistanceType.RESISTANT, Collections.emptySet());
  }

  public ResistanceSet(final String resistanceSetName, final SetResistanceType effect, final Set<String> agents) {

    this.resistanceSetName = resistanceSetName;
    this.modifiers = new HashMap<>(5);
    this.elementIds = new HashSet<>(10);
    this.effect = effect;
    this.agents = agents;
  }

  public static ResistanceSet buildSnpResistanceSet(final String setName, final String geneId, final String snpCode, final Set<String> agents, final SetResistanceType setResistanceType, final ElementEffect effect) {

    return new ResistanceSet(setName, setResistanceType, agents).addElementId(geneId + "_" + snpCode, effect);
  }

  public ResistanceSet addElementId(final String geneName, final ElementEffect elementEffect) {

    // Assuming that all set elements have confers/induces consistently set, so not checking, just logging the inconsistency.

    switch (elementEffect) {
      case RESISTANCE:
        this.logger.trace("Adding elementId={}", geneName);
        this.elementIds.add(geneName);
        break;
      case MODIFIES_SUPPRESSES:
      case MODIFIES_INDUCED:
        this.logger.trace("Adding modifier={} elementId={}", this.effect.name(), geneName);
        this.modifiers.put(geneName, elementEffect);
        break;
    }
    return this;
  }

  public String getResistanceSetName() {

    return this.resistanceSetName;
  }

  public Set<String> getAgents() {

    return this.agents;
  }

  public Collection<String> getElementIds() {

    return this.elementIds;
  }

  public Map<String, ElementEffect> getModifiers() {

    return this.modifiers;
  }

  public SetResistanceType getEffect() {

    return this.effect;
  }

  @Override
  public int hashCode() {

    int result = this.effect.hashCode();
    result = 31 * result + this.resistanceSetName.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object o) {

    if (this == o) {
      return true;
    }
    if (o == null || this.getClass() != o.getClass()) {
      return false;
    }

    final ResistanceSet that = (ResistanceSet) o;

    if (this.effect != that.effect) {
      return false;
    }
    return this.resistanceSetName.equals(that.resistanceSetName);

  }
}
