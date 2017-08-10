package net.cgps.wgsa.paarsnp.core.snpar.json;

import net.cgps.wgsa.paarsnp.core.lib.SequenceType;
import net.cgps.wgsa.paarsnp.core.lib.json.AbstractJsonnable;
import net.cgps.wgsa.paarsnp.core.snpar.MutationType;

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
  private final MutationType mutationType;


  @SuppressWarnings("unused")
  private ResistanceMutation() {

    this("", "", MutationType.S, SequenceType.PROTEIN, "", "", 0, "", "");
  }

  public ResistanceMutation(final String name, final String setId, final MutationType mutationType, final SequenceType sequenceType, final String referenceId, final String originalSequence, final int repSequenceLocation, final String mutationSequence, final String source) {
    // NB for the SNP archive the query location is the same as the representative location.
    this.name = name;
    this.mutationType = mutationType;
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

  public MutationType getMutationType() {
    return this.mutationType;
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

    if (this == o) {
      return true;
    }
    if (o == null || this.getClass() != o.getClass()) {
      return false;
    }

    final ResistanceMutation that = (ResistanceMutation) o;

    if (this.repSequenceLocation != that.repSequenceLocation) {
      return false;
    }
    if (this.sequenceType != that.sequenceType) {
      return false;
    }
    if (this.setId != null ? !this.setId.equals(that.setId) : that.setId != null) {
      return false;
    }
    if (this.source != null ? !this.source.equals(that.source) : that.source != null) {
      return false;
    }
    if (this.name != null ? !this.name.equals(that.name) : that.name != null) {
      return false;
    }
    if (this.referenceId != null ? !this.referenceId.equals(that.referenceId) : that.referenceId != null) {
      return false;
    }
    if (this.originalSequence != null ? !this.originalSequence.equals(that.originalSequence) : that.originalSequence != null) {
      return false;
    }
    if (this.mutationSequence != null ? !this.mutationSequence.equals(that.mutationSequence) : that.mutationSequence != null) {
      return false;
    }
    return this.mutationType == that.mutationType;
  }

  @Override
  public int hashCode() {

    int result = this.sequenceType != null ? this.sequenceType.hashCode() : 0;
    result = 31 * result + (this.setId != null ? this.setId.hashCode() : 0);
    result = 31 * result + (this.source != null ? this.source.hashCode() : 0);
    result = 31 * result + (this.name != null ? this.name.hashCode() : 0);
    result = 31 * result + (this.referenceId != null ? this.referenceId.hashCode() : 0);
    result = 31 * result + (this.originalSequence != null ? this.originalSequence.hashCode() : 0);
    result = 31 * result + this.repSequenceLocation;
    result = 31 * result + (this.mutationSequence != null ? this.mutationSequence.hashCode() : 0);
    result = 31 * result + (this.mutationType != null ? this.mutationType.hashCode() : 0);
    return result;
  }
}
