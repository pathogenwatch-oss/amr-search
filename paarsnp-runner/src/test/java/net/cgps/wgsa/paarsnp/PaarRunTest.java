package net.cgps.wgsa.paarsnp;

import net.cgps.wgsa.paarsnp.core.Constants;
import net.cgps.wgsa.paarsnp.core.lib.InputData;
import net.cgps.wgsa.paarsnp.core.lib.json.AbstractJsonnable;
import net.cgps.wgsa.paarsnp.core.paar.PaarLibrary;
import net.cgps.wgsa.paarsnp.core.paar.PaarResult;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Paths;

public class PaarRunTest {

  private final Logger logger = LoggerFactory.getLogger(PaarRunTest.class);

  @Test
  public void apply() throws Exception {

    final File paarDB = Paths.get("src/test/resources", "90370" + Constants.PAAR_APPEND + Constants.JSON_APPEND).toFile();

    this.logger.info("Using paar DB {}", paarDB.getPath());

    final PaarLibrary snparLibrary = AbstractJsonnable.fromJson(paarDB,PaarLibrary.class);

    final PaarRun snparRun = new PaarRun(snparLibrary, "src/test/resources");

    final PaarResult result = snparRun.apply(new InputData("test", "90370", Paths.get("src/test/resources/8616_4#40.contigs_velvet.fa")));

    Assert.assertTrue("Result produced", null != result);
    Assert.assertTrue("Result not empty", !result.toJson().isEmpty());

    this.logger.info("{}", result.toJson());
  }
}
