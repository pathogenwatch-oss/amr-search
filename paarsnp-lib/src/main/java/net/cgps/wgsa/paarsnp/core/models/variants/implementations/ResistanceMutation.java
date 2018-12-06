package net.cgps.wgsa.paarsnp.core.models.variants.implementations;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import net.cgps.wgsa.paarsnp.core.lib.blast.Mutation;
import net.cgps.wgsa.paarsnp.core.models.ResistanceMutationMatch;
import net.cgps.wgsa.paarsnp.core.models.variants.HasReferenceLocation;
import net.cgps.wgsa.paarsnp.core.models.variants.TranscribedVariant;
import net.cgps.wgsa.paarsnp.core.snpar.CodonMap;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@JsonDeserialize(as = ResistanceMutation.class)
public class ResistanceMutation implements HasReferenceLocation, TranscribedVariant {

  public enum TYPE {
    DNA, AA
  }

  public final static Set<Character> dnaCharacters = new HashSet<>(Arrays.asList('a', 't', 'c', 'g'));

  private final String name;
  private final char originalSequence;
  private final int referenceLocation;
  private final char mutationSequence;
  private final TYPE type;
  private final int aaLocation;

  @SuppressWarnings("unused")
  private ResistanceMutation() {
    this("", '0', 0, '0', TYPE.DNA, 0);
  }

  private ResistanceMutation(final String name, final char originalSequence, final int referenceLocation, final char mutationSequence, final TYPE type, final int aaLocation) {
    // NB for the SNP archive the query location is the same as the representative location.
    this.name = name;
    this.originalSequence = originalSequence;
    this.referenceLocation = referenceLocation;
    this.mutationSequence = mutationSequence;
    this.type = type;
    this.aaLocation = aaLocation;
  }

  public static ResistanceMutation build(final String name, final Map.Entry<Integer, Map.Entry<Character, Character>> mapping, final int referenceLength) {

    final TYPE type = determineType(mapping.getValue());

    final int offset = mapping.getKey() < 0 ? referenceLength - 49 : 0;

    final int referenceLocation = TYPE.DNA == type ? offset + mapping.getKey() : (mapping.getKey() * 3) - 2;

    return new ResistanceMutation(name, mapping.getValue().getKey(), referenceLocation, mapping.getValue().getValue(), type, mapping.getKey());
  }

  @Override
  public boolean isPresent(final Map<Integer, Collection<Mutation>> mutations, final CodonMap codonMap) {

    switch (this.type) {
      case DNA:
        return mutations.containsKey(this.getReferenceLocation()) && mutations.get(this.getReferenceLocation())
            .stream()
            .anyMatch(queryMutation -> queryMutation.getMutationSequence() == this.getMutationSequence());
      case AA:
      default:
        return this.getMutationSequence() == codonMap.get(this.getAaLocation());
    }
  }

  @Override
  public ResistanceMutationMatch buildMatch(final Map<Integer, Collection<Mutation>> mutations, final CodonMap codonMap) {
    switch (this.type) {
      case DNA:
        return new ResistanceMutationMatch(
            this,
            mutations.get(this.getReferenceLocation())
                .stream()
                .filter(queryMutation -> queryMutation.getMutationSequence() == this.getMutationSequence())
                .collect(Collectors.toList()));

      case AA:
      default:
        return new ResistanceMutationMatch(
            this,
            Stream.of(this.getReferenceLocation(), this.getReferenceLocation() + 1, this.getReferenceLocation() + 2)
                .filter(index -> mutations.keySet().contains(index))
                .map(mutations::get)
                .flatMap(Collection::stream)
                .collect(Collectors.toList()));
    }
  }

  @Override
  public boolean isWithinBoundaries(final int start, final int stop) {
    return (start <= this.referenceLocation && this.referenceLocation < stop) &&
        (this.getType() == TYPE.DNA
            ||
            (start <= this.referenceLocation + 2 && this.referenceLocation + 2 < stop));
  }

  @Override
  public int getReferenceLocation() {
    return this.referenceLocation;
  }

  public TYPE getType() {
    return this.type;
  }

  public char getMutationSequence() {
    return this.mutationSequence;
  }

  public String getName() {

    return this.name;
  }

  public int getAaLocation() {
    return this.aaLocation;
  }

  @SuppressWarnings("unused")
  public char getOriginalSequence() {
    return this.originalSequence;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || this.getClass() != o.getClass()) return false;
    final ResistanceMutation that = (ResistanceMutation) o;
    return this.originalSequence == that.originalSequence &&
        this.referenceLocation == that.referenceLocation &&
        this.mutationSequence == that.mutationSequence &&
        this.aaLocation == that.aaLocation &&
        Objects.equals(this.name, that.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.name, this.originalSequence, this.referenceLocation, this.mutationSequence, this.aaLocation);
  }

  public static TYPE determineType(final Map.Entry<Character, Character> mutation) {

    if (dnaCharacters.contains(mutation.getKey()) || dnaCharacters.contains(mutation.getValue())) {
      return TYPE.DNA;
    } else {
      return TYPE.AA;
    }
  }
}
