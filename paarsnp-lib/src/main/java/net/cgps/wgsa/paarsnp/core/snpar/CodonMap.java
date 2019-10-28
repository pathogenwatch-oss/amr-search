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
    return this.codonMap
        .keySet()
        .stream()
        .sorted(Integer::compareTo)
        .map(position -> new ImmutablePair<>(position, DnaSequence.translateCodon(this.codonMap.get(position)).orElse('-')));
  }

  public String getInsertTranslation(final int insertLocation) {
    return DnaSequence.translateMultiple(this.codonMap.get(insertLocation));
  }

  public boolean containsInsert(final Integer position) {
    return this.insertMap.containsKey(position);
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || this.getClass() != o.getClass()) return false;
    final CodonMap otherMap = (CodonMap) o;
    return this.codonMap.equals(otherMap.codonMap) &&
        this.insertMap.equals(otherMap.insertMap);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.codonMap, this.insertMap);
  }

  @Override
  public String toString() {
    return "CodonMap{" +
        "codonMap=" + codonMap +
        ", insertMap=" + insertMap +
        '}';
  }
}
