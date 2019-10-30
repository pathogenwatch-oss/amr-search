package net.cgps.wgsa.paarsnp.core.snpar.codonmapping;

import java.util.AbstractMap;
import java.util.Map;
import java.util.function.Function;

public class ExtractAlignment implements Function<String, Map.Entry<String, String>> {

  @Override
  public Map.Entry<String, String> apply(final String fastaString) {
    final var lines = fastaString.split("\n");
    return new AbstractMap.SimpleImmutableEntry<>(lines[1], lines[3]);
  }
}
