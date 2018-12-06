package net.cgps.wgsa.paarsnp.core.models.variants.implementations;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import net.cgps.wgsa.paarsnp.core.lib.blast.BlastSearchStatistics;
import net.cgps.wgsa.paarsnp.core.lib.blast.Mutation;
import net.cgps.wgsa.paarsnp.core.lib.utils.DnaSequence;
import net.cgps.wgsa.paarsnp.core.models.ResistanceMutationMatch;
import net.cgps.wgsa.paarsnp.core.models.variants.HasReferenceLocation;
import net.cgps.wgsa.paarsnp.core.models.variants.NonCodingVariant;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

@JsonDeserialize(as = PromoterMutation.class)
public class PromoterMutation implements HasReferenceLocation, NonCodingVariant {

  private final String name;
  private final int referenceLocation; // Should be a negative number
  private final char mutation;
  private final char originalSequence;

  @SuppressWarnings("unused")
  private PromoterMutation() {
    this("", 0, 'a', 't');
  }

  private PromoterMutation(final String name, final int referenceLocation, final char mutation, final char originalSequence) {
    this.name = name;
    this.referenceLocation = referenceLocation;
    this.mutation = mutation;
    this.originalSequence = originalSequence;
  }

  public static PromoterMutation build(final String name, final Map.Entry<Integer, Map.Entry<Character, Character>> mutation) {

    char mutation1 = Character.toUpperCase(mutation.getValue().getValue());
    char originalSequence = Character.toUpperCase(mutation.getValue().getKey());
    return new PromoterMutation(name, mutation.getKey(), mutation1, originalSequence);
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public boolean isWithinBoundaries(final int start, final int stop) {
    return this.referenceLocation <= start && stop <= this.referenceLocation;
  }

  @Override
  public int getReferenceLocation() {
    return this.referenceLocation;
  }

  public char getMutation() {
    return this.mutation;
  }

  @SuppressWarnings("unused")
  public char getOriginalSequence() {
    return this.originalSequence;
  }

  @Override
  public boolean isPresent(final String region, final BlastSearchStatistics searchStatistics) {
    return this.mutation == region.charAt(region.length() + this.referenceLocation);
  }

  @Override
  public ResistanceMutationMatch buildMatch(final String sequence, final BlastSearchStatistics searchStatistics) {
    final Mutation.MutationType mutationType;

    final int refOffset;
    if (DnaSequence.Strand.FORWARD == searchStatistics.getStrand()) {
      refOffset = this.referenceLocation;
    } else {
      refOffset = -1 * this.referenceLocation;
    }

    if (this.mutation == '-') {
      mutationType = Mutation.MutationType.D;
    } else if (this.originalSequence == '-') {
      mutationType = Mutation.MutationType.I;
    } else {
      mutationType = Mutation.MutationType.S;
    }
    return new ResistanceMutationMatch(
        this,
        Collections.singletonList(
            new Mutation(
                mutationType,
                searchStatistics.getQuerySequenceStart() + refOffset,
                this.originalSequence,
                this.mutation,
                this.referenceLocation)));

  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || this.getClass() != o.getClass()) return false;
    final PromoterMutation that = (PromoterMutation) o;
    return this.referenceLocation == that.referenceLocation &&
        this.mutation == that.mutation &&
        this.originalSequence == that.originalSequence &&
        Objects.equals(this.name, that.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.name, this.referenceLocation, this.mutation, this.originalSequence);
  }
}
