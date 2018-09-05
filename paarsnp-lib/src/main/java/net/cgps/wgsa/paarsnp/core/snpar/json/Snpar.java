package net.cgps.wgsa.paarsnp.core.snpar.json;

import net.cgps.wgsa.paarsnp.core.LibraryMetadata;
import net.cgps.wgsa.paarsnp.core.lib.json.ResistanceSet;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Snpar implements LibraryMetadata {

  private final Map<String, SnparReferenceSequence> genes;
  private final Map<String, ResistanceSet<SnparMember>> sets;
  private final double minimumPid;

  public Snpar(final Collection<SnparReferenceSequence> genes, final Collection<ResistanceSet<SnparMember>> sets) {

    this.genes = genes.stream().collect(Collectors.toMap(SnparReferenceSequence::getName, Function.identity()));
    this.sets = sets.stream().collect(Collectors.toMap(ResistanceSet::getName, set -> set));
    this.minimumPid = genes.stream().mapToDouble(SnparReferenceSequence::getPid).min().orElse(100.0);
  }

  public Map<String, SnparReferenceSequence> getGenes() {

    return this.genes;
  }

  public Map<String, ResistanceSet<SnparMember>> getSets() {

    return this.sets;
  }

  @Override
  public double getMinimumPid() {
    return this.minimumPid;
  }
}
