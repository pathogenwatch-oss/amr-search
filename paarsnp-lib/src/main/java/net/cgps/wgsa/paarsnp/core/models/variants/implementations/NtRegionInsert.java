package net.cgps.wgsa.paarsnp.core.models.variants.implementations;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import net.cgps.wgsa.paarsnp.core.lib.AbstractJsonnable;
import net.cgps.wgsa.paarsnp.core.lib.blast.Mutation;
import net.cgps.wgsa.paarsnp.core.models.Location;
import net.cgps.wgsa.paarsnp.core.models.ResistanceMutationMatch;
import net.cgps.wgsa.paarsnp.core.models.variants.Variant;
import net.cgps.wgsa.paarsnp.core.snpar.AaAlignment;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@JsonDeserialize(as = NtRegionInsert.class)
public class NtRegionInsert extends AbstractJsonnable implements Variant {

  private final String name;
  private final int rangeStart;
  private final int rangeStop;

  @SuppressWarnings("unused")
  private NtRegionInsert() {
    this("", 0, 0);
  }

  public NtRegionInsert(final String name, final int rangeStart, final int rangeStop) {
    this.name = name;
    this.rangeStart = rangeStart;
    this.rangeStop = rangeStop;
  }

  @SuppressWarnings("unused")
  private int getRangeStart() {
    return this.rangeStart;
  }

  @SuppressWarnings("unused")
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
  public Optional<ResistanceMutationMatch> match(final Map<Integer, Collection<Mutation>> mutations, final AaAlignment aaAlignment) {
    return this.isPresent(mutations) ?
           Optional.of(this.buildMatch(mutations)) :
           Optional.empty();
  }

  public boolean isPresent(final Map<Integer, Collection<Mutation>> mutations) {
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

  public ResistanceMutationMatch buildMatch(final Map<Integer, Collection<Mutation>> mutations) {
    return new ResistanceMutationMatch(
        this,
        mutations
            .keySet()
            .stream()
            .filter(this::isInRange)
            .map(mutations::get)
            .flatMap(Collection::stream)
            .filter(mutation -> mutation.getMutationType() == Mutation.MutationType.I)
            .map(mutation -> new Location(mutation.getQueryLocation(), mutation.getReferenceLocation()))
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
