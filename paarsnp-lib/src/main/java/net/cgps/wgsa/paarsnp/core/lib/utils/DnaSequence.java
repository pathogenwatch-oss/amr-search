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

  public static String translateMultiple(final String codons) {
    return Streams.stream(Splitter.fixedLength(3)
        .split(codons))
        .map(DnaSequence::translateCodon)
        .map(translationOpt -> translationOpt.orElse('-'))
        .collect(Collector.of(
            StringBuilder::new,
            StringBuilder::append,
            StringBuilder::append,
            StringBuilder::toString));
  }

  public enum Strand {
    FORWARD, REVERSE
  }

  public static Optional<Character> translateCodon(final String codon) {

    return DEFAULT_CODON_TABLE.translateCodon(codon);
  }
}

