package net.cgps.wgsa.paarsnp;

import net.cgps.wgsa.paarsnp.core.lib.json.AbstractJsonnable;
import net.cgps.wgsa.paarsnp.core.lib.json.OldStyleAntibioticProfile;
import net.cgps.wgsa.paarsnp.core.formats.OldStylePaarResult;
import net.cgps.wgsa.paarsnp.core.formats.OldStyleSnparResult;

import java.util.Collection;
import java.util.Collections;

public class OldStylePaarsnpResult extends AbstractJsonnable implements Result {

  private final OldStyleSnparResult snparResult;
  private final OldStylePaarResult paarResult;
  private final Collection<OldStyleAntibioticProfile> resistanceProfile;
  private final String assemblyId;

  @SuppressWarnings("unused")
  private OldStylePaarsnpResult() {

    this("", null, null, Collections.emptyList());
  }

  public OldStylePaarsnpResult(final String assemblyId, final OldStyleSnparResult snparResult, final OldStylePaarResult paarResult, final Collection<OldStyleAntibioticProfile> resistanceProfile) {

    this.assemblyId = assemblyId;
    this.snparResult = snparResult;
    this.paarResult = paarResult;
    this.resistanceProfile = resistanceProfile;
  }

  public OldStyleSnparResult getSnparResult() {

    return this.snparResult;
  }

  public OldStylePaarResult getPaarResult() {

    return this.paarResult;
  }

  public Collection<OldStyleAntibioticProfile> getResistanceProfile() {

    return this.resistanceProfile;
  }

  public String getAssemblyId() {

    return this.assemblyId;
  }
}
