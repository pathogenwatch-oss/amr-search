package net.cgps.wgsa.paarsnp.core.lib;

import com.fasterxml.jackson.annotation.JsonIgnore;
import net.cgps.wgsa.paarsnp.core.snpar.json.Mutation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * Class representing the results of a BLAST mutation search (extracted from the XML) and includes a list of identified mutations.
 */
public class MutationSearchResult {

  // Don't use a char[] here as the internal elements aren't immutable.
  private final BlastSearchStatistics blastSearchStatistics;
  private final List<Mutation> mutations;
  private final String queryMatchSequence;
  private final String referenceMatchSequence;

  @SuppressWarnings("unused")
  private MutationSearchResult() {

    this("", 0, 0, "", 0, 0, "", "", 0.0, 0.0, true, Collections.emptyList(), 0);
  }

  public MutationSearchResult(final String librarySequenceId, final int librarySequenceStart, final int librarySequenceStop, final String querySequenceId, final int querySequenceStart, final int querySequenceStop, final String queryMatchSequence, final String referenceMatchSequence, final double percentIdentity, final double evalue, final boolean reversed, final Collection<Mutation> mutations, final int librarySequenceLength) {

    this.blastSearchStatistics = new BlastSearchStatistics(librarySequenceId, librarySequenceStart, querySequenceId, querySequenceStart, percentIdentity, evalue, reversed, librarySequenceStop, querySequenceStop, librarySequenceLength);
    this.referenceMatchSequence = referenceMatchSequence;
    this.queryMatchSequence = queryMatchSequence;
    this.mutations = new ArrayList<>(mutations);
  }

  public String getReferenceMatchSequence() {

    return this.referenceMatchSequence;
  }

  public BlastSearchStatistics getBlastSearchStatistics() {

    return this.blastSearchStatistics;
  }

  public String getQueryMatchSequence() {

    return this.queryMatchSequence;
  }

  public Collection<Mutation> getMutations() {

    return Collections.unmodifiableCollection(this.mutations);
  }

  @JsonIgnore
  public boolean isComplete() {

    return this.blastSearchStatistics.getLibrarySequenceStop() == this.blastSearchStatistics.getLibrarySequenceLength();
  }

  @Override
  public String toString() {

    return "MutationSearchResult{" +
        "blastSearchStatistics='" + this.blastSearchStatistics.toString() + '\'' +
        ", queryMatchSequence='" + this.queryMatchSequence + '\'' +
        ", mutations=" + this.mutations +
        ", referenceMatchSequence='" + this.referenceMatchSequence + '\'' +
        '}';
  }

  public String format(final Function<MutationSearchResult, char[]> blastSnpMatchFormatter) {

    return new String(blastSnpMatchFormatter.apply(this));
  }
}
