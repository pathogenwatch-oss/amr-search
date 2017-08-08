package net.cgps.wgsa.paarsnp.builder;

import net.cgps.wgsa.paarsnp.core.lib.AntimicrobialAgent;
import net.cgps.wgsa.paarsnp.core.lib.ResistanceSet;
import net.cgps.wgsa.paarsnp.core.lib.ResistanceType;
import net.cgps.wgsa.paarsnp.core.paar.PaarAntibioticSummary;
import net.cgps.wgsa.paarsnp.core.paar.PaarGeneSummary;
import net.cgps.wgsa.paarsnp.core.paar.PaarLibrary;
import net.cgps.wgsa.paarsnp.core.paar.ResistanceGene;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PaarReader implements Function<Path, PaarLibrary> {

  private static final float DEFAULT_SIMILARITY_THRESHOLD = 80.0f;
  private static final float DEFAULT_LENGTH_THRESHOLD = 80.0f;
  private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+", Pattern.LITERAL);
  private final Logger logger = LoggerFactory.getLogger(PaarReader.class);

  private final String speciesId;

  PaarReader(String speciesId) {
    this.speciesId = speciesId;
  }

  @Override
  public PaarLibrary apply(final Path paarCsv) {

    final Map<String, ResistanceGene> paarGeneLib = new HashMap<>(100);
    final Map<String, ResistanceSet> resistanceSets = new HashMap<>(100);

    final StringBuilder fastaSb = new StringBuilder(50000);

    // File format: geneName resistanceGroup resistanceProfile sequence phenotypeDescription<:optional>

    this.logger.info("Reading {}", paarCsv.toString());

    try {
      final CSVParser parser = CSVParser.parse(paarCsv.toFile(), Charset.defaultCharset(), CSVFormat.RFC4180);

      for (final CSVRecord csvRecord : parser.getRecords()) {
        this.logger.trace("geneName={}", csvRecord.get(0));
        if ("gene_name".equals(csvRecord.get(0))) {
          continue;
        }
        this.generateResistanceGene(csvRecord, fastaSb, resistanceSets, paarGeneLib);
      }

    } catch (final IOException e) {
      throw new RuntimeException(e);
    }

    final Collection<PaarAntibioticSummary> amrSummary =
        resistanceSets.values()
            .stream()
            .flatMap(resistanceSet -> resistanceSet.getAgents()
                .stream()
                .map(agent -> new PaarAntibioticSummary(
                    agent.getName(),
                    Stream.concat(resistanceSet.getElementIds().stream(),
                        resistanceSet.getModifiers().keySet().stream()
                    )
                        .map(id -> new PaarGeneSummary(id, resistanceSet.getResistanceSetName()))
                        .collect(Collectors.toList())
                ))
            )
            .collect(Collectors.toList());

    final double minThreshold = paarGeneLib.values()
        .stream()
        .mapToDouble(ResistanceGene::getSimilarityThreshold)
        .min().orElseThrow(() -> new RuntimeException("Unable to find any minimum threshold data"))
        - 5;

    return new PaarLibrary(paarGeneLib.values(), resistanceSets.values(), amrSummary, this.speciesId, minThreshold);
  }

  private void generateResistanceGene(final CSVRecord csvRecord, final StringBuilder fastaSb, final Map<String, ResistanceSet> resistanceSetMap, final Map<String, ResistanceGene> resistanceGeneMap) {

    this.logger.debug("geneName={} setName={} effect={} agents={} simThreshold={} lengthThreshold", csvRecord.get(0), csvRecord.get(1), csvRecord.get(2), csvRecord.get(3), csvRecord.get(5), csvRecord.get(6));

    // Clean up any potential issues with the sequence.
    final String geneName = csvRecord.get(0);
    final String setName = csvRecord.get(1).isEmpty() ? csvRecord.get(0) : csvRecord.get(1);
    final ResistanceGene.EFFECT effect = ResistanceGene.EFFECT.valueOf(csvRecord.get(2));
    final Set<AntimicrobialAgent> allAgents = this.mapAgentsToFullObjects(csvRecord.get(3));
    final float simThreshold = "".equals(csvRecord.get(5)) ? DEFAULT_SIMILARITY_THRESHOLD : Float.valueOf(csvRecord.get(5));
    final float lengthThreshold = "".equals(csvRecord.get(6)) ? DEFAULT_LENGTH_THRESHOLD : Float.valueOf(csvRecord.get(6));
    final String sequence = WHITESPACE_PATTERN.matcher(csvRecord.get(4).trim()).replaceAll("").toUpperCase();
    final String phenotype = csvRecord.size() == 8 ? csvRecord.get(7) : "";

    if (!allAgents.isEmpty()) {
      try {

        if (resistanceGeneMap.containsKey(geneName)) {
          // Gene has already been seen in another set, so just add the extra set to the gene document.
          resistanceGeneMap.get(geneName).addResistanceSetName(setName);
        } else {
          // New gene, so initiate & append to the fasta.
          resistanceGeneMap.put(geneName, new ResistanceGene(setName, geneName, sequence.length(), lengthThreshold, simThreshold, effect));
          fastaSb.append(">").append(geneName).append("\n").append(sequence).append("\n");
        }

        if (resistanceSetMap.containsKey(setName)) {
          // The set has already been defined, so just add the gene to it.
          this.logger.debug("Adding geneName={} with effect={} to set={}", geneName, effect, setName);
          resistanceSetMap.get(setName).addElementId(geneName, effect);
        } else {
          // Construct a new resistance set.
          this.logger.debug("Constructing new set with geneName={} with effect={} to set={}", geneName, effect, setName);
          resistanceSetMap.put(setName, new ResistanceSet(setName, geneName, ResistanceType.RESISTANT, effect, allAgents, phenotype));
        }
      } catch (final NumberFormatException e) {
        this.logger.error("Field from line {} not a number: {}", csvRecord.toString(), e.getMessage());
        throw e;
      }
    } else {
      this.logger.info("Skipping {} from set {} due to no interesting antibiotics.", geneName, setName);
    }
  }

  private Set<AntimicrobialAgent> mapAgentsToFullObjects(final String agents) {
    return Arrays.stream(agents.split(","))
        .map(AntimicrobialAgent::new)
        .collect(Collectors.toSet());
  }
}
