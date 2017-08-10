package net.cgps.wgsa.paarsnp.core.lib.blast;

import net.cgps.wgsa.paarsnp.core.lib.DnaSequence;
import net.cgps.wgsa.paarsnp.core.snpar.MutationType;
import net.cgps.wgsa.paarsnp.core.snpar.json.Mutation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory class for building mutations. Handles positioning on reverse matches etc.
 */
public class MutationBuilder {

  private final Logger logger = LoggerFactory.getLogger(MutationBuilder.class);

  // Fields that don't change while parsing an alignment.
  private final String referenceSequenceId;
  private final StringBuilder querySequence = new StringBuilder(20000);
  private final StringBuilder referenceSequence = new StringBuilder(20000);
  // Modifiable fields
  private MutationType mutationType = MutationType.S;
  private int queryLocation;
  private int referenceLocation;
  private boolean reversed;

  public MutationBuilder(final String referenceSequenceId) {

    this.referenceSequenceId = referenceSequenceId;
    this.reset();
  }

  /**
   * Resets the builder object.
   */
  public void reset() {

    this.mutationType = MutationType.S;
    this.querySequence.setLength(0);
    this.referenceSequence.setLength(0);
    this.queryLocation = 0;
    this.referenceLocation = 0;
    this.reversed = false;
  }

  public MutationBuilder extendQuerySequence(final char seqCharacter) {

    this.querySequence.append(seqCharacter);
    return this;
  }

  public MutationBuilder extendReferenceSequence(final char seqCharacter) {

    this.referenceSequence.append(seqCharacter);
    return this;
  }

  public MutationBuilder setQueryLocation(final int queryLocation) {

    this.queryLocation = queryLocation;
    return this;
  }

  public MutationBuilder setReferenceLocation(final int referenceLocation) {

    this.referenceLocation = referenceLocation;
    return this;
  }

  public MutationBuilder setReversed(final boolean reversed) {

    this.reversed = reversed;
    return this;
  }

  /**
   * Building resets the builder so it can be used to build another mutation.
   *
   * @return the mutation as configured by the builder.
   */
  public Mutation build() {

    final Mutation mutation = new Mutation(this.mutationType, this.queryLocation, this.determineSequence(this.querySequence.toString()), this.determineSequence(this.referenceSequence.toString()), this.referenceSequenceId, this.determineActualLocation(this.referenceLocation));
    this.reset();
    return mutation;
  }

  private String determineSequence(final String sequence) {

    if (this.reversed) {
      return DnaSequence.complement(DnaSequence.reverseTranscribe(sequence));
    } else {
      return sequence.toUpperCase();
    }
  }

  private int determineActualLocation(final int referenceLocation) {

    if (!this.reversed) {
      return referenceLocation;
    }

    // Deal with reversed matches
    switch (this.getMutationType()) {
      case S:
        // Adjust by the length of substitution NB a sub of 1 nt is in the same position in either direction.
        return referenceLocation - this.referenceSequence.length() + 1;
      case I:
        // Happens between two nucleotides, we take the first in the reference sequence
        return referenceLocation - 1;
      case D:
        // Deletion starts at the beginning nt in the reference sequence
        return referenceLocation - this.referenceSequence.length() + 1;
      default:
        return 0;
    }
  }

  public MutationType getMutationType() {

    return this.mutationType;
  }

  public MutationBuilder setMutationType(final MutationType mutationType) {

    this.mutationType = mutationType;
    return this;
  }

  @SuppressWarnings("unused")
  private Logger getLogger() {

    return this.logger;
  }

  public MutationBuilder initiateQuerySequence(final char qChar) {

    if (this.querySequence.length() > 0) {
      this.querySequence.setLength(0);
    }
    this.querySequence.append(qChar);
    return this;
  }

  public MutationBuilder initiateReferenceSequence(final char rChar) {

    if (this.referenceSequence.length() > 0) {
      this.referenceSequence.setLength(0);
    }
    this.referenceSequence.append(rChar);
    return this;
  }

}
