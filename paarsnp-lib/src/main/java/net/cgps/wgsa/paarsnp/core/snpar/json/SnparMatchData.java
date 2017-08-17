package net.cgps.wgsa.paarsnp.core.snpar.json;

import net.cgps.wgsa.paarsnp.core.lib.blast.BlastSearchStatistics;
import net.cgps.wgsa.paarsnp.core.lib.json.AbstractJsonnable;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;

/**
 * SNPAR result for a given SNPAR gene match.
 */
public class SnparMatchData extends AbstractJsonnable {

  private final BlastSearchStatistics searchStatistics;
  private final Collection<SnpResistanceElement> snpResistanceElements;
  //List of all the mutations in the sequence
  private final Collection<Mutation> mutations;

  @SuppressWarnings("unused")
  private SnparMatchData() {

    this(null, Collections.emptyList(), Collections.emptyList());
  }

  public SnparMatchData(final BlastSearchStatistics searchStatistics, final Collection<SnpResistanceElement> snpResistanceElements, final Collection<Mutation> mutations) {

    this.searchStatistics = searchStatistics;
    this.snpResistanceElements = snpResistanceElements;
    this.mutations = mutations;
  }

  public BlastSearchStatistics getSearchStatistics() {

    return this.searchStatistics;
  }

  public Collection<SnpResistanceElement> getSnpResistanceElements() {

    return this.snpResistanceElements;
  }

  public Collection<Mutation> getMutations() {

    return this.mutations;
  }

  public String format(final Function<SnparMatchData, char[]> blastSnpMatchFormatter) {

    return new String(blastSnpMatchFormatter.apply(this));
  }
}
