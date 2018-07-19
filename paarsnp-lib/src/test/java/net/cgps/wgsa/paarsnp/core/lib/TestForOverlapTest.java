package net.cgps.wgsa.paarsnp.core.lib;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class TestForOverlapTest {

  @Test
  public void overlapCheck() {

    Assert.assertTrue(TestForOverlap.overlapCheck(1, 100, 100, 150, 0));
    Assert.assertFalse(TestForOverlap.overlapCheck(1, 100, 80, 150, 30));
    Assert.assertFalse(TestForOverlap.overlapCheck(1, 100, 71, 90, 30));
    Assert.assertTrue(TestForOverlap.overlapCheck(100, 200, 70, 110, 0));
    Assert.assertFalse(TestForOverlap.overlapCheck(100, 200, 70, 110, 330));
    Assert.assertTrue(TestForOverlap.overlapCheck(100, 200, 70, 110, 10));
    Assert.assertFalse(TestForOverlap.overlapCheck(100, 200, 70, 110, 11));
    Assert.assertTrue(TestForOverlap.overlapCheck(1, 200, 70, 110, 10));
    Assert.assertTrue(TestForOverlap.overlapCheck(1, 200, 20, 110, 30));
  }
}