package net.cgps.wgsa.paarsnp.core.snpar;

import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

public class AaAlignment {

  private final Map<Integer, Character> sequenceMap;
  private final int queryNtStart;
  private final Map<Integer, Integer> queryAaLocationMap;
  private final Map<Integer, String> insertMap;

  public AaAlignment(final Map<Integer, Character> sequenceMap, final int queryNtStart, final Map<Integer, Integer> queryAaLocationMap, final Map<Integer, String> insertMap) {

    this.sequenceMap = sequenceMap;
    this.queryNtStart = queryNtStart;
    this.queryAaLocationMap = queryAaLocationMap;
    this.insertMap = insertMap;
  }

  public Character get(final int aaLocation) {
    return this.sequenceMap.get(aaLocation);
  }

  public Set<Integer> getInsertLocations() {
    return this.insertMap.keySet();
  }

  public Stream<Map.Entry<Integer, Character>> getTranslation() {
    return this.sequenceMap
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

  public int getQueryNtLocation(final int referenceCodonIndex) {
    return this.queryNtStart + (3 * this.queryAaLocationMap.get(referenceCodonIndex)) - 2;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    final AaAlignment that = (AaAlignment) o;
    return queryNtStart == that.queryNtStart &&
        Objects.equals(sequenceMap, that.sequenceMap) &&
        Objects.equals(queryAaLocationMap, that.queryAaLocationMap) &&
        Objects.equals(insertMap, that.insertMap);
  }

  @Override
  public int hashCode() {
    return Objects.hash(sequenceMap, queryNtStart, queryAaLocationMap, insertMap);
  }

  @Override
  public String toString() {
    return "AaAlignment{" +
        "sequenceMap=" + sequenceMap +
        ", queryNtStart=" + queryNtStart +
        ", queryAaLocationMap=" + queryAaLocationMap +
        ", insertMap=" + insertMap +
        '}';
  }
}
