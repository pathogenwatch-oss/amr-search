package net.cgps.wgsa.paarsnp.builder;

import net.cgps.wgsa.paarsnp.core.lib.blast.StreamGobbler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.function.BiConsumer;

public class MakeBlastDB implements BiConsumer<String, Path> {

  private final Logger logger = LoggerFactory.getLogger(MakeBlastDB.class);

  private final Path outputDirectory;

  public MakeBlastDB(final Path outputDirectory) {
    this.outputDirectory = outputDirectory;
  }

  @Override
  public void accept(final String libraryName, final Path fastaPath) {
    final String[] cmd = {"makeblastdb",
        "-dbtype",
        "nucl",
        "-parse_seqids",
        "-in",
        fastaPath.toAbsolutePath().toString(),
        "-title",
        libraryName,
        "-out",
        FileSystems.getDefault().getPath(this.outputDirectory.toString(), libraryName).toAbsolutePath().toString()};

    final ProcessBuilder pb = new ProcessBuilder(cmd);

    this.logger.info("Prepared command: {}", StringUtils.join(cmd, " "));

    final Process p;

    try {
      p = pb.start();
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }

    this.logger.debug("Started running library build.");
    // Start the process
    try (final InputStream error = p.getErrorStream(); final InputStream stdout = p.getInputStream()) {

      // create listener for the stderr on separate thread (reports stderr to log).
      final Thread errorGobbler = new StreamGobbler(error, "ERROR");
      errorGobbler.start();

      final Thread stdoutGobbler = new StreamGobbler(stdout, "STDOUT");
      stdoutGobbler.start();

      final int result = p.waitFor();
      if (result != 0) {
        throw new RuntimeException("Make BLAST DB failed.");
      }
    } catch (final IOException | InterruptedException e) {
      this.logger.error("BLAST Failure", e);
      throw new RuntimeException(e);
    } finally {
      p.destroy();
    }

    this.logger.info("Library written to {}", fastaPath.toAbsolutePath());
  }
}
