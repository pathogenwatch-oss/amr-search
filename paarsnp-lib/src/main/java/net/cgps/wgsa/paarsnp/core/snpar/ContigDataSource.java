package net.cgps.wgsa.paarsnp.core.snpar;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

public class ContigDataSource implements Function<String, String> {

  private final Path inputFasta;

  public ContigDataSource(final Path inputFasta) {
    this.inputFasta = inputFasta;
  }

  @Override
  public String apply(final String id) {
    final StringBuilder contigSequence = new StringBuilder();
    try (final BufferedReader br = Files.newBufferedReader(this.inputFasta)) {
      String line;
      while ((line = br.readLine()) != null) {

        if (line.startsWith(">")) {
          if (id.equals(line.replace(">", ""))) {
            break;
          }
        }
      }
      while ((line = br.readLine()) != null) {
        if (line.startsWith(">")) {
          break;
        }
        contigSequence.append(line);
      }
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
    return contigSequence.toString();
  }
}
