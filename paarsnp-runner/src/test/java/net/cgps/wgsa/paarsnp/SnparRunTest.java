package net.cgps.wgsa.paarsnp;

import net.cgps.wgsa.paarsnp.core.Constants;
import net.cgps.wgsa.paarsnp.core.lib.InputData;
import net.cgps.wgsa.paarsnp.core.lib.json.AbstractJsonnable;
import net.cgps.wgsa.paarsnp.core.snpar.json.SnparLibrary;
import net.cgps.wgsa.paarsnp.core.snpar.json.SnparResult;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Paths;

public class SnparRunTest {

  private final Logger logger = LoggerFactory.getLogger(SnparRunTest.class);

  @org.junit.Test
  public void apply() throws Exception {

    final File snparDb = Paths.get("src/test/resources", "90370" + Constants.SNPAR_APPEND + Constants.JSON_APPEND).toFile();

    this.logger.info("Using snpar DB {}", snparDb.getPath());

    final SnparLibrary snparLibrary = AbstractJsonnable.fromJson(snparDb,SnparLibrary.class);

    final SnparRun snparRun = new SnparRun(snparLibrary, "src/test/resources", blastRunner, mutationReader);

    final SnparResult result = snparRun.apply(new InputData("test", "90370", Paths.get("src/test/resources/8616_4#40.contigs_velvet.fa")));

    Assert.assertTrue("Result produced", null != result);
    Assert.assertTrue("Result not empty", !result.toJson().isEmpty());

    this.logger.info("{}", result.toJson());
  }

}
