package net.cgps.wgsa.paarsnp.core.snpar;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class AaAlignmentTest {

  @Test
  public void equals() {

    final AaAlignment map1 = new AaAlignment(this.createSimpleMap(), 1, this.createSimpleLocationMap(), this.createSimpleInsertMap());
    final AaAlignment map2 = new AaAlignment(this.createSimpleMap(), 1, this.createSimpleLocationMap(), this.createSimpleInsertMap());
    final AaAlignment map3 = new AaAlignment(this.createSimpleMap(), 1, Collections.emptyMap(), Collections.emptyMap());

    assertEquals(map1, map2);
    assertNotEquals(map1, map3);
  }

  private Map<Integer, String> createSimpleInsertMap() {

    final var map = new HashMap<Integer, String>();
    map.put(1, "M");
    map.put(2, "I");
    map.put(3, "I");
    return map;
  }

  private Map<Integer, Integer> createSimpleLocationMap() {
    final var map = new HashMap<Integer, Integer>();
    map.put(1, 1);
    map.put(2, 2);
    map.put(3, 3);
    return map;
  }

  private Map<Integer, Character> createSimpleMap() {
    final var map = new HashMap<Integer, Character>();
    map.put(2, 'L');
    return map;
  }
}