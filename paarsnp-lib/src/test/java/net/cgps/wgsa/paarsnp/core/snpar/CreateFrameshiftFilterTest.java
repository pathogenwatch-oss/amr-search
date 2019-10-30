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

    final BitSet expected = new BitSet(23);
    expected.set(6, 17);

    assertEquals(expected, new CreateFrameshiftFilter().apply(referenceSequence, querySequence));
  }

  @Test
  public void shiftedUntilEnd() {
    final String referenceSequence = "ATCATC-ATCGGGATCATCATC";
    final String querySequence = "ATCATCGATC---ATCATCATC";

    final BitSet expected = new BitSet(22);
    expected.set(6, 22);

    assertEquals(expected, new CreateFrameshiftFilter().apply(referenceSequence, querySequence));
  }

  @Test
  public void multiframeShifts() {
    final String referenceSequence = "ATCATC-ATCGGATCAT--CATCGAT";
    final String querySequence = "ATCATCGATC--ATCATGGCATC-AT";

    final BitSet expected = new BitSet(26);
    expected.set(6, 24);

    assertEquals(expected, new CreateFrameshiftFilter().apply(referenceSequence, querySequence));
  }

  @Test
  public void longShifts() {
    final String referenceSequence = "ATCATC-----ATCGGGGATCAT---CATCGAT";
    final String querySequence = "ATCATCGGGGGATC----ATCATGGGCATC-AT";

    final BitSet expected = new BitSet(33);
    expected.set(6, 31);

    assertEquals(expected, new CreateFrameshiftFilter().apply(referenceSequence, querySequence));
  }
}
