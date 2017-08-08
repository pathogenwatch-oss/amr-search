package net.cgps.wgsa.paarsnp.core.lib;


/**
 * JSON document for an antimicrobial agent.
 */
public class AntimicrobialAgent implements Comparable<AntimicrobialAgent> {

  private final String name;

  @SuppressWarnings("unused")
  private AntimicrobialAgent() {

    this("");
  }

  public AntimicrobialAgent(final String name) {

    this.name = name;
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
}
