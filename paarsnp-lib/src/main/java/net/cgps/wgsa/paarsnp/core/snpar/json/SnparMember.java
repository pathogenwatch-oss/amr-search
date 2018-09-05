package net.cgps.wgsa.paarsnp.core.snpar.json;

import java.util.List;

public class SnparMember {

  private final String gene;
  private final List<String> variants;

  public SnparMember(final String gene, final List<String> variants) {
    this.gene = gene;
    this.variants = variants;
  }

  private List<String> getVariants() {
    return this.variants;
  }

  private String getGene() {
    return this.gene;
  }
}
