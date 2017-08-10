package net.cgps.wgsa.paarsnp.core.lib.json;

import net.cgps.wgsa.paarsnp.core.lib.ResistanceState;

import java.util.Collection;
import java.util.Collections;

public class AntibioticProfile extends AbstractJsonnable {

  private final AntimicrobialAgent name;
  private final ResistanceState resistanceState;
  private final Collection<ResistanceSet> resistanceSets;

  @SuppressWarnings("unused")
  private AntibioticProfile() {

    this(null, ResistanceState.UNKNOWN, Collections.emptyList());
  }

  public AntibioticProfile(final AntimicrobialAgent name, final ResistanceState resistanceState, final Collection<ResistanceSet> resistanceSets) {

    this.name = name;
    this.resistanceState = resistanceState;
    this.resistanceSets = resistanceSets;
  }

  public static AntibioticProfile buildDefault(final AntimicrobialAgent name) {

    return new AntibioticProfile(name, ResistanceState.UNKNOWN, Collections.emptyList());
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

  public void addSet(final ResistanceSet resistanceSet) {

    this.resistanceSets.add(resistanceSet);
  }
}