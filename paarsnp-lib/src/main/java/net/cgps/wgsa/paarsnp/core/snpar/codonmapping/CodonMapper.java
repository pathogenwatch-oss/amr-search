package net.cgps.wgsa.paarsnp.core.snpar.codonmapping;

import net.cgps.wgsa.paarsnp.core.lib.blast.BlastMatch;
import net.cgps.wgsa.paarsnp.core.snpar.AaAlignment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.function.Function;

public class CodonMapper implements Function<BlastMatch, AaAlignment> {

  private final Logger logger = LoggerFactory.getLogger(CodonMapper.class);
  private final FrameshiftFilter frameshiftFilter;

  public CodonMapper(final FrameshiftFilter frameshiftFilter) {
    this.frameshiftFilter = frameshiftFilter;
  }

  @Override
  public AaAlignment apply(final BlastMatch match) {

    this.logger.debug("Creating codon map for {}", match.getBlastSearchStatistics().getReferenceId());
    final var firstCodonIndex = (int) Math.ceil((double) match.getBlastSearchStatistics().getReferenceStart() / 3.0);
    final var frame = FRAME.toFrame(match.getBlastSearchStatistics().getReferenceStart());

    final var referenceAlignment = getInframeSequence(frame, match.getReferenceMatchSequence());
    final var queryAlignment = getInframeSequence(frame, match.getForwardQuerySequence());

    final var aaAlignment = new CreateAaAlignment().apply(referenceAlignment, queryAlignment);

    final var codonMap = new HashMap<Integer, Character>(3000);
    final var queryLocationMap = new HashMap<Integer, Integer>(3000);
    final var insertMap = new HashMap<Integer, String>(100);

    var refCodonLocation = firstCodonIndex + (FRAME.ONE == frame ? 0 : 1);
    var queryCodonLocation = FRAME.ONE == frame ? 1 : 2;
    final var currentInsert = new StringBuilder(20);

    for (int i = 0; i < aaAlignment.getKey().length(); i++) {

      if (aaAlignment.getKey().charAt(i) != '-') {
        if (0 != currentInsert.length()) {
          insertMap.put(refCodonLocation - 1, currentInsert.toString());
          queryCodonLocation += currentInsert.length();
          currentInsert.setLength(0);
        }
        final var isFrameshifted = frameshiftFilter.checkCodon(refCodonLocation);
        codonMap.put(refCodonLocation, isFrameshifted ? '!' : aaAlignment.getValue().charAt(i));
        queryLocationMap.put(refCodonLocation, isFrameshifted ? -1 : queryCodonLocation);
        refCodonLocation++;
        // Update the codon location if not a deletion
        if (aaAlignment.getValue().charAt(i) != '-') {
          queryCodonLocation++;
        }
      } else {
        currentInsert.append(aaAlignment.getValue().charAt(i));
      }
    }
    return new AaAlignment(codonMap, match.getBlastSearchStatistics().getQueryStart() + frame.getIndex(), queryLocationMap, insertMap);
  }

  public String getInframeSequence(final FRAME frame, final String referenceMatchSequence) {
    return referenceMatchSequence.substring(frame.getCodonOffset());
  }
}
