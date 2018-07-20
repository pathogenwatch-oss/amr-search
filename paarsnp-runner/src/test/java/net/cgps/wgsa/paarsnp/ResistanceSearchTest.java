package net.cgps.wgsa.paarsnp;

import net.cgps.wgsa.paarsnp.core.Constants;
import net.cgps.wgsa.paarsnp.core.lib.json.AbstractJsonnable;
import net.cgps.wgsa.paarsnp.core.snpar.ProcessVariants;
import net.cgps.wgsa.paarsnp.core.snpar.SimpleBlastMatchFilter;
import net.cgps.wgsa.paarsnp.core.snpar.SnparCalculation;
import net.cgps.wgsa.paarsnp.core.snpar.json.SnparLibrary;
import net.cgps.wgsa.paarsnp.core.snpar.json.SnparResult;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;

public class ResistanceSearchTest {

  private final Logger logger = LoggerFactory.getLogger(ResistanceSearchTest.class);

  @org.junit.Test
  public void apply() throws Exception {

    final File snparDb = Paths.get("src/test/resources", "90370" + Constants.SNPAR_APPEND + Constants.JSON_APPEND).toFile();

    this.logger.info("Using snpar DB {}", snparDb.getPath());

    final SnparLibrary snparLibrary = AbstractJsonnable.fromJson(snparDb, SnparLibrary.class);

    final ResistanceSearch<SnparResult> resistanceSearch = new ResistanceSearch<>(new SnparCalculation(snparLibrary, new ProcessVariants(snparLibrary)), new SimpleBlastMatchFilter(60.0));

    final ResistanceSearch.InputOptions inputOptions = new ResistanceSearch.InputOptions(
        "test",
        Arrays.asList(
            "-query", Paths.get("src/test/resources/8616_4#40.contigs_velvet.fa").toAbsolutePath().toString(),
            "-db", snparDb.getPath(),
            "-perc_identity", String.valueOf(snparLibrary.getMinimumPid()),
            "-evalue", "1e-5"
        ),
        60.0f);

    final SnparResult result = resistanceSearch.apply(inputOptions);

    Assert.assertNotNull("Result produced", result);
    Assert.assertTrue("Result not empty", !result.toJson().isEmpty());

    this.logger.info("{}", result.toJson());
  }

}
