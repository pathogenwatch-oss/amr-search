package net.cgps.wgsa.paarsnp.core.formats;

import net.cgps.wgsa.paarsnp.core.lib.json.ResistanceSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Paar {

  private final Map<String, ReferenceSequence> genes;
  private final Map<String, ResistanceSet> sets;
  private double minimumPid;

  public Paar() {
    this(new ArrayList<>(1000), new ArrayList<>(1000));
  }

  public Paar(final List<ReferenceSequence> genes, final List<ResistanceSet> sets) {

    this.genes = genes.stream().collect(Collectors.toMap(ReferenceSequence::getName, Function.identity()));
    this.sets = sets.stream().collect(Collectors.toMap(ResistanceSet::getName, Function.identity()));
    this.minimumPid = genes.stream().mapToDouble(ReferenceSequence::getPid).min().orElse(100.0);
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

  public void addResistanceGenes(final Map<String, ReferenceSequence> resistanceGene) {

    this.genes.putAll(resistanceGene);

    final double newMinimum = resistanceGene
        .values()
        .stream()
        .mapToDouble(ReferenceSequence::getPid)
        .min()
        .orElse(100.0);

    if (newMinimum < this.minimumPid) {
      this.minimumPid = newMinimum;
    }
  }

  public void addRecords(final Map<String, ResistanceSet> sets) {

    // TODO: Update this to integrate phenotypes.

    this.sets.putAll(sets);
  }
}
