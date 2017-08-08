package net.cgps.wgsa.paarsnp.core.snpar;

import net.cgps.wgsa.paarsnp.core.lib.ResistanceSet;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SnparLibrary {

  private final Map<String, SnparReferenceSequence> sequences;
  private final Map<String, ResistanceSet> resistanceSets;
  private final String speciesId;
  private final double minimumPid;

  @SuppressWarnings("unused")
  private SnparLibrary() {

    this(Collections.emptyMap(), Collections.emptyMap(), "", 0.0);
  }

  public SnparLibrary(final Map<String, SnparReferenceSequence> sequences, final Map<String, ResistanceSet> resistanceSets, final String speciesId, final double minimumPid) {

    this.sequences = new HashMap<>(sequences);
    this.resistanceSets = resistanceSets;
    this.speciesId = speciesId;
    this.minimumPid = minimumPid;
  }

  public Map<String, SnparReferenceSequence> getSequences() {

    return this.sequences;
  }

  public Map<String, ResistanceSet> getResistanceSets() {

    return this.resistanceSets;
  }

  public Optional<SnparReferenceSequence> getSequence(final String librarySequenceId) {

    return Optional.ofNullable(this.sequences.get(librarySequenceId));
  }

  public double getSequenceThreshold(final String librarySequenceId) {

    return this.sequences.get(librarySequenceId).getSeqIdThreshold();
  }

  public double getEvalueThreshold(final String librarySequenceId) {

    return this.sequences.get(librarySequenceId).getEvalueThreshold();
  }

  public String getSpeciesId() {
    return this.speciesId;
  }

  public double getMinimumPid() {
    return this.minimumPid;
  }
}
