package net.cgps.wgsa.paarsnp.core.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import net.cgps.wgsa.paarsnp.core.lib.AbstractJsonnable;
import net.cgps.wgsa.paarsnp.core.models.results.AntimicrobialAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PaarsnpLibrary extends AbstractJsonnable {

  private final Logger logger = LoggerFactory.getLogger(PaarsnpLibrary.class);
  private final String label;
  private final LibraryVersion version;
  private final List<AntimicrobialAgent> antimicrobials;
  private final Map<String, ReferenceSequence> genes;
  private final Map<String, ResistanceSet> sets;
  private double minimumPid;
  @JsonIgnore
  private final Set<String> agentKeys;

  @SuppressWarnings("unused")
  private PaarsnpLibrary() {
    this("", null, Collections.emptyList());
  }

  public PaarsnpLibrary(final String label, final LibraryVersion version, final List<AntimicrobialAgent> antimicrobials) {
    this(label, version, antimicrobials, new ArrayList<>(500), new ArrayList<>(500));
  }

  public PaarsnpLibrary(final String label, final LibraryVersion version, final List<AntimicrobialAgent> antimicrobials, final Collection<ReferenceSequence> genes, final Collection<ResistanceSet> sets) {
    this.label = label;
    this.version = version;
    this.antimicrobials = antimicrobials;
    this.agentKeys = antimicrobials.stream().map(AntimicrobialAgent::getKey).collect(Collectors.toSet());
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

  public LibraryVersion getVersion() {
    return this.version;
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

  public void merge(final PaarsnpLibrary that) {

    this.addResistanceGenes(that.getGenes());
    this.addRecords(that.getSets());
  }

  public void addRecords(final Map<String, ResistanceSet> newSets) {

    // First update already existing sets.
    newSets.entrySet()
        .stream()
        .filter(set -> this.sets.containsKey(set.getKey()))
        .forEach(updateSet -> this.sets
            .get(updateSet.getKey())
            .updatePhenotypes(updateSet
                .getValue()
                .getPhenotypes()
                .stream()
                .map(this::checkPhenotypeIsValid)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList())));

    // Finally add new sets
    this.sets.putAll(newSets.values()
        .stream()
        .filter(set -> !this.sets.containsKey(set.getName()))
        .map(set -> new ResistanceSet(
            set.getName(),
            set.getPhenotypes()
                .stream()
                .map(this::checkPhenotypeIsValid)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList()),
            set.getMembers()))
        .collect(Collectors.toMap(ResistanceSet::getName, Function.identity())));
  }

  private Optional<Phenotype> checkPhenotypeIsValid(final Phenotype phenotype) {
    if (this.agentKeys.containsAll(phenotype.getProfile())) {
      return Optional.of(phenotype);
    } else if (phenotype.getProfile().stream().anyMatch(this.agentKeys::contains)) {
      return Optional.of(new Phenotype(
          phenotype.getEffect(),
          phenotype.getProfile().stream().filter(this.agentKeys::contains).collect(Collectors.toList()),
          phenotype.getModifiers()));
    }
    return Optional.empty();
  }

  public void addAntimicrobials(final List<AntimicrobialAgent> antimicrobials) {

    this.antimicrobials.clear();
    this.antimicrobials.addAll(antimicrobials);
  }
}
