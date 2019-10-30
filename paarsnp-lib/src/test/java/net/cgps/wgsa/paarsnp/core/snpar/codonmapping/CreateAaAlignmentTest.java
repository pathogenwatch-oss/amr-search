package net.cgps.wgsa.paarsnp.core.snpar.codonmapping;

import org.junit.Assert;
import org.junit.Test;

public class CreateAaAlignmentTest {

  @Test
  public void frameTests() {
    Assert.assertEquals(FRAME.ONE, FRAME.toFrame(1));
    Assert.assertEquals(FRAME.TWO, FRAME.toFrame(2));
    Assert.assertEquals(FRAME.THREE, FRAME.toFrame(3));
    Assert.assertEquals(FRAME.ONE, FRAME.toFrame(4));
    Assert.assertEquals(FRAME.TWO, FRAME.toFrame(5));
    Assert.assertEquals(FRAME.THREE, FRAME.toFrame(6));
  }

}