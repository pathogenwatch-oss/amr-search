package net.cgps.wgsa.paarsnp.core.models.variants.implementations;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import net.cgps.wgsa.paarsnp.core.lib.AbstractJsonnable;
import net.cgps.wgsa.paarsnp.core.lib.blast.Mutation;
import net.cgps.wgsa.paarsnp.core.models.Location;
import net.cgps.wgsa.paarsnp.core.models.ResistanceMutationMatch;
import net.cgps.wgsa.paarsnp.core.models.variants.Variant;
import net.cgps.wgsa.paarsnp.core.snpar.AaAlignment;

import java.util.*;

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
  public Optional<ResistanceMutationMatch> match(final Map<Integer, Collection<Mutation>> mutations, final AaAlignment aaAlignment) {
    return this.isPresent(aaAlignment) ?
           Optional.of(new ResistanceMutationMatch(
               this,
               Collections.singleton(new Location(aaAlignment.getQueryNtLocation(this.aaLocation), this.referenceLocation))
           )) :
           Optional.empty();
  }

  @Override
  public boolean isWithinBoundaries(final int start, final int stop) {
    return start <= this.referenceLocation && this.referenceLocation + 2 < stop;
  }

  public boolean isPresent(final AaAlignment aaAlignment) {

    // Only inserts can be multiple characters
    if ('-' == this.originalSequence.charAt(0)) {
      return aaAlignment.containsInsert(this.aaLocation) && this.getMutationSequence().equals(aaAlignment.getInsertTranslation(this.aaLocation));
    } else {
      return this.getMutationSequence().charAt(0) == aaAlignment.get(this.aaLocation);
    }
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
