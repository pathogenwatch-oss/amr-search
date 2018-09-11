package net.cgps.wgsa.paarsnp.core.snpar.json;

import net.cgps.wgsa.paarsnp.core.LibraryMetadata;
import net.cgps.wgsa.paarsnp.core.lib.json.ResistanceSet;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Snpar implements LibraryMetadata {

  private final Map<String, SnparReferenceSequence> genes;
  private final Map<String, ResistanceSet> sets;
  private final double minimumPid;

  private Snpar() {
    this.genes = Collections.emptyMap();
    this.sets = Collections.emptyMap();
    this.minimumPid = 0.0;
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

  @Override
  public double getMinimumPid() {
    return this.minimumPid;
  }
}
