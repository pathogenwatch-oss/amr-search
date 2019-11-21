package net.cgps.wgsa.paarsnp.core.snpar;

import net.cgps.wgsa.paarsnp.core.lib.blast.Mutation;
import net.cgps.wgsa.paarsnp.core.snpar.codonmapping.CreateFrameshiftFilter;
import net.cgps.wgsa.paarsnp.core.snpar.codonmapping.FrameshiftFilter;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.BitSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CreateFrameshiftFilterTest {

  @Test
  public void testDouble() {

    final BitSet expected = new BitSet(28);
    expected.set(7, 19);

    assertEquals(
        new FrameshiftFilter(expected),
        new CreateFrameshiftFilter(27)
            .apply(Arrays.asList(
                new Mutation(Mutation.MutationType.I, 6, "C", "-", 6),
                new Mutation(Mutation.MutationType.I, 20, "CC", "--", 18))));
  }

  @Test
  public void simpleShifts() {
//    final String referenceSequence = "ATCATC-ATCGGGATCGATCAT";
//    final String querySequence =     "ATCATCGATC---ATC-ATCAT";

    final BitSet expected = new BitSet(22);
    expected.set(7, 17);

    assertEquals(
        new FrameshiftFilter(expected),
        new CreateFrameshiftFilter(21).apply(
            Arrays.asList(
                new Mutation(Mutation.MutationType.I, 7, "G", "-", 6),
                new Mutation(Mutation.MutationType.D, 10, "-", "G", 10),
                new Mutation(Mutation.MutationType.D, 10, "-", "G", 11),
                new Mutation(Mutation.MutationType.D, 10, "-", "G", 12),
                new Mutation(Mutation.MutationType.D, 13, "-", "G", 16)
            )
        ));
  }

  @Test
  public void shiftedUntilEnd() {
    final String referenceSequence = "ATCATC-ATCGGGATCATCATCC";
    final String querySequence = "ATCATCGATC---ATCATCATCC";

    final BitSet expected = new BitSet(22);
    expected.set(7, 22); // 2 - 7

    assertEquals(
        new FrameshiftFilter(expected),
        new CreateFrameshiftFilter(21).apply(
            Arrays.asList(
                new Mutation(Mutation.MutationType.I, 7, "G", "-", 6),
                new Mutation(Mutation.MutationType.D, 10, "-", "G", 10),
                new Mutation(Mutation.MutationType.D, 10, "-", "G", 11),
                new Mutation(Mutation.MutationType.D, 10, "-", "G", 12)
            )
        ));
  }

  @Test
  public void multiframeShifts() {
//    final String referenceSequence = "ATCATC-ATCGGATCAT--CATCGATC";
//    final String querySequence     = "ATCATCGATC--ATCATGGCATC-ATC";

    final BitSet expected = new BitSet(25);
    expected.set(7, 22); // codons 3-7

    assertEquals(
        new FrameshiftFilter(expected),
        new CreateFrameshiftFilter(24).apply(
            Arrays.asList(
                new Mutation(Mutation.MutationType.I, 7, "G", "-", 6),
                new Mutation(Mutation.MutationType.D, 10, "-", "G", 10),
                new Mutation(Mutation.MutationType.D, 10, "-", "G", 11),
                new Mutation(Mutation.MutationType.I, 16, "GG", "--", 16),
                new Mutation(Mutation.MutationType.D, 17, "-", "G", 21)
            )
        ));
  }

  @Test
  public void longShifts() {
    final String referenceSequence = "ATCATC-----ATCGGGGATCAT---CATGCACAT";
    final String querySequence = "ATCATCGGGGGATC----ATCATGGGCAT-CACAT";

    final BitSet expected = new BitSet(28);
    expected.set(7, 23);

    assertEquals(
        new FrameshiftFilter(expected),
        new CreateFrameshiftFilter(24).apply(
            Arrays.asList(
                new Mutation(Mutation.MutationType.I, 7, "GGGGG", "------", 6),
                new Mutation(Mutation.MutationType.D, 15, "-", "G", 10),
                new Mutation(Mutation.MutationType.D, 15, "-", "G", 11),
                new Mutation(Mutation.MutationType.D, 15, "-", "G", 12),
                new Mutation(Mutation.MutationType.D, 15, "-", "G", 13),
                new Mutation(Mutation.MutationType.I, 21, "GGG", "---", 18),
                new Mutation(Mutation.MutationType.D, 26, "-", "G", 22)
            )
        ));
  }
}