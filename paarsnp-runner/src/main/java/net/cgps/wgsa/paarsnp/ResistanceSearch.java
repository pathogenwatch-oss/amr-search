package net.cgps.wgsa.paarsnp;

import net.cgps.wgsa.paarsnp.core.lib.InputOptions;
import net.cgps.wgsa.paarsnp.core.lib.OverlapRemover;
import net.cgps.wgsa.paarsnp.core.lib.blast.BlastMatch;
import net.cgps.wgsa.paarsnp.core.lib.blast.BlastRunner;
import net.cgps.wgsa.paarsnp.core.lib.blast.MutationReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;
import java.util.stream.Collector;

public class ResistanceSearch<T> implements Function<InputOptions, T> {

  private final Logger logger = LoggerFactory.getLogger(ResistanceSearch.class);
  private final BlastRunner blastRunner;
  private final MutationReader mutationReader;
  private final Collector<BlastMatch,?,T> interpreter;
  private final OverlapRemover<BlastMatch> matchOverlapRemover;

  ResistanceSearch(final Collector<BlastMatch, ?, T> collector) {

    this.blastRunner = new BlastRunner();
    this.mutationReader = new MutationReader();
    this.interpreter = collector;
    this.matchOverlapRemover = new OverlapRemover<>(100);
  }

  @Override
  public T apply(final InputOptions inputData) {

    this.logger.debug("Preparing SNPAR request for {} ", inputData.getAssemblyId());

    return mutationReader.apply(blastRunner.apply(inputData.getBlastOptions().toArray(new String[0])))
        .filter(match -> inputData.getCoverageThreshold() <
            (((double) match.getBlastSearchStatistics().getLibrarySequenceStop() - match.getBlastSearchStatistics().getLibrarySequenceStart() + 1)
                / (double) match.getBlastSearchStatistics().getLibrarySequenceLength())
                * 100)
        .collect(this.matchOverlapRemover)
        .stream()
        .collect(this.interpreter);
  }
}
