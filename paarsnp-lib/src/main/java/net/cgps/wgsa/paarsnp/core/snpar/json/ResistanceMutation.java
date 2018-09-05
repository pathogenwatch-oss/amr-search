package net.cgps.wgsa.paarsnp.core.snpar.json;

import org.apache.commons.lang3.CharUtils;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResistanceMutation {

  private final static Pattern snpIdPattern = Pattern.compile("^([\\-A-Z]+)([0-9]+)([A-Z\\-]+)$");

  private final String name;
  private final char originalSequence;
  private final int repLocation;
  private final char mutationSequence;
  private final int aaLocation;

  public ResistanceMutation(final String name, final char originalSequence, final int repLocation, final char mutationSequence, final int aaLocation) {
    // NB for the SNP archive the query location is the same as the representative location.
    this.name = name;
    this.originalSequence = originalSequence;
    this.repLocation = repLocation;
    this.mutationSequence = mutationSequence;
    this.aaLocation = aaLocation;
  }

  static Function<String, ResistanceMutation> parseSnp() {

    return (snpName) -> {

      final Matcher matcher = snpIdPattern.matcher(snpName);

      final char originalSequence = CharUtils.toChar(matcher.group(1));
      final int rawPosition = Integer.valueOf(matcher.group(2));
      final char mutationSequence = CharUtils.toChar(matcher.group(3));

      return new ResistanceMutation(snpName, originalSequence, rawPosition, mutationSequence, rawPosition);
    };

  }

  static Function<String, ResistanceMutation> parseAaVariant() {
    return (snpName) -> {
      final Matcher matcher = snpIdPattern.matcher(snpName);

      final char originalSequence = CharUtils.toChar(matcher.group(1));
      final int rawPosition = Integer.valueOf(matcher.group(2));
      final char mutationSequence = CharUtils.toChar(matcher.group(3));

      final int representativePosition = (rawPosition * 3) - 2;

      return new ResistanceMutation(snpName, originalSequence, representativePosition, mutationSequence, rawPosition);
    };
  }

  public int getRepLocation() {
    return this.repLocation;
  }

  public char getMutationSequence() {
    return this.mutationSequence;
  }

  public String getName() {

    return this.name;
  }

  public int getAaLocation() {
    return this.aaLocation;
  }

  private char getOriginalSequence() {
    return this.originalSequence;
  }
}
