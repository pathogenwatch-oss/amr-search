package net.cgps.wgsa.paarsnp.builder;

import net.cgps.wgsa.paarsnp.core.models.variants.Variant;
import net.cgps.wgsa.paarsnp.core.models.variants.implementations.*;

import java.util.function.Function;

public class ParseVariant implements Function<String, Variant> {

  private final int referenceLength;

  public ParseVariant(final int referenceLength) {
    this.referenceLength = referenceLength;
  }

  @Override
  public Variant apply(final String encoding) {
    if ("truncated".equals(encoding.toLowerCase())) {
      return new PrematureStop(this.referenceLength);
    } else if ("frameshift".equals(encoding.toLowerCase())) {
      return new Frameshift();
    } else if (encoding.toLowerCase().startsWith("aa_insert")) {
      final var coords = encoding.split("_")[1].split("-");
      return new AaRegionInsert(encoding, Integer.parseInt(coords[0]), Integer.parseInt(coords[1]));
    } else if (encoding.toLowerCase().startsWith("nt_insert")) {
      final var coords = encoding.split("_")[1].split("-");
      return new NtRegionInsert(encoding, Integer.parseInt(coords[0]), Integer.parseInt(coords[1]));
    } else {
      return ResistanceMutation.build(encoding, new ParseMutation().apply(encoding), this.referenceLength);
    }
  }
}
