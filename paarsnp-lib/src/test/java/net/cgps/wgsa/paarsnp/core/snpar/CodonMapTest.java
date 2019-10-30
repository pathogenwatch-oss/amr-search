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

    final Map<Integer, String> map = new HashMap<>();
    map.put(1, "ATG");
    map.put(2, "ATA");
    map.put(3, "ATA");
    return map;
  }

  private Map<Integer, String> createSimpleMap() {
    final Map<Integer, String> map = new HashMap<>();
    map.put(2, "TTG");
    return map;
  }
}