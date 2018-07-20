package net.cgps.wgsa.paarsnp.builder;

import net.cgps.wgsa.paarsnp.core.lib.ElementEffect;
import net.cgps.wgsa.paarsnp.core.lib.SetResistanceType;
import net.cgps.wgsa.paarsnp.core.lib.json.ResistanceSet;
import net.cgps.wgsa.paarsnp.core.paar.json.PaarLibrary;
import net.cgps.wgsa.paarsnp.core.paar.json.ResistanceGene;
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

public class PaarReader implements Function<Path, PaarLibrary> {

  private final Logger logger = LoggerFactory.getLogger(PaarReader.class);

  private final String speciesId;

  PaarReader(String speciesId) {
    this.speciesId = speciesId;
  }

  @Override
  public PaarLibrary apply(final Path paarCsv) {

    this.logger.info("Reading {}", paarCsv.toString());

    final Map<String, ResistanceSet> resistanceSets = new HashMap<>(100);
    final Map<String, ResistanceGene> paarGeneLib = new HashMap<>(100);

    try (final CSVParser parser = CSVParser.parse(paarCsv.toFile(), Charset.defaultCharset(), CSVFormat.RFC4180.withFirstRecordAsHeader())) {

      for (final CSVRecord csvRecord : parser.getRecords()) {
        this.generateResistanceGene(csvRecord, resistanceSets, paarGeneLib);
      }

    } catch (final IOException e) {
      throw new RuntimeException(e);
    }

    final double minThreshold = paarGeneLib.values()
        .stream()
        .mapToDouble(ResistanceGene::getSimilarityThreshold)
        .min().orElseThrow(() -> new RuntimeException("Unable to find any minimum threshold data"))
        - 5;

    return new PaarLibrary(paarGeneLib, resistanceSets, this.speciesId, minThreshold);
  }

  private void generateResistanceGene(final CSVRecord csvRecord, final Map<String, ResistanceSet> resistanceSetMap, final Map<String, ResistanceGene> resistanceGeneMap) {

    // Clean up any potential issues with the sequence.
    final String geneName = csvRecord.get("Gene Name").trim();
    final String setName = csvRecord.get("Resistance Group").isEmpty() ? geneName : csvRecord.get("Resistance Group").trim();
    final SetResistanceType resistanceType = SetResistanceType.valueOf(csvRecord.get("Set Effect").trim());
    final ElementEffect effect = ElementEffect.valueOf(csvRecord.get("Effect").trim());
    final Set<String> allAgents = this.mapAgentsToFullObjects(csvRecord.get("Resistance Profile").trim());
    final float simThreshold = Float.valueOf(csvRecord.get("PID Threshold").trim());
    final float lengthThreshold = Float.valueOf(csvRecord.get("Coverage Threshold").trim());

    if (!allAgents.isEmpty()) {
      try {

        if (resistanceGeneMap.containsKey(geneName)) {
          // Gene has already been seen in another set, so just add the extra set to the gene document.
          resistanceGeneMap.get(geneName).addResistanceSetName(setName);
        } else {
          // New gene, so initiate & append to the fasta.
          resistanceGeneMap.put(geneName, new ResistanceGene(setName, geneName, lengthThreshold, simThreshold, effect));
        }

        if (resistanceSetMap.containsKey(setName)) {
          // The set has already been defined, so just add the gene to it.
          this.logger.debug("Adding geneName={} with effect={} to set={}", geneName, effect, setName);
          resistanceSetMap.get(setName).addElement(geneName, effect);
        } else {
          // Construct a new resistance set.
          this.logger.debug("Constructing new set with geneName={} with effect={} to set={}", geneName, effect, setName);
          resistanceSetMap.put(setName, new ResistanceSet(setName, resistanceType, allAgents).addElement(geneName, effect));
        }
      } catch (final NumberFormatException e) {
        this.logger.error("Field from line {} not a number: {}", csvRecord.toString(), e.getMessage());
        throw e;
      }
    } else {
      this.logger.info("Skipping {} from set {} due to no interesting antibiotics.", geneName, setName);
    }
  }

  private Set<String> mapAgentsToFullObjects(final String agents) {
    return new HashSet<>(Arrays.asList(agents.split(",")));
  }
}
