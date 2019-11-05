package net.cgps.wgsa.paarsnp.core.models.variants.implementations;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AaResistanceMutationTest {

  @Test
  public void testAAInBoundaries() {
    final var reference = new AaResistanceMutation("test", "D", 4, "E", 2);
    Assertions.assertFalse(reference.isWithinBoundaries(8, 100));
  }
}