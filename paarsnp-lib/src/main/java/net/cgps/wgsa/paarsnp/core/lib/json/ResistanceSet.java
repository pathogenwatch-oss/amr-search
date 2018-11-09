package net.cgps.wgsa.paarsnp.core.lib.json;

import net.cgps.wgsa.paarsnp.core.snpar.json.SetMember;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

public class ResistanceSet extends AbstractJsonnable {

  private final List<Phenotype> phenotypes;
  private final String name;
  private final List<SetMember> members;

  @SuppressWarnings("unused")
  private ResistanceSet() {
    this("", Collections.emptyList(), Collections.emptyList());
  }

  private ResistanceSet(final String name, final List<Phenotype> phenotypes, final List<SetMember> members) {

    this.name = name;
    this.phenotypes = phenotypes;
    this.members = members;
  }

  public static ResistanceSet build(final Optional<String> name, final List<Phenotype> phenotypes, final List<SetMember> members) {
    return new ResistanceSet(name.orElse(generateName(members)), phenotypes, members);
  }

  public static String generateName(final List<SetMember> members) {
    members.sort(Comparator.comparing(SetMember::getGene));
    return members
        .stream()
        .map(member -> {
          member.getVariants().sort(Comparator.comparingInt(variant -> Integer.valueOf(variant.replaceAll("\\D+", ""))));
          return member.getVariants().isEmpty() ?
                 member.getGene() :
                 member.getGene() + "_" + StringUtils.join(member.getVariants(), "_");
        })
        .collect(Collectors.joining("__"));
  }

  public String getName() {

    return this.name;
  }

  public List<Phenotype> getPhenotypes() {
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
}
