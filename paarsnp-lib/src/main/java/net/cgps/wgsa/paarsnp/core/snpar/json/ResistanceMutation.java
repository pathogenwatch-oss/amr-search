package net.cgps.wgsa.paarsnp.core.snpar.json;

import net.cgps.wgsa.paarsnp.core.lib.json.AbstractJsonnable;

import java.util.Objects;

public class ResistanceMutation extends AbstractJsonnable {

  // Whether the reference sequence is coding or non-coding (affects how the variation is assessed)
  private final String setId;
  // The source of the data.
  private final String source;
  // Mutation name, e.g. rpoB_A667T
  private final String name;
  private final String referenceId;
  private final char originalSequence;
  private final int repSequenceLocation;
  private final char mutationSequence;
  private final int aaLocation;

  @SuppressWarnings("unused")
  private ResistanceMutation() {

    this("", "", "", 'A', 0, 'T', "", 0);
  }

  public ResistanceMutation(final String name, final String setId, final String referenceId, final char originalSequence, final int repSequenceLocation, final char mutationSequence, final String source, final int aaLocation) {
    // NB for the SNP archive the query location is the same as the representative location.
    this.name = name;
    this.setId = setId;
    this.referenceId = referenceId;
    this.originalSequence = originalSequence;
    this.repSequenceLocation = repSequenceLocation;
    this.mutationSequence = mutationSequence;
    this.source = source;
    this.aaLocation = aaLocation;
  }

  public String getReferenceId() {
    return this.referenceId;
  }

  public char getOriginalSequence() {
    return this.originalSequence;
  }

  public int getNtLocation() {
    return this.repSequenceLocation;
  }

  public char getMutationSequence() {
    return this.mutationSequence;
  }

  public String getSetId() {

    return this.setId;
  }

  public String getName() {

    return this.name;
  }

  public String getSource() {

    return this.source;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ResistanceMutation that = (ResistanceMutation) o;
    return repSequenceLocation == that.repSequenceLocation &&
        Objects.equals(setId, that.setId) &&
        Objects.equals(source, that.source) &&
        Objects.equals(name, that.name) &&
        Objects.equals(referenceId, that.referenceId) &&
        Objects.equals(originalSequence, that.originalSequence) &&
        Objects.equals(mutationSequence, that.mutationSequence);
  }

  @Override
  public int hashCode() {

    return Objects.hash(setId, source, name, referenceId, originalSequence, repSequenceLocation, mutationSequence);
  }

  public int getAALocation() {
    return this.aaLocation;
  }
}
