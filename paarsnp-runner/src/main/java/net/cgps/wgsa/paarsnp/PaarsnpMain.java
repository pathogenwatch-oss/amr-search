package net.cgps.wgsa.paarsnp;

import ch.qos.logback.classic.Level;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.cgps.wgsa.paarsnp.core.AntimicrobialAgentLibrary;
import net.cgps.wgsa.paarsnp.core.paar.PaarLibrary;
import net.cgps.wgsa.paarsnp.core.snpar.SnparLibrary;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class PaarsnpMain {

  public static final String PAAR_FILE_APPEND = "_paar.jsn";
  public static final String SNPAR_FILE_APPEND = "_snpar.jsn";
  public static final String AGENT_FILE_APPEND = "_antimicrobials.jsn";
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

      new PaarsnpMain().run(commandLine.getOptionValue('s'), Arrays.asList(commandLine.getOptionValues('a')), commandLine.getOptionValue('d', "."));
    } catch (final Exception e) {
      LoggerFactory.getLogger(PaarsnpMain.class).error("Failed to run due to: ", e);
    }
  }

  private static Options myOptions() {

    // Required
    final Option speciesOption = Option.builder("s").longOpt("species").hasArg().argName("NCBI taxonomy numeric code").desc("Required: NCBI taxonomy numberic code for query species. e.g. 1280 for Staph. aureus").required().build();
    // Optional
    final Option assemblyListOption = Option.builder("a").longOpt("assembly-list").hasArg().argName("Assembly files to run").desc("Provide this option multiple times to run multiple assemblies (i.e. -a my_dir/assembly1.fna -a my_other_dir/assembly2.fna)").required().build();
    final Option resourceDirectoryOption = Option.builder("d").longOpt("database-directory").hasArg().argName("Database directory").desc("Location of the BLAST databases and resources for .").build();
    final Option logLevel = Option.builder("l").longOpt("log-level").hasArg().argName("Logging level").desc("INFO, DEBUG etc").build();

    final Options options = new Options();
    options.addOption(assemblyListOption)
        .addOption(speciesOption)
        .addOption(resourceDirectoryOption)
        .addOption(logLevel);

    return options;
  }

  private void run(final String speciesId, final Collection<String> assemblyFiles, String resourceDirectory) {

    final ObjectMapper objectMapper = new ObjectMapper();

    final PaarLibrary paarLibrary;
    final SnparLibrary snparLibrary;
    final AntimicrobialAgentLibrary agentLibrary;

    try {
      paarLibrary = objectMapper.readValue(Paths.get(resourceDirectory, speciesId + PAAR_FILE_APPEND).toFile(), PaarLibrary.class);
      snparLibrary = objectMapper.readValue(Paths.get(resourceDirectory, speciesId + SNPAR_FILE_APPEND).toFile(), SnparLibrary.class);
      agentLibrary = objectMapper.readValue(Paths.get(resourceDirectory, speciesId + AGENT_FILE_APPEND).toFile(), AntimicrobialAgentLibrary.class);
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }

    final int cores = Runtime.getRuntime().availableProcessors();
    final int blastThreads = cores > 2 ? cores - 1 : cores;
    final ExecutorService executorService = Executors.newFixedThreadPool(blastThreads);
    final Paarsnp paarsnp = new Paarsnp(speciesId, paarLibrary, snparLibrary, agentLibrary.getAgents(), resourceDirectory, executorService);

    final Consumer<PaarsnpResult> resultWriter = paarsnpResult -> {
      try (final StringWriter writer = new StringWriter()) {
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(writer, paarsnpResult);
      } catch (IOException e) {
        this.logger.error("Failed to write output for {}", paarsnpResult.getAssemblyId());
        throw new RuntimeException(e);
      }
    };

    assemblyFiles
        .parallelStream()
        .map(paarsnp)
        .forEach(resultWriter);

  }
}
