package net.cgps.wgsa.paarsnp;

import net.cgps.wgsa.paarsnp.core.lib.ResistanceState;
import net.cgps.wgsa.paarsnp.core.lib.json.AbstractJsonnable;
import net.cgps.wgsa.paarsnp.core.lib.json.AntibioticProfile;
import net.cgps.wgsa.paarsnp.core.paar.PaarResult;
import net.cgps.wgsa.paarsnp.core.snpar.json.SnparResult;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class PaarsnpResult extends AbstractJsonnable {

  private final SnparResult snparResult;
  private final PaarResult paarResult;
  private final Map<String, Map<String, ResistanceState>> resistanceProfile; // Set of agents resisted grouped by class
  private final Collection<AntibioticProfile> antibioticProfiles;
  private final String assemblyId;

  @SuppressWarnings("unused")
  private PaarsnpResult() {

    this("", null, null, Collections.emptyMap(), Collections.emptyList());
  }

  public PaarsnpResult(final String assemblyId, final SnparResult snparResult, final PaarResult paarResult, final Map<String, Map<String, ResistanceState>> resistanceProfile, final Collection<AntibioticProfile> antibioticProfiles) {

    this.assemblyId = assemblyId;
    this.snparResult = snparResult;
    this.paarResult = paarResult;
    this.resistanceProfile = resistanceProfile;
    this.antibioticProfiles = antibioticProfiles;
  }

  public SnparResult getSnparResult() {

    return this.snparResult;
  }

  public PaarResult getPaarResult() {

    return this.paarResult;
  }

  public Map<String, Map<String, ResistanceState>> getResistanceProfile() {

    return this.resistanceProfile;
  }

  public Collection<AntibioticProfile> getAntibioticProfiles() {

    return this.antibioticProfiles;
  }

  public String getAssemblyId() {

    return this.assemblyId;
  }
}
