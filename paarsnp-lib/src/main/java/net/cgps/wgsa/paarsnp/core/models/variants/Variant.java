package net.cgps.wgsa.paarsnp.core.models.variants;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import net.cgps.wgsa.paarsnp.core.lib.Jsonnable;
import net.cgps.wgsa.paarsnp.core.lib.blast.Mutation;
import net.cgps.wgsa.paarsnp.core.models.Location;
import net.cgps.wgsa.paarsnp.core.snpar.AaAlignment;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

@JsonDeserialize(using = VariantJsonDeserializer.class)
public interface Variant extends Jsonnable {

  String getName();

  boolean isWithinBoundaries(int start, int stop);

  Optional<Collection<Location>> match(Map<Integer, Collection<Mutation>> mutations, AaAlignment aaAlignment);
}
