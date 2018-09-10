package net.cgps.wgsa.paarsnp.core.lib.json;

import net.cgps.wgsa.paarsnp.core.snpar.json.SetMember;

import java.util.List;

public class ResistanceSet {

  private final List<Phenotype> phenotypes;
  private final String name;
  private final List<SetMember> members;

  public ResistanceSet(final String name, final List<Phenotype> phenotypes, final List<SetMember> members) {

    this.name = name;
    this.phenotypes = phenotypes;
    this.members = members;
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
}
