package net.cgps.wgsa.paarsnp.core.lib.blast;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.function.Function;

/**
 * T is the result type returned by the output parser.
 */
public class BlastRunner<T> implements Function<String[], T> {

  private final Logger logger = LoggerFactory.getLogger(BlastRunner.class);

  private final Function<BufferedReader, T> resultParser;

  public BlastRunner(final Function<BufferedReader, T> resultParser) {

    this.resultParser = resultParser;
  }

  @Override
  public T apply(final String[] command) {

    final ProcessBuilder pb = new ProcessBuilder(command);

    this.logger.info(StringUtils.join(command, " "));

    Process p = null;

    try {
      // Start the process
      p = pb.start();

      try (final InputStream error = p.getErrorStream(); final BufferedReader outputReader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {

        // create listener for the stderr on separate thread (reports stderr to log).
        final Thread errorGobbler = new StreamGobbler(error, "ERROR");
        errorGobbler.start();

        // Return finished result. The gobblers should clean themselves up.
        return this.resultParser.apply(outputReader);
      }
    } catch (final IOException e) {
      this.logger.error("BLAST Failure", e);
      throw new RuntimeException(e);
    } finally {
      if (null != p) {
        p.destroy();
      }
    }

  }
}
