package net.cgps.wgsa.paarsnp.core.models.variants;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VariantParser implements Function<String, Map.Entry<Integer, Map.Entry<Character, Character>>> {

  private final static Pattern snpIdPattern = Pattern.compile("^(-|ins|[A-Za-z])(-?[0-9]+)(-|del|[A-Za-z])$");

  @Override
  public Map.Entry<Integer, Map.Entry<Character, Character>> apply(final String variantEncoding) {
    final Matcher matcher = snpIdPattern.matcher(variantEncoding);
    if (matcher.find()) {
      return new ImmutablePair<>(
          Integer.valueOf(matcher.group(2)),
          new ImmutablePair<>(
              "ins".equals(matcher.group(1)) ? '-' : CharUtils.toChar(matcher.group(1)),
              "del".equals(matcher.group(3)) ? '-' : CharUtils.toChar(matcher.group(3))
          ));
    } else {
      throw new RuntimeException("Unable to parse variant encoding: " + variantEncoding);
    }
  }
}
