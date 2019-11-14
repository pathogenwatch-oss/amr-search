package net.cgps.wgsa.paarsnp.core.models.variants.implementations;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import net.cgps.wgsa.paarsnp.core.lib.AbstractJsonnable;
import net.cgps.wgsa.paarsnp.core.lib.blast.Mutation;
import net.cgps.wgsa.paarsnp.core.models.ResistanceMutationMatch;
import net.cgps.wgsa.paarsnp.core.models.variants.Variant;
import net.cgps.wgsa.paarsnp.core.snpar.CodonMap;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@JsonDeserialize(as = AaResistanceMutation.class)
public class AaResistanceMutation extends AbstractJsonnable implements Variant {

  private final String name;
  private final String originalSequence;
  private final int referenceLocation;
  private final String mutationSequence;
  private final int aaLocation;

  @SuppressWarnings("unused")
  private AaResistanceMutation() {
    this("", "", 0, "", 0);
  }

  public AaResistanceMutation(final String name, final String originalSequence, final int referenceLocation, final String mutationSequence, final int aaLocation) {
    // NB for the SNP archive the query location is the same as the representative location.
    this.name = name;
    this.originalSequence = originalSequence;
    this.referenceLocation = referenceLocation;
    this.mutationSequence = mutationSequence;
    this.aaLocation = aaLocation;
  }

  public static AaResistanceMutation build(final String name, final Map.Entry<Integer, Map.Entry<String, String>> mapping, final int referenceLength) {

    final int referenceLocation =
        '-' == mapping.getValue().getKey().charAt(0) ?
        mapping.getKey() * 3 :
        (mapping.getKey() * 3) - 2;

    return new AaResistanceMutation(name, mapping.getValue().getKey().toUpperCase(), referenceLocation, mapping.getValue().getValue().toUpperCase(), mapping.getKey());
  }

  @Override
  public boolean isPresent(final Map<Integer, Collection<Mutation>> mutations, final CodonMap codonMap) {

    // Only inserts can be multiple characters
    if ('-' == this.originalSequence.charAt(0)) {
      return codonMap.containsInsert(this.aaLocation) && this.getMutationSequence().equals(codonMap.getInsertTranslation(this.aaLocation));
    } else {
      return this.getMutationSequence().charAt(0) == codonMap.get(this.aaLocation);
    }
  }

  @Override
  public boolean isWithinBoundaries(final int start, final int stop) {
    return start <= this.referenceLocation && this.referenceLocation + 2 < stop;
  }

  public int getReferenceLocation() {
    return this.referenceLocation;
  }


  public String getMutationSequence() {
    return this.mutationSequence;
  }

  public String getName() {

    return this.name;
  }

  @SuppressWarnings("unused")
  public int getAaLocation() {
    return this.aaLocation;
  }

  @Override
  public ResistanceMutationMatch buildMatch(final Map<Integer, Collection<Mutation>> mutations, final CodonMap codonMap) {

    if ('-' == this.originalSequence.charAt(0)) { // Insert
      return new ResistanceMutationMatch(
          this,
          mutations.getOrDefault(this.referenceLocation, Collections.emptyList())
              .stream()
              .filter(mutation -> this.originalSequence.equals(mutation.getOriginalSequence()))
              .collect(Collectors.toList())
      );
    } else {
      return new ResistanceMutationMatch(
          this,
          Stream.of(this.referenceLocation, this.referenceLocation + 1, this.referenceLocation + 2)
              .filter(mutations::containsKey)
              .map(mutations::get)
              .flatMap(Collection::stream)
              .filter(mutation -> '-' != mutation.getOriginalSequence().charAt(0))
              .filter(mutation -> '-' != mutation.getMutationSequence().charAt(0) ||
                  ('-' == this.mutationSequence.charAt(0) && this.mutationSequence.equals(mutation.getMutationSequence())))
              .collect(Collectors.toList()));
    }
  }

  @SuppressWarnings("unused")
  public String getOriginalSequence() {
    return this.originalSequence;
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.name, this.originalSequence, this.referenceLocation, this.mutationSequence, this.aaLocation);
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || this.getClass() != o.getClass()) return false;
    final AaResistanceMutation that = (AaResistanceMutation) o;
    return this.originalSequence.equals(that.originalSequence) &&
        this.referenceLocation == that.referenceLocation &&
        this.mutationSequence.equals(that.mutationSequence) &&
        this.aaLocation == that.aaLocation &&
        Objects.equals(this.name, that.name);
  }
}