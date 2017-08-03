package net.cgps.wgsa.paarsnp;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.cgps.wgsa.paarsnp.core.AntimicrobialAgentLibrary;
import net.cgps.wgsa.paarsnp.core.paar.PaarLibrary;
import net.cgps.wgsa.paarsnp.core.snpar.SnparLibrary;
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

  public static final String LIBRARY_FOLDER = "resources/libraries/";
  public static final String PAAR_FILE_APPEND = "_paar.jsn";
  public static final String SNPAR_FILE_APPEND = "_snpar.jsn";
  public static final String AGENT_FILE_APPEND = "_antimicrobials.jsn";
  private final Logger logger = LoggerFactory.getLogger(PaarsnpMain.class);

  public static void main(final String[] args) {

    final String speciesId = args[0];
    final Collection<String> assemblyFiles = Arrays.asList(Arrays.copyOfRange(args, 1, args.length));

    try {
      new PaarsnpMain().run(speciesId, assemblyFiles);
    } catch (final Exception e) {
      LoggerFactory.getLogger(PaarsnpMain.class).error("Failed to run due to: ", e);
    }
  }

  private void run(final String speciesId, final Collection<String> assemblyFiles) {

    final ObjectMapper objectMapper = new ObjectMapper();

    final PaarLibrary paarLibrary;
    final SnparLibrary snparLibrary;
    final AntimicrobialAgentLibrary agentLibrary;

    try {
      paarLibrary = objectMapper.readValue(Paths.get(LIBRARY_FOLDER, speciesId + PAAR_FILE_APPEND).toFile(), PaarLibrary.class);
      snparLibrary = objectMapper.readValue(Paths.get(LIBRARY_FOLDER, speciesId + SNPAR_FILE_APPEND).toFile(), SnparLibrary.class);
      agentLibrary = objectMapper.readValue(Paths.get(LIBRARY_FOLDER, speciesId + AGENT_FILE_APPEND).toFile(), AntimicrobialAgentLibrary.class);
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }

    final int cores = Runtime.getRuntime().availableProcessors();
    final int blastThreads = cores > 2 ? cores - 1 : cores;
    final ExecutorService executorService = Executors.newFixedThreadPool(blastThreads);
    final Paarsnp paarsnp = new Paarsnp(speciesId, paarLibrary, snparLibrary, agentLibrary.getAgents(), executorService);

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
