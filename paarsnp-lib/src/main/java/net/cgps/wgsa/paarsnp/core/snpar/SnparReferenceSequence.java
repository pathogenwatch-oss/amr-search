package net.cgps.wgsa.paarsnp.core.snpar;

import net.cgps.wgsa.paarsnp.core.lib.json.AbstractJsonnable;
import net.cgps.wgsa.paarsnp.core.snpar.json.ResistanceMutation;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

public class SnparReferenceSequence extends AbstractJsonnable {

  private final String sequenceId;
  private final double seqIdThreshold;
  private final double evalueThreshold;
  private final Collection<ResistanceMutation> resistanceMutations;
  private final String sequence;

  @SuppressWarnings("unused")
  private SnparReferenceSequence() {

    this("", 0.0, 0.0, Collections.emptyList(), "");
  }

  public SnparReferenceSequence(final String sequenceId, final double seqIdThreshold, final double evalueThreshold, final Collection<ResistanceMutation> resistanceMutations, String sequence) {

    this.sequenceId = sequenceId;
    this.seqIdThreshold = seqIdThreshold;
    this.evalueThreshold = evalueThreshold;
    this.resistanceMutations = new HashSet<>(resistanceMutations);
    this.sequence = sequence;
  }

  public void addMutation(final ResistanceMutation resistanceMutation) {

    this.resistanceMutations.add(resistanceMutation);
  }

  public Collection<ResistanceMutation> getResistanceMutations() {

    return Collections.unmodifiableCollection(this.resistanceMutations);
  }

  public String getSequenceId() {

    return this.sequenceId;
  }

  public double getSeqIdThreshold() {

    return this.seqIdThreshold;
  }

  public double getEvalueThreshold() {

    return this.evalueThreshold;
  }

  public String getSequence() {

    return this.sequence;
  }

  @Override
  public boolean equals(final Object o) {

    if (this == o) {
      return true;
    }
    if (o == null || this.getClass() != o.getClass()) {
      return false;
    }

    final SnparReferenceSequence that = (SnparReferenceSequence) o;

    if (Double.compare(that.seqIdThreshold, this.seqIdThreshold) != 0) {
      return false;
    }
    if (Double.compare(that.evalueThreshold, this.evalueThreshold) != 0) {
      return false;
    }
    if (this.sequenceId != null ? !this.sequenceId.equals(that.sequenceId) : that.sequenceId != null) {
      return false;
    }
    if (this.resistanceMutations != null ? !this.resistanceMutations.equals(that.resistanceMutations) : that.resistanceMutations != null) {
      return false;
    }
    return this.sequence != null ? this.sequence.equals(that.sequence) : that.sequence == null;

  }

  @Override
  public int hashCode() {

    int result;
    long temp;
    result = this.sequenceId != null ? this.sequenceId.hashCode() : 0;
    temp = Double.doubleToLongBits(this.seqIdThreshold);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(this.evalueThreshold);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    result = 31 * result + (this.resistanceMutations != null ? this.resistanceMutations.hashCode() : 0);
    result = 31 * result + (this.sequence != null ? this.sequence.hashCode() : 0);
    return result;
  }
}
