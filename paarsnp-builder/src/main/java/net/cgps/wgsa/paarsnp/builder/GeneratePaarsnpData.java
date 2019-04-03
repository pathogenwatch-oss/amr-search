package net.cgps.wgsa.paarsnp.builder;

import net.cgps.wgsa.paarsnp.core.Constants;
import net.cgps.wgsa.paarsnp.core.lib.AbstractJsonnable;
import net.cgps.wgsa.paarsnp.core.models.LibraryMetadata;
import net.cgps.wgsa.paarsnp.core.models.results.AntimicrobialAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.*;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GeneratePaarsnpData implements Consumer<LibraryMetadata> {

  // Filter for species sub-directories.
  private static final DirectoryStream.Filter<Path> SPECIES_FOLDER_FILTER =
      entry -> Files.isRegularFile(entry) && entry.getFileName().toString().matches("^\\d+.toml$");

  private final Logger logger = LoggerFactory.getLogger(GeneratePaarsnpData.class);

  private final Path archivesDirectory;
  private final Path outputDirectory;

  public GeneratePaarsnpData(final Path archivesDirectory, final Path outputDirectory) {
    this.archivesDirectory = archivesDirectory;
    this.outputDirectory = outputDirectory;
  }

  @Override
  public void accept(final LibraryMetadata libraryMetadata) {

    final Path libaryPath = LibraryMetadata.Source.PUBLIC == libraryMetadata.getSource() ? Paths.get(this.archivesDirectory.toString(), "amr-libraries") : Paths.get(this.archivesDirectory.toString(), "amr-test-libraries");

    if (!Files.exists(libaryPath)) {
      throw new RuntimeException(libaryPath.toString() + " does not exist.");
    }

    final Map<String, AntimicrobialAgent> antimicrobialDb = new AntimicrobialDbReader()
        .apply(libaryPath)
        .stream()
        .collect(Collectors.toMap(AntimicrobialAgent::getKey, Function.identity()));

    try (final DirectoryStream<Path> dbStream = Files.newDirectoryStream(libaryPath, SPECIES_FOLDER_FILTER)) {

      dbStream.forEach(tomlPath -> {

        final String speciesId = tomlPath.getFileName().toString().replace(".toml", "");

        this.logger.info("Preparing {}", speciesId);

        // Create the blast databases.
        // First write the paarsnp fasta.
        final MakeBlastDB makeBlastDB = new MakeBlastDB(this.outputDirectory);

        final LibraryReader.LibraryDataAndSequences paarsnpLibraryAndSequences = new LibraryReader(libraryMetadata, antimicrobialDb).apply(tomlPath);

        final Path libraryFile = Paths.get(this.outputDirectory.toString(), speciesId + Constants.JSON_APPEND);
        final String snparLibraryName = speciesId + Constants.LIBRARY_APPEND;
        final Path snparFastaFile = Paths.get(this.outputDirectory.toString(), snparLibraryName + Constants.FASTA_APPEND);

        try (final BufferedOutputStream bw = new BufferedOutputStream(new FileOutputStream(libraryFile.toFile()))) {
          bw.write(AbstractJsonnable.toJson(paarsnpLibraryAndSequences.getPaarsnpLibrary()).getBytes());
        } catch (final IOException e) {
          throw new RuntimeException("Unable to serialise to " + libraryFile, e);
        }

        try {

          if (!paarsnpLibraryAndSequences.getSequences().isEmpty()) {
            Files.write(snparFastaFile, String.join("", paarsnpLibraryAndSequences.getSequences().values()).getBytes(), StandardOpenOption.CREATE);
            makeBlastDB.accept(snparLibraryName, snparFastaFile);
          }

        } catch (final IOException e) {
          throw new RuntimeException(e);
        }

        this.logger.info("{} files written.", speciesId);
      });

    } catch (final IOException e) {
      this.logger.info("Failed to read input database in {}", libaryPath.toString());
      throw new RuntimeException(e);
    }

  }
}
