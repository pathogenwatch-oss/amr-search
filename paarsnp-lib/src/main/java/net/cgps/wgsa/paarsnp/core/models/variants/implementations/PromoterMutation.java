package net.cgps.wgsa.paarsnp.core.models.variants.implementations;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import net.cgps.wgsa.paarsnp.core.lib.blast.Mutation;
import net.cgps.wgsa.paarsnp.core.models.ResistanceMutationMatch;
import net.cgps.wgsa.paarsnp.core.models.variants.HasReferenceLocation;
import net.cgps.wgsa.paarsnp.core.models.variants.NonCodingVariant;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

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

    return new PromoterMutation(name, mutation.getKey(), Character.toUpperCase(mutation.getValue().getValue()), Character.toUpperCase(mutation.getValue().getKey()));
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public boolean isWithinBoundaries(final int start, final int stop) {
    return this.referenceLocation < stop && start < this.referenceLocation;
  }

  @Override
  public int getReferenceLocation() {
    return this.referenceLocation;
  }

  @Override
  public Optional<ResistanceMutationMatch> isPresent(final String region, final Integer queryIndex) {
    if (this.mutation == region.charAt(this.referenceLocation)) {
      final Mutation.MutationType mutationType;

      if (this.mutation == '-') {
        mutationType = Mutation.MutationType.D;
      } else if (this.originalSequence == '-') {
        mutationType = Mutation.MutationType.I;
      } else {
        mutationType = Mutation.MutationType.S;
      }

      return Optional.of(new ResistanceMutationMatch(
          this,
          Collections.singletonList(
              new Mutation(
                  mutationType,
                  queryIndex + this.referenceLocation,
                  this.originalSequence,
                  this.mutation,
                  this.referenceLocation))));

    } else {
      return Optional.empty();
    }
  }
}
