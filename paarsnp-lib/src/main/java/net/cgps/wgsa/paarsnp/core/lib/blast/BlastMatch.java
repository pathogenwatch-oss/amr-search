package net.cgps.wgsa.paarsnp.core.lib.blast;

import com.fasterxml.jackson.annotation.JsonIgnore;
import net.cgps.wgsa.paarsnp.core.snpar.json.Mutation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class BlastMatch {

  // Don't use a char[] here as the internal elements aren't immutable.
  protected final List<Mutation> mutations;
  private final BlastSearchStatistics blastSearchStatistics;
  private final String queryMatchSequence;
  private final String referenceMatchSequence;

  public BlastMatch(final BlastSearchStatistics blastSearchStatistics, final String queryMatchSequence, final String referenceMatchSequence, final Collection<Mutation> mutations) {
    this.blastSearchStatistics = blastSearchStatistics;
    this.queryMatchSequence = queryMatchSequence;
    this.referenceMatchSequence = referenceMatchSequence;
    this.mutations = new ArrayList<>(mutations);
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

  public final boolean isComplete() {
    return this.blastSearchStatistics.getLibrarySequenceStop() == this.blastSearchStatistics.getLibrarySequenceLength();
  }

  public Collection<Mutation> getMutations() {

    return Collections.unmodifiableCollection(this.mutations);
  }


}
