package net.cgps.wgsa.paarsnp.core.lib.blast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

/**
 * Captures streams and logs them.
 */
public class StreamGobbler extends Thread {

  private final Logger logger = LoggerFactory.getLogger(StreamGobbler.class);
  private final InputStream is;
  private final String type;

  public StreamGobbler(final InputStream is, final String type) {

    this.is = is;
    this.type = type;
  }

  @Override
  public void run() {

    try (final BufferedReader br = new BufferedReader(new InputStreamReader(this.is, Charset.defaultCharset()))) {
      String line;
      while ((line = br.readLine()) != null) {
        this.getLogger().info(this.type + "> " + line);
      }
    } catch (final IOException e) {
      this.logger.info("{} stream closed.", this.type);
      Thread.currentThread().interrupt();
    }
  }

  protected Logger getLogger() {

    return this.logger;
  }

  @Override
  public String toString() {

    return "StreamGobbler{" + "is=" + this.is + ", type='" + this.type + '\'' + '}';
  }
}
