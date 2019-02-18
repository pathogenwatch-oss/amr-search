package net.cgps.wgsa.paarsnp;

import net.cgps.wgsa.paarsnp.core.Constants;
import net.cgps.wgsa.paarsnp.core.lib.FilterByIndividualThresholds;
import net.cgps.wgsa.paarsnp.core.models.Mechanisms;
import net.cgps.wgsa.paarsnp.core.models.results.AntimicrobialAgent;
import net.cgps.wgsa.paarsnp.core.models.results.SearchResult;
import net.cgps.wgsa.paarsnp.core.models.Paar;
import net.cgps.wgsa.paarsnp.core.snpar.ProcessMatches;
import net.cgps.wgsa.paarsnp.core.snpar.CombineResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PaarsnpRunner implements Function<Path, PaarsnpResult> {

  private final Logger logger = LoggerFactory.getLogger(PaarsnpRunner.class);

  private final String speciesId;
  private final Paar paarLibrary;
  private final Mechanisms mechanismsLibrary;
  private final Collection<AntimicrobialAgent> antimicrobialAgents;
  private final String resourceDirectory;

  PaarsnpRunner(final String speciesId, final Paar paarLibrary, final Mechanisms mechanismsLibrary, final Collection<AntimicrobialAgent> antimicrobialAgents, String resourceDirectory) {

    this.speciesId = speciesId;
    this.paarLibrary = paarLibrary;
    this.mechanismsLibrary = mechanismsLibrary;
    this.antimicrobialAgents = antimicrobialAgents;
    this.resourceDirectory = resourceDirectory;
  }

  public PaarsnpResult apply(final Path assemblyFile) {

    final String name = Optional.ofNullable(assemblyFile.getFileName()).orElseThrow(() -> new RuntimeException("Assembly file is null somehow.")).toString();

    final String assemblyId = name.substring(0, name.lastIndexOf('.'));

    this.logger.debug("Beginning {}", assemblyId);

//    final PaarResult paarResult;
//    if (!this.paarLibrary.getSets().isEmpty()) {
//      paarResult = new ResistanceSearch<>(
//          new ResistanceSearch.InputOptions(
//              this.buildBlastOptions(this.paarLibrary.getMinimumPid(), "1e-5", Constants.PAAR_APPEND)),
//          new PaarCalculation(this.paarLibrary),
//          FilterByIndividualThresholds.build(this.paarLibrary)).apply(assemblyFile.toAbsolutePath().toString()
//      );
//    } else {
//      paarResult = PaarResult.buildEmpty();
//    }

    final SearchResult searchResult;
    if (!this.mechanismsLibrary.getSets().isEmpty()) {
      searchResult = new ResistanceSearch<>(
          new ResistanceSearch.InputOptions(
              this.buildBlastOptions(this.mechanismsLibrary.getMinimumPid(), "1e-20", Constants.SNPAR_APPEND)),
          new CombineResults(this.mechanismsLibrary, new ProcessMatches(this.mechanismsLibrary)),
          FilterByIndividualThresholds.build(this.mechanismsLibrary)).apply(assemblyFile.toAbsolutePath().toString());
    } else {
      searchResult = SearchResult.buildEmpty();
    }

    final BuildPaarsnpResult.PaarsnpResultData paarsnpResultData = new BuildPaarsnpResult.PaarsnpResultData(assemblyId, searchResult, this.antimicrobialAgents.stream().map(AntimicrobialAgent::getKey).collect(Collectors.toList()));

    final Map<String, AntimicrobialAgent> agentMap = this.antimicrobialAgents.stream().collect(Collectors.toMap(AntimicrobialAgent::getKey, Function.identity()));

    return new BuildPaarsnpResult(agentMap, Stream.concat(
        this.paarLibrary.getSets().entrySet().stream(),
        this.mechanismsLibrary.getSets().entrySet().stream())
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)))
        .apply(paarsnpResultData);
  }

  private List<String> buildBlastOptions(final double minimumPid, final String evalue, final String libraryExtension) {
    return Arrays.asList(
        "-db", Paths.get(this.resourceDirectory, this.speciesId + libraryExtension).toAbsolutePath().toString(),
        "-perc_identity", String.valueOf(minimumPid),
        "-evalue", evalue
    );
  }
}
