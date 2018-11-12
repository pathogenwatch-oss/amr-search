package net.cgps.wgsa.paarsnp.core.formats;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class SetMember {

  private final String gene;
  private final List<String> variants;

  private SetMember() {
    this("", Collections.emptyList());
  }

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

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || this.getClass() != o.getClass()) return false;
    final SetMember setMember = (SetMember) o;
    return Objects.equals(this.gene, setMember.gene) &&
        Objects.equals(this.variants, setMember.variants);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.gene, this.variants);
  }

  @Override
  public String toString() {
    return "SetMember{" +
        "gene='" + this.gene + '\'' +
        ", variants=" + this.variants +
        '}';
  }
}
