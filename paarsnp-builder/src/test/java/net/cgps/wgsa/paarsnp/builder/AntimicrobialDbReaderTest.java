package net.cgps.wgsa.paarsnp.builder;

import com.moandjiezana.toml.Toml;
import net.cgps.wgsa.paarsnp.core.models.results.AntimicrobialAgent;
import org.junit.Assert;
import org.junit.Test;

public class AntimicrobialDbReaderTest {

  @Test
  public void parseAntimicrobialAgent() {
    final Toml agentToml = new Toml().read("antimicrobials = [{key = \"KAN\", type = \"Aminoglycosides\", name = \"Kanamycin\"}]")
        .getTables("antimicrobials").get(0);
    final AntimicrobialAgent agent = new AntimicrobialAgent("KAN", "Aminoglycosides", "Kanamycin");
    Assert.assertEquals(agent, AntimicrobialDbReader.parseAntimicrobialAgent().apply(agentToml));
  }

}