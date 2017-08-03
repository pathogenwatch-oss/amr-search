package net.cgps.wgsa.paarsnp.core.snpar.json;

import java.util.Collection;
import java.util.Collections;

public class SnpGeneSummary {

  private final String sequenceId;
  private final Collection<SnpSummary> snps;

  @SuppressWarnings("unused")
  private SnpGeneSummary() {

    this("", Collections.emptyList());
  }

  public SnpGeneSummary(final String sequenceId, final Collection<SnpSummary> snps) {

    this.sequenceId = sequenceId;
    this.snps = snps;
  }

  public String getSequenceId() {

    return this.sequenceId;
  }

  public Collection<SnpSummary> getSnps() {

    return this.snps;
  }
}
