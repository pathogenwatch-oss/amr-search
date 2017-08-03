package net.cgps.wgsa.paarsnp.core.lib;


/**
 * JSON document for an antimicrobial agent.
 */
public class AntimicrobialAgent implements Comparable<AntimicrobialAgent> {

  private final String fullName;
  private final String shortName;

  @SuppressWarnings("unused")
  private AntimicrobialAgent() {

    this("", "");
  }

  public AntimicrobialAgent(final String fullName, final String shortName) {

    this.shortName = shortName;
    this.fullName = fullName;
  }

  public String getFullName() {

    return this.fullName;
  }

  public String getShortName() {

    return this.shortName;
  }

  @Override
  public int hashCode() {

    return this.fullName.hashCode();
  }

  @Override
  public boolean equals(final Object o) {

    if (this == o) {
      return true;
    }
    if ((null == o) || (this.getClass() != o.getClass())) {
      return false;
    }

    final AntimicrobialAgent that = (AntimicrobialAgent) o;

    return this.fullName.equals(that.fullName) && this.shortName.equals(that.shortName);
  }

  @Override
  public String toString() {

    return "WgstAntimicrobialAgent{" +
        "fullName='" + this.fullName + '\'' +
        ", shortName='" + this.shortName + '\'' +
        '}';
  }

  @Override
  public int compareTo(final AntimicrobialAgent o) {

    return this.fullName.compareTo(o.getFullName());
  }
}
