package net.cgps.wgsa.paarsnp.core.snpar.codonmapping;

import net.cgps.wgsa.paarsnp.core.lib.utils.DnaSequence;

import java.util.BitSet;
import java.util.Objects;

public class FrameshiftFilter {
  private final BitSet filter;

  public FrameshiftFilter(final BitSet filter) {
    this.filter = filter;
  }

  public boolean checkCodon(final int index) {
    final var bitIndex = DnaSequence.ntIndexFromCodon(index);
    return this.filter.get(bitIndex) || this.filter.get(bitIndex + 1) || this.filter.get(bitIndex + 2);
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    final FrameshiftFilter that = (FrameshiftFilter) o;
    return Objects.equals(this.filter, that.filter);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.filter);
  }

  @Override
  public String toString() {
    return "FrameshiftFilter = " + this.filter;
  }
}
