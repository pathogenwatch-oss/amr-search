package net.cgps.wgsa.paarsnp.builder;

import ch.qos.logback.classic.Level;
import net.cgps.wgsa.paarsnp.PaarsnpMain;
import net.cgps.wgsa.paarsnp.core.paar.PaarLibrary;
import net.cgps.wgsa.paarsnp.core.snpar.SnparLibrary;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;

public class PaarsnpBuilderMain {

  private static final String DEFAULT_INPUT_PATH = "\"resources/\"";

  // Filter for species sub-directories.
  private static final DirectoryStream.Filter<Path> SPECIES_FOLDER_FILTER = entry -> Files.isDirectory(entry) && entry.getFileName().toString().matches("^\\d+$");

  private final Logger logger = LoggerFactory.getLogger(PaarsnpBuilderMain.class);
  private final JsonWriter jsonWriter = new JsonWriter();

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

    final Path outputFolderPath = Paths.get(outputDirectory);

    if (!filesArePresent(outputFolderPath)) {
      throw new RuntimeException("Output folder " + outputFolderPath.toAbsolutePath().toString() + " does not exist.");
    }

    try (final DirectoryStream<Path> dbStream = Files.newDirectoryStream(Paths.get(inputDirectory), SPECIES_FOLDER_FILTER)) {

      dbStream.forEach(taxonDir -> {

        final String speciesId = taxonDir.getFileName().toString();

        this.logger.info("Preparing {}", speciesId);

        final Path paarCsvPath = Paths.get(taxonDir.toString(), "resistance_genes.tsv");
        final Path snparCsvPath = Paths.get(taxonDir.toString(), "ar_snps.tsv");
        final Path snparFastaPath = Paths.get(taxonDir.toString(), "ar_snps_lib.fa");

        if (!filesArePresent(paarCsvPath, snparCsvPath, snparFastaPath)) {
          throw new RuntimeException("Not all input files are present for " + speciesId);
        }

        this.logger.debug("Reading PAAR CSV file {}", paarCsvPath.toAbsolutePath().toString());

        final PaarLibrary paarLibrary = new PaarReader(speciesId).apply(paarCsvPath);
        final SnparLibrary snparLibrary = new SnparReader(speciesId).apply(snparCsvPath, snparFastaPath);

        final Path paarLibraryFile = Paths.get(outputDirectory, speciesId + PaarsnpMain.PAAR_FILE_APPEND);
        final Path snparLibraryFile = Paths.get(outputDirectory, speciesId, PaarsnpMain.SNPAR_FILE_APPEND);

        try {
          Files.write(paarLibraryFile, jsonWriter.apply(paarLibrary).getBytes(), StandardOpenOption.CREATE);
          Files.write(snparLibraryFile, jsonWriter.apply(snparLibrary).getBytes(), StandardOpenOption.CREATE);
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

  private boolean filesArePresent(final Path... files) {
    return Arrays
        .stream(files)
        .allMatch(file -> Files.exists(file));
  }
}
