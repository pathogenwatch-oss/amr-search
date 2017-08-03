package net.cgps.wgsa.paarsnp.core.paar;

import java.util.Collection;
import java.util.Collections;

public class PaarAntibioticSummary {

  private final String antibiotic;
  private final Collection<PaarGeneSummary> genes;

  private PaarAntibioticSummary() {

    this("", Collections.emptyList());
  }

  public PaarAntibioticSummary(final String antibiotic, final Collection<PaarGeneSummary> genes) {

    this.antibiotic = antibiotic;
    this.genes = genes;
  }

  public String getAntibiotic() {

    return this.antibiotic;
  }

  public Collection<PaarGeneSummary> getGenes() {

    return this.genes;
  }
}
