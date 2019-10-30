package net.cgps.wgsa.paarsnp.core.lib.utils;

import org.junit.Assert;
import org.junit.Test;

public class DnaSequenceTest {

  @Test
  public void codonIndexAt() {
    Assert.assertEquals(1, DnaSequence.codonIndexAt(1));
    Assert.assertEquals(1, DnaSequence.codonIndexAt(2));
    Assert.assertEquals(1, DnaSequence.codonIndexAt(3));
    Assert.assertEquals(2, DnaSequence.codonIndexAt(4));
    Assert.assertEquals(2, DnaSequence.codonIndexAt(5));
    Assert.assertEquals(2, DnaSequence.codonIndexAt(6));

  }
}