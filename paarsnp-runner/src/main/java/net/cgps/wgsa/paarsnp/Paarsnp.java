package net.cgps.wgsa.paarsnp;

import net.cgps.wgsa.paarsnp.core.lib.InputData;
import net.cgps.wgsa.paarsnp.core.lib.json.AntimicrobialAgent;
import net.cgps.wgsa.paarsnp.core.paar.PaarLibrary;
import net.cgps.wgsa.paarsnp.core.paar.PaarResult;
import net.cgps.wgsa.paarsnp.core.snpar.json.SnparLibrary;
import net.cgps.wgsa.paarsnp.core.snpar.json.SnparResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Paarsnp implements Function<Path, PaarsnpResult> {

  private final Logger logger = LoggerFactory.getLogger(Paarsnp.class);

  private final String speciesId;
  private final PaarLibrary paarLibrary;
  private final SnparLibrary snparLibrary;
  private final Collection<AntimicrobialAgent> antimicrobialAgents;
  private final String resourceDirectory;
  private final ExecutorService executorService;

  public Paarsnp(final String speciesId, final PaarLibrary paarLibrary, final SnparLibrary snparLibrary, final Collection<AntimicrobialAgent> antimicrobialAgents, String resourceDirectory, final ExecutorService executorService) {

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

    final InputData inputData = new InputData(assemblyId, this.speciesId, assemblyFile);

    // Run these concurrently, because, why not.
    final Future<PaarResult> paarResultFuture = this.executorService.submit(() -> new PaarRun(this.paarLibrary, resourceDirectory).apply(inputData));
    final Future<SnparResult> snparResultFuture = this.executorService.submit(() -> new SnparRun(this.snparLibrary, resourceDirectory, blastRunner, mutationReader).apply(inputData));

    final SnparResult snparResult;
    final PaarResult paarResult;
    try {
      snparResult = snparResultFuture.get();
      paarResult = paarResultFuture.get();
    } catch (final InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }

    final BuildPaarsnpResult.PaarsnpResultData paarsnpResultData = new BuildPaarsnpResult.PaarsnpResultData(assemblyId, this.speciesId, snparResult, paarResult, this.antimicrobialAgents.stream().map(AntimicrobialAgent::getName).collect(Collectors.toList()));

    final Map<String, AntimicrobialAgent> agentMap = this.antimicrobialAgents.stream().collect(Collectors.toMap(AntimicrobialAgent::getName, Function.identity()));

    return new BuildPaarsnpResult(agentMap).apply(paarsnpResultData);
  }
}
