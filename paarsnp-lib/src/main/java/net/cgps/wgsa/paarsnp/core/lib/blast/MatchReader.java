package net.cgps.wgsa.paarsnp.core.lib.blast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class MatchReader implements Function<BufferedReader, Stream<BlastMatch>> {

  private final Logger logger = LoggerFactory.getLogger(MatchReader.class);
  private final Predicate<BlastMatch> filter;

  public MatchReader(final Predicate<BlastMatch> filter) {

    this.filter = filter;
  }

  @Override
  public Stream<BlastMatch> apply(final BufferedReader resultStream) {

    final BlastRowParser blastRowParser = new BlastRowParser();

    final Collection<BlastMatch> matchesTmp = new ArrayList<>();

    // NB If this is implemented using the streaming API it caused the result stream to be read before it is opened.
    String line;

    try {
      while ((line = resultStream.readLine()) != null) {

        this.logger.debug(line);

        if (line.startsWith("#")) {
          continue;
        }

        final BlastMatch blastMatch = blastRowParser.apply(line.split("\\s+")).orElseThrow(RuntimeException::new);
        if (this.filter.test(blastMatch)) {
          matchesTmp.add(blastMatch);
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return matchesTmp.stream();
  }
}
