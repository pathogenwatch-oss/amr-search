package net.cgps.wgsa.paarsnp.core.models.results;


import net.cgps.wgsa.paarsnp.core.lib.AbstractJsonnable;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * JSON document for an antimicrobial agent.
 */
public class AntimicrobialAgent extends AbstractJsonnable implements Comparable<AntimicrobialAgent> {

  private final String key;
  private final String type;
  private final String name;

  @SuppressWarnings("unused")
  private AntimicrobialAgent() {

    this("", "", "");
  }

  public AntimicrobialAgent(final String key, final String type, final String name) {

    this.key = key;
    this.name = name;
    this.type = type;
  }

  public String getKey() {

    return this.key;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || this.getClass() != o.getClass()) return false;
    final AntimicrobialAgent that = (AntimicrobialAgent) o;
    return Objects.equals(this.key, that.key) &&
        Objects.equals(this.type, that.type) &&
        Objects.equals(this.name, that.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.key, this.type, this.name);
  }

  @Override
  public String toString() {
    return "AntimicrobialAgent{" +
        "key='" + this.key + '\'' +
        ", type='" + this.type + '\'' +
        ", name='" + this.name + '\'' +
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
