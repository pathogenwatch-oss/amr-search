package net.cgps.wgsa.paarsnp.core.models.variants.implementations;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ResistanceMutationTest {

  @Test
  public void testAAInBoundaries() {
    final var reference = new ResistanceMutation("test", "D", 4, "E", ResistanceMutation.TYPE.AA, 2);
    Assertions.assertFalse(reference.isWithinBoundaries(8, 100));
  }
}