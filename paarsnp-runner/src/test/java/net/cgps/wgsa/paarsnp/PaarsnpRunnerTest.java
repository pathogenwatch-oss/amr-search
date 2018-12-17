package net.cgps.wgsa.paarsnp;

import net.cgps.wgsa.paarsnp.core.Constants;
import net.cgps.wgsa.paarsnp.core.lib.AbstractJsonnable;
import net.cgps.wgsa.paarsnp.core.lib.blast.Mutation;
import net.cgps.wgsa.paarsnp.core.models.PaarsnpLibrary;
import net.cgps.wgsa.paarsnp.core.models.ResistanceMutationMatch;
import net.cgps.wgsa.paarsnp.core.models.results.MatchJson;
import net.cgps.wgsa.paarsnp.core.models.results.OldStyleSnparResult;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

public class PaarsnpRunnerTest {

  private final Logger logger = LoggerFactory.getLogger(PaarsnpRunnerTest.class);

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

    final OldStyleSnparResult snparResult = convertFormat.apply(runner.apply(testFasta)).getSnparResult();

    Assert.assertEquals(
        expectedVariants,
        new TreeSet<>(snparResult.getResistanceMutationIds())
    );
    this.logger.info("Passed 1773 standard check.");

    Assert.assertEquals(
        expectedVariants,
        new TreeSet<>(convertFormat.apply(runner.apply(reverseTestFasta)).getSnparResult().getResistanceMutationIds())
    );
    this.logger.info("Passed 1773 reverse check.");

    Assert.assertEquals(
        AbstractJsonnable.fromJson("{\"originalSequence\" : \"T\", \"referenceLocation\" : 214, \"mutationSequence\" : \"C\", \"mutationType\" : \"S\", \"queryLocation\" : 214}", Mutation.class),
        snparResult
            .getBlastMatches()
            .stream()
            .map(MatchJson::getSnpResistanceElements)
            .flatMap(Collection::stream)
            .filter(resistanceMatch -> "C72P".equals(resistanceMatch.getResistanceMutation().getName()))
            .map(ResistanceMutationMatch::getCausalMutations)
            .flatMap(Collection::stream)
            .filter(mutation -> 214 == mutation.getReferenceLocation())
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Failed to find causal mutation"))
    );
    this.logger.info("Passed 1773 aa variant causal mutation check.");

    Assert.assertEquals(
        AbstractJsonnable.fromJson("{\"originalSequence\" : \"G\", \"referenceLocation\" : 21,\"mutationSequence\" : \"A\", \"mutationType\" : \"S\", \"queryLocation\" : 21}", Mutation.class),
        snparResult
            .getBlastMatches()
            .stream()
            .map(MatchJson::getSnpResistanceElements)
            .flatMap(Collection::stream)
            .filter(resistanceMatch -> "g-10a".equals(resistanceMatch.getResistanceMutation().getName()))
            .map(ResistanceMutationMatch::getCausalMutations)
            .flatMap(Collection::stream)
            .filter(mutation -> 21 == mutation.getReferenceLocation())
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Failed to find causal mutation"))
    );

    this.logger.info("Passed 1773 promoter variant causal mutation check.");

    Assert.assertEquals(
        3,
        snparResult
            .getBlastMatches()
            .stream()
            .map(MatchJson::getSnpResistanceElements)
            .flatMap(Collection::stream)
            .filter(resistanceMatch -> "-433F".equals(resistanceMatch.getResistanceMutation().getName()))
            .map(ResistanceMutationMatch::getCausalMutations)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("No insert mutation"))
            .size()
    );
  }
}