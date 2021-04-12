package net.cgps.wgsa.paarsnp.output;

import net.cgps.wgsa.paarsnp.core.lib.AbstractJsonnable;

import java.util.Collection;
import java.util.Collections;

public class DeterminantsProfile extends AbstractJsonnable {
  private final Collection<String> acquired;
  private final Collection<String> variants;

  private DeterminantsProfile() {
    this(Collections.emptyList(), Collections.emptyList());
  }

  public DeterminantsProfile(final Collection<String> acquired, final Collection<String> variants) {
    this.acquired = acquired;
    this.variants = variants;
  }

  private Collection<String> getAcquired() {
    return acquired;
  }

  private Collection<String> getVariants() {
    return variants;
  }
}
