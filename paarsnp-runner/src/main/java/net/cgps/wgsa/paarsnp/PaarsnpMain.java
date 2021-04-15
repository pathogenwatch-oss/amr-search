package net.cgps.wgsa.paarsnp;

import ch.qos.logback.classic.Level;
import net.cgps.wgsa.paarsnp.core.Constants;
import net.cgps.wgsa.paarsnp.core.lib.AbstractJsonnable;
import net.cgps.wgsa.paarsnp.core.models.PaarsnpLibrary;
import net.cgps.wgsa.paarsnp.output.ResultJson;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class PaarsnpMain {

  private final Logger logger = LoggerFactory.getLogger(PaarsnpMain.class);

  public static void main(final String[] args) {

    // Initialise the options parser
    final Options options = PaarsnpMain.myOptions();
    final CommandLineParser parser = new DefaultParser();

    if (args.length == 0) {
      final HelpFormatter formatter = new HelpFormatter();
      formatter.setWidth(200);
      formatter.printHelp("java -jar paarsnp.jar <options>", options);
      System.exit(1);
    }

    final CommandLine commandLine;
    try {
      commandLine = parser.parse(options, args);
    } catch (final ParseException e) {
      throw new RuntimeException(e);
    }

    final ch.qos.logback.classic.Logger rootLogger = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    rootLogger.setLevel(Level.valueOf(commandLine.getOptionValue('l', "INFO")));

    // Resolve the file path.
    final Path input = Paths.get(commandLine.getOptionValue('i'));

    final Collection<Path> fastas;
    final Path workingDirectory;

    if (Files.exists(input, LinkOption.NOFOLLOW_LINKS)) {

      if (Files.isRegularFile(input)) {
        workingDirectory = input.toAbsolutePath().getParent();
        fastas = Collections.singletonList(input);
        rootLogger.debug("Processing one file", input);
      } else {
        fastas = new ArrayList<>(10000);
        try (final DirectoryStream<Path> stream = Files.newDirectoryStream(
            input,
            entry -> entry.toString().endsWith(".fna") || entry.toString().endsWith(".fa") || entry.toString().endsWith(".fasta"))) {
          stream.forEach(fastas::add);
        } catch (final IOException e) {
          rootLogger.error("Failed to read input FASTA {}", input.toAbsolutePath().toString());
          throw new RuntimeException(e);
        }
        rootLogger.debug("Processing {} files from \"{}\".", fastas.size(), input.toAbsolutePath().toString());
        workingDirectory = input;
      }
    } else {
      throw new RuntimeException("Can't find input file or directory " + input.toAbsolutePath().toString());
    }

    String databasePath = commandLine.getOptionValue('d', "databases");

    // Little shim for running in Docker.
    if (!Files.exists(Paths.get(databasePath))) {
      databasePath = "/paarsnp/" + databasePath;
    }

    new PaarsnpMain().run(commandLine.getOptionValues('s'), fastas, workingDirectory, commandLine.hasOption('o'), databasePath);
    System.exit(0);
  }

  private static Options myOptions() {

    // Required
    final Option speciesOption = Option.builder("s").longOpt("species").hasArgs().argName("NCBI taxonomy numeric code").desc("Required: NCBI taxonomy numberic code for query species. e.g. 1280 for Staph. aureus. More than one code can be provided, paarsnp will use the first that matches a library.").required().build();
    // Optional
    final Option assemblyListOption = Option.builder("i").longOpt("input").hasArg().argName("Assembly file(s)").desc("If a directory is provided then all FASTAs (.fna, .fa, .fasta) are searched.").build();
    final Option resourceDirectoryOption = Option.builder("d").longOpt("database-directory").hasArg().argName("Database directory").desc("Location of the BLAST databases and resources for .").build();
    final Option logLevel = Option.builder("l").longOpt("log-level").hasArg().argName("Logging level").desc("INFO, DEBUG etc").build();
    final Option outputOption = Option.builder("o").longOpt("outfile").argName("Create output file").desc("Use this flag if you want the result written to STDOUT rather than file.").build();

    final Options options = new Options();
    options.addOption(assemblyListOption)
        .addOption(speciesOption)
        .addOption(resourceDirectoryOption)
        .addOption(outputOption)
        .addOption(logLevel);

    return options;
  }

  private void run(final String[] taxonIds, final Collection<Path> assemblyFiles, final Path workingDirectory, final boolean isToStdout, final String resourceDirectory) {

    final Optional<String> speciesIdOpt = this.selectLibrary(resourceDirectory, taxonIds);
    final String speciesId = speciesIdOpt.orElseThrow(() -> new RuntimeException("No library found for supplied identifiers."));
    this.logger.info("Selected {}", speciesId);
    final Path paarsnpLibraryFile = Paths.get(resourceDirectory, speciesId + Constants.JSON_APPEND);
    final PaarsnpLibrary paarsnpLibrary = AbstractJsonnable.fromJsonFile(paarsnpLibraryFile.toFile(), PaarsnpLibrary.class);

    final PaarsnpRunner paarsnpRunner = new PaarsnpRunner(paarsnpLibrary, resourceDirectory);
    final Consumer<ResultJson> resultWriter = this.getWriter(isToStdout, workingDirectory);

    // Run paarsnp on each assembly file.
    assemblyFiles
        .parallelStream()
        .peek(assemblyFile -> this.logger.info("{}", assemblyFile.toString()))
        .map(paarsnpRunner)
        .peek(paarsnpResult -> this.logger.debug("{}", paarsnpResult.toPrettyJson()))
//        .map(result -> new ConvertResultFormat().apply(result))
        .forEach(resultWriter);
  }

  private Optional<String> selectLibrary(final String resourceDirectory, final String[] taxonIds) {
    final Map<String, String> taxonIdMap = this.readTaxonIdMap(resourceDirectory);
    for (final String id : taxonIds) {
      if (taxonIdMap.containsKey(id)) {
        return Optional.of(taxonIdMap.get(id));
      }
    }
    return Optional.empty();
  }

  private Consumer<ResultJson> getWriter(final boolean isToStdout, final Path workingDirectory) {

    if (isToStdout) {
      return paarsnpResult -> {
        paarsnpResult.unsetAssemblyId();
        try (final BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(System.out))) {
          bufferedWriter.append(paarsnpResult.toJson());
          bufferedWriter.newLine();
        } catch (final IOException e) {
          throw new RuntimeException(e);
        }
      };
    } else {
      return paarsnpResult -> {
        final Path outFile = Paths.get(workingDirectory.toString(), paarsnpResult.getAssemblyId() + "_paarsnp.jsn");

        this.logger.info("Writing {}", outFile.toAbsolutePath().toString());

        try (final BufferedWriter writer = Files.newBufferedWriter(outFile)) {
          writer.write(paarsnpResult.toPrettyJson());
        } catch (IOException e) {
          this.logger.error("Failed to write output for {}", paarsnpResult.getAssemblyId());
          throw new RuntimeException(e);
        }
      };
    }
  }

  private Map<String, String> readTaxonIdMap(final String resourceDirectory) {

    try {
      return Files.readAllLines(Paths.get(resourceDirectory, "taxid.map"))
          .stream()
          .map(line -> line.split("\\s+"))
          .collect(Collectors.toMap(a -> a[0], b -> b[1]));
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }
}
