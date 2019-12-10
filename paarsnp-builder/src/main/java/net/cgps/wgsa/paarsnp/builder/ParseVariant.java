package net.cgps.wgsa.paarsnp.builder;

import net.cgps.wgsa.paarsnp.core.models.variants.Variant;
import net.cgps.wgsa.paarsnp.core.models.variants.implementations.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public class ParseVariant implements Function<String, Variant> {

  public final static Set<Character> dnaCharacters = new HashSet<>(Arrays.asList('a', 't', 'c', 'g'));

  private final int referenceLength;

  public ParseVariant(final int referenceLength) {
    this.referenceLength = referenceLength;
  }

  @Override
  public Variant apply(final String encoding) {
    if ("truncated".equals(encoding.toLowerCase())) {
      return new PrematureStop(this.referenceLength / 3);
    } else if ("frameshift".equals(encoding.toLowerCase())) {
      return new Frameshift();
    } else if ("disrupted".equals(encoding.toLowerCase())) {
      return new Disrupted(this.referenceLength / 3);
    } else if (encoding.toLowerCase().startsWith("aa_insert")) {
      final var coords = encoding.split("_")[2].split("-");
      return new AaRegionInsert(encoding, Integer.parseInt(coords[0]), Integer.parseInt(coords[1]));
    } else if (encoding.toLowerCase().startsWith("nt_insert")) {
      final var coords = encoding.split("_")[2].split("-");
      return new NtRegionInsert(encoding, Integer.parseInt(coords[0]), Integer.parseInt(coords[1]));
    } else {
      final var variant = new ParseMutation().apply(encoding);
      if (dnaCharacters.contains(variant.getValue().getKey().charAt(0)) || dnaCharacters.contains(variant.getValue().getValue().charAt(0))) {
        return NtResistanceMutation.build(encoding, variant, this.referenceLength);
      } else {
        return AaResistanceMutation.build(encoding, variant, this.referenceLength);
      }
    }
  }
}
