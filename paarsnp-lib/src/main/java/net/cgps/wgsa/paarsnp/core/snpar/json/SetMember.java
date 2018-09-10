package net.cgps.wgsa.paarsnp.core.snpar.json;

import java.util.List;

public class SetMember {

  private final String gene;
  private final List<String> variants;

  public SetMember(final String gene, final List<String> variants) {
    this.gene = gene;
    this.variants = variants;
  }

  public List<String> getVariants() {
    return this.variants;
  }

  public String getGene() {
    return this.gene;
  }
}
