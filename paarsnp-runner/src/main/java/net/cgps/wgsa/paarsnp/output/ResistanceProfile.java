package net.cgps.wgsa.paarsnp.output;

import net.cgps.wgsa.paarsnp.core.lib.AbstractJsonnable;
import net.cgps.wgsa.paarsnp.core.models.results.AntimicrobialAgent;
import net.cgps.wgsa.paarsnp.core.models.results.ResistanceState;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class ResistanceProfile extends AbstractJsonnable {

  private final AntimicrobialAgent agent;
  private final ResistanceState state;
  private final DeterminantsProfile determinants; // List of sets
  private final Map<String, ResistanceState> determinantRules;

  private ResistanceProfile() {
    this(null, null, null, Collections.emptyMap());
  }

  public ResistanceProfile(final AntimicrobialAgent agent, final ResistanceState state, final DeterminantsProfile determinants, final Map<String, ResistanceState> determinantRules) {
    this.agent = agent;
    this.state = state;
    this.determinants = determinants;
    this.determinantRules = determinantRules;
  }

  private AntimicrobialAgent getAgent() {
    return agent;
  }

  private ResistanceState getState() {
    return state;
  }

  private DeterminantsProfile getDeterminants() {
    return determinants;
  }

  private Map<String, ResistanceState> getDeterminantRules() {
    return determinantRules;
  }
}
