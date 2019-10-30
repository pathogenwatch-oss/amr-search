package net.cgps.wgsa.paarsnp.core.snpar;

import net.cgps.wgsa.paarsnp.core.lib.blast.BlastMatch;
import net.cgps.wgsa.paarsnp.core.lib.blast.BlastSearchStatistics;
import net.cgps.wgsa.paarsnp.core.lib.utils.DnaSequence;
import net.cgps.wgsa.paarsnp.core.snpar.codonmapping.CodonMapper;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CodonMapperTest {

  @Test
  public void simpleInsert() {

    final String referenceSequence = "ATGATA---TATATATATATATAG";
    final String querySequence = "ATGATAGCGTATATATATAGATAG";
    // MIYIYI
    // MIAYIYR

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

    final Map<Integer, String> codons = new HashMap<>();
    codons.put(1, "M");
    codons.put(2, "I");
    codons.put(3, "Y");
    codons.put(4, "I");
    codons.put(5, "Y");
    codons.put(6, "R");

    final CodonMap expectedCodonMap = new CodonMap(codons, Collections.singletonMap(2, "A"));

    Assert.assertEquals(expectedCodonMap, testMap);
  }

  @Test
  public void offsetFrameshiftInsert() {

    final BlastMatch blastMatch = new BlastMatch(new BlastSearchStatistics(
        "libId",
        1,
        21, 21, "queryId",
        1,
        25, 0.0001, 99.0,
        DnaSequence.Strand.FORWARD
    ),
        "ATGATATGCGCATATATATACATAG",
        "ATGATAT----ATATATATATATAG");

    final CodonMap testMap = new CodonMapper().apply(blastMatch);

    final Map<Integer, String> codons = new HashMap<>(7);
    codons.put(1, "M");
    codons.put(2, "I");
    codons.put(3, "!");
    codons.put(4, "!");
    codons.put(5, "!");
    codons.put(6, "!");
    codons.put(7, "!");

    final CodonMap expectedCodonMap = new CodonMap(codons, Collections.singletonMap(2, "CA"));

    Assert.assertEquals(expectedCodonMap, testMap);
  }

  @Test
  public void frameshiftInsert() {

    final String referenceSequence = "ATGATA-TATATATATATATAG";
    final String querySequence = "ATGATAGTATATATATAGATAG";

    final BlastSearchStatistics statistics = new BlastSearchStatistics(
        "libId",
        1,
        21, 21, "queryId",
        1,
        22, 0.0001, 99.0,
        DnaSequence.Strand.FORWARD
    );

    final BlastMatch blastMatch = new BlastMatch(statistics, querySequence, referenceSequence);

    final CodonMap testMap = new CodonMapper().apply(blastMatch);

    final Map<Integer, String> codons = new HashMap<>(7);
    codons.put(1, "ATG");
    codons.put(2, "ATA");
    codons.put(3, "!!!");
    codons.put(4, "!!!");
    codons.put(5, "!!!");
    codons.put(6, "!!!");
    codons.put(7, "!!!");

    final CodonMap expectedCodonMap = new CodonMap(codons, Collections.singletonMap(2, "G"));

    Assert.assertEquals(testMap, expectedCodonMap);
  }

  @Test
  public void doubleFrameshift() {

    final var referenceSequence = "ATGATG-ATGATGATGATG--ATG";
    final var querySequence = "ATGATGCATGATGATGATGCCATG";

    final var statistics = new BlastSearchStatistics(
        "libId",
        1,
        21, 21, "queryId",
        1,
        24, 0.0001, 99.0,
        DnaSequence.Strand.FORWARD
    );

    final var blastMatch = new BlastMatch(statistics, querySequence, referenceSequence);

    final var testMap = new CodonMapper().apply(blastMatch);

    final var codons = new HashMap<Integer, String>(7);
    codons.put(1, "ATG");
    codons.put(2, "ATG");
    codons.put(3, "!!!");
    codons.put(4, "!!!");
    codons.put(5, "!!!");
    codons.put(6, "!!!");
    codons.put(7, "ATG");

    final var inserts = new HashMap<Integer, String>(2);
    inserts.put(2, "C");
    inserts.put(6, "CC");

    final var expectedCodonMap = new CodonMap(codons, inserts);

    Assert.assertEquals(testMap, expectedCodonMap);
  }

  @Test
  public void doubleFrameshiftTwo() {

    final var referenceSequence = "ATGATG-ATGATGATGATGATG";
    final var querySequence = "ATGATGCATGATGATGAT-ATG";

    final var statistics = new BlastSearchStatistics(
        "libId",
        1,
        21, 21, "queryId",
        1,
        21, 0.0001, 99.0,
        DnaSequence.Strand.FORWARD
    );

    final var blastMatch = new BlastMatch(statistics, querySequence, referenceSequence);

    final var testMap = new CodonMapper().apply(blastMatch);

    final var codons = new HashMap<Integer, String>(7);
    codons.put(1, "ATG");
    codons.put(2, "ATG");
    codons.put(3, "!!!");
    codons.put(4, "!!!");
    codons.put(5, "!!!");
    codons.put(6, "!!!");
    codons.put(7, "ATG");

    final var inserts = new HashMap<Integer, String>(2);
    inserts.put(2, "C");

    final var expectedCodonMap = new CodonMap(codons, inserts);

    Assert.assertEquals(testMap, expectedCodonMap);
  }

}