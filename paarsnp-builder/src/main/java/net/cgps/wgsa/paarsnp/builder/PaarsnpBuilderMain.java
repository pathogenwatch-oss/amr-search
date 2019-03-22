package net.cgps.wgsa.paarsnp.builder;

import ch.qos.logback.classic.Level;
import net.cgps.wgsa.paarsnp.core.models.LibraryVersion;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.function.Consumer;

public class PaarsnpBuilderMain {

  private static final String DEFAULT_CONFIG_PATH = "resources";
  private static final String DEFAULT_LIBRARYPATH = "libraries";


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

      new PaarsnpBuilderMain().run(
          commandLine.getOptionValue('r', DEFAULT_CONFIG_PATH),
          commandLine.getOptionValue('l', DEFAULT_LIBRARYPATH),
          commandLine.getOptionValue('o', "."));

    } catch (final Exception e) {
      LoggerFactory.getLogger(PaarsnpBuilderMain.class).error("Failed to run due to: ", e);
      System.exit(1);
    }
  }

  private static Options myOptions() {

    final Option inputDirectoryOption = Option.builder("r").longOpt("resource-dir").hasArg().argName("Config directory").desc("Optional: Input database directory. Defaults to " + DEFAULT_CONFIG_PATH).build();
    final Option libraryDirectoryOption = Option.builder("l").longOpt("library-dir").hasArg().argName("Library directory").desc("Location of AMR libraries").build();
    final Option outputDirectoryOption = Option.builder("o").longOpt("output-dir").hasArg().argName("Output directory").desc("Optional: Output location for BLAST and paarsnp databases.").required().build();

    final Option logLevel = Option.builder("l").longOpt("log-level").hasArg().argName("Logging level").desc("INFO, DEBUG etc").build();

    return new Options()
        .addOption(outputDirectoryOption)
        .addOption(inputDirectoryOption)
        .addOption(libraryDirectoryOption)
        .addOption(logLevel);
  }

  private void run(String config, final String libraryDirectory, final String outputDirectory) {

    final Path resourceDirectoryPath = Paths.get(config);
    final Path outputDirectoryPath = Paths.get(outputDirectory);
    final Path libraryDirectoryPath = Paths.get(libraryDirectory);

    if (!Files.exists(resourceDirectoryPath)) {
      throw new RuntimeException("Input folder " + resourceDirectoryPath.toAbsolutePath().toString() + " does not exist.");
    }

    if (!Files.exists(libraryDirectoryPath)) {
      throw new RuntimeException("Input folder " + libraryDirectoryPath.toAbsolutePath().toString() + " does not exist.");
    }

    if (this.fileMissing(outputDirectoryPath)) {
      try {
        Files.createDirectories(outputDirectoryPath);
      } catch (IOException e) {
        throw new RuntimeException("Unable to create output folder " + outputDirectoryPath.toAbsolutePath().toString());
      }
    }

    final Consumer<LibraryVersion> dataCreator = new GeneratePaarsnpData(libraryDirectoryPath, outputDirectoryPath);

    new LibraryConfigReader()
        .apply(resourceDirectoryPath)
        .forEach(dataCreator);
  }

  private boolean fileMissing(final Path... files) {
    return Arrays
        .stream(files)
        .anyMatch(file -> !Files.exists(file));
  }
}
