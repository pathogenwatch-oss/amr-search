package net.cgps.wgsa.paarsnp.core.snpar.codonmapping;

import net.cgps.wgsa.paarsnp.core.lib.blast.BlastMatch;
import net.cgps.wgsa.paarsnp.core.snpar.CodonMap;

import java.util.HashMap;
import java.util.function.Function;

public class CodonMapper implements Function<BlastMatch, CodonMap> {

  @Override
  public CodonMap apply(final BlastMatch match) {

    final int firstCodonIndex = (int) Math.ceil((double) match.getBlastSearchStatistics().getLibrarySequenceStart() / 3.0);
    final int mod = (match.getBlastSearchStatistics().getLibrarySequenceStart() - 1) % 3;
    final int offset = mod == 0 ? 0 : 3 - mod;

    final var referenceAlignment = match.getReferenceMatchSequence().substring(offset);
    final var queryAlignment = match.getForwardQuerySequence().substring(offset);

    final var frameshiftFilter = new CreateFrameshiftFilter().apply(referenceAlignment, queryAlignment);

    var inFrameshiftRegion = false;
    final var codonMap = new HashMap<Integer, String>(10000);
    final var insertMap = new HashMap<Integer, String>(100);
    final int startPosition = offset == 0 ? 0 : 3 - offset;

    int refCodonLocation = firstCodonIndex + (0 == offset ? 0 : 1);
    final StringBuilder currentCodon = new StringBuilder(3);
    final StringBuilder currentInsert = new StringBuilder(20);

    for (int alignmentIndex = startPosition; alignmentIndex < match.getReferenceMatchSequence().length(); alignmentIndex++) {
      final char refChar = match.getReferenceMatchSequence().charAt(alignmentIndex);
      final char queryChar = match.getForwardQuerySequence().charAt(alignmentIndex);

      if (frameshiftFilter.get(alignmentIndex)) {
        inFrameshiftRegion = true;
      }

      if ('-' != refChar) {

        if (0 != currentInsert.length()) {
          insertMap.put(refCodonLocation - 1, currentInsert.toString());
          currentInsert.setLength(0);
        }

        currentCodon.append(queryChar);

        if (3 == currentCodon.length()) {
          // Codon is complete
          codonMap.put(refCodonLocation, inFrameshiftRegion ? "!!!" : currentCodon.toString());
          currentCodon.setLength(0);
          refCodonLocation++;
          inFrameshiftRegion = false;
        }
      } else {
        currentInsert.append(queryChar);
      }
    }

    return new CodonMap(codonMap, insertMap);
  }

}
