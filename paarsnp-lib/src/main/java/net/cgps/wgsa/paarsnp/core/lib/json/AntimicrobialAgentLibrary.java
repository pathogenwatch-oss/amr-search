package net.cgps.wgsa.paarsnp.core.lib.json;

import java.util.Collection;
import java.util.Collections;

public class AntimicrobialAgentLibrary extends AbstractJsonnable {

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
