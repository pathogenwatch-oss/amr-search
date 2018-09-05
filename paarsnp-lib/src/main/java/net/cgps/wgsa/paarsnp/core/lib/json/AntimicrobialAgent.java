package net.cgps.wgsa.paarsnp.core.lib.json;


import javax.annotation.Nonnull;

/**
 * JSON document for an antimicrobial agent.
 */
public class AntimicrobialAgent extends AbstractJsonnable implements Comparable<AntimicrobialAgent> {

  private final String key;
  private final String name;
  private final String type;


  @SuppressWarnings("unused")
  private AntimicrobialAgent() {

    this("", "", "");
  }

  public AntimicrobialAgent(final String key, String name, String type) {

    this.key = key;
    this.name = name;
    this.type = type;
  }

  public String getKey() {

    return this.key;
  }

  @Override
  public int hashCode() {
    return this.key.hashCode();
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

    return this.key.equals(that.key);
  }

  @Override
  public String toString() {

    return "WgstAntimicrobialAgent{" +
        ", name='" + this.key + '\'' +
        '}';
  }

  @Override
  public int compareTo(@Nonnull final AntimicrobialAgent o) {

    return this.key.compareTo(o.getKey());
  }

  @SuppressWarnings("unused")
  public String getName() {

    return this.name;
  }

  public String getType() {

    return this.type;
  }
}
