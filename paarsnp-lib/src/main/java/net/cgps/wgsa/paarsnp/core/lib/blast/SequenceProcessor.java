package net.cgps.wgsa.paarsnp.core.lib.blast;

import net.cgps.wgsa.paarsnp.core.snpar.MutationType;
import net.cgps.wgsa.paarsnp.core.snpar.json.Mutation;

import java.util.Collection;
import java.util.HashSet;

/**
 * Processes the match sequence to find premature stop codons etc.
 */
public class SequenceProcessor {

  public static final char DELETION_CHAR = '-';
  private final CharSequence refAlignSeq;
  private final int refStart;
  private final boolean reverse;
  private final CharSequence queryAlignSeq;
  private final int queryStart;
  private final MutationBuilder mutationBuilder;
  private final Collection<Mutation> mutations;

  public SequenceProcessor(final CharSequence refAlignSeq, final int refStart, final boolean reverse, final CharSequence queryAlignSeq, final int queryStart, final MutationBuilder mutationBuilder) {

    this.refAlignSeq = refAlignSeq;
    this.refStart = refStart;
    this.reverse = reverse;
    this.queryAlignSeq = queryAlignSeq;
    this.queryStart = queryStart;
    this.mutationBuilder = mutationBuilder;
    this.mutations = new HashSet<>();
  }

  /**
   * Steps through the two sequences identifying the location and type of mutations (differences).
   *
   */
  public SequenceProcessingResult call() {

    // The two sequence lengths should be exactly the same length.
    // Determine the direction to increment the reference position.
    final int incr = this.reverse ? -1 : 1;

    int querySeqLocation = this.queryStart - 1; // Start the position before.
    int refSeqLocation = this.refStart - incr; // Start the position before.

    boolean inMutation = false;
    int mutatedCodonPosition = 0;

    for (int alignmentLocation = 0; alignmentLocation < this.refAlignSeq.length(); alignmentLocation++) {

      final char refChar = this.refAlignSeq.charAt(alignmentLocation);
      final char queryChar = this.queryAlignSeq.charAt(alignmentLocation);

      if (0 < mutatedCodonPosition) {
        // Once down to 0 no more checks until another mutation is found.
        mutatedCodonPosition--;
      }

      if (refChar == queryChar) {
        // No mutation here => close old mutations

        querySeqLocation++;
        refSeqLocation += incr;

        if (inMutation) {
          // build the old mutation (build resets the builder as well)
          this.closeCurrentMutation();
          inMutation = false;
        }
      } else if (DELETION_CHAR == queryChar) {

        // Deletion
        refSeqLocation += incr;

        // Don't check for stop codon here (since it's a gap!), but set the counter to 3 so that the next 2 positions are checked.
        mutatedCodonPosition = 3;

        if (inMutation) {
          // check if deletion.
          if (MutationType.D == this.mutationBuilder.getMutationType()) {
            // Extend the deletion
            this.extendTheMutationSequences(refChar, DELETION_CHAR);
          } else {
            // If not, build the old one and initiate a deletion mutation.
            this.closeCurrentMutation();
            this.mutationBuilder.setMutationType(MutationType.D).initiateQuerySequence(DELETION_CHAR).initiateReferenceSequence(refChar).setQueryLocation(querySeqLocation).setReferenceLocation(refSeqLocation).setReversed(this.reverse);
          }

        } else {
          // New mutation
          inMutation = true;
          // Initialise builder stuff
          this.mutationBuilder.setMutationType(MutationType.D).initiateQuerySequence(DELETION_CHAR).initiateReferenceSequence(refChar).setQueryLocation(querySeqLocation).setReferenceLocation(refSeqLocation).setReversed(this.reverse);
        }

      } else if (DELETION_CHAR == refChar) {

        // Insertion
        querySeqLocation++;

        if (inMutation) {
          // Check if in insert

          if (MutationType.I == this.mutationBuilder.getMutationType()) {
            this.extendTheMutationSequences(DELETION_CHAR, queryChar);
          } else {
            // Build the old one and initiate an insert mutation
            this.closeCurrentMutation();
            this.mutationBuilder.setMutationType(MutationType.I).initiateQuerySequence(queryChar).initiateReferenceSequence(DELETION_CHAR).setQueryLocation(querySeqLocation).setReferenceLocation(refSeqLocation).setReversed(this.reverse);
          }

        } else {
          // new Mutation
          inMutation = true;
          this.mutationBuilder.setMutationType(MutationType.I).initiateQuerySequence(queryChar).initiateReferenceSequence(DELETION_CHAR).setQueryLocation(querySeqLocation).setReferenceLocation(refSeqLocation).setReversed(this.reverse);
        }
      } else {

        querySeqLocation++;
        refSeqLocation += incr;

//        // Need to check if a stop codon has been formed
        mutatedCodonPosition = 2;

        // Substitution
        if (inMutation) {
          if (MutationType.S == this.mutationBuilder.getMutationType()) {
            // extend the mutation
            this.extendTheMutationSequences(refChar, queryChar);
          } else {
            // build the old mutation and start a new one.
            this.closeCurrentMutation();
            this.mutationBuilder.setMutationType(MutationType.S).initiateQuerySequence(queryChar).initiateReferenceSequence(refChar).setQueryLocation(querySeqLocation).setReferenceLocation(refSeqLocation).setReversed(this.reverse);
          }
        } else {
          inMutation = true;
          this.mutationBuilder.setMutationType(MutationType.S).initiateQuerySequence(queryChar).initiateReferenceSequence(refChar).setQueryLocation(querySeqLocation).setReferenceLocation(refSeqLocation).setReversed(this.reverse);
        }
      }
    }

    // Check last position
    if (inMutation) {
      this.closeCurrentMutation();
    }

    // Return
    return new SequenceProcessingResult(this.mutations);
  }

  private void closeCurrentMutation() {
    final Mutation mutation = this.mutationBuilder.build();
    this.mutations.add(mutation);
  }

  private void extendTheMutationSequences(final char refChar, final char qChar) {

    this.mutationBuilder.extendReferenceSequence(refChar).extendQuerySequence(qChar);
  }
}
