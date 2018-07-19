package net.cgps.wgsa.paarsnp.core.lib;

import java.util.Collection;

public class InputOptions {

  private final String assemblyId;
  private final Collection<String> blastOptions;
  private final float coverageThreshold;

  public InputOptions(String assemblyId, final Collection<String> blastOptions, float coverageThreshold) {
    this.assemblyId = assemblyId;
    this.blastOptions = blastOptions;
    this.coverageThreshold = coverageThreshold;
  }

  public Collection<String> getBlastOptions() {
    return this.blastOptions;
  }

  public String getAssemblyId() {
    return this.assemblyId;
  }

  public float getCoverageThreshold() {
    return this.coverageThreshold;
  }
}
