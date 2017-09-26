package net.cgps.wgsa.paarsnp;

import net.cgps.wgsa.paarsnp.core.lib.InputData;
import net.cgps.wgsa.paarsnp.core.lib.blast.BlastRunner;
import net.cgps.wgsa.paarsnp.core.lib.blast.MutationReader;
import net.cgps.wgsa.paarsnp.core.snpar.SnparCalculation;
import net.cgps.wgsa.paarsnp.core.snpar.json.SnparLibrary;
import net.cgps.wgsa.paarsnp.core.snpar.json.SnparResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.function.Function;

public class SnparRun implements Function<InputData, SnparResult> {

  public static final String OUTPUT_FORMAT = "5";
  private static final short MAX_NUM_MATCHES = 500;
  private final Logger logger = LoggerFactory.getLogger(SnparRun.class);
  private final SnparLibrary snpLibrary;
  private final String resourceDirectory;

  public SnparRun(final SnparLibrary snpLibrary, String resourceDirectory) {

    this.snpLibrary = snpLibrary;
    this.resourceDirectory = resourceDirectory;
  }

  @Override
  public SnparResult apply(final InputData inputData) {

    this.logger.debug("Preparing SNPAR request for {} ", inputData.getAssemblyId());

    final String[] command = new String[]{
        "blastn",
        "-task", "blastn",
        "-outfmt", OUTPUT_FORMAT,
        "-query", inputData.getSequenceFile().toAbsolutePath().toString(),
        "-db", Paths.get(resourceDirectory, this.snpLibrary.getSpeciesId() + "_snpar").toAbsolutePath().toString(),
        "-perc_identity", String.valueOf(this.snpLibrary.getMinimumPid()),
        "-evalue", "1e-40",
        "-num_alignments", String.valueOf(MAX_NUM_MATCHES),
    };

    if (0 == snpLibrary.getSequences().size()) {
      return new SnparResult(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
    }

    return new SnparCalculation(this.snpLibrary).apply(new BlastRunner<>(new MutationReader()).apply(command));
  }
}
