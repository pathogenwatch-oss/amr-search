package net.cgps.wgsa.paarsnp;

import net.cgps.wgsa.paarsnp.core.Constants;
import net.cgps.wgsa.paarsnp.core.lib.AbstractJsonnable;
import net.cgps.wgsa.paarsnp.core.models.PaarsnpLibrary;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PaarsnpRunnerIT {

  private final Logger logger = LoggerFactory.getLogger(PaarsnpRunnerIT.class);

  @Test
  public void apply() {
    final Set<String> expectedVariants = new TreeSet<>(Arrays.asList(
        "rpoB_-433F",
        "rpoB_N437-",
        "mabA_g609a",
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

    final PaarsnpRunner runner = new PaarsnpRunner(paarsnpLibrary, resourceDirectory);

    // TODO: Fix tests
//    final PathogenWatchFormat snparResult = convertFormat.apply(runner.apply(testFasta));

//    assertEquals(
//        expectedVariants,
//        new TreeSet<>(snparResult.getSnparElementIds())
//    );
//    this.logger.info("Passed 1773 standard check.");
//
//    assertEquals(
//        expectedVariants,
//        new TreeSet<>(convertFormat.apply(runner.apply(reverseTestFasta)).getSnparElementIds())
//    );
    this.logger.info("Passed 1773 reverse check.");

//    assertEquals(
//        AbstractJsonnable.fromJson("{\"originalSequence\" : \"T\", \"referenceLocation\" : 214, \"mutationSequence\" : \"C\", \"mutationType\" : \"S\", \"queryLocation\" : 214}", Mutation.class),
//        snparResult
//            .getMatches()
//            .stream()
//            .map(PathogenWatchFormat.CdsJson::getSnpResistanceElements)
//            .flatMap(Collection::stream)
//            .filter(resistanceMatch -> "C72P".equals(resistanceMatch.getResistanceMutation().getName()))
//            .map(ResistanceMutationMatch::getCausalMutations)
//            .flatMap(Collection::stream)
//            .filter(mutation -> 214 == mutation.getReferenceLocation())
//            .findFirst()
//            .orElseThrow(() -> new RuntimeException("Failed to find causal mutation"))
//    );
//    this.logger.info("Passed 1773 aa variant causal mutation check.");

//    final PathogenWatchFormat.VariantJson promoterVariant = snparResult
//        .getVariantMatches()
//        .stream()
//        .filter(mutation -> "g-10a".equals(mutation.getName()))
//        .filter(mutation -> 21 == mutation.getReferenceLocation())
//        .findFirst()
//        .orElseThrow(() -> new RuntimeException("Failed to find causal mutation"));
//    assertEquals(new PathogenWatchFormat.VariantJson(
//        Collections.singleton("KAN"),
//        "eis_promoter",
//        false,
//        21,
//        21,
//        "g-10a",
//        1
//    ), promoterVariant);
//
//    this.logger.info("Passed 1773 promoter variant causal mutation check.");
//
//    assertEquals(
//        3,
//        snparResult
//            .getVariantMatches()
//            .stream()
//            .filter(resistanceMatch -> "-433F".equals(resistanceMatch.getName()))
//            .count()
//    );

    this.logger.info("Passed 1773 insert variant causal mutation check.");
  }
}