package net.cgps.wgsa.paarsnp.core.snpar;

import net.cgps.wgsa.paarsnp.core.lib.blast.BlastSearchStatistics;
import net.cgps.wgsa.paarsnp.core.lib.utils.DnaSequence;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
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

    final StringBuilder contigSequence = new StringBuilder();
    try (final BufferedReader br = Files.newBufferedReader(this.inputFasta)) {
      String line;
      while ((line = br.readLine()) != null) {

        if (line.startsWith(">")) {
          if (blastSearchStatistics.getQuerySequenceId().equals(line.replace(">", ""))) {
            break;
          }
        }
      }
      while ((line = br.readLine()) != null) {
        if (line.startsWith(">")) {
          break;
        }
        contigSequence.append(line);
      }
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }


    final Optional<String> promoterSequence;

    if (DnaSequence.Strand.FORWARD == blastSearchStatistics.getStrand()) {

      if (1 == blastSearchStatistics.getQuerySequenceStart()) {
        promoterSequence = Optional.empty();
      } else {
        final int startSite = 60 < blastSearchStatistics.getQuerySequenceStart() ?
                              1 :
                              blastSearchStatistics.getQuerySequenceStop() - 60;

        promoterSequence = Optional.of(contigSequence.substring(startSite - 1, blastSearchStatistics.getQuerySequenceStart() - 1));
      }
    } else {
      if (contigSequence.length() == blastSearchStatistics.getQuerySequenceStop()) {
        promoterSequence = Optional.empty();
      } else {
        final int startSite = 60 < contigSequence.length() - blastSearchStatistics.getQuerySequenceStop() ?
                              contigSequence.length() :
                              blastSearchStatistics.getQuerySequenceStop() + 60;
        promoterSequence = Optional.of(DnaSequence.complement(contigSequence.substring(blastSearchStatistics.getLibrarySequenceStop() - 1, startSite)));
      }
    }

    return promoterSequence;
  }
}

