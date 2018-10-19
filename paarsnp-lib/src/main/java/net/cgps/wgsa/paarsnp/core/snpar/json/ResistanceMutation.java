package net.cgps.wgsa.paarsnp.core.snpar.json;

import net.cgps.wgsa.paarsnp.core.lib.json.AbstractJsonnable;

import java.util.Objects;

public class ResistanceMutation extends AbstractJsonnable {

  // The source of the data.
  private final String source;
  // Mutation name, e.g. rpoB_A667T
  private final String name;
  private final String referenceId;
  private final char originalSequence;
  private final int referenceLocation;
  private final char mutationSequence;
  private final int aaLocation;

  @SuppressWarnings("unused")
  private ResistanceMutation() {

    this("", "", 'A', 0, 'T', "", 0);
  }

  public ResistanceMutation(final String name, final String referenceId, final char originalSequence, final int referenceLocation, final char mutationSequence, final String source, final int aaLocation) {
    // NB for the SNP archive the query location is the same as the representative location.
    this.name = name;
    this.referenceId = referenceId;
    this.originalSequence = originalSequence;
    this.referenceLocation = referenceLocation;
    this.mutationSequence = mutationSequence;
    this.source = source;
    this.aaLocation = aaLocation;
  }

  @SuppressWarnings("unused")
  public String getReferenceId() {
    return this.referenceId;
  }

  @SuppressWarnings("unused")
  public char getOriginalSequence() {
    return this.originalSequence;
  }

  public int getReferenceLocation() {
    return this.referenceLocation;
  }

  public char getMutationSequence() {
    return this.mutationSequence;
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
    return referenceLocation == that.referenceLocation &&
        Objects.equals(source, that.source) &&
        Objects.equals(name, that.name) &&
        Objects.equals(referenceId, that.referenceId) &&
        Objects.equals(originalSequence, that.originalSequence) &&
        Objects.equals(mutationSequence, that.mutationSequence);
  }

  @Override
  public int hashCode() {

    return Objects.hash(source, name, referenceId, originalSequence, referenceLocation, mutationSequence);
  }

  @SuppressWarnings("unused")
  public int getAaLocation() {
    return this.aaLocation;
  }
}
