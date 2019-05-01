package net.cgps.wgsa.paarsnp.builder;

import net.cgps.wgsa.paarsnp.core.models.LibraryMetadata;
import net.cgps.wgsa.paarsnp.core.models.results.AntimicrobialAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GeneratePaarsnpData implements Function<LibraryMetadata, Collection<LibraryReader.LibraryDataAndSequences>> {

  // Filter for species sub-directories.
  private static final DirectoryStream.Filter<Path> SPECIES_FOLDER_FILTER =
      entry -> Files.isRegularFile(entry) && entry.getFileName().toString().matches("^\\d+.toml$");

  private final Logger logger = LoggerFactory.getLogger(GeneratePaarsnpData.class);

  private final Path archivesDirectory;

  public GeneratePaarsnpData(final Path archivesDirectory) {
    this.archivesDirectory = archivesDirectory;
  }

  @Override
  public Collection<LibraryReader.LibraryDataAndSequences> apply(final LibraryMetadata libraryMetadata) {

    final Path libraryPath = LibraryMetadata.Source.PUBLIC == libraryMetadata.getSource() ? Paths.get(this.archivesDirectory.toString(), "amr-libraries") : Paths.get(this.archivesDirectory.toString(), "amr-test-libraries");

    if (!Files.exists(libraryPath)) {
      throw new RuntimeException(libraryPath.toString() + " does not exist.");
    }

    final Map<String, AntimicrobialAgent> antimicrobialDb = new AntimicrobialDbReader()
        .apply(libraryPath)
        .stream()
        .collect(Collectors.toMap(AntimicrobialAgent::getKey, Function.identity()));

    final Collection<LibraryReader.LibraryDataAndSequences> libraries = new ArrayList<>();

    try (final DirectoryStream<Path> dbStream = Files.newDirectoryStream(libraryPath, SPECIES_FOLDER_FILTER)) {

      dbStream.forEach(tomlPath -> {

        final String speciesId = tomlPath.getFileName().toString().replace(".toml", "");

        this.logger.info("Preparing {}", speciesId);

        // Create the blast databases.
        // First write the paarsnp fasta.

        final LibraryReader.LibraryDataAndSequences paarsnpLibraryAndSequences = new LibraryReader(libraryMetadata, antimicrobialDb).apply(tomlPath);

        libraries.add(paarsnpLibraryAndSequences);
      });

    } catch (final IOException e) {
      this.logger.info("Failed to read input database in {}", libraryPath.toString());
      throw new RuntimeException(e);
    }

    return libraries;
  }
}
