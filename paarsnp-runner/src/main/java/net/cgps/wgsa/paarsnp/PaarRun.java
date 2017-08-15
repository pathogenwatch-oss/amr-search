package net.cgps.wgsa.paarsnp;

import net.cgps.wgsa.paarsnp.core.lib.InputData;
import net.cgps.wgsa.paarsnp.core.lib.OverlapRemover;
import net.cgps.wgsa.paarsnp.core.lib.blast.BlastMatch;
import net.cgps.wgsa.paarsnp.core.lib.blast.BlastRunner;
import net.cgps.wgsa.paarsnp.core.lib.blast.MatchReader;
import net.cgps.wgsa.paarsnp.core.lib.blast.PaarMatchFilter;
import net.cgps.wgsa.paarsnp.core.paar.PaarCalculation;
import net.cgps.wgsa.paarsnp.core.paar.PaarLibrary;
import net.cgps.wgsa.paarsnp.core.paar.PaarResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class PaarRun implements Function<InputData, PaarResult> {

  private static final int ALLOWED_OVERLAP = 50;
  private static final short MAX_NUM_MATCHES = 500;

  public static final String OUTPUT_FORMAT = "7 qseqid sseqid qlen slen pident length mismatch gapopen qstart qend sstart send evalue bitscore sstrand";
  //public static final String OUTPUT_FORMAT = "7";
  private final Logger logger = LoggerFactory.getLogger(PaarRun.class);

  private final PaarLibrary paarLibrary;
  private final String resourceDirectory;

  public PaarRun(final PaarLibrary paarSpeciesLibrary, String resourceDirectory) {

    this.paarLibrary = paarSpeciesLibrary;
    this.resourceDirectory = resourceDirectory;
  }

  public PaarResult apply(final InputData inputData) {

    this.logger.debug("Preparing PAAR request for {} ", inputData.getAssemblyId());

    final String[] command = new String[]{
        "blastn",
        "-task", "blastn",
        "-outfmt", OUTPUT_FORMAT,
        "-query", inputData.getSequenceFile().toAbsolutePath().toString(),
        "-db", Paths.get(resourceDirectory, this.paarLibrary.getSpeciesId() + "_paar").toAbsolutePath().toString(),
        "-perc_identity", String.valueOf(this.paarLibrary.getMinimumPid()),
        "-evalue", "1e-5",
        "-max_target_seqs", String.valueOf(MAX_NUM_MATCHES)
    };

    // Removes matches according to length & similarity threshold criteria
    final Predicate<BlastMatch> matchFilter = new PaarMatchFilter(this.paarLibrary);

    return
        new PaarCalculation(this.paarLibrary).apply(
            new OverlapRemover(ALLOWED_OVERLAP).apply(
                new BlastRunner<>(
                    new MatchReader(matchFilter))
                    .apply(command)
                    .collect(Collectors.toList())
            )
        );
  }
}
