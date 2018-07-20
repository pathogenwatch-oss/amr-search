package net.cgps.wgsa.paarsnp.core.lib.blast;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import net.cgps.wgsa.paarsnp.core.lib.utils.DnaSequence;
import net.cgps.wgsa.paarsnp.core.snpar.json.Mutation;

import java.util.Collection;
import java.util.Map;

/**
 * Processes the match sequence to find premature stop codons etc.
 */
class SequenceProcessor {

  private static final char DELETION_CHAR = '-';
  private final CharSequence refAlignSeq;
  private final int refStart;
  private final DnaSequence.Strand strand;
  private final CharSequence queryAlignSeq;
  private final int queryStart;
  private final MutationBuilder mutationBuilder;

  SequenceProcessor(final CharSequence refAlignSeq, final int refStart, final DnaSequence.Strand strand, final CharSequence queryAlignSeq, final int queryStart, final MutationBuilder mutationBuilder) {

    this.refAlignSeq = refAlignSeq;
    this.refStart = refStart;
    this.strand = strand;
    this.queryAlignSeq = queryAlignSeq;
    this.queryStart = queryStart;
    this.mutationBuilder = mutationBuilder;
  }

  /**
   * Steps through the two sequences identifying the location and type of mutations (differences).
   */
  Map<Integer, Collection<Mutation>> call() {

    final ListMultimap<Integer, Mutation> mutations = ArrayListMultimap.create();

    // The two sequence lengths should be exactly the same.
    // Determine the direction to increment the reference position.
    final int incr = DnaSequence.Strand.FORWARD == strand ? 1 : -1;

    int querySeqLocation = this.queryStart - 1; // Start the position before.
    int refSeqLocation = this.refStart - incr; // Start the position before.

    for (int alignmentLocation = 0; alignmentLocation < this.refAlignSeq.length(); alignmentLocation++) {

      final char refChar = this.refAlignSeq.charAt(alignmentLocation);
      final char queryChar = this.queryAlignSeq.charAt(alignmentLocation);

      if (refChar == queryChar) {
        // No mutation here

        querySeqLocation++;
        refSeqLocation += incr;

      } else {
        if (DELETION_CHAR == queryChar) {

          // Deletion
          refSeqLocation += incr;
          mutations.put(refSeqLocation, this.mutationBuilder.build(queryChar, refChar, Mutation.MutationType.D, querySeqLocation, refSeqLocation, strand));
        } else if (DELETION_CHAR == refChar) {

          // Insert
          querySeqLocation++;
          mutations.put(refSeqLocation, this.mutationBuilder.build(queryChar, refChar, Mutation.MutationType.I, querySeqLocation, refSeqLocation, strand));
        } else {

          // Substitution
          querySeqLocation++;
          refSeqLocation += incr;
          mutations.put(refSeqLocation, this.mutationBuilder.build(queryChar, refChar, Mutation.MutationType.S, querySeqLocation, refSeqLocation, strand));
        }
      }
    }

    return mutations.asMap();
  }
}
