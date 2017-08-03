package net.cgps.wgsa.paarsnp.core.paar;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ResistanceGene {

  private final String checksum;
  private final Set<String> resistanceSetNames;
  private final String familyName;
  private final int length;
  private final float lengthThreshold;
  private final float similarityThreshold;
  private final ResistanceGene.EFFECT effect;

  @SuppressWarnings("unused")
  private ResistanceGene() {

    this("aa", "", "", 0, 0, 0, EFFECT.RESISTANT);
  }

  public ResistanceGene(final String checksum, final String setName, final String familyName, final int length, final float lengthThreshold, final float similarityThreshold, final EFFECT effect) {

    this.checksum = checksum;
    this.effect = effect;
    this.resistanceSetNames = new HashSet<>(5);
    this.resistanceSetNames.add(setName);
    this.familyName = familyName;
    this.length = length;
    this.lengthThreshold = lengthThreshold;
    this.similarityThreshold = similarityThreshold;
  }

  public EFFECT getEffect() {

    return this.effect;
  }

  public String getFamilyName() {

    return this.familyName;
  }

  public int getLength() {

    return this.length;
  }

  public float getLengthThreshold() {

    return this.lengthThreshold;
  }

  public String getChecksum() {

    return this.checksum;
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
    result = (prime * result) + this.length;
    result = (prime * result) + ((null == this.checksum) ? 0 : this.checksum.hashCode());
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
    if (this.length != other.length) {
      return false;
    }
    if (this.checksum == null) {
      if (other.checksum != null) {
        return false;
      }
    } else if (!this.checksum.equals(other.checksum)) {
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
        "checksum=" + this.checksum +
        ", resistanceSetNames='" + this.resistanceSetNames + '\'' +
        ", familyName='" + this.familyName + '\'' +
        ", length=" + this.length +
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

  public enum EFFECT {
    RESISTANT, INDUCED, MODIFIES_SUPPRESSES, MODIFIES_INDUCED, MODIFIES_RESISTANT
  }
}
