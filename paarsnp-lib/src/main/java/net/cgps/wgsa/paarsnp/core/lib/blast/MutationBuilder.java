package net.cgps.wgsa.paarsnp.core.lib.blast;

import net.cgps.wgsa.paarsnp.core.lib.utils.DnaSequence;

/**
 * Factory class for building mutations. Handles positioning on reverse matches etc.
 */
public class MutationBuilder {

  /**
   *
   * @return the mutation as configured by the builder.
   */
  public Mutation build(final char querySequence, final char referenceSequence, final Mutation.MutationType mutationType, final int queryLocation, final int referenceLocation, final DnaSequence.Strand strand) {

    return new Mutation(mutationType, queryLocation, this.formatChar(querySequence), this.formatChar(referenceSequence), this.determineActualLocation(referenceLocation, mutationType, strand));
  }

  private String formatChar(final char sequence) {
    return this.formatSequence(Character.toString(sequence));
  }

  private String formatSequence(final String sequence) {
    return sequence.toUpperCase();
  }

  public Mutation build(final String querySequence, final String referenceSequence, final Mutation.MutationType mutationType, final int queryLocation, final int referenceLocation, final DnaSequence.Strand strand) {

    return new Mutation(mutationType, queryLocation, this.formatSequence(querySequence), this.formatSequence(referenceSequence), this.determineActualLocation(referenceLocation, mutationType, strand));
  }

  private int determineActualLocation(final int referenceLocation, final Mutation.MutationType mutationType, final DnaSequence.Strand strand) {

    if (DnaSequence.Strand.FORWARD == strand) {
      return referenceLocation;
    }

    // Deal with reversed matches
    switch (mutationType) {
      case S:
        // Adjust by the length of substitution NB a sub of 1 nt is in the same position in either direction.
        return referenceLocation;
      case I:
        // Happens between two nucleotides, we take the first in the reference sequence
        return referenceLocation - 1;
      case D:
        // Deletion starts at the beginning nt in the reference sequence
        return referenceLocation;
      default:
        return 0;
    }
  }
}
