package net.cgps.wgsa.paarsnp.core;

import net.cgps.wgsa.paarsnp.core.lib.OverlapRemover;
import net.cgps.wgsa.paarsnp.core.lib.blast.BlastMatch;
import net.cgps.wgsa.paarsnp.core.lib.blast.BlastRunner;
import net.cgps.wgsa.paarsnp.core.lib.blast.MatchReader;
import net.cgps.wgsa.paarsnp.core.lib.blast.PaarMatchFilter;
import net.cgps.wgsa.paarsnp.core.paar.PaarLibrary;
import net.cgps.wgsa.paarsnp.core.paar.PaarResult;
import net.cgps.wgsa.paarsnp.core.paar.PaarResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class PaarRun implements Function<InputData, PaarResult> {

  private static final int ALLOWED_OVERLAP = 50;
  private static final short MAX_NUM_MATCHES = 500;

  public static final String OUTPUT_FORMAT = "7 qseqid sseqid qlen slen pident length mismatch gapopen qstart qend sstart send evalue bitscore sstrand";
  private final Logger logger = LoggerFactory.getLogger(PaarRun.class);

  private final PaarLibrary paarLibrary;
  private final String resourceDirectory;

  public PaarRun(final PaarLibrary paarSpeciesLibrary, String resourceDirectory) {

    this.paarLibrary = paarSpeciesLibrary;
    this.resourceDirectory = resourceDirectory;
  }

  public PaarResult apply(final InputData inputData) {

    this.logger.info("Preparing PAAR request for {} ", inputData.getAssemblyId());

    final String[] command = new String[]{
        "blastn",
        "-task", "blastn",
        "-outfmt", OUTPUT_FORMAT,
        "-query", inputData.getSequenceFile(),
        "-db", resourceDirectory + "paar_" + this.paarLibrary.getSpeciesId(),
        "-perc_identity", String.valueOf(this.paarLibrary.getMinimumPid()),
        "-evalue", "1e-5",
        "-num_alignments", String.valueOf(MAX_NUM_MATCHES),
        "-num_descriptions", String.valueOf(MAX_NUM_MATCHES)
    };

    // Removes matches according to length & similarity threshold criteria
    final Predicate<BlastMatch> matchFilter = new PaarMatchFilter(this.paarLibrary);

    // Also remove overlapping matches. The overlap remover would be more elegant as a collector.
    final Collection<BlastMatch> blastMatches = new OverlapRemover(ALLOWED_OVERLAP).apply(
        // Run BLAST command with appropriate parser
        new BlastRunner<>(new MatchReader(matchFilter))
            .apply(command)
            .collect(Collectors.toList()));

    this.logger.info("PAAR BLAST response received for {}", inputData.getAssemblyId());

    try {
      return new PaarResultBuilder(this.paarLibrary).apply(blastMatches);
    } catch (final Exception e) {
      this.logger.error("PAAR calculation error", e);
      throw new RuntimeException(e);
    }
  }
}
