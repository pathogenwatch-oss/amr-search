package net.cgps.wgsa.paarsnp.core.lib.blast;

import net.cgps.wgsa.paarsnp.core.lib.blast.ncbi.BlastOutput;
import net.cgps.wgsa.paarsnp.core.lib.utils.StreamGobbler;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.function.Function;

/**
 * T is the result type returned by the output parser.
 */
public class BlastRunner implements Function<String[], BlastOutput> {

  private final Logger logger = LoggerFactory.getLogger(BlastRunner.class);

  private static final String[] baseCommand = new String[]{
      "blastn",
      "-task", "blastn",
      "-outfmt", "5",
      "-num_alignments", "500",
  };

  @Override
  public BlastOutput apply(final String[] options) {

    final String[] command = ArrayUtils.addAll(baseCommand, options);
    final BlastReader.BlastXmlReader xmlReader = new BlastReader.BlastXmlReader();

    final ProcessBuilder pb = new ProcessBuilder(command);

    this.logger.debug(StringUtils.join(command, " "));

    Process p = null;

    try {
      // Start the process
      p = pb.start();

      try (final InputStream error = p.getErrorStream(); final BufferedReader outputReader = new BufferedReader(new InputStreamReader(p.getInputStream(), Charset.defaultCharset()))) {

        // create listener for the stderr on separate thread (reports stderr to log).
        final Thread errorGobbler = new StreamGobbler(error, "ERROR");
        errorGobbler.start();

        // Return finished result. The gobblers should clean themselves up.
        return xmlReader.apply(outputReader);
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
