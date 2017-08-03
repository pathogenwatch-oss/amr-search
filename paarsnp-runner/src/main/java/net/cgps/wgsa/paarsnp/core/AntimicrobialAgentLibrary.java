package net.cgps.wgsa.paarsnp.core;

import net.cgps.wgsa.paarsnp.core.lib.AntimicrobialAgent;

import java.util.Collection;
import java.util.Collections;

public class AntimicrobialAgentLibrary {

  private final Collection<AntimicrobialAgent> agents;
  private final String speciesId;

  @SuppressWarnings("unused")
  private AntimicrobialAgentLibrary() {

    this(Collections.emptyList(), "");
  }

  public AntimicrobialAgentLibrary(final Collection<AntimicrobialAgent> agents, final String speciesId) {

    this.agents = agents;
    this.speciesId = speciesId;
  }

  public Collection<AntimicrobialAgent> getAgents() {

    return this.agents;
  }

  public String getSpeciesId() {

    return this.speciesId;
  }
}
