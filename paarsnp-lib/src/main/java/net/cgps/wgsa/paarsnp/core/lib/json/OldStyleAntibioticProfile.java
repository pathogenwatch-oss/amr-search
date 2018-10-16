package net.cgps.wgsa.paarsnp.core.lib.json;

import net.cgps.wgsa.paarsnp.core.lib.ResistanceState;

import java.util.Collection;
import java.util.Collections;

public class OldStyleAntibioticProfile extends AbstractJsonnable{

  private final AntimicrobialAgent name;
  private final ResistanceState resistanceState;
  private final Collection<OldStyleSetDescription> resistanceSets;

  @SuppressWarnings("unused")
  private OldStyleAntibioticProfile() {

    this(null, ResistanceState.NOT_FOUND, Collections.emptyList());
  }

  public OldStyleAntibioticProfile(final AntimicrobialAgent name, final ResistanceState resistanceState, final Collection<OldStyleSetDescription> resistanceSets) {

    this.name = name;
    this.resistanceState = resistanceState;
    this.resistanceSets = resistanceSets;
  }

  public ResistanceState getResistanceState() {

    return this.resistanceState;
  }

  public Collection<OldStyleSetDescription> getResistanceSets() {

    return this.resistanceSets;
  }

  public AntimicrobialAgent getAgent() {

    return this.name;
  }

}
