package net.cgps.wgsa.paarsnp;

import net.cgps.wgsa.paarsnp.core.Constants;
import net.cgps.wgsa.paarsnp.core.lib.FilterByIndividualThresholds;
import net.cgps.wgsa.paarsnp.core.lib.SimpleBlastMatchFilter;
import net.cgps.wgsa.paarsnp.core.lib.json.AntimicrobialAgent;
import net.cgps.wgsa.paarsnp.core.paar.PaarCalculation;
import net.cgps.wgsa.paarsnp.core.paar.PaarResult;
import net.cgps.wgsa.paarsnp.core.paar.json.Paar;
import net.cgps.wgsa.paarsnp.core.snpar.ProcessVariants;
import net.cgps.wgsa.paarsnp.core.snpar.SnparCalculation;
import net.cgps.wgsa.paarsnp.core.snpar.json.Snpar;
import net.cgps.wgsa.paarsnp.core.snpar.json.SnparResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PaarsnpRunner implements Function<Path, PaarsnpResult> {

  private final Logger logger = LoggerFactory.getLogger(PaarsnpRunner.class);

  private final String speciesId;
  private final Optional<Paar> paarLibrary;
  private final Optional<Snpar> snparLibrary;
  private final Collection<AntimicrobialAgent> antimicrobialAgents;
  private final String resourceDirectory;

  PaarsnpRunner(final String speciesId, final Optional<Paar> paarLibrary, final Optional<Snpar> snparLibrary, final Collection<AntimicrobialAgent> antimicrobialAgents, String resourceDirectory) {

    this.speciesId = speciesId;
    this.paarLibrary = paarLibrary;
    this.snparLibrary = snparLibrary;
    this.antimicrobialAgents = antimicrobialAgents;
    this.resourceDirectory = resourceDirectory;
  }

  public PaarsnpResult apply(final Path assemblyFile) {

    final String name = Optional.ofNullable(assemblyFile.getFileName()).orElseThrow(() -> new RuntimeException("Assembly file is null somehow.")).toString();

    final String assemblyId = name.substring(0, name.lastIndexOf('.'));

    this.logger.debug("Beginning {}", assemblyId);

    final PaarResult paarResult;
    if (this.paarLibrary.isPresent()) {
      paarResult = new ResistanceSearch<>(new ResistanceSearch.InputOptions(this.buildBlastOptions(this.paarLibrary.get().getMinimumPid(), "1e-5", Constants.PAAR_APPEND)), new PaarCalculation(this.paarLibrary.get()), FilterByIndividualThresholds.build(this.paarLibrary.get())).apply(assemblyFile.toAbsolutePath().toString());

    } else {
      paarResult = PaarResult.buildEmpty();
    }

    final SnparResult snparResult;
    if (this.snparLibrary.isPresent()) {
      snparResult = new ResistanceSearch<>(new ResistanceSearch.InputOptions(
          this.buildBlastOptions(this.snparLibrary.get().getMinimumPid(), "1e-40", Constants.SNPAR_APPEND)
      ), new SnparCalculation(this.snparLibrary.get(), new ProcessVariants(this.snparLibrary.get())), new SimpleBlastMatchFilter(60.0)).apply(assemblyFile.toAbsolutePath().toString());
    } else {
      snparResult = SnparResult.buildEmpty();
    }

    final BuildPaarsnpResult.PaarsnpResultData paarsnpResultData = new BuildPaarsnpResult.PaarsnpResultData(assemblyId, snparResult, paarResult, this.antimicrobialAgents.stream().map(AntimicrobialAgent::getKey).collect(Collectors.toList()));

    final Map<String, AntimicrobialAgent> agentMap = this.antimicrobialAgents.stream().collect(Collectors.toMap(AntimicrobialAgent::getKey, Function.identity()));

    return new BuildPaarsnpResult(agentMap).apply(paarsnpResultData);
  }

  private List<String> buildBlastOptions(final double minimumPid, final String evalue, final String libraryExtension) {
    return Arrays.asList(
        "-db", Paths.get(this.resourceDirectory, this.speciesId + libraryExtension).toAbsolutePath().toString(),
        "-perc_identity", String.valueOf(minimumPid),
        "-evalue", evalue
    );
  }
}
