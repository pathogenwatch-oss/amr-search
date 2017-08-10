package net.cgps.wgsa.paarsnp.core.snpar.json;

import net.cgps.wgsa.paarsnp.core.lib.json.AbstractJsonnable;
import net.cgps.wgsa.paarsnp.core.lib.json.ResistanceSet;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.function.Function;

public class SnparResult extends AbstractJsonnable {

  private final Collection<String> resistanceMutationIds;
  private final Collection<ResistanceSet> completeSets;
  private final Collection<ResistanceSet> partialSets;
  private final Collection<SnparMatchData> blastMatches;


  @SuppressWarnings("unused")
  private SnparResult() {

    this(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
  }

  public SnparResult(final Collection<String> mutationIds, final Collection<ResistanceSet> completeSets, final Collection<ResistanceSet> partialSets, final Collection<SnparMatchData> blastMatches) {

    this.partialSets = partialSets;
    this.blastMatches = blastMatches;
    this.completeSets = new HashSet<>(completeSets);
    this.resistanceMutationIds = new HashSet<>(mutationIds);
  }

  public Collection<String> getResistanceMutationIds() {

    return this.resistanceMutationIds;
  }

  public Collection<ResistanceSet> getCompleteSets() {

    return this.completeSets;
  }

  public Collection<ResistanceSet> getPartialSets() {

    return this.partialSets;
  }

  public String format(final Function<SnparResult, char[]> snparResultFormatter) {

    return new String(snparResultFormatter.apply(this));
  }

  public Collection<SnparMatchData> getBlastMatches() {

    return this.blastMatches;
  }
}
