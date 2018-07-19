package net.cgps.wgsa.paarsnp.core.lib;

import net.cgps.wgsa.paarsnp.core.snpar.MutationType;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;


/**
 * Sequences that conform strictly to DNA sequence.
 * <p>
 * Created by cyeats on 07/04/2014.
 */
public class DnaSequence {

  public static final CodonTable DEFAULT_CODON_TABLE = new CodonTable();


  public static String reverseTranscribe(final String dnaSequence) {

    return complement(new StringBuffer(dnaSequence).reverse().toString());
  }

  public static String complement(final String dnaSequence) {

    return StringUtils.replaceChars(dnaSequence, "GTCAgtca", "CAGTcagt");
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

