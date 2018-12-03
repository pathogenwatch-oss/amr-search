package net.cgps.wgsa.paarsnp.core.models.results;

import net.cgps.wgsa.paarsnp.core.lib.AbstractJsonnable;
import net.cgps.wgsa.paarsnp.core.models.ResistanceSet;

import java.util.Collection;
import java.util.Collections;

public class AntibioticProfile extends AbstractJsonnable {

  private final AntimicrobialAgent name;
  private final ResistanceState resistanceState;
  private final Collection<ResistanceSet> resistanceSets;

  @SuppressWarnings("unused")
  private AntibioticProfile() {

    this(null, ResistanceState.NOT_FOUND, Collections.emptyList());
  }

  public AntibioticProfile(final AntimicrobialAgent name, final ResistanceState resistanceState, final Collection<ResistanceSet> resistanceSets) {

    this.name = name;
    this.resistanceState = resistanceState;
    this.resistanceSets = resistanceSets;
  }

  public ResistanceState getResistanceState() {

    return this.resistanceState;
  }

  public Collection<ResistanceSet> getResistanceSets() {

    return this.resistanceSets;
  }

  public AntimicrobialAgent getAgent() {

    return this.name;
  }

}
