package net.cgps.wgsa.paarsnp.core.snpar;

import net.cgps.wgsa.paarsnp.core.lib.blast.BlastMatch;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class CodonMapper implements Function<BlastMatch, CodonMap> {

  @Override
  public CodonMap apply(final BlastMatch match) {

    final Map<Integer, String> codonMap = new HashMap<>(10000);
    final Map<Integer, String> insertMap = new HashMap<>(100);

    final int firstCodonIndex = (int) Math.ceil((double) match.getBlastSearchStatistics().getLibrarySequenceStart() / 3.0);

    // Deal with out of frame matches
    final int offset = (match.getBlastSearchStatistics().getLibrarySequenceStart() - 1) % 3;
    final int startPosition = offset == 0 ? 0 : 3 - offset;

    int refCodonLocation = firstCodonIndex + (0 == offset ? 0 : 1);
    final StringBuilder currentCodon = new StringBuilder(3);
    final StringBuilder currentInsert = new StringBuilder(20);

    for (int alignmentIndex = startPosition; alignmentIndex < match.getReferenceMatchSequence().length(); alignmentIndex++) {
      final char refChar = match.getReferenceMatchSequence().charAt(alignmentIndex);
      final char queryChar = match.getForwardQuerySequence().charAt(alignmentIndex);

      if ('-' != refChar) {

        if (0 != currentInsert.length()) {
          insertMap.put(refCodonLocation - 1, currentInsert.toString());
          currentInsert.setLength(0);
        }

        currentCodon.append(queryChar);

        if (3 == currentCodon.length()) {
          // Codon is complete
          codonMap.put(refCodonLocation, currentCodon.toString());
          currentCodon.setLength(0);
          refCodonLocation++;
        }
      } else {
        currentInsert.append(queryChar);
      }
    }
    return new CodonMap(codonMap, insertMap);
  }
}
