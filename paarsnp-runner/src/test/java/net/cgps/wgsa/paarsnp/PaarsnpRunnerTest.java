package net.cgps.wgsa.paarsnp;

import net.cgps.wgsa.paarsnp.core.Constants;
import net.cgps.wgsa.paarsnp.core.lib.AbstractJsonnable;
import net.cgps.wgsa.paarsnp.core.models.PaarsnpLibrary;
import org.junit.Assert;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

public class PaarsnpRunnerTest {

  @Test
  public void apply() {
    final Set<String> expectedVariants = new TreeSet<>(Arrays.asList(
        "rpoB_-433F",
        "rpoB_N437-",
        "mabA_g609a", "katG_truncated",
        "katG_S315I",
        "eis_promoter_g-10a",
        "rrs_c1402t",
        "pncA_C72P",
        "pncA_truncated",
        "eis_promoter_c-12t",
        "katG_frameshift",
        "gyrA_D94N"
    ));

    final String speciesId = "1773";
    final String resourceDirectory = "../build/databases/";
    final Path testFasta = Paths.get("src/test/resources/1773_library_test.fasta");
    final Path reverseTestFasta = Paths.get("src/test/resources/1773_library_reverse_test.fasta");

    final PaarsnpLibrary paarsnpLibrary = AbstractJsonnable.fromJsonFile(Paths.get(resourceDirectory, speciesId + Constants.JSON_APPEND).toFile(), PaarsnpLibrary.class);

    final PaarsnpRunner runner = new PaarsnpRunner(speciesId, paarsnpLibrary.getPaar(), paarsnpLibrary.getSnpar(), paarsnpLibrary.getAntimicrobials(), resourceDirectory);
    final ConvertResultFormat convertFormat = new ConvertResultFormat();

    Assert.assertEquals(
        expectedVariants,
        new TreeSet<>(convertFormat.apply(runner.apply(testFasta)).getSnparResult().getResistanceMutationIds())
    );

    Assert.assertEquals(
        expectedVariants,
        new TreeSet<>(convertFormat.apply(runner.apply(reverseTestFasta)).getSnparResult().getResistanceMutationIds())
    );
  }
}