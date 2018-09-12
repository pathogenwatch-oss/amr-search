package net.cgps.wgsa.paarsnp;

import net.cgps.wgsa.paarsnp.core.lib.blast.BlastMatch;
import net.cgps.wgsa.paarsnp.core.lib.blast.BlastReader;
import net.cgps.wgsa.paarsnp.core.lib.blast.BlastRunner;
import net.cgps.wgsa.paarsnp.core.lib.utils.OverlapRemover;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;

public class ResistanceSearch<T> implements Function<String, T> {

  private final Logger logger = LoggerFactory.getLogger(ResistanceSearch.class);
  private final BlastRunner blastRunner;
  private final BlastReader blastReader;
  private final Collector<BlastMatch, ?, T> interpreter;
  private final OverlapRemover<BlastMatch> matchOverlapRemover;
  private final InputOptions searchOptions;
  private Predicate<BlastMatch> blastMatchFilter;

  ResistanceSearch(final InputOptions searchOptions, final Collector<BlastMatch, ?, T> collector, final Predicate<BlastMatch> blastMatchFilter) {
    this.searchOptions = searchOptions;
    this.blastMatchFilter = blastMatchFilter;

    this.blastRunner = new BlastRunner();
    this.blastReader = new BlastReader();
    this.interpreter = collector;
    this.matchOverlapRemover = new OverlapRemover<>(100);
  }

  @Override
  public T apply(final String assemblyId) {

    this.logger.debug("Preparing search request for {} with options:\n{}", assemblyId, this.searchOptions.getBlastOptions());

    final List<String> options = new ArrayList<>(this.searchOptions.getBlastOptions());
    options.add("-query");
    options.add(assemblyId);

    return this.blastReader.apply(this.blastRunner.apply(options.toArray(new String[0])))
        .peek(match -> this.logger.debug("Checking match {}", match.toString()))
        .filter(this.blastMatchFilter)
        .peek(match -> this.logger.debug("Pre-overlap check: {}", match.getBlastSearchStatistics().toString()))
        .collect(this.matchOverlapRemover)
        .stream()
        .peek(match -> this.logger.debug("After overlap removal: {}", match.getBlastSearchStatistics().toString()))
        .collect(this.interpreter);
  }

  static class InputOptions {

    private final Collection<String> blastOptions;

    InputOptions(final Collection<String> blastOptions) {
      this.blastOptions = blastOptions;
    }

    Collection<String> getBlastOptions() {
      return this.blastOptions;
    }

  }
}
