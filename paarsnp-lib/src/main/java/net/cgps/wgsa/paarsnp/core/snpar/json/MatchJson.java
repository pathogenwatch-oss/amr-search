package net.cgps.wgsa.paarsnp.core.snpar.json;

import net.cgps.wgsa.paarsnp.core.lib.blast.BlastSearchStatistics;

import java.util.Collection;

public class MatchJson {

  private final BlastSearchStatistics blastSearchStatistics;
  private final Collection<String> resistanceVariants;

  public MatchJson(final BlastSearchStatistics blastSearchStatistics, final Collection<String> resistanceVariants) {
    this.blastSearchStatistics = blastSearchStatistics;
    this.resistanceVariants = resistanceVariants;
  }

  // Would be better unwrapped, but maintaining compatiblity with current API.
//  @JsonUnwrapped
  public BlastSearchStatistics getBlastSearchStatistics() {
    return this.blastSearchStatistics;
  }

  public Collection<String> getResistanceVariants() {
    return this.resistanceVariants;
  }
}
