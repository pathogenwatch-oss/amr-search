package net.cgps.wgsa.paarsnp.core.paar.json;

import net.cgps.wgsa.paarsnp.core.lib.ElementEffect;
import net.cgps.wgsa.paarsnp.core.lib.json.AbstractJsonnable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ResistanceGene extends AbstractJsonnable {

  private final Set<String> resistanceSetNames;
  private final String familyName;
  private final float lengthThreshold;
  private final float similarityThreshold;
  private final ElementEffect effect;

  @SuppressWarnings("unused")
  private ResistanceGene() {

    this("", "", 0, 0, ElementEffect.RESISTANCE);
  }

  public ResistanceGene(final String setName, final String familyName,final float lengthThreshold, final float similarityThreshold, final ElementEffect effect) {

    this.effect = effect;
    this.resistanceSetNames = new HashSet<>(5);
    this.resistanceSetNames.add(setName);
    this.familyName = familyName;
    this.lengthThreshold = lengthThreshold;
    this.similarityThreshold = similarityThreshold;
  }

  public ElementEffect getEffect() {

    return this.effect;
  }

  public String getFamilyName() {

    return this.familyName;
  }

  public float getLengthThreshold() {

    return this.lengthThreshold;
  }

  public Collection<String> getResistanceSetNames() {

    return this.resistanceSetNames;
  }

  public float getSimilarityThreshold() {

    return this.similarityThreshold;
  }

  @Override
  public int hashCode() {

    final int prime = 31;
    int result = 1;
    result = (prime * result) + ((null == this.familyName) ? 0 : this.familyName.hashCode());
    result = (prime * result) + ((null == this.resistanceSetNames) ? 0 : this.resistanceSetNames.hashCode());
    return result;
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(final Object obj) {

    if (this == obj) {
      return true;
    }
    if (null == obj) {
      return false;
    }
    if (!(obj instanceof ResistanceGene)) {
      return false;
    }
    final ResistanceGene other = (ResistanceGene) obj;
    if (null == this.familyName) {
      if (null != other.familyName) {
        return false;
      }
    } else if (!this.familyName.equals(other.familyName)) {
      return false;
    }
    if (this.resistanceSetNames == null) {
      if (other.resistanceSetNames != null) {
        return false;
      }
    } else if (!this.resistanceSetNames.equals(other.resistanceSetNames)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {

    return "ResistanceGene{" +
        ", resistanceSetNames='" + this.resistanceSetNames + '\'' +
        ", familyName='" + this.familyName + '\'' +
        ", lengthThreshold=" + this.lengthThreshold +
        ", similarityThreshold=" + this.similarityThreshold +
        '}';
  }    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */

  public void addResistanceSetName(final String setName) {

    this.resistanceSetNames.add(setName);
  }

}
