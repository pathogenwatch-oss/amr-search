package net.cgps.wgsa.paarsnp.core.snpar.json;

import net.cgps.wgsa.paarsnp.core.LibraryMetadata;
import net.cgps.wgsa.paarsnp.core.lib.json.AbstractJsonnable;
import net.cgps.wgsa.paarsnp.core.lib.json.ResistanceSet;
import net.cgps.wgsa.paarsnp.core.snpar.SnparReferenceSequence;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SnparLibrary extends AbstractJsonnable implements LibraryMetadata {

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

  public SnparReferenceSequence getSequence(final String librarySequenceId) {

    return this.sequences.get(librarySequenceId);
  }

  @Override
  public String getSpeciesId() {
    return this.speciesId;
  }

  @Override
  public double getMinimumPid() {
    return this.minimumPid;
  }
}
