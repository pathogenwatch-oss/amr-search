package net.cgps.wgsa.paarsnp.core.snpar.codonmapping;

import net.cgps.wgsa.paarsnp.core.lib.utils.DnaSequence;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.*;
import java.util.function.BiFunction;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CreateFrameshiftFilter implements BiFunction<String, String, BitSet> {

  private final Pattern indelPattern = Pattern.compile("-+");

  @Override
  public BitSet apply(final String referenceSequence, final String querySequence) {
    // List of frameshifting indels with frameshift as second value
    final var frameShiftLocations = Stream.concat(
        this.extractFrameshifts(referenceSequence, 1),
        this.extractFrameshifts(querySequence, -1))
        .sorted(Comparator.comparingInt(Map.Entry::getKey))
        .collect(Collectors.toList());

    // Identify frameshift regions
    final var referenceIndex = new HashMap<Integer, Integer>();
    var currentIndex = 0;
    for (int i = 0; i < referenceSequence.length(); i++) {
      if (referenceSequence.charAt(i) != '-') {
        currentIndex++;
      }
      referenceIndex.put(i, currentIndex);
    }

    final var codonCount = DnaSequence.countCodons(currentIndex);
    final var frameshiftFilter = new BitSet(codonCount);

    var shiftStartCodon = 0; // track the start of the current shift
    var currentFrame = 0; // track the frame of the shift (0,1,2)

    for (final var shift : frameShiftLocations) {
      if (shiftStartCodon == 0) {
        shiftStartCodon = DnaSequence.codonIndexAt(referenceIndex.get(shift.getKey()) + 1);
        currentFrame = shift.getValue();
      } else {
        currentFrame = (currentFrame + shift.getValue()) % 3;
        if (currentFrame == 0) {
          // End of region
          frameshiftFilter.set(
              shiftStartCodon,
              DnaSequence.codonIndexAt(referenceIndex.get(shift.getKey() + (0 < shift.getValue() ? shift.getValue() : 0))) + 1);
          shiftStartCodon = 0;
        }
        // else still frameshifted, carry on.
      }
    }

    if (shiftStartCodon != 0) {
      frameshiftFilter.set(DnaSequence.codonIndexAt(referenceIndex.get(shiftStartCodon) + 1), codonCount + 1);
    }

    return frameshiftFilter;
  }

  private Stream<Map.Entry<Integer, Integer>> extractFrameshifts(final String alignedSequence, final int direction) {

    return new Scanner(alignedSequence).findAll(indelPattern)
        .filter(indel -> (indel.end() - indel.start()) % 3 != 0)
        .map(indel -> new ImmutablePair<>(indel.start(), (indel.end() - indel.start()) * direction));
  }
}
