package net.cgps.wgsa.paarsnp.core.paar.json;

import net.cgps.wgsa.paarsnp.core.lib.json.ResistanceSet;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Paar {

  private final Map<String, ResistanceGene> genes;
  private final Map<String, ResistanceSet> sets;
  private final double minimumPid;

  private Paar() {
    this(Collections.emptyList(), Collections.emptyList());
  }

  public Paar(final List<ResistanceGene> genes, final List<ResistanceSet> sets) {

    this.genes = genes.stream().collect(Collectors.toMap(ResistanceGene::getFamilyName, Function.identity()));
    this.sets = sets.stream().collect(Collectors.toMap(ResistanceSet::getName, Function.identity()));
    this.minimumPid = genes.stream().mapToDouble(ResistanceGene::getPid).min().orElse(100.0);
  }

  public Map<String, ResistanceGene> getGenes() {

    return this.genes;
  }

  public Map<String, ResistanceSet> getSets() {

    return this.sets;
  }

  public double getMinimumPid() {

    return this.minimumPid;
  }
}
