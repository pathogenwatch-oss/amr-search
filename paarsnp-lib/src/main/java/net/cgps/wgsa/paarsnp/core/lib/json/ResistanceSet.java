package net.cgps.wgsa.paarsnp.core.lib.json;

import com.fasterxml.jackson.annotation.JsonIgnore;
import net.cgps.wgsa.paarsnp.core.lib.ResistanceType;
import net.cgps.wgsa.paarsnp.core.paar.ResistanceGene;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;

import static net.cgps.wgsa.paarsnp.core.paar.ResistanceGene.EFFECT.RESISTANT;

public class ResistanceSet extends AbstractJsonnable {

  @JsonIgnore
  private final Logger logger = LoggerFactory.getLogger(ResistanceSet.class);

  private final ResistanceType effect;
  private final String resistanceSetName;
  private final Set<String> agents;
  private final Collection<String> elementIds;
  private final String phenotype;
  private final Map<String, ResistanceGene.EFFECT> modifiers;

  @SuppressWarnings("unused")
  private ResistanceSet() {

    this("", "", ResistanceType.RESISTANT, RESISTANT, Collections.emptySet(), "");
  }

  public ResistanceSet(final String resistanceSetName, final String elementId, final ResistanceType effect, final ResistanceGene.EFFECT elementEffect, final Set<String> agents, final String phenotype) {

    this.resistanceSetName = resistanceSetName;
    this.modifiers = new HashMap<>(5);
    this.elementIds = new HashSet<>(10);
    this.effect = effect;
    this.addElementId(elementId, elementEffect);
    this.agents = agents;
    this.phenotype = phenotype;
  }

  public static ResistanceSet buildSnpResistanceSet(final String setName, final String geneId, final String snpCode, final Set<String> agents, final String phenotype, final ResistanceType resistanceType) {

    return new ResistanceSet(setName, geneId + "_" + snpCode, resistanceType, RESISTANT, agents, phenotype);
  }

  public void addElementId(final String geneName, final ResistanceGene.EFFECT elementEffect) {

    // Assuming that all set elements have confers/induces consistently set, so not checking, just logging the inconsistency.

    switch (elementEffect) {
      case RESISTANT:
      case INDUCED:
        this.logger.debug("Adding elementId={}", geneName);
        this.elementIds.add(geneName);
        break;
      case MODIFIES_SUPPRESSES:
      case MODIFIES_INDUCED:
      case MODIFIES_RESISTANT:
        this.logger.debug("Adding modifier={} elementId={}", this.effect.name(), geneName);
        this.modifiers.put(geneName, elementEffect);
        break;
    }
  }

  public String getPhenotype() {

    return this.phenotype;
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

  public String format(final Function<ResistanceSet, char[]> resistanceSetFormatter) {

    return new String(resistanceSetFormatter.apply(this));
  }

  public Map<String, ResistanceGene.EFFECT> getModifiers() {

    return this.modifiers;
  }

  public ResistanceType getEffect() {

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
