package net.cgps.wgsa.paarsnp.core.lib.blast;

import com.fasterxml.jackson.annotation.JsonIgnore;
import net.cgps.wgsa.paarsnp.core.lib.utils.DnaSequence;
import net.cgps.wgsa.paarsnp.core.snpar.json.Mutation;

import java.util.*;

public class BlastMatch {

  // Don't use a char[] here as the internal elements aren't immutable.
  protected final Map<Integer, Collection<Mutation>> mutations;
  private final BlastSearchStatistics blastSearchStatistics;
  private final String queryMatchSequence;
  private final String referenceMatchSequence;
  private String forwardMatchSequence;

  public BlastMatch(final BlastSearchStatistics blastSearchStatistics, final String queryMatchSequence, final String referenceMatchSequence, final Map<Integer, Collection<Mutation>> mutations) {
    this.blastSearchStatistics = blastSearchStatistics;
    this.queryMatchSequence = queryMatchSequence;
    this.referenceMatchSequence = referenceMatchSequence;
    this.mutations = new HashMap<>(mutations);
  }

  public double calculateCoverage() {
    return (((double) this.blastSearchStatistics.getLibrarySequenceStop() - this.blastSearchStatistics.getLibrarySequenceStart() + 1.0)
        / (double) this.blastSearchStatistics.getLibrarySequenceLength())
        * 100;
  }

  @JsonIgnore
  public final int getSubjectMatchLength() {

    final List<Integer> startStop = new ArrayList<>(2);
    startStop.add(this.blastSearchStatistics.getLibrarySequenceStart());
    startStop.add(this.blastSearchStatistics.getLibrarySequenceStop());

    // To allow for reversed matches.
    Collections.sort(startStop);

    return (startStop.get(1) - startStop.get(0)) + 1;
  }

  public final BlastSearchStatistics getBlastSearchStatistics() {
    return this.blastSearchStatistics;
  }

  public final String getQueryMatchSequence() {
    return this.queryMatchSequence;
  }

  public final String getReferenceMatchSequence() {
    return this.referenceMatchSequence;
  }

  public final String getForwardQuerySequence() {

    if (null == this.forwardMatchSequence) {
      if (DnaSequence.Strand.FORWARD == this.blastSearchStatistics.getStrand()) {
        this.forwardMatchSequence = this.queryMatchSequence;
      } else {
        this.forwardMatchSequence = DnaSequence.reverseTranscribe(this.queryMatchSequence);
      }
    }
    return this.forwardMatchSequence;
  }

  public Map<Integer, Collection<Mutation>> getMutations() {

    return Collections.unmodifiableMap(this.mutations);
  }


}
