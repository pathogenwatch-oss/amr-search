package net.cgps.wgsa.paarsnp.core.lib.blast;

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
public class MutationSearchMatch extends BaseBlastMatch {

  MutationSearchMatch(final String librarySequenceId, final int librarySequenceStart, final int librarySequenceStop, final String querySequenceId, final int querySequenceStart, final int querySequenceStop, final String queryMatchSequence, final String referenceMatchSequence, final double percentIdentity, final double evalue, final boolean reversed, final Collection<Mutation> mutations, final int librarySequenceLength) {
    super(new BlastSearchStatistics(librarySequenceId, librarySequenceStart, querySequenceId, querySequenceStart, percentIdentity, evalue, reversed, librarySequenceStop, querySequenceStop, librarySequenceLength), queryMatchSequence, referenceMatchSequence, mutations);
  }

}
