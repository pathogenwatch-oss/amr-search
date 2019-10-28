package net.cgps.wgsa.paarsnp.core.snpar.codonmapping;

import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.BitSet;
import java.util.Comparator;
import java.util.Map;
import java.util.Scanner;
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
    final var frameshiftFilter = new BitSet(querySequence.length());
    var shiftStart = 0;
    var currentFrame = 0;

    for (final var shift : frameShiftLocations) {
      if (shiftStart == 0) {
        shiftStart = shift.getKey();
        currentFrame = shift.getValue();
      } else {
        currentFrame = (currentFrame + shift.getValue()) % 3;
        if (currentFrame == 0) {
          // End of region
          frameshiftFilter.set(shiftStart, shift.getKey() + Math.abs(shift.getValue()));
          shiftStart = 0;
        }
        // else still frameshifted, carry on.
      }
    }

    if (shiftStart != 0) {
      frameshiftFilter.set(shiftStart, querySequence.length());
    }

    return frameshiftFilter;
  }

  private Stream<Map.Entry<Integer, Integer>> extractFrameshifts(final String alignedSequence, final int direction) {

    return new Scanner(alignedSequence).findAll(indelPattern)
        .filter(indel -> (indel.end() - indel.start()) % 3 != 0)
        .map(indel -> new ImmutablePair<>(indel.start(), (indel.end() - indel.start()) * direction));
  }
}
