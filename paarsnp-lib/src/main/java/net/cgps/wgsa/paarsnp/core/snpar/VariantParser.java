package net.cgps.wgsa.paarsnp.core.snpar;

import net.cgps.wgsa.paarsnp.core.models.variants.Frameshift;
import net.cgps.wgsa.paarsnp.core.models.variants.PrematureStop;
import net.cgps.wgsa.paarsnp.core.models.variants.ResistanceMutation;
import net.cgps.wgsa.paarsnp.core.models.variants.Variant;
import org.apache.commons.lang3.CharUtils;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VariantParser implements Function<String, Variant> {

  private final static Pattern snpIdPattern = Pattern.compile("^([\\-A-Za-z]+)([0-9]+)([A-Z\\-a-z]+)$");

  private final int referenceLength;

  public VariantParser(final int referenceLength) {

    this.referenceLength = referenceLength;
  }

  @Override
  public Variant apply(final String variantEncoding) {
    if ("truncated".equals(variantEncoding.toLowerCase())) {
      return new PrematureStop(this.referenceLength);
    } else if ("frameshift".equals(variantEncoding.toLowerCase())) {
      return new Frameshift();
    } else {

      final Matcher matcher = snpIdPattern.matcher(variantEncoding);
      if (matcher.find()) {
        final char originalSequence = CharUtils.toChar(matcher.group(1));
        final int rawPosition = Integer.valueOf(matcher.group(2));
        final char mutationSequence = CharUtils.toChar(matcher.group(3));
        final List<Character> dnaCharacters = Arrays.asList('a', 't', 'c', 'g');
        if (dnaCharacters.contains(originalSequence) || dnaCharacters.contains(mutationSequence)) {
          return new ResistanceMutation(variantEncoding, Character.toUpperCase(originalSequence), rawPosition, Character.toUpperCase(mutationSequence), ResistanceMutation.TYPE.DNA, rawPosition);
        } else {
          final int representativePosition = (rawPosition * 3) - 2;

          return new ResistanceMutation(variantEncoding, originalSequence, representativePosition, mutationSequence, ResistanceMutation.TYPE.AA, rawPosition);
        }
      } else {
        throw new RuntimeException("Unable to parse variant encoding: " + variantEncoding);
      }
    }
  }
}
