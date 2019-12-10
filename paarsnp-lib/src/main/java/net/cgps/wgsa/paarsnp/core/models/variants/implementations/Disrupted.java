package net.cgps.wgsa.paarsnp.core.models.variants.implementations;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import net.cgps.wgsa.paarsnp.core.lib.AbstractJsonnable;
import net.cgps.wgsa.paarsnp.core.lib.blast.Mutation;
import net.cgps.wgsa.paarsnp.core.models.Location;
import net.cgps.wgsa.paarsnp.core.models.variants.Variant;
import net.cgps.wgsa.paarsnp.core.snpar.AaAlignment;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@JsonDeserialize(as = Disrupted.class)
public class Disrupted extends AbstractJsonnable implements Variant {

  private final String name = "disrupted";
  private final int expectedStop;
  private final Frameshift frameshift;
  private final PrematureStop prematureStop;

  public Disrupted() {
    this(0);
  }

  public Disrupted(final int expectedStop) {
    this.expectedStop = expectedStop;
    this.frameshift = new Frameshift();
    this.prematureStop = new PrematureStop(expectedStop);
  }

  public String getName() {
    return this.name;
  }

  public int getExpectedStop() {
    return expectedStop;
  }

  private Frameshift getFrameshift() {
    return this.frameshift;
  }

  private PrematureStop getPrematureStop() {
    return this.prematureStop;
  }

  @Override
  public boolean isWithinBoundaries(final int start, final int stop) {
    return true;
  }

  @Override
  public Optional<Collection<Location>> match(final Map<Integer, Collection<Mutation>> mutations, final AaAlignment aaAlignment) {
    return this.frameshift.match(mutations, aaAlignment)
        .or(() -> this.prematureStop.match(mutations, aaAlignment));
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    final Disrupted disrupted = (Disrupted) o;
    return Objects.equals(frameshift, disrupted.frameshift) && Objects.equals(prematureStop, disrupted.prematureStop);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, frameshift, prematureStop);
  }
}
