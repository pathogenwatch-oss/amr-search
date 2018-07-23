package net.cgps.wgsa.paarsnp;

import net.cgps.wgsa.paarsnp.core.lib.blast.BlastMatch;
import net.cgps.wgsa.paarsnp.core.lib.blast.BlastReader;
import net.cgps.wgsa.paarsnp.core.lib.blast.BlastRunner;
import net.cgps.wgsa.paarsnp.core.lib.utils.OverlapRemover;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;

public class ResistanceSearch<T> implements Function<ResistanceSearch.InputOptions, T> {

  private final Logger logger = LoggerFactory.getLogger(ResistanceSearch.class);
  private final BlastRunner blastRunner;
  private final BlastReader blastReader;
  private final Collector<BlastMatch, ?, T> interpreter;
  private final OverlapRemover<BlastMatch> matchOverlapRemover;
  private Predicate<BlastMatch> blastMatchFilter;

  ResistanceSearch(final Collector<BlastMatch, ?, T> collector, final Predicate<BlastMatch> blastMatchFilter) {
    this.blastMatchFilter = blastMatchFilter;

    this.blastRunner = new BlastRunner();
    this.blastReader = new BlastReader();
    this.interpreter = collector;
    this.matchOverlapRemover = new OverlapRemover<>(100);
  }

  @Override
  public T apply(final InputOptions inputData) {

    this.logger.debug("Preparing search request for {} with options:\n{}", inputData.getAssemblyId(), inputData.getBlastOptions());

    return blastReader.apply(blastRunner.apply(inputData.getBlastOptions().toArray(new String[0])))
        .filter(this.blastMatchFilter)
        .peek(match -> this.logger.debug("{}", match.getBlastSearchStatistics().toString()))
        .collect(this.matchOverlapRemover)
        .stream()
        .collect(this.interpreter);
  }

  public static class InputOptions {

    private final String assemblyId;
    private final Collection<String> blastOptions;
    private final float coverageThreshold;

    public InputOptions(String assemblyId, final Collection<String> blastOptions, float coverageThreshold) {
      this.assemblyId = assemblyId;
      this.blastOptions = blastOptions;
      this.coverageThreshold = coverageThreshold;
    }

    public Collection<String> getBlastOptions() {
      return this.blastOptions;
    }

    public String getAssemblyId() {
      return this.assemblyId;
    }

    public float getCoverageThreshold() {
      return this.coverageThreshold;
    }
  }
}
