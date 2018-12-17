package net.cgps.wgsa.paarsnp.core.models.variants.implementations;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import net.cgps.wgsa.paarsnp.core.lib.AbstractJsonnable;
import net.cgps.wgsa.paarsnp.core.lib.blast.Mutation;
import net.cgps.wgsa.paarsnp.core.models.ResistanceMutationMatch;
import net.cgps.wgsa.paarsnp.core.models.variants.Variant;
import net.cgps.wgsa.paarsnp.core.snpar.CodonMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@JsonDeserialize(as = PrematureStop.class)
public class PrematureStop extends AbstractJsonnable implements Variant {

  private final Integer expectedStop;
  private final String name = "truncated";
  @JsonIgnore
  private final Logger logger = LoggerFactory.getLogger(PrematureStop.class);

  @SuppressWarnings("unused")
  private PrematureStop() {
    this(1000000);
  }

  public PrematureStop(final Integer expectedStop) {
    this.expectedStop = expectedStop;
  }

  @SuppressWarnings("unused")
  public Integer getExpectedStop() {
    return this.expectedStop;
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public boolean isPresent(final Map<Integer, Collection<Mutation>> mutations, final CodonMap codonMap) {

    final Collection<Integer> prematureStops = codonMap
        .getTranslation()
        .filter(position -> '*' == position.getValue())
        // NB the position is aa, while the stop is nt.
        .filter(position -> position.getKey() * 3 < this.expectedStop)
        .map(Map.Entry::getKey)
        .collect(Collectors.toList());

    return !prematureStops.isEmpty();
  }

  @Override
  public ResistanceMutationMatch buildMatch(final Map<Integer, Collection<Mutation>> mutations, final CodonMap codonMap) {
    final Collection<Integer> prematureStops = codonMap
        .getTranslation()
        .filter(position -> '*' == position.getValue())
        // NB the position is aa, while the stop is nt.
        .filter(position -> position.getKey() * 3 < this.expectedStop)
        .map(Map.Entry::getKey)
        .collect(Collectors.toList());

    return new ResistanceMutationMatch(
        this,
        prematureStops
            .stream()
            .map(aaPosition -> (aaPosition * 3) - 2)
            .map(codonStart -> Arrays.asList(codonStart, codonStart + 1, codonStart + 2))
            .flatMap(Collection::stream)
            .peek(position -> this.logger.debug("{}", position))
            .filter(mutations::containsKey)
            .map(mutations::get)
            .flatMap(Collection::stream)
            .collect(Collectors.toList())
    );
  }

  @Override
  public boolean isWithinBoundaries(final int start, final int stop) {
    return true;
  }


  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || this.getClass() != o.getClass()) return false;
    final PrematureStop that = (PrematureStop) o;
    return Objects.equals(this.name, that.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.name);
  }
}
