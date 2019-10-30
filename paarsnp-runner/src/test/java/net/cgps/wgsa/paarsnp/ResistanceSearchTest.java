package net.cgps.wgsa.paarsnp;

import net.cgps.wgsa.paarsnp.core.lib.AbstractJsonnable;
import net.cgps.wgsa.paarsnp.core.lib.FilterByIndividualThresholds;
import net.cgps.wgsa.paarsnp.core.models.PaarsnpLibrary;
import net.cgps.wgsa.paarsnp.core.models.results.SearchResult;
import net.cgps.wgsa.paarsnp.core.snpar.CombineResults;
import net.cgps.wgsa.paarsnp.core.snpar.ProcessMatches;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ResistanceSearchTest {

  private final Logger logger = LoggerFactory.getLogger(ResistanceSearchTest.class);

  @Test
  public void apply() {

    final File paarsnpLibraryFile = Paths.get("../build/databases", "90370.jsn").toFile();

    this.logger.info("Using paarsnp DB {}", paarsnpLibraryFile.getPath());

    final PaarsnpLibrary paarsnpLibrary = AbstractJsonnable.fromJsonFile(paarsnpLibraryFile, PaarsnpLibrary.class);

    final List<String> inputOptions = Arrays.asList(
        "-db", paarsnpLibraryFile.getPath().replace(".jsn", "_paarsnp"),
        "-perc_identity", String.valueOf(paarsnpLibrary.getMinimumPid()),
        "-evalue", "1e-5"
    );

    final Path inputFasta = Paths.get("src/test/resources/8616_4#40.contigs_velvet.fa");

    final ResistanceSearch<SearchResult> resistanceSearch = new ResistanceSearch<>(
        inputOptions,
        new CombineResults(
            paarsnpLibrary.getSets().values(),
            new ProcessMatches(paarsnpLibrary)),
        FilterByIndividualThresholds.build(paarsnpLibrary));

    final SearchResult result = resistanceSearch.apply(inputFasta.toAbsolutePath().toString());

    assertNotNull(result, "Result produced");
    assertFalse(result.toJson().isEmpty(), "Result not empty");

    this.logger.info("{}", result.toJson());
  }
}
