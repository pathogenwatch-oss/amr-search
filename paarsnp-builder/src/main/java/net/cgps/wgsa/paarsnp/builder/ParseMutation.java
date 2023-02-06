package net.cgps.wgsa.paarsnp.builder;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParseMutation implements Function<String, Map.Entry<Integer, Map.Entry<String, String>>> {

  private final static Pattern snpIdPattern = Pattern.compile("^([*]|-+|ins|[A-Za-z]+)(-?[0-9]+)([*]|-+|del|[A-Za-z]+)$");

  @Override
  public Map.Entry<Integer, Map.Entry<String, String>> apply(final String variantEncoding) {
    final Matcher matcher = snpIdPattern.matcher(variantEncoding);
    if (matcher.find()) {
      return new ImmutablePair<>(
          Integer.valueOf(matcher.group(2)),
          new ImmutablePair<>(
              "ins".equals(matcher.group(1)) ? StringUtils.repeat('-', matcher.group(3).length()) : matcher.group(1),
              "del".equals(matcher.group(3)) ? "-" : matcher.group(3)
          ));
    } else {
      throw new RuntimeException("Unable to parse variant encoding: " + variantEncoding);
    }
  }
}
