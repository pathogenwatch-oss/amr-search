package net.cgps.wgsa.paarsnp.core.models.variants;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import net.cgps.wgsa.paarsnp.core.models.ResistanceMutationMatch;

import java.util.Optional;

@JsonDeserialize(using = VariantJsonDeserializer.class)
public interface Variant<A, B> {

  String getName();

  boolean isWithinBoundaries(int start, int stop);

  Optional<ResistanceMutationMatch> isPresent(A resourceA, B resourceB);
}
