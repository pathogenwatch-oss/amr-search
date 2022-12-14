package net.cgps.wgsa.paarsnp.core.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import net.cgps.wgsa.paarsnp.core.lib.AbstractJsonnable;
import net.cgps.wgsa.paarsnp.core.models.results.HasVariants;

import java.util.*;
import java.util.stream.Stream;

public class SetMember extends AbstractJsonnable implements HasVariants {

  private final String gene;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private final Set<String> variants;

  @SuppressWarnings("unused")
  private SetMember() {
    this("", Collections.emptySet());
  }

  public SetMember(final String gene, final Collection<String> variants) {
    this.gene = gene;
    this.variants = new HashSet<>(variants);
  }

  public Set<String> getVariants() {
    return Collections.unmodifiableSet(this.variants);
  }

  public String getGene() {
    return this.gene;
  }

  public Stream<String> toVariantNames() {
    return this.variants.stream().map(name -> this.gene + "_" + name);
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
