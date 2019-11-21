package net.cgps.wgsa.paarsnp.core.lib.utils;

import com.google.common.base.Splitter;
import com.google.common.collect.Streams;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;
import java.util.stream.Collector;


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

  public static String translateMultiple(final String codons, final char failureChar) {
    return Streams.stream(Splitter.fixedLength(3)
        .split(codons))
        .map(DnaSequence::translateCodon)
        .map(translationOpt -> translationOpt.orElse(failureChar))
        .collect(Collector.of(
            StringBuilder::new,
            StringBuilder::append,
            StringBuilder::append,
            StringBuilder::toString));
  }

  public static int ntIndexFromCodon(final int codonIndex) {
    return (codonIndex * 3) - 2;
  }

  public enum Strand {
    FORWARD, REVERSE
  }

  public static int codonIndexAt(final int nucleotideIndex) {
    if (1 > nucleotideIndex) {
      throw new IndexOutOfBoundsException();
    }
    return (int) Math.ceil((double) nucleotideIndex / 3);
  }

  public static int countCodons(final int dnaLength) {
    return (int) Math.ceil((double) dnaLength / 3);
  }

  public static Optional<Character> translateCodon(final String codon) {

    return DEFAULT_CODON_TABLE.translateCodon(codon);
  }
}

