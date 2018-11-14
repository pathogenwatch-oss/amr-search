package net.cgps.wgsa.paarsnp.core.formats;

import net.cgps.wgsa.paarsnp.core.lib.blast.BlastMatch;
import net.cgps.wgsa.paarsnp.core.snpar.CodonMap;
import org.apache.commons.lang3.CharUtils;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ResistanceMutation implements Variant {

  public TYPE getType() {
    return this.type;
  }

  @Override
  public Optional<ResistanceMutationMatch> isPresent(final Map<Integer, Collection<Mutation>> mutations, final CodonMap codonMap) {

    switch (this.type) {
      case DNA:
        if (mutations.containsKey(this.getReferenceLocation()) && mutations.get(this.getReferenceLocation())
            .stream()
            .anyMatch(queryMutation -> queryMutation.getMutationSequence() == this.getMutationSequence())) {

          return Optional.of(new ResistanceMutationMatch(
              this,
              mutations.get(this.getReferenceLocation())
                  .stream()
                  .filter(queryMutation -> queryMutation.getMutationSequence() == this.getMutationSequence())
                  .collect(Collectors.toList())));

        } else {
          return Optional.empty();
        }
      case AA:
      default:
        if (this.getMutationSequence() == codonMap.get(this.getAaLocation())) {
          return Optional.of(new ResistanceMutationMatch(
              this,
              Stream.of(this.getReferenceLocation(), this.getReferenceLocation() + 1, this.getReferenceLocation() + 2)
                  .filter(index -> mutations.keySet().contains(index))
                  .map(mutations::get)
                  .flatMap(Collection::stream)
                  .collect(Collectors.toList())));

        } else {
          return Optional.empty();
        }
    }
  }

  public boolean isWithin(final BlastMatch match) {
    return match.containsPosition(this.referenceLocation)
        && this.getType() == ResistanceMutation.TYPE.DNA || match.containsPosition(this.getReferenceLocation() + 2);
  }

  public enum TYPE {
    DNA, AA
  }

  private final static Pattern snpIdPattern = Pattern.compile("^([\\-A-Za-z]+)([0-9]+)([A-Z\\-a-z]+)$");

  private final String name;
  private final char originalSequence;
  private final int referenceLocation;
  private final char mutationSequence;
  private final TYPE type;
  private final int aaLocation;

  @SuppressWarnings("unused")
  private ResistanceMutation() {
    this("", '0', 0, '0', TYPE.DNA, 0);
  }

  public ResistanceMutation(final String name, final char originalSequence, final int referenceLocation,
                            final char mutationSequence, final TYPE type, final int aaLocation) {
    // NB for the SNP archive the query location is the same as the representative location.
    this.name = name;
    this.originalSequence = originalSequence;
    this.referenceLocation = referenceLocation;
    this.mutationSequence = mutationSequence;
    this.type = type;
    this.aaLocation = aaLocation;
  }

  static Function<String, ResistanceMutation> parseVariant() {

    return (snpName) -> {
      final Matcher matcher = snpIdPattern.matcher(snpName);
      matcher.find();
      final char originalSequence = CharUtils.toChar(matcher.group(1));
      final int rawPosition = Integer.valueOf(matcher.group(2));
      final char mutationSequence = CharUtils.toChar(matcher.group(3));
      final List<Character> dnaCharacters = Arrays.asList('a', 't', 'c', 'g');
      if (dnaCharacters.contains(originalSequence) || dnaCharacters.contains(mutationSequence)) {
        return new ResistanceMutation(snpName, Character.toUpperCase(originalSequence), rawPosition, Character.toUpperCase(mutationSequence), TYPE.DNA, rawPosition);
      } else {
        final int representativePosition = (rawPosition * 3) - 2;

        return new ResistanceMutation(snpName, originalSequence, representativePosition, mutationSequence, TYPE.AA, rawPosition);
      }
    };
  }

//  static Function<String, ResistanceMutation> parseSnp() {
//
//    return (snpName) -> {
//
//      final Matcher matcher = snpIdPattern.matcher(snpName);
//      matcher.find();
//      final char originalSequence = CharUtils.toChar(matcher.group(1));
//      final int rawPosition = Integer.valueOf(matcher.group(2));
//      final char mutationSequence = CharUtils.toChar(matcher.group(3));
//
//      return new ResistanceMutation(snpName, originalSequence, rawPosition, mutationSequence, rawPosition);
//    };
//
//  }
//
//  static Function<String, ResistanceMutation> parseAaVariant() {
//    return (snpName) -> {
//      final Matcher matcher = snpIdPattern.matcher(snpName);
//      matcher.find();
//      final char originalSequence = CharUtils.toChar(matcher.group(1));
//      final int rawPosition = Integer.valueOf(matcher.group(2));
//      final char mutationSequence = CharUtils.toChar(matcher.group(3));
//
//      final int representativePosition = (rawPosition * 3) - 2;
//
//      return new ResistanceMutation(snpName, originalSequence, representativePosition, mutationSequence, rawPosition);
//    };
//  }

  public int getReferenceLocation() {
    return this.referenceLocation;
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

  @SuppressWarnings("unused")
  public char getOriginalSequence() {
    return this.originalSequence;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || this.getClass() != o.getClass()) return false;
    final ResistanceMutation that = (ResistanceMutation) o;
    return this.originalSequence == that.originalSequence &&
        this.referenceLocation == that.referenceLocation &&
        this.mutationSequence == that.mutationSequence &&
        this.aaLocation == that.aaLocation &&
        Objects.equals(this.name, that.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.name, this.originalSequence, this.referenceLocation, this.mutationSequence, this.aaLocation);
  }
}
