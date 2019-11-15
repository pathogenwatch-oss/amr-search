package net.cgps.wgsa.paarsnp.core.snpar;

import net.cgps.wgsa.paarsnp.core.snpar.codonmapping.CreateFrameshiftFilter;
import org.junit.jupiter.api.Test;

import java.util.BitSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CreateFrameshiftFilterTest {

  @Test
  public void simpleShifts() {
    final String referenceSequence = "ATCATC-ATCGGGATCGATCATC";
    final String querySequence = "ATCATCGATC---ATC-ATCATC";

    final BitSet expected = new BitSet(9);
    expected.set(3, 7);

    assertEquals(expected, new CreateFrameshiftFilter().apply(referenceSequence, querySequence));
  }

  @Test
  public void shiftedUntilEnd() {
    final String referenceSequence = "ATCATC-ATCGGGATCATCATC";
    final String querySequence = "ATCATCGATC---ATCATCATC";

    final BitSet expected = new BitSet(8);
    expected.set(3, 8); // 2 - 7

    assertEquals(expected, new CreateFrameshiftFilter().apply(referenceSequence, querySequence));
  }

  @Test
  public void multiframeShifts() {
    final String referenceSequence = "ATCATC-ATCGGATCAT--CATCGATC";
    final String querySequence = "ATCATCGATC--ATCATGGCATC-ATC";

    final BitSet expected = new BitSet(9);
    expected.set(3, 8); // codons 3-7

    assertEquals(expected, new CreateFrameshiftFilter().apply(referenceSequence, querySequence));
  }

  @Test
  public void longShifts() {
    final String referenceSequence = "ATCATC-----ATCGGGGATCAT---CATGCACAT";
    final String querySequence = "ATCATCGGGGGATC----ATCATGGGCAT-CACAT";

    final BitSet expected = new BitSet(10);
    expected.set(3, 9);

    assertEquals(expected, new CreateFrameshiftFilter().apply(referenceSequence, querySequence));
  }
}