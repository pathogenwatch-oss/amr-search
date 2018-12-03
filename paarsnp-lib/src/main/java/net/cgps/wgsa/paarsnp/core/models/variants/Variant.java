package net.cgps.wgsa.paarsnp.core.models.variants;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import net.cgps.wgsa.paarsnp.core.lib.blast.BlastMatch;
import net.cgps.wgsa.paarsnp.core.lib.blast.Mutation;
import net.cgps.wgsa.paarsnp.core.models.ResistanceMutationMatch;
import net.cgps.wgsa.paarsnp.core.snpar.CodonMap;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

@JsonDeserialize(using = VariantDeserializer.class)
public interface Variant {

  String getName();

  Optional<ResistanceMutationMatch> isPresent(Map<Integer, Collection<Mutation>> mutations, CodonMap codonMap);

  boolean isWithinBoundaries(BlastMatch match);
}
