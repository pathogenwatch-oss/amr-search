package net.cgps.wgsa.paarsnp.core.snpar;

import net.cgps.wgsa.paarsnp.core.lib.blast.BlastSearchStatistics;

import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Function;

public class PromoterFetcher implements Function<BlastSearchStatistics, Optional<String>> {

  private final Path inputFasta;

  public PromoterFetcher(final Path inputFasta) {
    this.inputFasta = inputFasta;
  }

  @Override
  public Optional<String> apply(final BlastSearchStatistics blastSearchStatistics) {

    // Can't rely on promoter region if library sequence start isn't matched.
    if (1 < blastSearchStatistics.getLibrarySequenceStart()) {
      return Optional.empty();
    }

    final String contigSequence = new ContigDataSource(this.inputFasta).apply(blastSearchStatistics.getQuerySequenceId());

    return new ExtractPromoterRegion().apply(blastSearchStatistics, contigSequence);
  }
}

