package net.cgps.wgsa.paarsnp.core.paar;

import com.fasterxml.jackson.annotation.JsonIgnore;
import net.cgps.wgsa.paarsnp.core.LibraryMetadata;
import net.cgps.wgsa.paarsnp.core.lib.json.AbstractJsonnable;
import net.cgps.wgsa.paarsnp.core.lib.json.ResistanceSet;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

public class PaarLibrary extends AbstractJsonnable implements LibraryMetadata {

  private final Map<String, ResistanceGene> resistanceGenes;
  private final Map<String, ResistanceSet> resistanceSets;
  private final String speciesId;
  private final double minimumPid;

  @SuppressWarnings("unused")
  private PaarLibrary() {

    this(Collections.emptyMap(), Collections.emptyMap(), "", 80.0);
  }

  public PaarLibrary(final Map<String, ResistanceGene> resistanceGenes, final Map<String, ResistanceSet> resistanceSets, final String speciesId, final double minimumPid) {

    this.resistanceGenes = resistanceGenes;
    this.resistanceSets = resistanceSets;
    this.speciesId = speciesId;
    this.minimumPid = minimumPid;
  }

  public Map<String, ResistanceGene> getResistanceGenes() {

    return this.resistanceGenes;
  }

  public Map<String, ResistanceSet> getResistanceSets() {

    return this.resistanceSets;
  }

  @JsonIgnore
  Collection<ResistanceGene> getPaarGeneSet(final String resistanceGeneSetId) {

    return this.resistanceSets.get(resistanceGeneSetId).getElementIds()
        .stream()
        .map(resistanceGenes::get)
        .collect(Collectors.toSet());
  }


  public ResistanceGene getPaarGene(final String geneId) {

    return this.resistanceGenes.get(geneId);
  }

  @JsonIgnore
  ResistanceSet getSetById(final String id) {

    return this.resistanceSets.get(id);
  }

  @Override
  public String getSpeciesId() {

    return this.speciesId;
  }

  @Override
  public double getMinimumPid() {

    return this.minimumPid;
  }
}
