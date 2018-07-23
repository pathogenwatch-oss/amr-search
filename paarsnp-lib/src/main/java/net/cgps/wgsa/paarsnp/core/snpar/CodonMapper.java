package net.cgps.wgsa.paarsnp.core.snpar;

import net.cgps.wgsa.paarsnp.core.lib.blast.BlastMatch;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class CodonMapper implements Function<BlastMatch, CodonMap> {

  @Override
  public CodonMap apply(final BlastMatch match) {

    final Map<Integer, String> codonMap = new HashMap<>();

    final int firstCodonIndex = (int) Math.floor(match.getBlastSearchStatistics().getLibrarySequenceStart() / 3) + 1;

    // Deal with out of frame matches
    final int offset = (match.getBlastSearchStatistics().getLibrarySequenceStart() - 1) % 3;

    int refCodonLocation = firstCodonIndex;
    final StringBuilder currentCodon = new StringBuilder(3);

    for (int alignmentIndex = offset; alignmentIndex < match.getReferenceMatchSequence().length(); alignmentIndex++) {
      final char refChar = match.getReferenceMatchSequence().charAt(alignmentIndex);
      final char queryChar = match.getForwardQuerySequence().charAt(alignmentIndex);

      if ('-' != refChar) {
        currentCodon.append(queryChar);
        if (3 == currentCodon.length()) {
          // Codon is complete
          codonMap.put(refCodonLocation, currentCodon.toString());
          currentCodon.setLength(0);
          refCodonLocation++;
        }
      }
    }
    return new CodonMap(match.getBlastSearchStatistics().getLibrarySequenceId(), codonMap);
  }
}
