package net.cgps.wgsa.paarsnp.core.models.results;

import net.cgps.wgsa.paarsnp.core.lib.AbstractJsonnable;
import net.cgps.wgsa.paarsnp.core.lib.blast.BlastSearchStatistics;
import net.cgps.wgsa.paarsnp.core.models.ResistanceMutationMatch;

import java.util.Collection;

public class MatchJson extends AbstractJsonnable {

  private final BlastSearchStatistics statistics;
  private final Collection<ResistanceMutationMatch> resistanceVariants;

  @SuppressWarnings("unused")
  private MatchJson() {
    this(null, null);
  }
  public MatchJson(final BlastSearchStatistics searchStatistics, final Collection<ResistanceMutationMatch> resistanceVariants) {
    this.statistics = searchStatistics;
    this.resistanceVariants = resistanceVariants;
  }

  // Would be better unwrapped, but maintaining compatiblity with current API.
//  @JsonUnwrapped
  @SuppressWarnings("unused")
  public BlastSearchStatistics getStatistics() {
    return this.statistics;
  }

  @SuppressWarnings("unused")
  public Collection<ResistanceMutationMatch> getResistanceVariants() {
    return this.resistanceVariants;
  }
}
