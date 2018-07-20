package net.cgps.wgsa.paarsnp.core.paar.json;

import net.cgps.wgsa.paarsnp.core.lib.json.AbstractJsonnable;

public class PaarGeneSummary extends AbstractJsonnable {

  private final String paarGene;

  private final String setName;

  private PaarGeneSummary() {

    this("", "");
  }

  public PaarGeneSummary(final String paarGene, final String setName) {

    this.paarGene = paarGene;
    this.setName = setName;
  }

  public String getPaarGene() {

    return this.paarGene;
  }

  public String getSetName() {

    return this.setName;
  }
}
