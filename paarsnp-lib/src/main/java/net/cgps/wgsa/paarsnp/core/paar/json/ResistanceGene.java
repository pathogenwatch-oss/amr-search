package net.cgps.wgsa.paarsnp.core.paar.json;

public class ResistanceGene {

  private final String familyName;
  private final float coverage;
  private final float pid;
  private final String type = "Protein";

  public ResistanceGene(final String familyName, final float coverage, final float pid) {

    this.familyName = familyName;
    this.coverage = coverage;
    this.pid = pid;
  }

  public String getFamilyName() {

    return this.familyName;
  }

  public float getCoverage() {

    return this.coverage;
  }

  public float getPid() {

    return this.pid;
  }

  private String getType() {
    return this.type;
  }

  @Override
  public String toString() {
    return "ResistanceGene{" +
        "familyName='" + this.familyName + '\'' +
        ", coverage=" + this.coverage +
        ", pid=" + this.pid +
        ", type='" + this.type + '\'' +
        '}';
  }
}
