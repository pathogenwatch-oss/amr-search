package net.cgps.wgsa.paarsnp.core.models.variants.implementations;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import net.cgps.wgsa.paarsnp.core.lib.AbstractJsonnable;
import net.cgps.wgsa.paarsnp.core.lib.blast.Mutation;
import net.cgps.wgsa.paarsnp.core.lib.utils.DnaSequence;
import net.cgps.wgsa.paarsnp.core.models.Location;
import net.cgps.wgsa.paarsnp.core.models.ResistanceMutationMatch;
import net.cgps.wgsa.paarsnp.core.models.variants.Variant;
import net.cgps.wgsa.paarsnp.core.snpar.AaAlignment;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@JsonDeserialize(as = AaRegionInsert.class)
public class AaRegionInsert extends AbstractJsonnable implements Variant {

  private final String name;
  private final int rangeStart;
  private final int rangeStop;

  @SuppressWarnings("unused")
  private AaRegionInsert() {
    this("", 0, 0);
  }

  public AaRegionInsert(final String name, final int rangeStart, final int rangeStop) {
    this.name = name;
    this.rangeStart = rangeStart;
    this.rangeStop = rangeStop;
  }

  @Override
  public String getName() {
    return this.name;
  }

  @SuppressWarnings("unused")
  public int getRangeStart() {
    return this.rangeStart;
  }

  @SuppressWarnings("unused")
  public int getRangeStop() {
    return this.rangeStop;
  }

  @Override
  public boolean isWithinBoundaries(final int start, final int stop) {
    return DnaSequence.codonIndexAt(start) <= this.rangeStart && this.rangeStop <= DnaSequence.codonIndexAt(stop);
  }

  @Override
  public boolean isPresent(final Map<Integer, Collection<Mutation>> mutations, final AaAlignment aaAlignment) {
    return aaAlignment
        .getInsertLocations()
        .stream()
        .filter(this::isInRange)
        .map(mutations::get)
        .flatMap(Collection::stream)
        .anyMatch(mutation -> mutation.getMutationType() == Mutation.MutationType.I);
  }

  private Boolean isInRange(final Integer location) {
    return this.rangeStart <= location && location <= this.rangeStop;
  }

  @Override
  public ResistanceMutationMatch buildMatch(final Map<Integer, Collection<Mutation>> mutations, final AaAlignment aaAlignment) {
    return new ResistanceMutationMatch(
        this,
        aaAlignment
            .getInsertLocations()
            .stream()
            .filter(this::isInRange)
            .map(codonLocation -> new Location(DnaSequence.ntIndexFromCodon(this.rangeStart), (codonLocation * 3) - 2))
            .collect(Collectors.toList()));
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    final AaRegionInsert that = (AaRegionInsert) o;
    return rangeStart == that.rangeStart &&
        rangeStop == that.rangeStop &&
        Objects.equals(name, that.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, rangeStart, rangeStop);
  }
}
