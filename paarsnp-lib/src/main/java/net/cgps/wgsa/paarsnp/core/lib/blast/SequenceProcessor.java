package net.cgps.wgsa.paarsnp.core.lib.blast;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import net.cgps.wgsa.paarsnp.core.lib.utils.DnaSequence;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Processes the match sequence to find premature stop codons etc.
 */
public class SequenceProcessor {

  private static final char GAP_CHAR = '-';
  private final CharSequence refAlignSeq;
  private final int refStart;
  private final DnaSequence.Strand strand;
  private final CharSequence queryAlignSeq;
  private final int queryStart;
  private final MutationBuilder mutationBuilder;

  public SequenceProcessor(final CharSequence refAlignSeq, final int refStart, final DnaSequence.Strand strand, final CharSequence queryAlignSeq, final int queryStart, final MutationBuilder mutationBuilder) {

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
  public Map<Integer, Collection<Mutation>> call() {

    final ListMultimap<Integer, Mutation> mutations = ArrayListMultimap.create();

    // The two sequence lengths should be exactly the same.
    int querySeqLocation = this.queryStart - 1; // Start the position before.
    int refSeqLocation = this.refStart - 1; // Start the position before.

    for (int alignmentLocation = 0; alignmentLocation < this.refAlignSeq.length(); alignmentLocation++) {

      final char refChar = this.refAlignSeq.charAt(alignmentLocation);
      final char queryChar = this.queryAlignSeq.charAt(alignmentLocation);

      if (refChar == queryChar) {
        // No mutation here

        querySeqLocation++;
        refSeqLocation++;

      } else {
        if (GAP_CHAR == queryChar) {
          // Deletion
          refSeqLocation++;
          mutations.put(refSeqLocation, this.mutationBuilder.build(queryChar, refChar, Mutation.MutationType.D, querySeqLocation, refSeqLocation, this.strand));

        } else if (GAP_CHAR == refChar) {
          // Insert
          var in_insert = true;
          var insertSequence = new StringBuilder(5);
          insertSequence.append(queryChar);
          querySeqLocation++;

          while (in_insert) {
            // Inserts can never happen at the end of the alignment so we don't have to worry about going out of bounds.
            if (GAP_CHAR == this.refAlignSeq.charAt(alignmentLocation + 1)) {
              // Insert continues
              alignmentLocation++;
              querySeqLocation++;
              insertSequence.append(queryAlignSeq.charAt(alignmentLocation));
            } else {
              in_insert = false;
            }
          }
          mutations.put(refSeqLocation, this.mutationBuilder.build(insertSequence.toString(), StringUtils.repeat(GAP_CHAR, insertSequence.length()), Mutation.MutationType.I, querySeqLocation, refSeqLocation, this.strand));

        } else {
          // Substitution
          querySeqLocation++;
          refSeqLocation++;
          mutations.put(refSeqLocation, this.mutationBuilder.build(queryChar, refChar, Mutation.MutationType.S, querySeqLocation, refSeqLocation, this.strand));
        }
      }
    }

    return Collections.unmodifiableMap(mutations.asMap());
  }
}
