package net.cgps.wgsa.paarsnp.core.models.variants;

import net.cgps.wgsa.paarsnp.core.lib.blast.Mutation;
import net.cgps.wgsa.paarsnp.core.snpar.CodonMap;

import java.util.Collection;
import java.util.Map;

public interface TranscribedVariant extends Variant<Map<Integer, Collection<Mutation>>, CodonMap> {
  // Provides a defined type of variant.
}
