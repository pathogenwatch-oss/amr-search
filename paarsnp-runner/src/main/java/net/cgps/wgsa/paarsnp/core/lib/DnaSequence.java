package net.cgps.wgsa.paarsnp.core.lib;

import net.cgps.wgsa.paarsnp.core.snpar.MutationType;
import net.cgps.wgsa.paarsnp.core.snpar.json.Mutation;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.regex.Pattern;


/**
 * Sequences that conform strictly to DNA sequence.
 * <p>
 * Created by cyeats on 07/04/2014.
 */
public class DnaSequence {

  public static final CodonTable DEFAULT_CODON_TABLE = new CodonTable();
  private static final Pattern NOT_DNA_CHARS = Pattern.compile("\\.|-|\\s|\\r?\\n");

  /**
   * Removes whitespace, new lines & alignment characters (e.g. '.' & '-'), and lower cases it all.
   *
   * @param dnaSequence Dirty DNA sequence.
   * @return cleaned sequence
   */
  public static String clean(final String dnaSequence) {

    return NOT_DNA_CHARS.matcher(dnaSequence).replaceAll("").toUpperCase();
  }


  public static String reverseTranscribe(final String dnaSequence) {

    return complement(new StringBuffer(dnaSequence).reverse().toString());
  }

  public static String complement(final String dnaSequence) {

    return StringUtils.replaceChars(dnaSequence, "GTCAgtca", "CAGTcagt");
  }

  /**
   * Given the start of the match to the reference sequence & the location of the match in the reference sequence, along with the match sequence, the mutated sequence is returned.
   *
   * @param mutation                Mutation object
   * @param referenceMatchStart     The start position of the sequence to be mutated relative to what's given in the mutation record.
   * @param originalMatchedSequence The sequence to be mutated
   * @return Mutated sequence.
   */
  public static String mutateSequence(final Mutation mutation, final int referenceMatchStart, final String originalMatchedSequence) {

    // Validate
    if ((1 > mutation.getReferenceLocation()) || (mutation.getReferenceLocation() > originalMatchedSequence.length())) {
      final Logger logger = LoggerFactory.getLogger(DnaSequence.class);
      logger.error("Mutation is in an impossible place: {} in sequence of length {}", mutation.getReferenceLocation(), originalMatchedSequence.length());
      logger.error(mutation.toString());
      throw new RuntimeException("Mutation location error!");
    }

    final int mutationLocation = mutation.getReferenceLocation() - referenceMatchStart + 1;
    return mutateSequence(mutationLocation, mutation.getMutationType(), mutation.getMutationSequence(), originalMatchedSequence);
  }

  public static String mutateSequence(final int mutationLocation, final MutationType mutationType, final String mutationSequence, final String originalSequence) {

    final StringBuilder mutSeqSb = new StringBuilder(originalSequence);

    switch (mutationType) {

      // Remember to adjust index by one due to the sequence location index starting at 1, not 0.
      case S:
        substituteMutation(mutSeqSb, mutationLocation, mutationSequence);
        break;

      case I:
        insertMutation(mutSeqSb, mutationLocation, mutationSequence);
        break;

      case D:
        deletionMutation(mutSeqSb, mutationLocation, mutationSequence.length());
        break;
    }

    return mutSeqSb.toString();
  }

  public static void substituteMutation(final StringBuilder sequenceToMutate, final int sequenceLocation, final String newSequence) {

    sequenceToMutate.replace(sequenceLocation - 1, sequenceLocation + newSequence.length() - 1, newSequence);
  }

  public static void insertMutation(final StringBuilder sequenceToMutate, final int sequenceLocation, final String newSequence) {

    sequenceToMutate.insert(sequenceLocation - 1, newSequence);
  }

  public static void deletionMutation(final StringBuilder sequenceToMutate, final int sequenceLocation, final int length) {

    sequenceToMutate.delete(sequenceLocation - 1, sequenceLocation - 1 + length);
  }

  public static Optional<Character> translateCodon(final String codon) {

    return DEFAULT_CODON_TABLE.translateCodon(codon);
  }
}

