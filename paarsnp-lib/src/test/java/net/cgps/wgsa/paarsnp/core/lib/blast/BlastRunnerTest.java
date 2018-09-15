package net.cgps.wgsa.paarsnp.core.lib.blast;

import net.cgps.wgsa.paarsnp.core.lib.blast.ncbi.BlastOutput;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

public class BlastRunnerTest {

  private final Logger logger = LoggerFactory.getLogger(BlastRunnerTest.class);

  @Test
  public void snpTest() {

    final String[] command = new String[]{
        "-query", "../paarsnp-runner/src/test/resources/8616_4#40.contigs_velvet.fa",
        "-db", "../paarsnp-runner/src/test/resources/90370_snpar",
        "-perc_identity", "75.0",
        "-evalue", "1e-40"
    };

    final BlastRunner blastRunner = new BlastRunner();

    final BlastOutput apply = blastRunner.apply(command);

    final List<BlastMatch> mutationSearchResultList = new BlastReader().apply(apply).collect(Collectors.toList());

    Assert.assertTrue("Result exists", mutationSearchResultList.size() != 0);

    this.logger.info("Result size: {}", mutationSearchResultList.size());
  }
}
