package net.cgps.wgsa.paarsnp.core.snpar;

import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public class CodonMap {

  private final Map<Integer, Character> codonMap;
  private final Map<Integer, String> insertMap;

  public CodonMap(final Map<Integer, Character> codonMap, final Map<Integer, String> insertMap) {

    this.codonMap = codonMap;
    this.insertMap = insertMap;
  }

  public Character get(final int aaLocation) {
    return this.codonMap.get(aaLocation);
  }

  public Stream<Map.Entry<Integer, Character>> getTranslation() {
    return this.codonMap
        .entrySet()
        .stream()
        .sorted(Comparator.comparingInt(Map.Entry::getKey));
  }

  public String getInsertTranslation(final int insertLocation) {
    return this.insertMap.get(insertLocation);
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
