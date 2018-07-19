package net.cgps.wgsa.paarsnp;

import net.cgps.wgsa.paarsnp.core.lib.InputData;
import net.cgps.wgsa.paarsnp.core.lib.OverlapRemover;
import net.cgps.wgsa.paarsnp.core.lib.blast.BlastRunner;
import net.cgps.wgsa.paarsnp.core.lib.blast.MutationReader;
import net.cgps.wgsa.paarsnp.core.lib.blast.MutationSearchMatch;
import net.cgps.wgsa.paarsnp.core.snpar.ProcessVariants;
import net.cgps.wgsa.paarsnp.core.snpar.SnparCalculation;
import net.cgps.wgsa.paarsnp.core.snpar.SnparReferenceSequence;
import net.cgps.wgsa.paarsnp.core.snpar.json.SnparLibrary;
import net.cgps.wgsa.paarsnp.core.snpar.json.SnparResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Function;

public class SnparRun implements Function<InputData, SnparResult> {

  private final Logger logger = LoggerFactory.getLogger(SnparRun.class);
  private final SnparLibrary snparLibrary;
  private final String resourceDirectory;
  private final BlastRunner blastRunner;
  private final MutationReader mutationReader;
  private final SnparCalculation interpreter;
  private final OverlapRemover<MutationSearchMatch> matchOverlapRemover;
  private final ProcessVariants processVariants;

  SnparRun(final SnparLibrary snparLibrary, final String resourceDirectory) {

    this.snparLibrary = snparLibrary;
    this.resourceDirectory = resourceDirectory;
    this.blastRunner = new BlastRunner();
    this.mutationReader = new MutationReader();
    this.interpreter = new SnparCalculation(this.snparLibrary);
    this.matchOverlapRemover = new OverlapRemover<>(100);
    processVariants = new ProcessVariants(this.snparLibrary);
  }

  @Override
  public SnparResult apply(final InputData inputData) {

    this.logger.debug("Preparing SNPAR request for {} ", inputData.getAssemblyId());

    final String[] blastOptions = new String[]{
        "-query", inputData.getSequenceFile().toAbsolutePath().toString(),
        "-db", Paths.get(resourceDirectory, this.snparLibrary.getSpeciesId() + "_snpar").toAbsolutePath().toString(),
        "-perc_identity", String.valueOf(this.snparLibrary.getMinimumPid()),
        "-evalue", "1e-40",
    };

    if (0 == snparLibrary.getSequences().size()) {
      return new SnparResult(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
    }

    return mutationReader.apply(blastRunner.apply(blastOptions))
        .filter(match -> {
              // Error check, skipping matches without a reference in the library. Flash a warning.
              final Optional<SnparReferenceSequence> mutationReferenceSequence = this.snparLibrary.getSequence(match.getBlastSearchStatistics().getLibrarySequenceId());
              if (!mutationReferenceSequence.isPresent()) {
                this.logger.error("Sequence {} in PAARSNP BLAST library, but not in database.", match.getBlastSearchStatistics().getLibrarySequenceId());
              }
              return mutationReferenceSequence.isPresent();
            }
        )
        .filter(match -> {
              final double coverage = (((double) match.getBlastSearchStatistics().getLibrarySequenceStop() - match.getBlastSearchStatistics().getLibrarySequenceStart() + 1) / (double) match.getBlastSearchStatistics().getLibrarySequenceLength()) * 100;
              return coverage > 60;
            }
        )
        .collect(this.matchOverlapRemover)
        .stream()
        .map(this.processVariants)
        .collect(this.interpreter);
  }
}
