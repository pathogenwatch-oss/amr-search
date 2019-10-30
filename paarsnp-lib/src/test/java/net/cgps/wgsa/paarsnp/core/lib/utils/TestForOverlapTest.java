package net.cgps.wgsa.paarsnp.core.lib.utils;

import net.cgps.wgsa.paarsnp.core.lib.OverlapRemover;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestForOverlapTest {

  @Test
  public void overlapCheck() {

    assertTrue(OverlapRemover.TestForOverlap.overlapCheck(1, 100, 100, 150, 0));
    assertFalse(OverlapRemover.TestForOverlap.overlapCheck(1, 100, 80, 150, 30));
    assertFalse(OverlapRemover.TestForOverlap.overlapCheck(1, 100, 71, 90, 30));
    assertTrue(OverlapRemover.TestForOverlap.overlapCheck(100, 200, 70, 110, 0));
    assertFalse(OverlapRemover.TestForOverlap.overlapCheck(100, 200, 70, 110, 330));
    assertTrue(OverlapRemover.TestForOverlap.overlapCheck(100, 200, 70, 110, 10));
    assertFalse(OverlapRemover.TestForOverlap.overlapCheck(100, 200, 70, 110, 11));
    assertTrue(OverlapRemover.TestForOverlap.overlapCheck(1, 200, 70, 110, 10));
    assertTrue(OverlapRemover.TestForOverlap.overlapCheck(1, 200, 20, 110, 30));
  }
}