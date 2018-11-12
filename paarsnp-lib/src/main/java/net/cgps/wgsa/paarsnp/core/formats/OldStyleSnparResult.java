package net.cgps.wgsa.paarsnp.core.formats;

import net.cgps.wgsa.paarsnp.core.lib.json.AbstractJsonnable;
import net.cgps.wgsa.paarsnp.core.lib.json.OldStyleSetDescription;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

public class OldStyleSnparResult extends AbstractJsonnable {

  private final Collection<String> resistanceMutationIds;
  private final Collection<OldStyleSetDescription> completeSets;
  private final Collection<OldStyleSetDescription> partialSets;
  private final Collection<OldStyleSetDescription> modifiedSets;
  private final Collection<MatchJson> blastMatches;

  @SuppressWarnings("unused")
  private OldStyleSnparResult() {

    this(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
  }

  public OldStyleSnparResult(final Collection<String> mutationIds, final Collection<OldStyleSetDescription> completeSets, final Collection<OldStyleSetDescription> partialSets, final Collection<OldStyleSetDescription> modifiedSets, final Collection<MatchJson> blastMatches) {

    this.partialSets = partialSets;
    this.modifiedSets = modifiedSets;
    this.blastMatches = blastMatches;
    this.completeSets = completeSets;
    this.resistanceMutationIds = new HashSet<>(mutationIds);
  }

  public static OldStyleSnparResult buildEmpty() {
    return new OldStyleSnparResult();
  }

  public Collection<String> getResistanceMutationIds() {

    return this.resistanceMutationIds;
  }

  public Collection<OldStyleSetDescription> getCompleteSets() {

    return this.completeSets;
  }

  public Collection<OldStyleSetDescription> getPartialSets() {

    return this.partialSets;
  }

  public Collection<MatchJson> getBlastMatches() {

    return this.blastMatches;
  }

  public Collection<OldStyleSetDescription> getModifiedSets() {
    return this.modifiedSets;
  }
}
