package net.cgps.wgsa.paarsnp.core.snpar.json;

import net.cgps.wgsa.paarsnp.core.lib.json.ResistanceSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Snpar {

  private final Map<String, SnparReferenceSequence> genes;
  private final Map<String, ResistanceSet> sets;
  private double minimumPid;

  public Snpar() {
    this(new ArrayList<>(100), new ArrayList<>(500));
  }

  public Snpar(final Collection<SnparReferenceSequence> genes, final Collection<ResistanceSet> sets) {

    this.genes = genes.stream().collect(Collectors.toMap(SnparReferenceSequence::getName, Function.identity()));
    this.sets = sets.stream().collect(Collectors.toMap(ResistanceSet::getName, set -> set));
    this.minimumPid = genes.stream().mapToDouble(SnparReferenceSequence::getPid).min().orElse(100.0);
  }

  public Map<String, SnparReferenceSequence> getGenes() {

    return this.genes;
  }

  public Map<String, ResistanceSet> getSets() {

    return this.sets;
  }

  public double getMinimumPid() {
    return this.minimumPid;
  }

  public void addResistanceGenes(final Map<String, SnparReferenceSequence> genes) {
    this.genes.putAll(genes);

    final double newMinimum = genes
        .values()
        .stream()
        .mapToDouble(SnparReferenceSequence::getPid)
        .min()
        .orElse(100.0);

    if (newMinimum < this.minimumPid) {
      this.minimumPid = newMinimum;
    }
  }

  public void addResistanceSets(final Map<String, ResistanceSet> sets) {

    this.sets.putAll(sets);
  }
}
