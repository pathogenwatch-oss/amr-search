package net.cgps.wgsa.paarsnp.core.paar.json;

import net.cgps.wgsa.paarsnp.core.lib.blast.BlastSearchStatistics;
import net.cgps.wgsa.paarsnp.core.lib.json.AbstractJsonnable;
import net.cgps.wgsa.paarsnp.core.lib.json.OldStyleSetDescription;

import java.util.*;

public class OldStylePaarResult extends AbstractJsonnable {

  private final Collection<String> paarElementIds;
  private final Collection<OldStyleSetDescription> completeResistanceSets; // set ID -> resistance Gene
  private final Collection<OldStyleSetDescription> partialResistanceSets; // set ID -> resistance Gene
  private final Collection<OldStyleSetDescription> modifiedResistanceSets;
  private final Map<String, List<BlastSearchStatistics>> blastMatches;

  @SuppressWarnings("unused")
  private OldStylePaarResult() {

    this(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyMap(), Collections.emptyList());
  }

  public OldStylePaarResult(final Collection<OldStyleSetDescription> completeResistanceSets, final Collection<OldStyleSetDescription> partialResistanceSets, final Collection<OldStyleSetDescription> modifiedResistanceSets, final Map<String, List<BlastSearchStatistics>> blastMatches, final Collection<String> paarElementIds) {

    this.completeResistanceSets = new ArrayList<>(completeResistanceSets);
    this.partialResistanceSets = new ArrayList<>(partialResistanceSets);
    this.modifiedResistanceSets = modifiedResistanceSets;
    this.blastMatches = new HashMap<>(blastMatches);
    this.paarElementIds = paarElementIds;
  }

  public static OldStylePaarResult buildEmpty() {
    return new OldStylePaarResult();
  }

  @SuppressWarnings("unused") // for json serialisation.
  public Map<String, List<BlastSearchStatistics>> getBlastMatches() {

    return this.blastMatches;
  }

  public Collection<OldStyleSetDescription> getCompleteResistanceSets() {

    return this.completeResistanceSets;
  }

  @SuppressWarnings("unused") // for json serialisation.
  public Collection<OldStyleSetDescription> getPartialResistanceSets() {

    return this.partialResistanceSets;
  }

  @SuppressWarnings("unused")
  public Collection<String> getPaarElementIds() {

    return this.paarElementIds;
  }

  public Collection<OldStyleSetDescription> getModifiedResistanceSets() {
    return this.modifiedResistanceSets;
  }
}
