package net.cgps.wgsa.paarsnp.core.lib.blast;

import java.io.BufferedReader;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class MatchReader implements Function<BufferedReader, Stream<BlastMatch>> {

  private final Predicate<BlastMatch> filter;

  public MatchReader(final Predicate<BlastMatch> filter) {

    this.filter = filter;
  }

  @Override
  public Stream<BlastMatch> apply(final BufferedReader resultStream) {

    final BlastRowParser blastRowParser = new BlastRowParser();

    return resultStream
        .lines()
        .filter(line -> line.startsWith("#"))
        .map(line -> line.split("\\s+"))
        .map(blastRowParser)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .filter(this.filter);
  }
}
