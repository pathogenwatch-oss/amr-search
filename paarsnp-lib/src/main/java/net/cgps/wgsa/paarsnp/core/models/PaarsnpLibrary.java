package net.cgps.wgsa.paarsnp.core.models;

import net.cgps.wgsa.paarsnp.core.lib.AbstractJsonnable;
import net.cgps.wgsa.paarsnp.core.models.results.AntimicrobialAgent;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PaarsnpLibrary extends AbstractJsonnable {

  private final String label;
  private final List<AntimicrobialAgent> antimicrobials;
  private final Map<String, ReferenceSequence> genes;
  private final Map<String, ResistanceSet> sets;
  private double minimumPid;

  @SuppressWarnings("unused")
  private PaarsnpLibrary() {
    this("");
  }

  public PaarsnpLibrary(final String label) {
    this(label, new ArrayList<>(50), new ArrayList<>(500), new ArrayList<>(500));
  }

  public PaarsnpLibrary(final String label, final List<AntimicrobialAgent> antimicrobials, final Collection<ReferenceSequence> genes, final Collection<ResistanceSet> sets) {
    this.label = label;
    this.antimicrobials = antimicrobials;
    this.genes = genes.stream().collect(Collectors.toMap(ReferenceSequence::getName, Function.identity()));
    this.sets = sets.stream().collect(Collectors.toMap(ResistanceSet::getName, set -> set));
    this.minimumPid = genes.stream().mapToDouble(ReferenceSequence::getPid).min().orElse(100.0);
  }

  public String getLabel() {
    return this.label;
  }

  public List<AntimicrobialAgent> getAntimicrobials() {
    return this.antimicrobials;
  }

  public Map<String, ReferenceSequence> getGenes() {
    return this.genes;
  }

  public Map<String, ResistanceSet> getSets() {
    return this.sets;
  }

  public double getMinimumPid() {
    return this.minimumPid;
  }

  public void addResistanceGenes(final Map<String, ReferenceSequence> genes) {
    this.genes.putAll(genes);

    final double newMinimum = genes
        .values()
        .stream()
        .mapToDouble(ReferenceSequence::getPid)
        .min()
        .orElse(100.0);

    if (newMinimum < this.minimumPid) {
      this.minimumPid = newMinimum;
    }
  }

  public void addRecords(final Map<String, ResistanceSet> newSets) {

    // First update already existing sets.
    newSets.entrySet()
        .stream()
        .filter(set -> this.sets.containsKey(set.getKey()))
        .forEach(updatedSet -> {
          final ResistanceSet originalSet = this.sets.get(updatedSet.getKey());
          originalSet.updatePhenotypes(updatedSet.getValue().getPhenotypes());
        });


    // Finally add new sets
    this.sets.putAll(newSets.entrySet()
        .stream()
        .filter(set -> !this.sets.containsKey(set.getKey()))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

  }

  public PaarsnpLibrary merge(final PaarsnpLibrary that) {

    this.addAntimicrobials(that.getAntimicrobials());
    this.addResistanceGenes(that.getGenes());
    this.addRecords(that.getSets());

    return this;
  }

  public void addAntimicrobials(final List<AntimicrobialAgent> antimicrobials) {
    // Only add new antibiotics, preserve order
    this.antimicrobials.addAll(
        antimicrobials
            .stream()
            .filter(antimicrobialAgent -> !this.antimicrobials.contains(antimicrobialAgent))
            .collect(Collectors.toList()));

  }
}
