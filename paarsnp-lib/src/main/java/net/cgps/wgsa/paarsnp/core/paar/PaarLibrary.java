package net.cgps.wgsa.paarsnp.core.paar;

import net.cgps.wgsa.paarsnp.core.lib.json.AbstractJsonnable;
import net.cgps.wgsa.paarsnp.core.lib.json.ResistanceSet;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

public class PaarLibrary extends AbstractJsonnable {

  private final Collection<ResistanceGene> resistanceGenes;
  private final Collection<ResistanceSet> resistanceSets;
  private final Collection<PaarAntibioticSummary> amrSummary;
  private final String speciesId;
  private final double minimumPid;

  @SuppressWarnings("unused")
  private PaarLibrary() {

    this(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), "", 80.0);
  }

  public PaarLibrary(final Collection<ResistanceGene> resistanceGenes, final Collection<ResistanceSet> resistanceSets, final Collection<PaarAntibioticSummary> amrSummary, final String speciesId, final double minimumPid) {

    this.resistanceGenes = resistanceGenes;
    this.resistanceSets = resistanceSets;
    this.amrSummary = amrSummary;
    this.speciesId = speciesId;
    this.minimumPid = minimumPid;
  }

  public Collection<ResistanceGene> getResistanceGenes() {

    return this.resistanceGenes;
  }

  public Collection<ResistanceSet> getResistanceSets() {

    return this.resistanceSets;
  }

  public Collection<ResistanceGene> getPaarGeneSet(final String resistanceGeneSetId) {

    return this.resistanceSets
        .stream()
        .filter(resistanceSet -> resistanceSet.getResistanceSetName().equals(resistanceGeneSetId))
        .findFirst()
        .get()
        .getElementIds() // Get all the gene IDs for the set and map them to the corresponding resistance gene.
        .stream()
        .map(geneId -> this.getPaarGene(geneId).get())
        .collect(Collectors.toSet());
  }


  public Optional<ResistanceGene> getPaarGene(final String geneId) {

    return this
        .resistanceGenes
        .stream()
        .filter(resistanceGene -> resistanceGene.getFamilyName().equals(geneId))
        .findFirst();
  }

  public Optional<ResistanceSet> getSetById(final String id) {

    return this.resistanceSets
        .stream()
        .filter(set -> set.getResistanceSetName().equals(id))
        .findFirst();
  }

  public Collection<PaarAntibioticSummary> getAmrSummary() {

    return this.amrSummary;
  }

  public String getSpeciesId() {

    return this.speciesId;
  }

  public double getMinimumPid() {

    return this.minimumPid;
  }
}
