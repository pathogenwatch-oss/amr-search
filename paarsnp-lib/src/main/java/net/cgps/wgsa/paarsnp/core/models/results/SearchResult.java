package net.cgps.wgsa.paarsnp.core.models.results;

import net.cgps.wgsa.paarsnp.core.lib.AbstractJsonnable;

import java.util.Collection;
import java.util.Collections;

public class SearchResult extends AbstractJsonnable {

  private final Collection<SetResult> setResults;
  private final Collection<MatchJson> blastMatches;

  @SuppressWarnings("unused")
  private SearchResult() {

    this(Collections.emptyList(), Collections.emptyList());
  }

  public SearchResult(final Collection<SetResult> setResults, final Collection<MatchJson> blastMatches) {
    this.setResults = setResults;
    this.blastMatches = blastMatches;
  }

  public static SearchResult buildEmpty() {
    return new SearchResult();
  }

  public Collection<MatchJson> getBlastMatches() {

    return this.blastMatches;
  }

  public Collection<SetResult> getSetResults() {
    return this.setResults;
  }
}
