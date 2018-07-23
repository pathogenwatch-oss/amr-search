package net.cgps.wgsa.paarsnp.core.snpar;

import net.cgps.wgsa.paarsnp.core.lib.utils.DnaSequence;

import java.util.Map;

public class CodonMap {

  private final String librarySequenceId;
  private final Map<Integer, String> codonMap;

  public CodonMap(final String librarySequenceId, final Map<Integer, String> codonMap) {

    this.librarySequenceId = librarySequenceId;
    this.codonMap = codonMap;
  }

  public String getLibrarySequenceId() {
    return this.librarySequenceId;
  }

  public Character get(final int aaLocation) {
    return DnaSequence.translateCodon(this.codonMap.get(aaLocation)).orElse('-');
  }
}
