package net.cgps.wgsa.paarsnp.core.paar.json;

import java.util.Objects;

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

  public String getType() {
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

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || this.getClass() != o.getClass()) return false;
    final ResistanceGene that = (ResistanceGene) o;
    return Float.compare(that.coverage, this.coverage) == 0 &&
        Float.compare(that.pid, this.pid) == 0 &&
        Objects.equals(this.familyName, that.familyName) &&
        Objects.equals(this.type, that.type);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.familyName, this.coverage, this.pid, this.type);
  }
}
