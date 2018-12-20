package net.cgps.wgsa.paarsnp;

import net.cgps.wgsa.paarsnp.core.lib.AbstractJsonnable;
import net.cgps.wgsa.paarsnp.core.lib.FilterByIndividualThresholds;
import net.cgps.wgsa.paarsnp.core.models.PaarsnpLibrary;
import net.cgps.wgsa.paarsnp.core.models.results.SnparResult;
import net.cgps.wgsa.paarsnp.core.snpar.ProcessVariants;
import net.cgps.wgsa.paarsnp.core.snpar.ResultCombiner;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class ResistanceSearchTest {

  private final Logger logger = LoggerFactory.getLogger(ResistanceSearchTest.class);

  @Test
  public void apply() {


    final File paarsnpLibraryFile = Paths.get("../build/databases", "90370.jsn").toFile();

    this.logger.info("Using snpar DB {}", paarsnpLibraryFile.getPath());

    final PaarsnpLibrary paarsnpLibrary = AbstractJsonnable.fromJsonFile(paarsnpLibraryFile, PaarsnpLibrary.class);

    final ResistanceSearch.InputOptions inputOptions = new ResistanceSearch.InputOptions(
        Arrays.asList(
            "-db", paarsnpLibraryFile.getPath().replace(".jsn", "_snpar"),
            "-perc_identity", String.valueOf(paarsnpLibrary.getSnpar().getMinimumPid()),
            "-evalue", "1e-5"
        )
    );
    final Path inputFasta = Paths.get("src/test/resources/8616_4#40.contigs_velvet.fa");

    final ResistanceSearch<SnparResult> resistanceSearch = new ResistanceSearch<>(
        inputOptions,
        new ResultCombiner(
            paarsnpLibrary.getSnpar(),
            new ProcessVariants(paarsnpLibrary.getSnpar())),
        FilterByIndividualThresholds.build(paarsnpLibrary.getSnpar()));

    final SnparResult result = resistanceSearch.apply(inputFasta.toAbsolutePath().toString());

    Assert.assertNotNull("Result produced", result);
    Assert.assertTrue("Result not empty", !result.toJson().isEmpty());

    this.logger.info("{}", result.toJson());
  }

}
