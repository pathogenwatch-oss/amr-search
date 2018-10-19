package net.cgps.wgsa.paarsnp.core.snpar;

import net.cgps.wgsa.paarsnp.core.lib.blast.BlastMatch;
import net.cgps.wgsa.paarsnp.core.lib.utils.DnaSequence;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class CodonMapper implements Function<BlastMatch, CodonMap> {

  @Override
  public CodonMap apply(final BlastMatch match) {

    final Map<Integer, String> codonMap = new HashMap<>();
    final Map<Integer, Integer> positionMap = new HashMap<>();

    final int firstCodonIndex = (int) Math.ceil((double) match.getBlastSearchStatistics().getLibrarySequenceStart() / 3.0);

    // Deal with out of frame matches
    final int offset = (match.getBlastSearchStatistics().getLibrarySequenceStart() - 1) % 3;
    final int startPosition = offset == 0 ? 0 : 3 - offset;

    int refCodonLocation = firstCodonIndex + (0 == offset ? 0 : 1);
    final StringBuilder currentCodon = new StringBuilder(3);
    final AtomicInteger queryOffset = new AtomicInteger(0);

    for (int alignmentIndex = startPosition; alignmentIndex < match.getReferenceMatchSequence().length(); alignmentIndex++) {
      final char refChar = match.getReferenceMatchSequence().charAt(alignmentIndex);
      final char queryChar = match.getForwardQuerySequence().charAt(alignmentIndex);

      if ('-' != refChar) {
        currentCodon.append(queryChar);
        if ('-' == queryChar) {
          queryOffset.decrementAndGet();
        }
        if (3 == currentCodon.length()) {
          // Codon is complete
          codonMap.put(refCodonLocation, currentCodon.toString());
          final int queryPosition = DnaSequence.Strand.FORWARD == match.getBlastSearchStatistics().getStrand() ?
                                    match.getBlastSearchStatistics().getQuerySequenceStart() + alignmentIndex + queryOffset.get() :
                                    match.getBlastSearchStatistics().getQuerySequenceStop() - alignmentIndex - queryOffset.get();
          positionMap.put(refCodonLocation, queryPosition);
          currentCodon.setLength(0);
          refCodonLocation++;
        }
      } else {
        queryOffset.incrementAndGet();
      }
    }
    return new CodonMap(codonMap, positionMap);
  }
}
