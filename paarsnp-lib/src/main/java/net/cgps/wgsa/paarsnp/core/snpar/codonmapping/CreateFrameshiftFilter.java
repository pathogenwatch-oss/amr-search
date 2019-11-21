package net.cgps.wgsa.paarsnp.core.snpar.codonmapping;

import net.cgps.wgsa.paarsnp.core.lib.blast.Mutation;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CreateFrameshiftFilter implements Function<Collection<Mutation>, FrameshiftFilter> {

  private final int sequenceLength;

  public CreateFrameshiftFilter(final int sequenceLength) {
    this.sequenceLength = sequenceLength;
  }

  @Override
  public FrameshiftFilter apply(final Collection<Mutation> mutations) {

    if (mutations.isEmpty()) {
      return new FrameshiftFilter(new BitSet(this.sequenceLength));
    }

    final var partCleanedFrameshifts = mutations
        .stream()
        .filter(Mutation::isIndel)
        .filter(mutation -> mutation.getMutationSequence().length() != 3)
        .collect(Collectors.toList());

    final var frameshifts = this.cleanDeletions(partCleanedFrameshifts);

    var frameshiftFilter = new BitSet(this.sequenceLength);
    var offset = 0;
    var currentStart = 0;

    for (final var shift : frameshifts) {

      offset = (offset + shift.getValue()) % 3;

      if (0 == currentStart && 0 != offset) {
        // Start of frameshift
        currentStart = shift.getKey();
      } else if (0 < currentStart && 0 == offset) {
        // End of frameshift
        frameshiftFilter.set(
            currentStart,
            shift.getKey() + (
                0 < shift.getValue() ?
                shift.getValue() :
                0
            ));
        currentStart = 0;
      }
    }

    if (0 < currentStart) {
      frameshiftFilter.set(currentStart, this.sequenceLength + 1);
    }

    return new FrameshiftFilter(frameshiftFilter);
  }

  private List<Map.Entry<Integer, Integer>> cleanDeletions(final List<Mutation> partCleanedFrameshifts) {
    final var possibles = new ArrayList<Integer>(10);
    final var frameshifts = new ArrayList<Map.Entry<Integer, Integer>>(20);

    for (final var mutation : partCleanedFrameshifts) {
      if (Mutation.MutationType.I == mutation.getMutationType()) {
        frameshifts.add(new ImmutablePair<>(mutation.getReferenceLocation() + 1, mutation.getMutationSequence().length() * -1));
        if (!possibles.isEmpty() && possibles.size() % 3 != 0) {
          frameshifts.add(new ImmutablePair<>(possibles.get(0), possibles.size()));
        }
        possibles.clear();
      } else {
        final var possible = mutation.getReferenceLocation();
        if (possibles.isEmpty()) {
          possibles.add(possible);
        } else {
          if (possible != possibles.get(possibles.size() - 1) + 1) {
            if (possibles.size() % 3 != 0) {
              frameshifts.add(new ImmutablePair<>(possibles.get(0), possibles.size()));
            }
            possibles.clear();
            possibles.add(possible);
          } else {
            possibles.add(possible);
          }
        }
      }
    }

    if (!possibles.isEmpty() && possibles.size() % 3 != 0) {
      frameshifts.add(new ImmutablePair<>(possibles.get(0), possibles.size()));
    }

    frameshifts.sort(Comparator.comparingInt(Map.Entry::getKey));
    return frameshifts;
  }
}
