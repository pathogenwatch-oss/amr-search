package net.cgps.wgsa.paarsnp.core.snpar;

import net.cgps.wgsa.paarsnp.core.lib.utils.DnaSequence;

import java.util.Map;

public class CodonMap {

  private final Map<Integer, String> codonMap;

  public CodonMap(final Map<Integer, String> codonMap) {

    this.codonMap = codonMap;
  }

  public Character get(final int aaLocation) {
    return DnaSequence.translateCodon(this.codonMap.get(aaLocation)).orElse('-');
  }
}
