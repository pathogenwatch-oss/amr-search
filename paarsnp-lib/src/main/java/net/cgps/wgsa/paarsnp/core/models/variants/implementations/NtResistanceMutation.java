package net.cgps.wgsa.paarsnp.core.models.variants.implementations;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import net.cgps.wgsa.paarsnp.core.lib.AbstractJsonnable;
import net.cgps.wgsa.paarsnp.core.lib.blast.Mutation;
import net.cgps.wgsa.paarsnp.core.models.ResistanceMutationMatch;
import net.cgps.wgsa.paarsnp.core.models.variants.Variant;
import net.cgps.wgsa.paarsnp.core.snpar.CodonMap;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@JsonDeserialize(as = NtResistanceMutation.class)
public class NtResistanceMutation extends AbstractJsonnable implements Variant {

  private final String name;
  private final String originalSequence;
  private final int referenceLocation;
  private final String mutationSequence;

  @SuppressWarnings("unused")
  private NtResistanceMutation() {
    this("", "", 0, "");
  }

  public NtResistanceMutation(final String name, final String originalSequence, final int referenceLocation, final String mutationSequence) {
    // NB for the SNP archive the query location is the same as the representative location.
    this.name = name;
    this.originalSequence = originalSequence;
    this.referenceLocation = referenceLocation;
    this.mutationSequence = mutationSequence;
  }

  public static NtResistanceMutation build(final String name, final Map.Entry<Integer, Map.Entry<String, String>> mapping, final int referenceLength) {

    // Accounts for promoter region variants
    final int referenceLocation = (mapping.getKey() < 0 ? referenceLength - 49 : 0) + mapping.getKey();

    return new NtResistanceMutation(name, mapping.getValue().getKey().toUpperCase(), referenceLocation, mapping.getValue().getValue().toUpperCase());
  }

  @Override
  public boolean isPresent(final Map<Integer, Collection<Mutation>> mutations, final CodonMap codonMap) {

    return mutations.containsKey(this.referenceLocation) &&
        mutations.get(this.referenceLocation)
            .stream()
            .filter(mutation -> this.originalSequence.equals(mutation.getOriginalSequence())) // Ensures inserts are compared to inserts and not substitutions.
            .anyMatch(queryMutation -> queryMutation.getMutationSequence().equals(this.mutationSequence));
  }

  @Override
  public boolean isWithinBoundaries(final int start, final int stop) {
    return start <= this.referenceLocation && this.referenceLocation < stop;
  }

  public int getReferenceLocation() {
    return this.referenceLocation;
  }

  @SuppressWarnings("unused")
  public String getMutationSequence() {
    return this.mutationSequence;
  }

  public String getName() {
    return this.name;
  }

  @Override
  public ResistanceMutationMatch buildMatch(final Map<Integer, Collection<Mutation>> mutations, final CodonMap codonMap) {
    return new ResistanceMutationMatch(
        this,
        mutations.get(this.referenceLocation)
            .stream()
            .filter(mutation -> this.originalSequence.equals(mutation.getOriginalSequence())) // Ensures inserts are compared to inserts and not substitutions.
            .filter(queryMutation -> queryMutation.getMutationSequence().equals(this.mutationSequence))
            .collect(Collectors.toList()));
  }

  @SuppressWarnings("unused")
  public String getOriginalSequence() {
    return this.originalSequence;
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.name, this.originalSequence, this.referenceLocation, this.mutationSequence);
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || this.getClass() != o.getClass()) return false;
    final NtResistanceMutation that = (NtResistanceMutation) o;
    return this.originalSequence.equals(that.originalSequence) &&
        this.referenceLocation == that.referenceLocation &&
        this.mutationSequence.equals(that.mutationSequence) &&
        Objects.equals(this.name, that.name);
  }
}
