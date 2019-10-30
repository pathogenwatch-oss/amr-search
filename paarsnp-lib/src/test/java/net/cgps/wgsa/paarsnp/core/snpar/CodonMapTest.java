package net.cgps.wgsa.paarsnp.core.snpar;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class CodonMapTest {

  @Test
  public void equals() {

    final CodonMap map1 = new CodonMap(this.createSimpleMap(), this.createSimpleInsertMap());
    final CodonMap map2 = new CodonMap(this.createSimpleMap(), this.createSimpleInsertMap());
    final CodonMap map3 = new CodonMap(this.createSimpleMap(), Collections.emptyMap());

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

  private Map<Integer, Character> createSimpleMap() {
    final var map = new HashMap<Integer, Character>();
    map.put(2, 'L');
    return map;
  }
}