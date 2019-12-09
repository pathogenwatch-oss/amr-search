package net.cgps.wgsa.paarsnp.core.models.variants.implementations;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import net.cgps.wgsa.paarsnp.core.lib.AbstractJsonnable;
import net.cgps.wgsa.paarsnp.core.lib.blast.Mutation;
import net.cgps.wgsa.paarsnp.core.lib.utils.DnaSequence;
import net.cgps.wgsa.paarsnp.core.models.Location;
import net.cgps.wgsa.paarsnp.core.models.ResistanceMutationMatch;
import net.cgps.wgsa.paarsnp.core.models.variants.Variant;
import net.cgps.wgsa.paarsnp.core.snpar.AaAlignment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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
  public Optional<ResistanceMutationMatch> match(final Map<Integer, Collection<Mutation>> mutations, final AaAlignment aaAlignment) {
    return this.isPresent(aaAlignment) ?
           Optional.of(this.buildMatch(aaAlignment)) :
           Optional.empty();
  }

  public boolean isPresent(final AaAlignment aaAlignment) {

    final Collection<Integer> prematureStops = aaAlignment
        .getTranslation()
        .filter(position -> '*' == position.getValue())
        .peek(position -> this.logger.debug("Premature stop found at {} aa in {} nt", position.getKey(), this.expectedStop))
        // NB the position is aa, while the stop is nt.
        .filter(position -> position.getKey() * 3 < this.expectedStop)
        .map(Map.Entry::getKey)
        .collect(Collectors.toList());

    return !prematureStops.isEmpty();
  }

  @Override
  public boolean isWithinBoundaries(final int start, final int stop) {
    return true;
  }

  public ResistanceMutationMatch buildMatch(final AaAlignment aaAlignment) {
    final Collection<Integer> prematureStops = aaAlignment
        .getTranslation()
        .filter(position -> '*' == position.getValue())
        // NB the position is aa, while the stop is nt.
        .filter(position -> position.getKey() * 3 < this.expectedStop)
        .map(Map.Entry::getKey)
        .collect(Collectors.toList());

    return new ResistanceMutationMatch(
        this,
        aaAlignment
            .getTranslation()
            .filter(position -> '*' == position.getValue())
            // NB the position is aa, while the stop is nt.
            .filter(position -> position.getKey() * 3 < this.expectedStop)
            .map(Map.Entry::getKey)
            .map(stopLocation -> new Location(aaAlignment.getQueryNtLocation(stopLocation), DnaSequence.ntIndexFromCodon(stopLocation)))
            .collect(Collectors.toList())
    );
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    final PrematureStop that = (PrematureStop) o;
    return Objects.equals(expectedStop, that.expectedStop);
  }

  @Override
  public int hashCode() {
    return Objects.hash(expectedStop, name);
  }
}
