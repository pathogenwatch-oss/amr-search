package net.cgps.wgsa.paarsnp.core.formats;

import net.cgps.wgsa.paarsnp.core.lib.blast.BlastMatch;
import net.cgps.wgsa.paarsnp.core.snpar.CodonMap;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PrematureStop implements Variant {

  private final Integer expectedStop;

  @SuppressWarnings("unused")
  private PrematureStop() {
    this(1000000);
  }

  public PrematureStop(final Integer expectedStop) {
    this.expectedStop = expectedStop;
  }

  @Override
  public String getName() {
    return "Truncated";
  }

  @Override
  public Optional<ResistanceMutationMatch> isPresent(final Map<Integer, Collection<Mutation>> mutations, final CodonMap codonMap) {

    final Collection<Integer> prematureStops = codonMap
        .getTranslation()
        .filter(position -> '*' == position.getValue())
        .filter(position -> position.getKey() < this.expectedStop)
        .map(Map.Entry::getKey)
        .collect(Collectors.toList());

    if (!prematureStops.isEmpty()) {
      return Optional.of(new ResistanceMutationMatch(
          this,
          prematureStops
              .stream()
              .map(aaPosition -> (aaPosition * 3) - 2)
              .flatMap(codonStart -> Stream.of(codonStart, codonStart + 1, codonStart + 2))
              .map(mutations::get)
              .flatMap(Collection::stream)
              .collect(Collectors.toList())
      ));
    } else {
      return Optional.empty();
    }
  }

  @Override
  public boolean isWithinBoundaries(final BlastMatch match) {
    return true;
  }
}
