package net.cgps.wgsa.paarsnp.builder;

import ch.qos.logback.classic.Level;
import net.cgps.wgsa.paarsnp.core.Constants;
import net.cgps.wgsa.paarsnp.core.lib.json.AntimicrobialAgentLibrary;
import net.cgps.wgsa.paarsnp.core.paar.PaarLibrary;
import net.cgps.wgsa.paarsnp.core.snpar.json.SnparLibrary;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;

public class PaarsnpBuilderMain {

  private static final String DEFAULT_INPUT_PATH = "\"resources/\"";

  // Filter for species sub-directories.
  private static final DirectoryStream.Filter<Path> SPECIES_FOLDER_FILTER = entry -> Files.isDirectory(entry) && entry.getFileName().toString().matches("^\\w+$" );

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
      root.setLevel(Level.valueOf(commandLine.getOptionValue('l', "INFO" )));

      new PaarsnpBuilderMain().run(commandLine.getOptionValue('i', DEFAULT_INPUT_PATH), commandLine.getOptionValue('o', "." ));

    } catch (final Exception e) {
      LoggerFactory.getLogger(PaarsnpBuilderMain.class).error("Failed to run due to: ", e);
      System.exit(1);
    }
  }

  private static Options myOptions() {

    final Option inputDirectoryOption = Option.builder("i" ).longOpt("input-dir" ).hasArg().argName("Input directory" ).desc("Optional: Input database directory. Defaults to " + DEFAULT_INPUT_PATH).build();

    final Option outputDirectoryOption = Option.builder("o" ).longOpt("output-dir" ).hasArg().argName("Output directory" ).desc("Optional: Output location for BLAST and paarsnp databases." ).required().build();

    final Option logLevel = Option.builder("l" ).longOpt("log-level" ).hasArg().argName("Logging level" ).desc("INFO, DEBUG etc" ).build();

    final Options options = new Options();
    options.addOption(outputDirectoryOption)
        .addOption(inputDirectoryOption)
        .addOption(logLevel);

    return options;
  }

  private void run(String inputDirectory, final String outputDirectory) {

    final Path inputFolderPath = Paths.get(inputDirectory);
    final Path outputFolderPath = Paths.get(outputDirectory);

    if (fileMissing(inputFolderPath)) {
      throw new RuntimeException("Input folder " + inputFolderPath.toAbsolutePath().toString() + " does not exist." );
    }

    if (fileMissing(outputFolderPath)) {
      try {
        Files.createDirectories(outputFolderPath);
      } catch (IOException e) {
        throw new RuntimeException("Unable to create output folder " + outputFolderPath.toAbsolutePath().toString());
      }
    }

    try (final DirectoryStream<Path> dbStream = Files.newDirectoryStream(inputFolderPath, SPECIES_FOLDER_FILTER)) {

      dbStream.forEach(taxonDir -> {

        final String speciesId = taxonDir.getFileName().toString();

        this.logger.info("Preparing {}", speciesId);

        final Path paarCsvPath = Paths.get(taxonDir.toString(), "resistance_genes.csv" );
        final Path paarFastaPath = Paths.get(taxonDir.toString(), "resistance_genes.fa" );
        final Path snparCsvPath = Paths.get(taxonDir.toString(), "ar_snps.csv" );
        final Path snparFastaPath = Paths.get(taxonDir.toString(), "ar_snps.fa" );
        final Path amPath = Paths.get(taxonDir.toString(), "ar_agents.csv" );

        if (fileMissing(paarCsvPath, snparCsvPath, snparFastaPath)) {
          throw new RuntimeException("Not all input files are present for " + speciesId);
        }

        this.logger.debug("Reading PAAR CSV file {}", paarCsvPath.toAbsolutePath().toString());

        // Read the CSVs and generate the libraries
        final PaarLibrary paarLibrary = new PaarReader(speciesId).apply(paarCsvPath);
        final SnparLibrary snparLibrary = new SnparReader(speciesId).apply(snparCsvPath, snparFastaPath);
        final AntimicrobialAgentLibrary agentLibrary = new AntibioticsListReader().apply(speciesId, amPath);

        final String paarLibraryName = speciesId + Constants.PAAR_APPEND;
        final String snparLibraryName = speciesId + Constants.SNPAR_APPEND;

        final Path paarLibraryFile = Paths.get(outputDirectory, paarLibraryName + Constants.JSON_APPEND);
        final Path snparLibraryFile = Paths.get(outputDirectory, snparLibraryName + Constants.JSON_APPEND);
        final Path paarFastaFile = Paths.get(outputDirectory, paarLibraryName + Constants.FASTA_APPEND);
        final Path snparFastaFile = Paths.get(outputDirectory, snparLibraryName + Constants.FASTA_APPEND);
        final Path amLibraryFile = Paths.get(outputDirectory, speciesId + Constants.AGENT_FILE_APPEND);

        try {
          Files.write(paarLibraryFile, paarLibrary.toJson().getBytes(), StandardOpenOption.CREATE);
          Files.write(snparLibraryFile, snparLibrary.toJson().getBytes(), StandardOpenOption.CREATE);
          Files.write(amLibraryFile, agentLibrary.toJson().getBytes(), StandardOpenOption.CREATE);
          Files.copy(paarFastaPath, paarFastaFile, StandardCopyOption.REPLACE_EXISTING);
          Files.copy(snparFastaPath, snparFastaFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (final IOException e) {
          throw new RuntimeException(e);
        }

        // Create the blast databases.
        final MakeBlastDB makeBlastDB = new MakeBlastDB(outputFolderPath);

        makeBlastDB.accept(paarLibraryName, paarFastaFile);
        makeBlastDB.accept(snparLibraryName, snparFastaFile);

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
