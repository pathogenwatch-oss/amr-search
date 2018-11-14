package net.cgps.wgsa.paarsnp.core.formats;

import net.cgps.wgsa.paarsnp.core.lib.blast.BlastMatch;
import net.cgps.wgsa.paarsnp.core.snpar.CodonMap;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public interface Variant {

  String getName();

  Optional<ResistanceMutationMatch> isPresent(Map<Integer, Collection<Mutation>> mutations, CodonMap codonMap);

  boolean isWithinBoundaries(BlastMatch match);
}
