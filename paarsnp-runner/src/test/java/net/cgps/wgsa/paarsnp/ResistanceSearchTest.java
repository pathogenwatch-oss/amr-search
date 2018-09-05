package net.cgps.wgsa.paarsnp;

import net.cgps.wgsa.paarsnp.core.Constants;
import net.cgps.wgsa.paarsnp.core.lib.SimpleBlastMatchFilter;
import net.cgps.wgsa.paarsnp.core.lib.json.AbstractJsonnable;
import net.cgps.wgsa.paarsnp.core.snpar.ProcessVariants;
import net.cgps.wgsa.paarsnp.core.snpar.SnparCalculation;
import net.cgps.wgsa.paarsnp.core.snpar.json.Snpar;
import net.cgps.wgsa.paarsnp.core.snpar.json.SnparResult;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;

public class ResistanceSearchTest {

  private final Logger logger = LoggerFactory.getLogger(ResistanceSearchTest.class);

  @org.junit.Test
  public void apply() throws Exception {

    final File snparDb = Paths.get("src/test/resources", "90370" + Constants.SNPAR_APPEND + Constants.JSON_APPEND).toFile();

    this.logger.info("Using snpar DB {}", snparDb.getPath());

    final Optional<Snpar> snparLibrary = Optional.ofNullable(AbstractJsonnable.fromJsonFile(snparDb, Snpar.class));

    final ResistanceSearch.InputOptions inputOptions = new ResistanceSearch.InputOptions(
        Arrays.asList(
            "-db", snparDb.getPath().replace(Constants.JSON_APPEND, ""),
            "-perc_identity", String.valueOf(snparLibrary.get().getMinimumPid()),
            "-evalue", "1e-5"
        )
    );
    final ResistanceSearch<SnparResult> resistanceSearch = new ResistanceSearch<>(inputOptions, new SnparCalculation(snparLibrary.get(), new ProcessVariants(snparLibrary.get())), new SimpleBlastMatchFilter(60.0));


    final SnparResult result = resistanceSearch.apply(Paths.get("src/test/resources/8616_4#40.contigs_velvet.fa").toAbsolutePath().toString());

    Assert.assertNotNull("Result produced", result);
    Assert.assertTrue("Result not empty", !result.toJson().isEmpty());

    this.logger.info("{}", result.toJson());
  }

}
