package net.cgps.wgsa.paarsnp.core.snpar.json;

public class SnpSummary {

  private final String snpId;
  private final String setId;

  @SuppressWarnings("unused")
  private SnpSummary() {

    this("", "");
  }

  public SnpSummary(final String snpId, final String setId) {

    this.snpId = snpId;
    this.setId = setId;
  }

  public String getSetId() {

    return this.setId;
  }

  public String getSnpId() {

    return this.snpId;
  }
}
