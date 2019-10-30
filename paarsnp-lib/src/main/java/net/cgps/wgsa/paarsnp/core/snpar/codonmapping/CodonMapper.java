package net.cgps.wgsa.paarsnp.core.snpar.codonmapping;

import net.cgps.wgsa.paarsnp.core.lib.blast.BlastMatch;
import net.cgps.wgsa.paarsnp.core.snpar.CodonMap;

import java.util.HashMap;
import java.util.function.Function;

public class CodonMapper implements Function<BlastMatch, CodonMap> {

  @Override
  public CodonMap apply(final BlastMatch match) {

    final int firstCodonIndex = (int) Math.ceil((double) match.getBlastSearchStatistics().getLibrarySequenceStart() / 3.0);
    final var frame = FRAME.toFrame(match.getBlastSearchStatistics().getLibrarySequenceStart() % 3);

    final var referenceAlignment = getInframeSequence(frame, match.getReferenceMatchSequence());
    final var queryAlignment = getInframeSequence(frame, match.getForwardQuerySequence());

    final var frameshiftFilter = new CreateFrameshiftFilter().apply(referenceAlignment, queryAlignment);
    final var aaAlignment = new CreateAaAlignment().apply(referenceAlignment, queryAlignment);

    // TODO: Check this for new logic
    final var codonMap = new HashMap<Integer, String>(3000);
    final var insertMap = new HashMap<Integer, String>(100);

    var refCodonLocation = firstCodonIndex + (FRAME.ONE == frame ? 0 : 1);
    final var currentInsert = new StringBuilder(20);

    for (int i = 0; i < aaAlignment.getKey().length(); i++) {

      if (aaAlignment.getKey().charAt(i) != '-') {
        if (0 != currentInsert.length()) {
          insertMap.put(refCodonLocation - 1, currentInsert.toString());
          currentInsert.setLength(0);
        }
        codonMap.put(refCodonLocation, frameshiftFilter.get(i) ? "!" : String.valueOf(aaAlignment.getValue().charAt(i)));
        // Update the codon location if not an insert
        refCodonLocation++;
      } else if (!frameshiftFilter.get(i)) {
        currentInsert.append(aaAlignment.getValue().charAt(i));
      }
    }
//    var inFrameshiftRegion = false;
//    final var codonMap = new HashMap<Integer, String>(10000);
//    final var insertMap = new HashMap<Integer, String>(100);
//    final int startPosition = offset == 0 ? 0 : 3 - offset;
//
//    int refCodonLocation = firstCodonIndex + (0 == offset ? 0 : 1);
//    final var currentCodon = new StringBuilder(3);
//    final var currentInsert = new StringBuilder(20);
//
//    for (int alignmentIndex = startPosition; alignmentIndex < match.getReferenceMatchSequence().length(); alignmentIndex++) {
//      final var refChar = match.getReferenceMatchSequence().charAt(alignmentIndex);
//      final var queryChar = match.getForwardQuerySequence().charAt(alignmentIndex);
//
//      if (frameshiftFilter.get(alignmentIndex)) {
//        inFrameshiftRegion = true;
//      }
//
//      if ('-' != refChar) {
//        if (0 != currentInsert.length()) {
//          insertMap.put(refCodonLocation - 1, currentInsert.toString());
//          currentInsert.setLength(0);
//          inFrameshiftRegion = false;
//        }
//
//        currentCodon.append(queryChar);
//
//        if (3 == currentCodon.length()) {
//          // Codon is complete
//          codonMap.put(refCodonLocation, inFrameshiftRegion ? "!!!" : currentCodon.toString());
//          currentCodon.setLength(0);
//          refCodonLocation++;
//          inFrameshiftRegion = false;
//        }
//      } else {
//        currentInsert.append(queryChar);
//      }
//    }
    return new CodonMap(codonMap, insertMap);
  }

  public String getInframeSequence(final FRAME frame, final String referenceMatchSequence) {
    return referenceMatchSequence.substring(frame.getOffset());
  }
}
