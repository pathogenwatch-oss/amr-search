package net.cgps.wgsa.paarsnp.core.snpar;

import net.cgps.wgsa.paarsnp.core.lib.utils.DnaSequence;

import java.util.Map;

public class CodonMap {

  private final Map<Integer, String> codonMap;
  private final Map<Integer, Integer> positionMap;

  public CodonMap(final Map<Integer, String> codonMap, final Map<Integer, Integer> positionMap) {

    this.codonMap = codonMap;
    this.positionMap = positionMap;
  }

  public Character get(final int aaLocation) {
    return DnaSequence.translateCodon(this.codonMap.get(aaLocation)).orElse('-');
  }

  public Integer getQueryLocation(final int aaLocation) {
    return this.positionMap.get(aaLocation);
  }
}
