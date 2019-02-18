package net.cgps.wgsa.paarsnp.core.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import net.cgps.wgsa.paarsnp.core.lib.AbstractJsonnable;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

public class ResistanceSet extends AbstractJsonnable {

  private final Collection<Phenotype> phenotypes;
  private final String name;
  private final List<SetMember> members;
  @JsonIgnore
  private int size = 0;

  @SuppressWarnings("unused")
  private ResistanceSet() {
    this("", Collections.emptySet(), Collections.emptyList());
  }

  public ResistanceSet(final String name, final Collection<Phenotype> phenotypes, final List<SetMember> members) {

    this.name = name;
    this.phenotypes = new HashSet<>(phenotypes);
    this.members = members;

  }

  public static ResistanceSet build(final Optional<String> name, final List<Phenotype> phenotypes, final List<SetMember> members) {
    return new ResistanceSet(name.orElse(generateName(members)), phenotypes, members);
  }

  public static String generateName(final List<SetMember> members) {
    return members
        .stream()
        .map(member -> {
          final List<String> variants = new ArrayList<>(member.getVariants());
          variants.sort(Comparator.comparingInt(variant -> Integer.valueOf(variant.replaceAll("\\D+", ""))));
          return variants.isEmpty() ?
                 member.getGene() :
                 member.getGene() + "_" + StringUtils.join(variants, "_");
        })
        .collect(Collectors.joining("__"));
  }

  public String getName() {

    return this.name;
  }

  public Collection<Phenotype> getPhenotypes() {
    return this.phenotypes;
  }

  public List<SetMember> getMembers() {
    return this.members;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || this.getClass() != o.getClass()) return false;
    final ResistanceSet that = (ResistanceSet) o;
    return Objects.equals(this.phenotypes, that.phenotypes) &&
        Objects.equals(this.name, that.name) &&
        Objects.equals(this.members, that.members);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.phenotypes, this.name, this.members);
  }

  public void addPhenotype(final Phenotype phenotype) {

    final Collection<Phenotype> updatedPhenotypes = new ArrayList<>(this.phenotypes)
        .stream()
        .filter(oldPhenotype -> oldPhenotype.getProfile()
            .stream()
            .noneMatch(antimicrobial -> phenotype.getProfile().contains(antimicrobial)))
        .collect(Collectors.toSet());

    this.phenotypes.clear();

    this.phenotypes.addAll(updatedPhenotypes);

    this.phenotypes.add(phenotype);
  }

  /**
   * Removes any old phenotypes that have antimicrobials in common with the new ones.
   *
   * @param newPhenotypes - updated phenotypes for the resistance set.
   */
  public void updatePhenotypes(final Collection<Phenotype> newPhenotypes) {
    newPhenotypes
        .forEach(this::addPhenotype);
  }

  public int size() {
    if (this.size == 0) {
      this.size = this.members
          .stream()
          .mapToInt(setMember -> {
            if (setMember.getVariants().isEmpty()) {
              return 1;
            } else {
              return setMember.getVariants().size();
            }
          })
          .sum();
    }
    return this.size;
  }
}
