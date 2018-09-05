package net.cgps.wgsa.paarsnp.core.lib.json;

import java.util.List;

public class ResistanceSet<T> {

  private final List<Phenotype> phenotypes;
  private final String name;
  private final List<T> members;

  public ResistanceSet(final String name, final List<Phenotype> phenotypes, final List<T> members) {

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

  public List<T> getMembers() {
    return this.members;
  }
}
