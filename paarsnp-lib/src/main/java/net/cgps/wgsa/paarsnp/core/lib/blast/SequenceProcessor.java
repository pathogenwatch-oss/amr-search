package net.cgps.wgsa.paarsnp.core.lib.blast;

import net.cgps.wgsa.paarsnp.core.lib.DnaSequence;
import net.cgps.wgsa.paarsnp.core.snpar.json.Mutation;

import java.util.HashMap;
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
  private final Map<Integer, Mutation> mutations;

  SequenceProcessor(final CharSequence refAlignSeq, final int refStart, final DnaSequence.Strand strand, final CharSequence queryAlignSeq, final int queryStart, final MutationBuilder mutationBuilder) {

    this.refAlignSeq = refAlignSeq;
    this.refStart = refStart;
    this.strand = strand;
    this.queryAlignSeq = queryAlignSeq;
    this.queryStart = queryStart;
    this.mutationBuilder = mutationBuilder;
    this.mutations = new HashMap<>(300);
  }

  /**
   * Steps through the two sequences identifying the location and type of mutations (differences).
   */
  SequenceProcessingResult call() {


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
        final Mutation.MutationType mutationType;
        if (DELETION_CHAR == queryChar) {

          // Deletion
          refSeqLocation += incr;
          mutationType = Mutation.MutationType.D;
        } else if (DELETION_CHAR == refChar) {

          // Insert
          querySeqLocation++;
          mutationType = Mutation.MutationType.I;
        } else {

          // Substitution
          querySeqLocation++;
          refSeqLocation += incr;
          mutationType = Mutation.MutationType.S;
        }
        this.mutations.put(refSeqLocation, this.mutationBuilder.build(queryChar, refChar, mutationType, querySeqLocation, refSeqLocation, strand));
      }

    }

    return new SequenceProcessingResult(this.mutations);
  }
}
