package net.cgps.wgsa.paarsnp.core.lib;

import java.util.Collection;
import java.util.Collections;

public class AntibioticProfile {

  private final String name;
  private final ResistanceState resistanceState;
  private final Collection<ResistanceSet> resistanceSets;

  @SuppressWarnings("unused")
  private AntibioticProfile() {

    this("", ResistanceState.UNKNOWN, Collections.emptyList());
  }

  public AntibioticProfile(final String name, final ResistanceState resistanceState, final Collection<ResistanceSet> resistanceSets) {

    this.name = name;
    this.resistanceState = resistanceState;
    this.resistanceSets = resistanceSets;
  }

  public static AntibioticProfile buildDefault(final String name) {

    return new AntibioticProfile(name, ResistanceState.UNKNOWN, Collections.emptyList());
  }

  public ResistanceState getResistanceState() {

    return this.resistanceState;
  }

  public Collection<ResistanceSet> getResistanceSets() {

    return this.resistanceSets;
  }

  public String getName() {

    return this.name;
  }

  public void addSet(final ResistanceSet resistanceSet) {

    this.resistanceSets.add(resistanceSet);
  }
}
