package net.cgps.wgsa.paarsnp.core.snpar.json;

import java.util.Collection;
import java.util.Collections;

public class SnparAntibioticSummary {

  private final String antibiotic;
  private final Collection<SnpGeneSummary> snpGenes;

  @SuppressWarnings("unused")
  private SnparAntibioticSummary() {

    this("", Collections.emptyList());
  }

  public SnparAntibioticSummary(final String antibiotic, final Collection<SnpGeneSummary> snpGenes) {

    this.antibiotic = antibiotic;
    this.snpGenes = snpGenes;
  }

  public Collection<SnpGeneSummary> getSnpGenes() {

    return this.snpGenes;
  }

  public String getAntibiotic() {

    return this.antibiotic;
  }
}
