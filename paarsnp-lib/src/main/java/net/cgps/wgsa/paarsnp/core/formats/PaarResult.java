package net.cgps.wgsa.paarsnp.core.formats;

import net.cgps.wgsa.paarsnp.core.lib.SetResult;
import net.cgps.wgsa.paarsnp.core.lib.blast.BlastSearchStatistics;
import net.cgps.wgsa.paarsnp.core.lib.json.AbstractJsonnable;

import java.util.*;

/**
 * Document class for storing the P/A AR result for an assembly. AR genes come in sets, which are required to be complete (all found in the query assembly) in order to confer resistance.
 */
public class PaarResult extends AbstractJsonnable {

  private final Collection<String> paarElementIds;
  private final Collection<SetResult> setResults;
  private final Map<String, List<BlastSearchStatistics>> blastMatches;

  @SuppressWarnings("unused")
  private PaarResult() {

    this(Collections.emptyList(), Collections.emptyMap(), Collections.emptyList());
  }

  public PaarResult(final Collection<SetResult> setResults, final Map<String, List<BlastSearchStatistics>> blastMatches, final Collection<String> paarElementIds) {
    this.setResults = setResults;
    this.blastMatches = new HashMap<>(blastMatches);
    this.paarElementIds = paarElementIds;
  }

  public static PaarResult buildEmpty() {
    return new PaarResult();
  }

  @SuppressWarnings("unused") // for json serialisation.
  public Map<String, List<BlastSearchStatistics>> getBlastMatches() {

    return this.blastMatches;
  }

  @SuppressWarnings("unused")
  public Collection<String> getPaarElementIds() {

    return this.paarElementIds;
  }

  public Collection<SetResult> getSetResults() {
    return this.setResults;
  }
}
