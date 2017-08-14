package net.cgps.wgsa.paarsnp.builder;

import net.cgps.wgsa.paarsnp.core.lib.json.AntimicrobialAgent;
import net.cgps.wgsa.paarsnp.core.lib.json.AntimicrobialAgentLibrary;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class AntibioticsListReader implements BiFunction<String, Path, AntimicrobialAgentLibrary> {

  private final Logger logger = LoggerFactory.getLogger(AntibioticsListReader.class);

  @Override
  public AntimicrobialAgentLibrary apply(final String speciesId, final Path path) {

    final List<CSVRecord> records;
    try (final CSVParser csvParser = new CSVParser(Files.newBufferedReader(path), CSVFormat.RFC4180)) {
      records = csvParser.getRecords();
    } catch (final IOException e) {
      this.logger.error("Failed to read path {}", path.toAbsolutePath().toString());
      throw new RuntimeException(e);
    }

    final Collection<AntimicrobialAgent> antimicrobials = records
        .stream()
        .skip(1)
        .map(line -> new AntimicrobialAgent(line.get(0), line.get(2), line.get(1)))
        .collect(Collectors.toList());

    return new AntimicrobialAgentLibrary(antimicrobials, speciesId);
  }
}
