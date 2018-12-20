package net.cgps.wgsa.paarsnp.core.models.results;

import net.cgps.wgsa.paarsnp.core.lib.AbstractJsonnable;

public class OldStyleAntimicrobialAgent extends AbstractJsonnable {

  private final String name;
  private final String fullName;
  private final String type;

  @SuppressWarnings("unused")
  private OldStyleAntimicrobialAgent() {
    this("", "", "");
  }

  public OldStyleAntimicrobialAgent(final String name, final String fullName, final String type) {
    this.name = name;
    this.fullName = fullName;
    this.type = type;
  }

  public String getType() {
    return this.type;
  }

  public String getFullName() {
    return this.fullName;
  }

  public String getName() {
    return this.name;
  }
}
