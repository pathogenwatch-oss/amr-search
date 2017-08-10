package net.cgps.wgsa.paarsnp.builder;

import net.cgps.wgsa.paarsnp.core.lib.json.ResistanceSet;
import net.cgps.wgsa.paarsnp.core.paar.ResistanceGene;
import net.cgps.wgsa.paarsnp.core.snpar.MutationType;
import net.cgps.wgsa.paarsnp.core.snpar.SnparReferenceSequence;
import net.cgps.wgsa.paarsnp.core.snpar.json.ResistanceMutation;
import net.cgps.wgsa.paarsnp.core.snpar.json.SnparLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class SnparReader implements BiFunction<Path, Path, SnparLibrary> {

  private static final String SAP_KEY = "SAP";
  private static final String SNP_KEY = "SNP";

  private final Logger logger = LoggerFactory.getLogger(SnparReader.class);

  private final String speciesId;

  public SnparReader(String speciesId) {
    this.speciesId = speciesId;
  }

  private static MutationType determineMutationType(final String key) {

    final MutationType mutationType;

    if (SNP_KEY.equals(key) || SAP_KEY.equals(key)) {
      mutationType = MutationType.S;
    } else {
      mutationType = MutationType.valueOf(key);
    }

    return mutationType;
  }

  @Override
  public SnparLibrary apply(final Path snparCsvFile, final Path snparFastaFile) {

    final Map<String, String> sequences = new OneLineFastaReader().apply(snparFastaFile);

    final List<String> lines;

    try {
      lines = Files.readAllLines(snparCsvFile);
    } catch (final IOException e) {
      this.logger.error("Failed to read paarsnp TSV due to: ", e);
      throw new RuntimeException(e);
    }

    final Map<String, SnparReferenceSequence> sapData = new HashMap<>(100);
    final Map<String, ResistanceSet> resistanceSets = new HashMap<>(50);

    for (final String line : lines) {
      // Skip the header if it exists.
      if (line.startsWith(MutationLine.HEADER_START)) {
        continue;
      }

      final MutationLine csvLine;
      try {

        this.logger.debug("Reading line {}", line);

        final Optional<MutationLine> csvLineOpt = MutationLine.parseLine(line);

        if (csvLineOpt.isPresent()) {
          this.logger.debug("Data found.");
          csvLine = csvLineOpt.get();
        } else {
          this.logger.debug("Skipping line {}", line);
          continue;
        }
      } catch (final Exception e) {
        this.logger.error("Failure parsing mutation data line {}", line);
        this.logger.error("Reason:", e);
        throw new RuntimeException(e);
      }

      // Check there's a rep available.
      if (!sequences.containsKey(csvLine.getRepresentativeId())) {
        throw new RuntimeException("Terminal error, not found sequence: " + csvLine.getRepresentativeId());
      }

      final Set<String> antimicrobialAgents = new HashSet<>(Arrays.asList(csvLine.getAntibiotics().split(",")));

      final String mutationId = csvLine.getRepresentativeId() + "_" + csvLine.getName();

      // Create the resistance set if new, otherwise add to it.
      if (!resistanceSets.containsKey(csvLine.getSetName())) {
        this.logger.debug("Using {} to initialise set {}", mutationId, csvLine.getSetName());
        resistanceSets.put(csvLine.getSetName(), ResistanceSet.buildSnpResistanceSet(csvLine.getSetName(), csvLine.getRepresentativeId(), csvLine.getName(), antimicrobialAgents, "", csvLine.getGroupEffect()));
      } else {
        this.logger.debug("Adding {} to set {}", mutationId, csvLine.getSetName());
        resistanceSets.get(csvLine.getSetName()).addElementId(mutationId, ResistanceGene.EFFECT.RESISTANT);
        this.logger.debug("Set now contains {}", String.join(",", resistanceSets.get(csvLine.getSetName()).getElementIds()));
      }

      // Add the reference sequence if it doesn't exist.
      if (!sapData.containsKey(csvLine.getRepresentativeId())) {
        // Create new mutation reference sequence
        sapData.put(csvLine.getRepresentativeId(), new SnparReferenceSequence(csvLine.getRepresentativeId(), 80.0, 1e-40, sequences.get(csvLine.getRepresentativeId())));
      }

      // Now add the parsed mutation.
      // First determine the mutation type.
      try {

        final MutationType mutationType = this.determineMutationType(csvLine);
        final Integer position = csvLine.getType().equals(SAP_KEY) ? ((csvLine.getPosition() * 3) - 2) : csvLine.getPosition();

        sapData.get(csvLine.getRepresentativeId()).addMutation(new ResistanceMutation(csvLine.getRepresentativeId() + "_" + csvLine.getName(), csvLine.getSetName(), mutationType, csvLine.getSequenceType(), csvLine.getRepresentativeId(), csvLine.getOriginalSequence(), position, csvLine.getMutantSequence(), csvLine.getSource()));
      } catch (final Exception e) {
        this.logger.error("Failure: ", e);
        throw new RuntimeException(e);
      }
    }

    final Map<String, String> filteredSequences = sequences
        .entrySet()
        .stream()
        .filter(idToSequence -> sapData.containsKey(idToSequence.getKey()))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

//    final Collection<SnparAntibioticSummary> amrSummary = new SnpaarSummariser().apply(sapData.values(), resistanceSets.values()).collect(Collectors.toList());

    // TODO:
    // Write FASTA library & run makeblastdb

    final double minPid = sapData.values()
        .stream()
        .mapToDouble(SnparReferenceSequence::getSeqIdThreshold)
        .min().orElseThrow(() -> new RuntimeException("PID values for reference sequences."))
        - 5.0;

    return new SnparLibrary(sapData, resistanceSets, this.speciesId, minPid);
  }

  private MutationType determineMutationType(final MutationLine csvLine) {

    return SnparReader.determineMutationType(csvLine.getType());
  }

}
