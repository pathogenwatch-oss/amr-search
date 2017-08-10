package net.cgps.wgsa.paarsnp.core.lib.json;


/**
 * JSON document for an antimicrobial agent.
 */
public class AntimicrobialAgent extends AbstractJsonnable implements Comparable<AntimicrobialAgent> {

  private final String name;
  private final String fullName;
  private final String type;


  @SuppressWarnings("unused")
  private AntimicrobialAgent() {

    this("", "", "");
  }

  public AntimicrobialAgent(final String name, String fullName, String type) {

    this.name = name;
    this.fullName = fullName;
    this.type = type;
  }

  public String getName() {

    return this.name;
  }

  @Override
  public int hashCode() {
    return name.hashCode();
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

    return this.name.equals(that.name);
  }

  @Override
  public String toString() {

    return "WgstAntimicrobialAgent{" +
        ", name='" + this.name + '\'' +
        '}';
  }

  @Override
  public int compareTo(final AntimicrobialAgent o) {

    return this.name.compareTo(o.getName());
  }

  @SuppressWarnings("unused")
  public String getFullName() {

    return this.fullName;
  }

  public String getType() {

    return this.type;
  }
}
