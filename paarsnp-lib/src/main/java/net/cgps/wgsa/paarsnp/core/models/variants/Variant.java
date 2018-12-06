package net.cgps.wgsa.paarsnp.core.models.variants;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import net.cgps.wgsa.paarsnp.core.models.ResistanceMutationMatch;

@JsonDeserialize(using = VariantJsonDeserializer.class)
public interface Variant<A, B> {

  String getName();

  boolean isWithinBoundaries(int start, int stop);

  boolean isPresent(A resourceA, B resourceB);

  ResistanceMutationMatch buildMatch(A resourceA, B resourceB);
}
