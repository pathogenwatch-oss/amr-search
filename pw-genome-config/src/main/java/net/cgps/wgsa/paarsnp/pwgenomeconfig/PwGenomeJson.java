package net.cgps.wgsa.paarsnp.pwgenomeconfig;

import net.cgps.wgsa.paarsnp.core.lib.AbstractJsonnable;
import net.cgps.wgsa.paarsnp.core.models.PhenotypeEffect;

import java.util.*;
import java.util.regex.Pattern;

public class PwGenomeJson extends AbstractJsonnable {

  private static final Pattern variantLocation = Pattern.compile("^[A-Za-z\\-_]*(-?\\d+)([A-Za-z\\-_]*)$");

  private final Collection<PwAgent> antibiotics;
  private final Map<String, Map<String, Set<PwSnpRecord>>> snp;
  private final Map<String, Set<PwPaarRecord>> paar;

  @SuppressWarnings("unused")
  private PwGenomeJson() {
    this(Collections.emptyList(), Collections.emptyMap(), Collections.emptyMap());
  }

  public PwGenomeJson(final Collection<PwAgent> antibiotics, final Map<String, Map<String, Set<PwSnpRecord>>> snp, final Map<String, Set<PwPaarRecord>> paar) {
    this.antibiotics = antibiotics;
    this.snp = snp;
    this.paar = paar;
  }

  public Collection<PwAgent> getAntibiotics() {
    return this.antibiotics;
  }

  public Map<String, Map<String, Set<PwSnpRecord>>> getSnp() {
    return this.snp;
  }

  public Map<String, Set<PwPaarRecord>> getPaar() {
    return this.paar;
  }

  public static class PwSnpRecord implements Comparable<PwSnpRecord> {
    private final String snpName;
    private final PhenotypeEffect effect;

    private PwSnpRecord() {
      this("", PhenotypeEffect.RESISTANT);
    }

    public PwSnpRecord(final String snpName, final PhenotypeEffect effect) {
      this.snpName = snpName;
      this.effect = effect;
    }

    public String getSnpName() {
      return this.snpName;
    }

    public PhenotypeEffect getEffect() {
      return this.effect;
    }

    @Override
    public int hashCode() {
      return Objects.hash(snpName);
    }

    @Override
    public int compareTo(final PwSnpRecord that) {
      if (this.equals(that)) {
        return 0;
      }
      final var thisLocation = location(this.snpName);
      final var otherLocation = location(that.getSnpName());
      if (thisLocation == otherLocation) {
        return this.snpName.compareTo(that.getSnpName());
      }
      return Integer.compare(thisLocation, otherLocation);
    }

    @Override
    public boolean equals(final Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      final PwSnpRecord that = (PwSnpRecord) o;
      return Objects.equals(snpName, that.snpName);
    }

    private int location(final String variant) {
      final var matcher = variantLocation.matcher(variant);
      if (matcher.matches()) {
        return Integer.parseInt(matcher.group(1));
      } else {
        return 1000000;
      }
    }

    @Override
    public String toString() {
      return "PwSnpRecord{" +
          "snpName='" + snpName + '\'' +
          ", effect=" + effect +
          '}';
    }
  }

  public static class PwAgent {
    private final String key;
    private final String displayName;
    private final String fullName;
    private final String antimicrobialClass;

    private PwAgent() {
      this("", "", "", "");
    }

    public PwAgent(final String key, final String displayName, final String fullName, final String antimicrobialClass) {
      this.key = key;
      this.displayName = displayName;
      this.fullName = fullName;
      this.antimicrobialClass = antimicrobialClass;
    }

    public String getAntimicrobialClass() {
      return this.antimicrobialClass;
    }

    public String getFullName() {
      return this.fullName;
    }

    public String getKey() {
      return this.key;
    }

    public String getDisplayName() {
      return this.displayName;
    }

    @Override
    public boolean equals(final Object o) {
      if (this == o) return true;
      if (o == null || this.getClass() != o.getClass()) return false;
      final PwAgent pwAgent = (PwAgent) o;
      return Objects.equals(this.key, pwAgent.key) &&
          Objects.equals(this.displayName, pwAgent.displayName) &&
          Objects.equals(this.fullName, pwAgent.fullName) &&
          Objects.equals(this.antimicrobialClass, pwAgent.antimicrobialClass);
    }

    @Override
    public int hashCode() {
      return Objects.hash(this.key, this.displayName, this.fullName, this.antimicrobialClass);
    }

    @Override
    public String toString() {
      return "PwAgent{" +
          "key='" + key + '\'' +
          ", displayName='" + displayName + '\'' +
          ", fullName='" + fullName + '\'' +
          ", antimicrobialClass='" + antimicrobialClass + '\'' +
          '}';
    }
  }

  public static class PwPaarRecord implements Comparable<PwPaarRecord> {
    private final String element;
    private final PhenotypeEffect effect;

    private PwPaarRecord() {
      this("", PhenotypeEffect.RESISTANT);
    }

    public PwPaarRecord(final String element, final PhenotypeEffect effect) {
      this.element = element;
      this.effect = effect;
    }

    public String getElement() {
      return this.element;
    }

    public PhenotypeEffect getEffect() {
      return this.effect;
    }

    @Override
    public boolean equals(final Object o) {
      if (this == o) return true;
      if (o == null || this.getClass() != o.getClass()) return false;
      final PwPaarRecord that = (PwPaarRecord) o;
      return Objects.equals(this.element, that.element) &&
          this.effect == that.effect;
    }

    @Override
    public int hashCode() {
      return Objects.hash(this.element, this.effect);
    }

    @Override
    public int compareTo(final PwPaarRecord other) {
      return this.element.compareTo(other.getElement());
    }
  }
}
