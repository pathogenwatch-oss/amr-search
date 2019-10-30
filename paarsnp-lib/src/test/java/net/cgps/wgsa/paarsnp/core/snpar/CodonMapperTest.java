package net.cgps.wgsa.paarsnp.core.snpar;

import net.cgps.wgsa.paarsnp.core.lib.blast.BlastMatch;
import net.cgps.wgsa.paarsnp.core.lib.blast.BlastSearchStatistics;
import net.cgps.wgsa.paarsnp.core.lib.utils.DnaSequence;
import net.cgps.wgsa.paarsnp.core.snpar.codonmapping.CodonMapper;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CodonMapperTest {

  @Test
  public void simpleInsert() {

    // MIYIYI
    // MIAYIYR

    final Map<Integer, Character> codons = new HashMap<>();
    codons.put(1, 'M');
    codons.put(2, 'I');
    codons.put(3, 'Y');
    codons.put(4, 'I');
    codons.put(5, 'Y');
    codons.put(6, 'R');

    assertEquals(
        new CodonMap(codons, Collections.singletonMap(2, "A")),
        new CodonMapper().apply(new BlastMatch(new BlastSearchStatistics(
            "libId",
            1,
            21, 24, "queryId",
            1,
            24, 0.0001, 99.0,
            DnaSequence.Strand.FORWARD
        ), "ATGATAGCGTATATATATAGATAG", "ATGATA---TATATATATATATAG")));
  }

  @Test
  public void offsetFrameshiftInsert() {

    final Map<Integer, Character> codons = new HashMap<>(7);
    codons.put(1, 'M');
    codons.put(2, 'I');
    codons.put(3, '!');
    codons.put(4, '!');
    codons.put(5, '!');
    codons.put(6, '!');

    assertEquals(
        new CodonMap(codons, Collections.singletonMap(2, "CA")),
        new CodonMapper().apply(new BlastMatch(new BlastSearchStatistics(
            "libId",
            1,
            21, 21, "queryId",
            1,
            25, 0.0001, 99.0,
            DnaSequence.Strand.FORWARD
        ),
            "ATGATATGCGCATATATATACATAG",
            "ATGATAT----ATATATATATATAG")));
  }

  @Test
  public void frameshiftInsert() {

    final Map<Integer, Character> codons = new HashMap<>(7);
    codons.put(1, 'M');
    codons.put(2, 'I');
    codons.put(3, '!');
    codons.put(4, '!');
    codons.put(5, '!');
    codons.put(6, '!');

    assertEquals(
        new CodonMap(codons, Collections.singletonMap(2, "V")),
        new CodonMapper().apply(new BlastMatch(new BlastSearchStatistics(
            "libId",
            1,
            21, 21, "queryId",
            1,
            22, 0.0001, 99.0,
            DnaSequence.Strand.FORWARD
        ),
            "ATGATAGTATATATATAGATAG",
            "ATGATA-TATATATATATATAG"))
    );
  }

  @Test
  public void doubleFrameshift() {

    final var codons = new HashMap<Integer, Character>(7);
    codons.put(1, 'M');
    codons.put(2, 'D');
    codons.put(3, '!');
    codons.put(4, '!');
    codons.put(5, '!');
    codons.put(6, '!');
    codons.put(7, '!');
    codons.put(8, 'D');

    assertEquals(
        new CodonMap(codons, Collections.emptyMap()),
        new CodonMapper().apply(new BlastMatch(new BlastSearchStatistics(
            "libId",
            1,
            24, 21, "queryId",
            1,
            27, 0.0001, 99.0,
            DnaSequence.Strand.FORWARD
        ), "ATGGATCATGATGATGATGCCATGGAT", "ATGGAT-ATGATGATGATG--ATGGAT")));
  }

  @Test
  public void doubleFrameshiftTwo() {

    final var codons = new HashMap<Integer, Character>(7);
    codons.put(1, 'M');
    codons.put(2, 'M');
    codons.put(3, '!');
    codons.put(4, '!');
    codons.put(5, '!');
    codons.put(6, '!');
    codons.put(7, 'M');

    assertEquals(
        new CodonMap(codons, Collections.emptyMap()),
        new CodonMapper().apply(new BlastMatch(new BlastSearchStatistics(
            "libId",
            1,
            21, 21, "queryId",
            1,
            21, 0.0001, 99.0,
            DnaSequence.Strand.FORWARD
        ),
            "ATGATGCATGATGATGAT-ATG",
            "ATGATG-ATGATGATGATGATG")));
  }

  @Test
  public void prematureStopCodon() {

    final var codons = new HashMap<Integer, Character>();
    codons.put(1, 'M');
    codons.put(2, 'M');
    codons.put(3, 'M');
    codons.put(4, 'M');
    codons.put(5, '*');
    codons.put(6, 'Y');

    final var expectedCodonMap = new CodonMap(codons, Collections.emptyMap());

    assertEquals(expectedCodonMap, new CodonMapper().apply(new BlastMatch(new BlastSearchStatistics(
        "libId",
        1,
        21, 21, "queryId",
        1,
        21, 0.0001, 99.0,
        DnaSequence.Strand.FORWARD
    ),
        "ATGATGATGATGTAGTATTAG",
        "ATGATGATGATGTATTATTAG")));
  }
}