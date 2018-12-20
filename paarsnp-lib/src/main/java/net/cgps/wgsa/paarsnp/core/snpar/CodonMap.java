package net.cgps.wgsa.paarsnp.core.snpar;

import net.cgps.wgsa.paarsnp.core.lib.utils.DnaSequence;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public class CodonMap {

  private final Map<Integer, String> codonMap;
  private final Map<Integer, String> insertMap;

  public CodonMap(final Map<Integer, String> codonMap, final Map<Integer, String> insertMap) {

    this.codonMap = codonMap;
    this.insertMap = insertMap;
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

  public Character getInsertTranslation(final int insertLocation) {
    return DnaSequence.translateCodon(this.codonMap.get(insertLocation)).orElse('-');
  }

  public boolean containsInsert(final Integer position) {
    return this.insertMap.containsKey(position);
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || this.getClass() != o.getClass()) return false;
    final CodonMap codonMap1 = (CodonMap) o;
    return Objects.equals(this.codonMap, codonMap1.codonMap) &&
        Objects.equals(this.insertMap, codonMap1.insertMap);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.codonMap, this.insertMap);
  }
}
