package net.cgps.wgsa.paarsnp.core.snpar;

import net.cgps.wgsa.paarsnp.core.lib.blast.BlastMatch;
import net.cgps.wgsa.paarsnp.core.lib.blast.BlastSearchStatistics;
import net.cgps.wgsa.paarsnp.core.lib.utils.DnaSequence;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CodonMapperTest {

  @Test
  public void apply() {

    final String referenceSequence = "ATGATA---TATATATATATATAG";
    final String querySequence     = "ATGATAGCGTATATATATAGATAG";

    final BlastSearchStatistics statistics = new BlastSearchStatistics(
        "libId",
        1,
        21, 24, "queryId",
        1,
        24, 0.0001, 99.0,
        DnaSequence.Strand.FORWARD
    );

    final BlastMatch blastMatch = new BlastMatch(statistics, querySequence, referenceSequence);

    final CodonMap testMap = new CodonMapper().apply(blastMatch);

    final Map<Integer,String> codons = new HashMap<>(1);
    codons.put(1, "ATG");
    codons.put(2, "ATA");
    codons.put(3, "TAT");
    codons.put(4, "ATA");
    codons.put(5, "TAT");
    codons.put(6, "AGA");
    codons.put(7, "TAG");

    final CodonMap expectedCodonMap = new CodonMap(codons, Collections.singletonMap(2, "GCG"));

    Assert.assertEquals(testMap, expectedCodonMap);
  }
}