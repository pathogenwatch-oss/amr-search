package net.cgps.wgsa.paarsnp;

import net.cgps.wgsa.paarsnp.core.lib.json.AbstractJsonnable;
import net.cgps.wgsa.paarsnp.core.lib.json.AntibioticProfile;
import net.cgps.wgsa.paarsnp.core.paar.PaarResult;
import net.cgps.wgsa.paarsnp.core.snpar.json.SnparResult;

import java.util.Collection;
import java.util.Collections;

public class PaarsnpResult extends AbstractJsonnable {

  private final SnparResult snparResult;
  private final PaarResult paarResult;
  private final Collection<AntibioticProfile> resistanceProfile;
  private final String assemblyId;

  @SuppressWarnings("unused")
  private PaarsnpResult() {

    this("", null, null, Collections.emptyList());
  }

  public PaarsnpResult(final String assemblyId, final SnparResult snparResult, final PaarResult paarResult, final Collection<AntibioticProfile> resistanceProfile) {

    this.assemblyId = assemblyId;
    this.snparResult = snparResult;
    this.paarResult = paarResult;
    this.resistanceProfile = resistanceProfile;
  }

  public SnparResult getSnparResult() {

    return this.snparResult;
  }

  public PaarResult getPaarResult() {

    return this.paarResult;
  }

  public Collection<AntibioticProfile> getResistanceProfile() {

    return this.resistanceProfile;
  }

  public String getAssemblyId() {

    return this.assemblyId;
  }
}
