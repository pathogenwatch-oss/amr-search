package net.cgps.wgsa.paarsnp;

import net.cgps.wgsa.paarsnp.core.lib.json.AntimicrobialAgent;
import net.cgps.wgsa.paarsnp.core.paar.PaarCalculation;
import net.cgps.wgsa.paarsnp.core.paar.PaarLibrary;
import net.cgps.wgsa.paarsnp.core.paar.PaarResult;
import net.cgps.wgsa.paarsnp.core.snpar.ProcessVariants;
import net.cgps.wgsa.paarsnp.core.snpar.SimpleBlastMatchFilter;
import net.cgps.wgsa.paarsnp.core.snpar.SnparCalculation;
import net.cgps.wgsa.paarsnp.core.snpar.json.SnparLibrary;
import net.cgps.wgsa.paarsnp.core.snpar.json.SnparResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PaarsnpRunner implements Function<Path, PaarsnpResult> {

  private final Logger logger = LoggerFactory.getLogger(PaarsnpRunner.class);

  private final String speciesId;
  private final PaarLibrary paarLibrary;
  private final SnparLibrary snparLibrary;
  private final Collection<AntimicrobialAgent> antimicrobialAgents;
  private final String resourceDirectory;
  private final ExecutorService executorService;

  PaarsnpRunner(final String speciesId, final PaarLibrary paarLibrary, final SnparLibrary snparLibrary, final Collection<AntimicrobialAgent> antimicrobialAgents, String resourceDirectory, final ExecutorService executorService) {

    this.speciesId = speciesId;
    this.paarLibrary = paarLibrary;
    this.snparLibrary = snparLibrary;
    this.antimicrobialAgents = antimicrobialAgents;
    this.resourceDirectory = resourceDirectory;
    this.executorService = executorService;
  }

  public PaarsnpResult apply(final Path assemblyFile) {

    final String name = assemblyFile.getFileName().toString();
    final String assemblyId = name.substring(0, name.lastIndexOf('.'));

    this.logger.debug("Beginning {}", assemblyId);

    final ResistanceSearch.InputOptions snparInputOptions = new ResistanceSearch.InputOptions(
        assemblyId,
        this.buildBlastOptions(assemblyFile, this.snparLibrary.getMinimumPid(), "1e-40"),
        60.0f);

    final ResistanceSearch.InputOptions paarInputOptions = new ResistanceSearch.InputOptions(
        assemblyId,
        this.buildBlastOptions(assemblyFile, this.paarLibrary.getMinimumPid(), "1e-5"),
        60.0f);

    // Run these concurrently, because, why not.
    final Future<PaarResult> paarResultFuture = this.executorService.submit(() -> new ResistanceSearch<>(new PaarCalculation(this.paarLibrary), new TwoStageBlastMatchFilter(60.0)).apply(paarInputOptions));
    final Future<SnparResult> snparResultFuture = this.executorService.submit(() -> new ResistanceSearch<>(new SnparCalculation(this.snparLibrary, new ProcessVariants(this.snparLibrary)), new SimpleBlastMatchFilter(60.0)).apply(snparInputOptions));

    final SnparResult snparResult;
    final PaarResult paarResult;
    try {
      snparResult = snparResultFuture.get();
      paarResult = paarResultFuture.get();
    } catch (final InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }

    final BuildPaarsnpResult.PaarsnpResultData paarsnpResultData = new BuildPaarsnpResult.PaarsnpResultData(assemblyId, snparResult, paarResult, this.antimicrobialAgents.stream().map(AntimicrobialAgent::getName).collect(Collectors.toList()));

    final Map<String, AntimicrobialAgent> agentMap = this.antimicrobialAgents.stream().collect(Collectors.toMap(AntimicrobialAgent::getName, Function.identity()));

    return new BuildPaarsnpResult(agentMap).apply(paarsnpResultData);
  }

  private List<String> buildBlastOptions(final Path assemblyFile, final double minimumPid, final String evalue) {
    return Arrays.asList(
        "-query", assemblyFile.toAbsolutePath().toString(),
        "-db", Paths.get(this.resourceDirectory, this.speciesId + "_snpar").toAbsolutePath().toString(),
        "-perc_identity", String.valueOf(minimumPid),
        "-evalue", evalue
    );
  }
}
