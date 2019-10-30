package net.cgps.wgsa.paarsnp.core.snpar.codonmapping;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FrameTest {

  @Test
  public void frameTests() {
    assertEquals(FRAME.ONE, FRAME.toFrame(1));
    assertEquals(FRAME.TWO, FRAME.toFrame(2));
    assertEquals(FRAME.THREE, FRAME.toFrame(3));
    assertEquals(FRAME.ONE, FRAME.toFrame(4));
    assertEquals(FRAME.TWO, FRAME.toFrame(5));
    assertEquals(FRAME.THREE, FRAME.toFrame(6));
  }

}