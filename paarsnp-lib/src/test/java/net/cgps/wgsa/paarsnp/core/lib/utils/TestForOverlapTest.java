package net.cgps.wgsa.paarsnp.core.lib.utils;

import net.cgps.wgsa.paarsnp.core.lib.OverlapRemover;
import org.junit.Assert;
import org.junit.Test;

public class TestForOverlapTest {

  @Test
  public void overlapCheck() {

    Assert.assertTrue(OverlapRemover.TestForOverlap.overlapCheck(1, 100, 100, 150, 0));
    Assert.assertFalse(OverlapRemover.TestForOverlap.overlapCheck(1, 100, 80, 150, 30));
    Assert.assertFalse(OverlapRemover.TestForOverlap.overlapCheck(1, 100, 71, 90, 30));
    Assert.assertTrue(OverlapRemover.TestForOverlap.overlapCheck(100, 200, 70, 110, 0));
    Assert.assertFalse(OverlapRemover.TestForOverlap.overlapCheck(100, 200, 70, 110, 330));
    Assert.assertTrue(OverlapRemover.TestForOverlap.overlapCheck(100, 200, 70, 110, 10));
    Assert.assertFalse(OverlapRemover.TestForOverlap.overlapCheck(100, 200, 70, 110, 11));
    Assert.assertTrue(OverlapRemover.TestForOverlap.overlapCheck(1, 200, 70, 110, 10));
    Assert.assertTrue(OverlapRemover.TestForOverlap.overlapCheck(1, 200, 20, 110, 30));
  }
}