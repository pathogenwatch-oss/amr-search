package net.cgps.wgsa.paarsnp.core.lib.blast;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

public class BlastRunnerTest {

  private final Logger logger = LoggerFactory.getLogger(BlastRunnerTest.class);

  @Test
  public void snpTest() throws Exception {

    final String[] command = new String[]{
        "blastn",
        "-task", "blastn",
        "-outfmt", "5",
        "-query", "../paarsnp-runner/src/test/resources/8616_4#40.contigs_velvet.fa",
        "-db", "../paarsnp-runner/src/test/resources/90370_snpar",
        "-perc_identity", "75.0",
        "-evalue", "1e-40",
        "-num_alignments", "500",
    };

    final List<MutationSearchResult> mutationSearchResultList = new BlastRunner<>(new MutationReader()).apply(command).collect(Collectors.toList());

    Assert.assertTrue("Result exists", null != mutationSearchResultList && mutationSearchResultList.size() != 0);

    this.logger.info("Result size: {}", mutationSearchResultList.size());
  }

  @Test
  public void matchTest() throws Exception {

    final String[] command = new String[]{
        "blastn",
        "-task", "blastn",
        "-outfmt", "7 qseqid sseqid qlen slen pident length mismatch gapopen qstart qend sstart send evalue bitscore sstrand",
        "-query", "../paarsnp-runner/src/test/resources/8616_4#40.contigs_velvet.fa",
        "-db", "../paarsnp-runner/src/test/resources/90370_snpar",
        "-perc_identity", "75.0",
        "-evalue", "1e-5",
        "-max_target_seqs", "500"
    };

    final List<BlastMatch> matches = new BlastRunner<>(new MatchReader(blastMatch -> true)).apply(command).collect(Collectors.toList());

    Assert.assertTrue("Result exists", null != matches);

    this.logger.info("Result size: {}", matches.size());
  }

}
