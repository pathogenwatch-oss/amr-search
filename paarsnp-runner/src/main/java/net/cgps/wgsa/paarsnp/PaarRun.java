package net.cgps.wgsa.paarsnp;

import net.cgps.wgsa.paarsnp.core.lib.InputData;
import net.cgps.wgsa.paarsnp.core.lib.OverlapRemover;
import net.cgps.wgsa.paarsnp.core.lib.blast.*;
import net.cgps.wgsa.paarsnp.core.paar.PaarCalculation;
import net.cgps.wgsa.paarsnp.core.paar.PaarLibrary;
import net.cgps.wgsa.paarsnp.core.paar.PaarResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PaarRun implements Function<InputData, PaarResult> {

  private static final int ALLOWED_OVERLAP = 50;

  private final Logger logger = LoggerFactory.getLogger(PaarRun.class);

  private final PaarLibrary paarLibrary;
  private final String resourceDirectory;

  PaarRun(final PaarLibrary paarSpeciesLibrary, String resourceDirectory) {

    this.paarLibrary = paarSpeciesLibrary;
    this.resourceDirectory = resourceDirectory;
  }

  public PaarResult apply(final InputData inputData) {

    this.logger.debug("Preparing PAAR request for {} ", inputData.getAssemblyId());

    final String[] blastOptions = new String[]{
        "-query", inputData.getSequenceFile().toAbsolutePath().toString(),
        "-db", Paths.get(resourceDirectory, this.paarLibrary.getSpeciesId() + "_paar").toAbsolutePath().toString(),
        "-perc_identity", String.valueOf(this.paarLibrary.getMinimumPid()),
        "-evalue", "1e-5",
    };

    // Removes matches according to length & similarity threshold criteria
    final PaarMatchFilter matchFilter = new PaarMatchFilter(this.paarLibrary);

    final Collection<BlastMatch> matches = new MutationReader().apply(new BlastRunner().apply(blastOptions))
        .filter(matchFilter)
        .collect(new OverlapRemover<>(ALLOWED_OVERLAP));

    return new PaarCalculation(this.paarLibrary).apply(matches);
  }
}
