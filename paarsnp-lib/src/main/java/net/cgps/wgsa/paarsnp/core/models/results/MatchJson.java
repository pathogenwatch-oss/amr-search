package net.cgps.wgsa.paarsnp.core.models.results;

import net.cgps.wgsa.paarsnp.core.lib.AbstractJsonnable;
import net.cgps.wgsa.paarsnp.core.lib.blast.BlastSearchStatistics;
import net.cgps.wgsa.paarsnp.core.models.ResistanceMutationMatch;

import java.util.Collection;

public class MatchJson extends AbstractJsonnable {

  private final BlastSearchStatistics searchStatistics;
  private final Collection<ResistanceMutationMatch> snpResistanceElements;

  @SuppressWarnings("unused")
  private MatchJson() {
    this(null, null);
  }
  public MatchJson(final BlastSearchStatistics searchStatistics, final Collection<ResistanceMutationMatch> snpResistanceElements) {
    this.searchStatistics = searchStatistics;
    this.snpResistanceElements = snpResistanceElements;
  }

  // Would be better unwrapped, but maintaining compatiblity with current API.
//  @JsonUnwrapped
  @SuppressWarnings("unused")
  public BlastSearchStatistics getSearchStatistics() {
    return this.searchStatistics;
  }

  @SuppressWarnings("unused")
  public Collection<ResistanceMutationMatch> getSnpResistanceElements() {
    return this.snpResistanceElements;
  }
}
