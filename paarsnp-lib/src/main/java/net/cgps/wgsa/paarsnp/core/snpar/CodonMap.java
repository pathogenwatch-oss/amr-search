package net.cgps.wgsa.paarsnp.core.snpar;

import net.cgps.wgsa.paarsnp.core.lib.utils.DnaSequence;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.Map;
import java.util.stream.Stream;

public class CodonMap {

  private final Map<Integer, String> codonMap;

  public CodonMap(final Map<Integer, String> codonMap) {

    this.codonMap = codonMap;
  }

  public Character get(final int aaLocation) {
    return DnaSequence.translateCodon(this.codonMap.get(aaLocation)).orElse('-');
  }

  public Stream<Map.Entry<Integer, Character>> getTranslation() {
    return this.codonMap.keySet()
        .stream()
        .sorted(Integer::compareTo)
        .map(position -> new ImmutablePair<>(position, DnaSequence.translateCodon(this.codonMap.get(position)).orElse('-')));
  }
}
