package net.cgps.wgsa.paarsnp;

import ch.qos.logback.classic.Level;
import net.cgps.wgsa.paarsnp.core.Constants;
import net.cgps.wgsa.paarsnp.core.lib.ObjectMappingException;
import net.cgps.wgsa.paarsnp.core.lib.json.AbstractJsonnable;
import net.cgps.wgsa.paarsnp.core.lib.json.AntimicrobialAgentLibrary;
import net.cgps.wgsa.paarsnp.core.paar.PaarLibrary;
import net.cgps.wgsa.paarsnp.core.snpar.json.SnparLibrary;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class PaarsnpMain {

  private final Logger logger = LoggerFactory.getLogger(PaarsnpMain.class);

  public static void main(final String[] args) {

    // Initialise the options parser
    final Options options = PaarsnpMain.myOptions();
    final CommandLineParser parser = new DefaultParser();

    if (args.length == 0) {
      final HelpFormatter formatter = new HelpFormatter();
      formatter.setWidth(200);
      formatter.printHelp("paarsnp-runner: ", options);
      System.exit(1);
    }

    try {
      final CommandLine commandLine = parser.parse(options, args);

      final ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
      root.setLevel(Level.valueOf(commandLine.getOptionValue('l', "INFO")));

      // Resolve the file path.
      final Path input = Paths.get(commandLine.getOptionValue('i'));

      final Collection<Path> fastas = new ArrayList<>();
      final Path workingDirectory;

      if (Files.exists(input, LinkOption.NOFOLLOW_LINKS)) {

        if (Files.isRegularFile(input)) {
          workingDirectory = input.getParent();
          fastas.add(input);
        } else {
          Files.newDirectoryStream(input, entry -> entry.endsWith(".fna") || entry.endsWith(".fa") || entry.endsWith("fasta")).forEach(fastas::add);
          workingDirectory = input;
        }
      } else if (Files.exists(Paths.get("/data", commandLine.getOptionValue('i')))) {
        fastas.add(Paths.get("/data", commandLine.getOptionValue('i')));
        workingDirectory = Paths.get("/data");
      } else {
        throw new RuntimeException("Can't find input file or directory " + input.toAbsolutePath().toString());
      }

      new PaarsnpMain().run(commandLine.getOptionValue('s'), fastas, workingDirectory, commandLine.hasOption('o'), commandLine.getOptionValue('d', "databases"));
    } catch (final Exception e) {
      LoggerFactory.getLogger(PaarsnpMain.class).error("Failed to run due to: ", e);
      System.exit(1);
    }
    System.exit(0);
  }

  private static Options myOptions() {

    // Required
    final Option speciesOption = Option.builder("s").longOpt("species").hasArg().argName("NCBI taxonomy numeric code").desc("Required: NCBI taxonomy numberic code for query species. e.g. 1280 for Staph. aureus").required().build();
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

  private void run(final String speciesId, final Collection<Path> assemblyFiles, final Path workingDirectory, final boolean isToStdout, final String resourceDirectory) {


    final PaarLibrary paarLibrary;
    final SnparLibrary snparLibrary;
    final AntimicrobialAgentLibrary agentLibrary;

    try {
      paarLibrary = AbstractJsonnable.fromJson(Paths.get(resourceDirectory, speciesId + Constants.PAAR_APPEND + Constants.JSON_APPEND).toFile(), PaarLibrary.class);
      snparLibrary = AbstractJsonnable.fromJson(Paths.get(resourceDirectory, speciesId + Constants.SNPAR_APPEND + Constants.JSON_APPEND).toFile(), SnparLibrary.class);
      agentLibrary = AbstractJsonnable.fromJson(Paths.get(resourceDirectory, speciesId + Constants.AGENT_FILE_APPEND).toFile(), AntimicrobialAgentLibrary.class);
    } catch (final ObjectMappingException e) {
      throw new RuntimeException(e);
    }

    final int cores = Runtime.getRuntime().availableProcessors();
    final int blastThreads = cores > 2 ? cores - 1 : cores;
    final ExecutorService executorService = Executors.newFixedThreadPool(blastThreads);
    final Paarsnp paarsnp = new Paarsnp(speciesId, paarLibrary, snparLibrary, agentLibrary.getAgents(), resourceDirectory, executorService);

    final Consumer<PaarsnpResult> resultWriter = this.getWriter(isToStdout, workingDirectory);

    // Run paarsnp on each assembly file.
    assemblyFiles
        .parallelStream()
        .map(paarsnp)
        .peek(paarsnpResult -> this.logger.debug("{}", paarsnpResult.toPrettyJson()))
        .forEach(resultWriter);
  }

  private Consumer<PaarsnpResult> getWriter(final boolean isToStdout, final Path workingDirectory) {

    if (isToStdout) {
      return paarsnpResult -> {
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
}
