package net.cgps.wgsa.paarsnp.core.models.variants;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import net.cgps.wgsa.paarsnp.core.lib.Jsonnable;
import net.cgps.wgsa.paarsnp.core.lib.blast.Mutation;
import net.cgps.wgsa.paarsnp.core.models.ResistanceMutationMatch;
import net.cgps.wgsa.paarsnp.core.snpar.AaAlignment;

import java.util.Collection;
import java.util.Map;

@JsonDeserialize(using = VariantJsonDeserializer.class)
public interface Variant extends Jsonnable {

  String getName();

  boolean isWithinBoundaries(int start, int stop);

  boolean isPresent(Map<Integer, Collection<Mutation>> mutation, AaAlignment aaAlignment);

  ResistanceMutationMatch buildMatch(Map<Integer, Collection<Mutation>> mutation, AaAlignment aaAlignment);
}
