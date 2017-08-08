package net.cgps.wgsa.paarsnp.builder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class OneLineFastaReader implements Function<Path, Map<String, String>> {

  private final Logger logger = LoggerFactory.getLogger(OneLineFastaReader.class);

  @Override
  public Map<String, String> apply(final Path fastaFile) {

    this.logger.info("Reading fasta file {}", fastaFile.toAbsolutePath().toString());

    final Map<String, String> map = new HashMap<>(100);

    try (final BufferedReader br = new BufferedReader(new FileReader(fastaFile.toAbsolutePath().toString()))) {
      String line;
      String currentId = "";

      while (null != (line = br.readLine())) {

        if ("".equals(line)) {
          continue;
        }

        if (line.startsWith(">")) {
          // fasta header line
          currentId = line.replaceFirst(">", "").split("\\s+")[0]; // Remove the ">" and take the first part of the line.
        } else {

          if (!"".equals(currentId)) { // Skip first line.
            // Sequence line. Assumes sequences only take up a single line.
            map.put(currentId, line.toUpperCase());
          }
        }
      }
    } catch (final IOException e) {

      this.logger.error("Failed to read SNPAR fasta file {}", fastaFile.toAbsolutePath().toString());
      this.logger.error("Reason - {}", e);
      throw new RuntimeException(e);
    }

    return map;
  }
}
