package net.cgps.wgsa.paarsnp.core.models.variants.implementations;

import net.cgps.wgsa.paarsnp.core.lib.AbstractJsonnable;
import net.cgps.wgsa.paarsnp.core.lib.blast.Mutation;
import net.cgps.wgsa.paarsnp.core.models.ResistanceMutationMatch;
import net.cgps.wgsa.paarsnp.core.models.variants.Variant;
import net.cgps.wgsa.paarsnp.core.snpar.CodonMap;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class NtRegionInsert extends AbstractJsonnable implements Variant {

  private final String name;
  private final int rangeStart;
  private final int rangeStop;

  public NtRegionInsert(final String name, final int rangeStart, final int rangeStop) {
    this.name = name;
    this.rangeStart = rangeStart;
    this.rangeStop = rangeStop;
  }

  private int getRangeStart() {
    return this.rangeStart;
  }

  private int getRangeStop() {
    return this.rangeStop;
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public boolean isWithinBoundaries(final int start, final int stop) {
    return start <= rangeStart && rangeStop <= stop;
  }

  @Override
  public boolean isPresent(final Map<Integer, Collection<Mutation>> mutations, final CodonMap codonMap) {
    return mutations
        .keySet()
        .stream()
        .filter(this::isInRange)
        .map(mutations::get)
        .flatMap(Collection::stream)
        .anyMatch(mutation -> mutation.getMutationType() == Mutation.MutationType.I);
  }

  private Boolean isInRange(final Integer location) {
    return rangeStart <= location && location <= rangeStop;
  }

  @Override
  public ResistanceMutationMatch buildMatch(final Map<Integer, Collection<Mutation>> mutations, final CodonMap codonMap) {
    return new ResistanceMutationMatch(
        this,
        mutations
            .keySet()
            .stream()
            .filter(this::isInRange)
            .map(mutations::get)
            .flatMap(Collection::stream)
            .filter(mutation -> mutation.getMutationType() == Mutation.MutationType.I)
            .collect(Collectors.toList()));
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    final NtRegionInsert that = (NtRegionInsert) o;
    return rangeStart == that.rangeStart &&
        rangeStop == that.rangeStop &&
        Objects.equals(name, that.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, rangeStart, rangeStop);
  }
}
