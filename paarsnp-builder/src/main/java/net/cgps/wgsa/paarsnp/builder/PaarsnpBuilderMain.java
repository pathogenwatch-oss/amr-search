package net.cgps.wgsa.paarsnp.builder;

import ch.qos.logback.classic.Level;
import net.cgps.wgsa.paarsnp.core.Constants;
import net.cgps.wgsa.paarsnp.core.lib.AbstractJsonnable;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;

public class PaarsnpBuilderMain {

  private static final String DEFAULT_INPUT_PATH = "\"resources/\"";

  // Filter for species sub-directories.
  private static final DirectoryStream.Filter<Path> SPECIES_FOLDER_FILTER =
      entry -> Files.isRegularFile(entry) && entry.getFileName().toString().matches("^\\d+.toml$");

  private final Logger logger = LoggerFactory.getLogger(PaarsnpBuilderMain.class);

  public static void main(final String[] args) {

    // Initialise the options parser
    final Options options = PaarsnpBuilderMain.myOptions();
    final CommandLineParser parser = new DefaultParser();

    if (args.length == 0) {
      final HelpFormatter formatter = new HelpFormatter();
      formatter.setWidth(200);
      formatter.printHelp("paarsnp-builder: ", options);
      System.exit(1);
    }

    try {

      final CommandLine commandLine = parser.parse(options, args);
      final ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
      root.setLevel(Level.valueOf(commandLine.getOptionValue('l', "INFO")));

      new PaarsnpBuilderMain().run(commandLine.getOptionValue('i', DEFAULT_INPUT_PATH), commandLine.getOptionValue('o', "."));

    } catch (final Exception e) {
      LoggerFactory.getLogger(PaarsnpBuilderMain.class).error("Failed to run due to: ", e);
      System.exit(1);
    }
  }

  private static Options myOptions() {

    final Option inputDirectoryOption = Option.builder("i").longOpt("input-dir").hasArg().argName("Input directory").desc("Optional: Input database directory. Defaults to " + DEFAULT_INPUT_PATH).build();

    final Option outputDirectoryOption = Option.builder("o").longOpt("output-dir").hasArg().argName("Output directory").desc("Optional: Output location for BLAST and paarsnp databases.").required().build();

    final Option logLevel = Option.builder("l").longOpt("log-level").hasArg().argName("Logging level").desc("INFO, DEBUG etc").build();

    final Options options = new Options();
    options.addOption(outputDirectoryOption)
        .addOption(inputDirectoryOption)
        .addOption(logLevel);

    return options;
  }

  private void run(String inputDirectory, final String outputDirectory) {

    final Path inputFolderPath = Paths.get(inputDirectory);
    final Path outputFolderPath = Paths.get(outputDirectory);

    if (this.fileMissing(inputFolderPath)) {
      throw new RuntimeException("Input folder " + inputFolderPath.toAbsolutePath().toString() + " does not exist.");
    }

    if (this.fileMissing(outputFolderPath)) {
      try {
        Files.createDirectories(outputFolderPath);
      } catch (IOException e) {
        throw new RuntimeException("Unable to create output folder " + outputFolderPath.toAbsolutePath().toString());
      }
    }

    try (final DirectoryStream<Path> dbStream = Files.newDirectoryStream(inputFolderPath, SPECIES_FOLDER_FILTER)) {

      dbStream.forEach(tomlPath -> {

        final String speciesId = tomlPath.getFileName().toString().replace(".toml", "");

        this.logger.info("Preparing {}", speciesId);

        // Create the blast databases.
        // First write the paarsnp fasta.
        final MakeBlastDB makeBlastDB = new MakeBlastDB(outputFolderPath);

        final LibraryReader.LibraryDataAndSequences paarsnpLibraryAndSequences = new LibraryReader().apply(tomlPath);

        final Path libraryFile = Paths.get(outputDirectory, speciesId + Constants.JSON_APPEND);
        final String snparLibraryName = speciesId + Constants.LIBRARY_APPEND;
        final Path snparFastaFile = Paths.get(outputDirectory, snparLibraryName + Constants.FASTA_APPEND);

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
      this.logger.info("Failed to read input database in {}", inputDirectory);
      throw new RuntimeException(e);
    }
  }

  private boolean fileMissing(final Path... files) {
    return Arrays
        .stream(files)
        .anyMatch(file -> !Files.exists(file));
  }
}
