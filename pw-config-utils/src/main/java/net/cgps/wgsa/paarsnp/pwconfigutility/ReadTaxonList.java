package net.cgps.wgsa.paarsnp.pwconfigutility;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;
import java.util.stream.Stream;

public class ReadTaxonList implements Function<Path, Stream<String>> {

  public Stream<String> apply(final Path path) {
    try {
      return Files.readAllLines(path).stream().map(line -> line.split("\\s+")[0]);
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }
}
