package net.cgps.wgsa.paarsnp.core.snpar.json;

import net.cgps.wgsa.paarsnp.core.lib.SetResult;
import net.cgps.wgsa.paarsnp.core.lib.json.AbstractJsonnable;

import java.util.Collection;
import java.util.Collections;

public class SnparResult extends AbstractJsonnable {

  private final Collection<SetResult> setResults;
  private final Collection<MatchJson> blastMatches;

  @SuppressWarnings("unused")
  private SnparResult() {

    this(Collections.emptyList(), Collections.emptyList());
  }

  public SnparResult(final Collection<SetResult> setResults, final Collection<MatchJson> blastMatches) {
    this.setResults = setResults;
    this.blastMatches = blastMatches;
  }

  public static SnparResult buildEmpty() {
    return new SnparResult();
  }

  public Collection<MatchJson> getBlastMatches() {

    return this.blastMatches;
  }

  public Collection<SetResult> getSetResults() {
    return this.setResults;
  }
}
