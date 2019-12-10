package net.cgps.wgsa.paarsnp.core.models.results;

import java.util.Set;

public interface HasVariants {

  String getGene();

  Set<String> getVariants();

}
