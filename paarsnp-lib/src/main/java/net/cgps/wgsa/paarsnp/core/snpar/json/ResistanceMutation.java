package net.cgps.wgsa.paarsnp.core.snpar.json;

import net.cgps.wgsa.paarsnp.core.lib.SequenceType;
import net.cgps.wgsa.paarsnp.core.lib.json.AbstractJsonnable;

public class ResistanceMutation extends AbstractJsonnable {

  // Whether the reference sequence is coding or non-coding (affects how the variation is assessed)
  private final SequenceType sequenceType;
  private final String setId;
  // The source of the data.
  private final String source;
  // Mutation name, e.g. rpoB_A667T
  private final String name;
  private final String referenceId;
  private final String originalSequence;
  private final int repSequenceLocation;
  private final String mutationSequence;

  @SuppressWarnings("unused")
  private ResistanceMutation() {

    this("", "", SequenceType.PROTEIN, "", "", 0, "", "");
  }

  public ResistanceMutation(final String name, final String setId, final SequenceType sequenceType, final String referenceId, final String originalSequence, final int repSequenceLocation, final String mutationSequence, final String source) {
    // NB for the SNP archive the query location is the same as the representative location.
    this.name = name;
    this.sequenceType = sequenceType;
    this.setId = setId;
    this.referenceId = referenceId;
    this.originalSequence = originalSequence;
    this.repSequenceLocation = repSequenceLocation;
    this.mutationSequence = mutationSequence;
    this.source = source;
  }

  public String getReferenceId() {
    return this.referenceId;
  }

  public String getOriginalSequence() {
    return this.originalSequence;
  }

  public int getRepSequenceLocation() {
    return this.repSequenceLocation;
  }

  public String getMutationSequence() {
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

  public SequenceType getSequenceType() {

    return this.sequenceType;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final ResistanceMutation that = (ResistanceMutation) o;

    if (repSequenceLocation != that.repSequenceLocation) return false;
    if (sequenceType != that.sequenceType) return false;
    if (setId != null ? !setId.equals(that.setId) : that.setId != null) return false;
    if (source != null ? !source.equals(that.source) : that.source != null) return false;
    if (name != null ? !name.equals(that.name) : that.name != null) return false;
    if (referenceId != null ? !referenceId.equals(that.referenceId) : that.referenceId != null) return false;
    if (originalSequence != null ? !originalSequence.equals(that.originalSequence) : that.originalSequence != null)
      return false;
    return mutationSequence != null ? mutationSequence.equals(that.mutationSequence) : that.mutationSequence == null;
  }

  @Override
  public int hashCode() {
    int result = sequenceType != null ? sequenceType.hashCode() : 0;
    result = 31 * result + (setId != null ? setId.hashCode() : 0);
    result = 31 * result + (source != null ? source.hashCode() : 0);
    result = 31 * result + (name != null ? name.hashCode() : 0);
    result = 31 * result + (referenceId != null ? referenceId.hashCode() : 0);
    result = 31 * result + (originalSequence != null ? originalSequence.hashCode() : 0);
    result = 31 * result + repSequenceLocation;
    result = 31 * result + (mutationSequence != null ? mutationSequence.hashCode() : 0);
    return result;
  }
}
