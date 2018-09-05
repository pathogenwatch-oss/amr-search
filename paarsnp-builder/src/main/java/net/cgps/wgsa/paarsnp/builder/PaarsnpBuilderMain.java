package net.cgps.wgsa.paarsnp.builder;

import ch.qos.logback.classic.Level;
import net.cgps.wgsa.paarsnp.core.Constants;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.*;
import java.util.Arrays;
import java.util.stream.Collectors;

public class PaarsnpBuilderMain {

  private static final String DEFAULT_INPUT_PATH = "\"resources/\"";

  // Filter for species sub-directories.
  private static final DirectoryStream.Filter<Path> SPECIES_FOLDER_FILTER =
      entry -> Files.isRegularFile(entry) && entry.getFileName().toString().matches("^\\w+.toml$");

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

        final LibraryReader.PaarsnpLibraryAndSequences paarsnpLibraryAndSequences = new LibraryReader().apply(tomlPath);

        final Path libraryFile = Paths.get(outputDirectory, tomlPath.getFileName().toString());
        final String paarLibraryName = speciesId + Constants.PAAR_APPEND;
        final String snparLibraryName = speciesId + Constants.SNPAR_APPEND;
        final Path paarFastaFile = Paths.get(outputDirectory, paarLibraryName + Constants.FASTA_APPEND);
        final Path snparFastaFile = Paths.get(outputDirectory, snparLibraryName + Constants.FASTA_APPEND);

        // Serialise the main library to disk.
        try (final ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(libraryFile.toFile()))) {
          os.writeObject(paarsnpLibraryAndSequences.getPaarsnpLibrary());
        } catch (final IOException e) {
          throw new RuntimeException("Unable to serialise to " + libraryFile, e);
        }

        try {

          Files.write(libraryFile, paarsnpLibraryAndSequences.getPaarSequences().entrySet().stream().map(entry1 -> ">" + entry1.getKey() + "\n" + entry1.getValue() + "\n").collect(Collectors.joining()).getBytes(), StandardOpenOption.CREATE);
          makeBlastDB.accept(paarLibraryName, paarFastaFile);

          Files.write(snparFastaFile, paarsnpLibraryAndSequences.getSnparSequences().entrySet().stream().map(entry -> ">" + entry.getKey() + "\n" + entry.getValue() + "\n").collect(Collectors.joining()).getBytes(), StandardOpenOption.CREATE);
          makeBlastDB.accept(snparLibraryName, snparFastaFile);

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
