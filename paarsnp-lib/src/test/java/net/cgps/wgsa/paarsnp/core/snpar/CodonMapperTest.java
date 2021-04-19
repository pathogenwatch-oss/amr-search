package net.cgps.wgsa.paarsnp.core.snpar;

import net.cgps.wgsa.paarsnp.core.lib.blast.BlastMatch;
import net.cgps.wgsa.paarsnp.core.lib.blast.BlastSearchStatistics;
import net.cgps.wgsa.paarsnp.core.lib.utils.DnaSequence;
import net.cgps.wgsa.paarsnp.core.snpar.codonmapping.CodonMapper;
import net.cgps.wgsa.paarsnp.core.snpar.codonmapping.FrameshiftFilter;
import org.junit.jupiter.api.Test;

import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CodonMapperTest {

  @Test
  public void simpleInsert() {

    // MIYIYI
    // MIAYIYR

    final var codons = new HashMap<Integer, Character>();
    codons.put(1, 'M');
    codons.put(2, 'I');
    codons.put(3, 'Y');
    codons.put(4, 'I');
    codons.put(5, 'Y');
    codons.put(6, 'R');

    final var queryMap = new HashMap<Integer, Integer>();
    queryMap.put(1, 1);
    queryMap.put(2, 2);
    queryMap.put(3, 4);
    queryMap.put(4, 5);
    queryMap.put(5, 6);
    queryMap.put(6, 7);

    assertEquals(
        new AaAlignment(codons, 1, queryMap, Collections.singletonMap(2, "A")),
        new CodonMapper(this.emptyFrameshift()).apply(new BlastMatch(new BlastSearchStatistics(
            "libId",
            1,
            21, 24, "queryId",
            1,
            24, 0.0001, 99.0,
            DnaSequence.Strand.FORWARD), "ATGATAGCGTATATATATAGATAG", "ATGATA---TATATATATATATAG")));
  }

  private FrameshiftFilter emptyFrameshift() {
    return new FrameshiftFilter(new BitSet());
  }

  @Test
  public void offsetFrameshiftInsert() {

    final var codons = new HashMap<Integer, Character>(7);
    codons.put(1, 'M');
    codons.put(2, 'I');
    codons.put(3, '!');
    codons.put(4, '!');
    codons.put(5, '!');
    codons.put(6, '!');

    final var queryLocationMap = new HashMap<Integer, Integer>(7);
    queryLocationMap.put(1, 1);
    queryLocationMap.put(2, 2);
    queryLocationMap.put(3, -1);
    queryLocationMap.put(4, -1);
    queryLocationMap.put(5, -1);
    queryLocationMap.put(6, -1);

    final var filter = new BitSet(22);
    filter.set(8, 22);

    assertEquals(
        new AaAlignment(codons, 1, queryLocationMap, Collections.singletonMap(2, "CA")),
        new CodonMapper(new FrameshiftFilter(filter)).apply(new BlastMatch(new BlastSearchStatistics(
            "libId",
            1,
            21, 21, "queryId",
            1,
            25, 0.0001, 99.0,
            DnaSequence.Strand.FORWARD),
            "ATGATATGCGCATATATATACATAG",
            "ATGATAT----ATATATATATATAG")));
  }

  @Test
  public void frameshiftInsert() {

    final var codons = new HashMap<Integer, Character>(7);
    codons.put(1, 'M');
    codons.put(2, 'I');
    codons.put(3, '!');
    codons.put(4, '!');
    codons.put(5, '!');
    codons.put(6, '!');

    final var queryLocationMap = new HashMap<Integer, Integer>(7);
    queryLocationMap.put(1, 1);
    queryLocationMap.put(2, 2);
    queryLocationMap.put(3, -1);
    queryLocationMap.put(4, -1);
    queryLocationMap.put(5, -1);
    queryLocationMap.put(6, -1);

    final var filter = new BitSet(22);
    filter.set(7, 22);

    assertEquals(
        new AaAlignment(codons, 1, queryLocationMap, Collections.singletonMap(2, "V")),
        new CodonMapper(new FrameshiftFilter(filter)).apply(new BlastMatch(new BlastSearchStatistics(
            "libId",
            1,
            21, 21, "queryId",
            1,
            22, 0.0001, 99.0,
            DnaSequence.Strand.FORWARD),
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
    codons.put(7, 'M');
    codons.put(8, 'D');

    final var queryLocationMap = new HashMap<Integer, Integer>(7);
    queryLocationMap.put(1, 1);
    queryLocationMap.put(2, 2);
    queryLocationMap.put(3, -1);
    queryLocationMap.put(4, -1);
    queryLocationMap.put(5, -1);
    queryLocationMap.put(6, -1);
    queryLocationMap.put(7, 8);
    queryLocationMap.put(8, 9);

    final var filter = new BitSet(25);
    filter.set(7, 19);

    assertEquals(
        new AaAlignment(codons, 1, queryLocationMap, Collections.singletonMap(5, "D")),
        new CodonMapper(new FrameshiftFilter(filter)).apply(new BlastMatch(new BlastSearchStatistics(
            "libId",
            1,
            24, 21, "queryId",
            1,
            27, 0.0001, 99.0,
            DnaSequence.Strand.FORWARD), "ATGGATCATGATGATGATGCCATGGAT", "ATGGAT-ATGATGATGATG--ATGGAT")));
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

    final var queryLocationMap = new HashMap<Integer, Integer>(7);
    queryLocationMap.put(1, 1);
    queryLocationMap.put(2, 2);
    queryLocationMap.put(3, -1);
    queryLocationMap.put(4, -1);
    queryLocationMap.put(5, -1);
    queryLocationMap.put(6, -1);
    queryLocationMap.put(7, 7);

    final var filter = new BitSet(22);
    filter.set(7, 18);

    assertEquals(
        new AaAlignment(codons, 4, queryLocationMap, Collections.emptyMap()),
        new CodonMapper(new FrameshiftFilter(filter)).apply(new BlastMatch(new BlastSearchStatistics(
            "libId",
            1,
            21, 21, "queryId",
            4,
            24, 0.0001, 99.0,
            DnaSequence.Strand.FORWARD),
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

    final var queryLocationMap = new HashMap<Integer, Integer>();
    queryLocationMap.put(1, 1);
    queryLocationMap.put(2, 2);
    queryLocationMap.put(3, 3);
    queryLocationMap.put(4, 4);
    queryLocationMap.put(5, 5);
    queryLocationMap.put(6, 6);

    final var expectedCodonMap = new AaAlignment(codons, 1, queryLocationMap, Collections.emptyMap());

    assertEquals(expectedCodonMap, new CodonMapper(this.emptyFrameshift()).apply(new BlastMatch(new BlastSearchStatistics(
        "libId",
        1,
        21, 21, "queryId",
        1,
        21, 0.0001, 99.0,
        DnaSequence.Strand.FORWARD),
        "ATGATGATGATGTAGTATTAG",
        "ATGATGATGATGTATTATTAG")));
  }
}