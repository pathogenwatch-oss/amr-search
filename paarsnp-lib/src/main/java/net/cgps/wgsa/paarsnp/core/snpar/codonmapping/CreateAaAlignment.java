package net.cgps.wgsa.paarsnp.core.snpar.codonmapping;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.biojava.nbio.alignment.Alignments;
import org.biojava.nbio.alignment.SimpleGapPenalty;
import org.biojava.nbio.core.alignment.matrices.SubstitutionMatrixHelper;
import org.biojava.nbio.core.alignment.template.SubstitutionMatrix;
import org.biojava.nbio.core.exceptions.CompoundNotFoundException;
import org.biojava.nbio.core.sequence.DNASequence;
import org.biojava.nbio.core.sequence.ProteinSequence;
import org.biojava.nbio.core.sequence.compound.AminoAcidCompound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.function.BiFunction;

public class CreateAaAlignment implements BiFunction<String, String, Map.Entry<String, String>> {

  private static final SubstitutionMatrix<AminoAcidCompound> matrix = SubstitutionMatrixHelper.getBlosum80();
  private final Logger logger = LoggerFactory.getLogger(CreateAaAlignment.class);

  @Override
  public Map.Entry<String, String> apply(final String referenceAlignment, final String queryAlignment) {

    // Remove indels, remove incomplete codons from the end, translate and pairwise align
//    final var translateProcessBuilder = new ProcessBuilder("goalign", "translate", "--unaligned");

    final ProteinSequence transCleanRef;
    final ProteinSequence transCleanQuery;

    try {
      transCleanRef = new DNASequence(cleanSequence(referenceAlignment)).getRNASequence().getProteinSequence();
      transCleanQuery = new DNASequence(cleanSequence(queryAlignment)).getRNASequence().getProteinSequence();
    } catch (final CompoundNotFoundException e) {
      throw new RuntimeException(e);
    }

    this.logger.info("Aligning:\n{}\n{}\n", transCleanRef.getSequenceAsString(), transCleanQuery.getSequenceAsString());
    final var nwAligner = Alignments.getPairwiseAligner(transCleanQuery, transCleanRef, Alignments.PairwiseSequenceAlignerType.GLOBAL, new SimpleGapPenalty(), matrix);

    final var pair = nwAligner.getPair();

    return new ImmutablePair<>(pair.getTarget().getSequenceAsString(), pair.getQuery().getSequenceAsString());
  }

  /**
   * Removes indels and trims incomplete codon from the end of the sequence.
   *
   * @param sequence to be cleaned
   * @return Codon-complete and indel free String.
   */
  public static String cleanSequence(final String sequence) {
    final var noIndels = sequence.replace("-", "");
    return noIndels.substring(0, noIndels.length() - noIndels.length() % 3);
  }
}