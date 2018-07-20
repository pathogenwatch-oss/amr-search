package net.cgps.wgsa.paarsnp.core.snpar.json;

import net.cgps.wgsa.paarsnp.core.lib.blast.BlastSearchStatistics;
import net.cgps.wgsa.paarsnp.core.lib.json.AbstractJsonnable;

import java.util.Collection;
import java.util.Collections;

/**
 * SNPAR result for a given SNPAR gene match.
 */
public class SnparMatchData extends AbstractJsonnable {

  private final BlastSearchStatistics searchStatistics;
  private final Collection<SnpResistanceElement> snpResistanceElements;

  @SuppressWarnings("unused")
  private SnparMatchData() {

    this(null, Collections.emptyList());
  }

  public SnparMatchData(final BlastSearchStatistics searchStatistics, final Collection<SnpResistanceElement> snpResistanceElements) {

    this.searchStatistics = searchStatistics;
    this.snpResistanceElements = snpResistanceElements;
  }

  public BlastSearchStatistics getSearchStatistics() {

    return this.searchStatistics;
  }

  public Collection<SnpResistanceElement> getSnpResistanceElements() {

    return this.snpResistanceElements;
  }
}
