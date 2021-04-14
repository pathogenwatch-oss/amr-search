package net.cgps.wgsa.paarsnp;

import net.cgps.wgsa.paarsnp.core.Constants;
import net.cgps.wgsa.paarsnp.core.lib.FilterByIndividualThresholds;
import net.cgps.wgsa.paarsnp.core.models.PaarsnpLibrary;
import net.cgps.wgsa.paarsnp.core.models.results.AntimicrobialAgent;
import net.cgps.wgsa.paarsnp.core.models.results.SearchResult;
import net.cgps.wgsa.paarsnp.core.snpar.CombineResults;
import net.cgps.wgsa.paarsnp.core.snpar.ProcessMatches;
import net.cgps.wgsa.paarsnp.output.ResultJson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PaarsnpRunner implements Function<Path, ResultJson> {

  private final Logger logger = LoggerFactory.getLogger(PaarsnpRunner.class);

  private final PaarsnpLibrary paarsnpLibrary;
  private final String resourceDirectory;

  PaarsnpRunner(final PaarsnpLibrary paarsnpLibrary, final String resourceDirectory) {
    this.paarsnpLibrary = paarsnpLibrary;
    this.resourceDirectory = resourceDirectory;
  }

  public ResultJson apply(final Path assemblyFile) {

    final String name = Optional.ofNullable(assemblyFile.getFileName()).orElseThrow(() -> new RuntimeException("Assembly file is null somehow.")).toString();

    final String assemblyId = name.substring(0, name.lastIndexOf('.'));

    this.logger.debug("Beginning {}", assemblyId);

    final SearchResult searchResult;
    if (!this.paarsnpLibrary.getSets().isEmpty()) {
      searchResult = new ResistanceSearch<>(
          this.buildBlastOptions(this.paarsnpLibrary.getMinimumPid()),
          new CombineResults(this.paarsnpLibrary.getSets().values(), new ProcessMatches(this.paarsnpLibrary)),
          FilterByIndividualThresholds.build(this.paarsnpLibrary)).apply(assemblyFile.toAbsolutePath().toString());
    } else {
      searchResult = SearchResult.buildEmpty();
    }

    final PaarsnpResultData paarsnpResultData = new PaarsnpResultData(this.paarsnpLibrary.getVersion(), assemblyId, searchResult, this.paarsnpLibrary.getAntimicrobials().stream().map(AntimicrobialAgent::getKey).collect(Collectors.toList()));

    final Map<String, AntimicrobialAgent> agentMap = this.paarsnpLibrary.getAntimicrobials().stream().collect(Collectors.toMap(AntimicrobialAgent::getKey, Function.identity()));

    return new BuildPaarsnpResult(agentMap).apply(paarsnpResultData);
  }

  private List<String> buildBlastOptions(final double minimumPid) {
    return Arrays.asList(
        "-db", Paths.get(this.resourceDirectory, this.paarsnpLibrary.getVersion().getLabel() + Constants.LIBRARY_APPEND).toAbsolutePath().toString(),
        "-perc_identity", String.valueOf(minimumPid),
        "-evalue", "1e-5"
    );
  }
}
