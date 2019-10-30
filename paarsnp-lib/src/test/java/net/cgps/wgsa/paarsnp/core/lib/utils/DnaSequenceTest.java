package net.cgps.wgsa.paarsnp.core.lib.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DnaSequenceTest {

  @Test
  public void codonIndexAt() {
    assertEquals(1, DnaSequence.codonIndexAt(1));
    assertEquals(1, DnaSequence.codonIndexAt(2));
    assertEquals(1, DnaSequence.codonIndexAt(3));
    assertEquals(2, DnaSequence.codonIndexAt(4));
    assertEquals(2, DnaSequence.codonIndexAt(5));
    assertEquals(2, DnaSequence.codonIndexAt(6));
    assertThrows(IndexOutOfBoundsException.class, () -> DnaSequence.codonIndexAt(-1));
  }
}