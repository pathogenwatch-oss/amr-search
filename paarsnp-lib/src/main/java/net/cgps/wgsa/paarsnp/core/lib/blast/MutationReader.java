package net.cgps.wgsa.paarsnp.core.lib.blast;

import net.cgps.wgsa.paarsnp.core.lib.blast.ncbi.BlastOutput;
import net.cgps.wgsa.paarsnp.core.lib.utils.DnaSequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Handles a BLAST XML format stream and parses out mutations.
 */
public class MutationReader implements Function<BlastOutput, Stream<BlastMatch>> {

  private final Logger logger = LoggerFactory.getLogger(MutationReader.class);

  private static double calculatePid(final BigInteger hspIdentity, final BigInteger hspAlignLen) {

    return ((double) hspIdentity.intValue() / (double) hspAlignLen.intValue()) * 100;
  }

  /**
   * Returns a list of {@link BlastMatch} objects, keyed by query sequence ID.
   */
  public Stream<BlastMatch> apply(final BlastOutput blastOutput) {

    this.logger.debug("Mapping matches");

    // An "iteration" in blast speak is the result for a contig search (i.e. a single fasta record in a multi-fasta). So
    // a single sequence fasta will only have one iteration.
    return blastOutput.getBlastOutputIterations()
        .getIteration()
        .parallelStream()
        .flatMap(iteration -> iteration
            .getIterationHits()
            .getHit()
            .stream()
            .flatMap(hit -> hit
                .getHitHsps()
                .getHsp()
                .stream()
                .map(hsp -> {
                  // Check if the match is reversed
                  final DnaSequence.Strand strand = hsp.getHspHitFrom().intValue() < hsp.getHspHitTo().intValue() ? DnaSequence.Strand.FORWARD : DnaSequence.Strand.REVERSE;

                  final MutationBuilder mutationBuilder = new MutationBuilder();

                  // Extract the list of mutations
                  final SequenceProcessingResult sequenceProcessingResult = new SequenceProcessor(hsp.getHspHseq(), hsp.getHspHitFrom().intValue(), strand, hsp.getHspQseq(), hsp.getHspQueryFrom().intValue(), mutationBuilder).call();

                  final BlastSearchStatistics stats = new BlastSearchStatistics(
                      hit.getHitAccession(),
                      hsp.getHspHitFrom().intValue(),
                      iteration.getIterationQueryDef(),
                      hsp.getHspQueryFrom().intValue(),
                      calculatePid(hsp.getHspIdentity(), hsp.getHspAlignLen()),
                      hsp.getHspEvalue(),
                      strand,
                      hsp.getHspHitTo().intValue(),
                      hsp.getHspQueryTo().intValue(),
                      hit.getHitLen().intValue()
                  );
                  // Add the match w/ mutations to the collection.
                  return new BlastMatch(
                      stats,
                      hsp.getHspQseq(),
                      hsp.getHspHseq(),
                      sequenceProcessingResult.getMutations()
                  );
                })
            )
        );
  }
}
