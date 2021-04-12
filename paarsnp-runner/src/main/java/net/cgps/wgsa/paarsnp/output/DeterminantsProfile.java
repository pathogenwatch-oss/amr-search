package net.cgps.wgsa.paarsnp.output;

import net.cgps.wgsa.paarsnp.core.lib.AbstractJsonnable;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class DeterminantsProfile extends AbstractJsonnable {
  private final Collection<Determinant> acquired;
  private final Collection<Determinant> variants;

  private DeterminantsProfile() {
    this(Collections.emptyList(), Collections.emptyList());
  }

  public DeterminantsProfile(final Collection<Determinant> acquired, final Collection<Determinant> variants) {
    this.acquired = acquired;
    this.variants = variants;
  }

  private Collection<Determinant> getAcquired() {
    return acquired;
  }

  private Collection<Determinant> getVariants() {
    return variants;
  }
}
