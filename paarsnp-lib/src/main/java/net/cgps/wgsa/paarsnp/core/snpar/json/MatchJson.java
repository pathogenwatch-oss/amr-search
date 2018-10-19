package net.cgps.wgsa.paarsnp.core.snpar.json;

import net.cgps.wgsa.paarsnp.core.lib.blast.BlastSearchStatistics;

import java.util.Collection;

public class MatchJson {

  private final BlastSearchStatistics searchStatistics;
  private final Collection<SnpResistanceElement> snpResistanceElements;

  public MatchJson(final BlastSearchStatistics searchStatistics, final Collection<String> snpResistanceElements) {
    this.searchStatistics = searchStatistics;
    this.snpResistanceElements = snpResistanceElements;
  }

  // Would be better unwrapped, but maintaining compatiblity with current API.
//  @JsonUnwrapped
  public BlastSearchStatistics getSearchStatistics() {
    return this.searchStatistics;
  }

  public Collection<String> getSnpResistanceElements() {
    return this.snpResistanceElements;
  }
}
