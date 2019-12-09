package net.cgps.wgsa.paarsnp.core.models.variants.implementations;

import net.cgps.wgsa.paarsnp.core.lib.AbstractJsonnable;
import net.cgps.wgsa.paarsnp.core.lib.blast.Mutation;
import net.cgps.wgsa.paarsnp.core.models.ResistanceMutationMatch;
import net.cgps.wgsa.paarsnp.core.models.variants.Variant;
import net.cgps.wgsa.paarsnp.core.snpar.AaAlignment;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public class Disrupted extends AbstractJsonnable implements Variant {

  private final String name = "disrupted";
  private final Frameshift frameshift;
  private final PrematureStop prematureStop;

  public Disrupted() {
    this(0);
  }


  public Disrupted(final int expectedStop) {
    this.frameshift = new Frameshift();
    this.prematureStop = new PrematureStop(expectedStop);
  }

  public String getName() {
    return this.name;
  }

  @Override
  public boolean isWithinBoundaries(final int start, final int stop) {
    return true;
  }

  @Override
  public Optional<ResistanceMutationMatch> match(final Map<Integer, Collection<Mutation>> mutations, final AaAlignment aaAlignment) {
    return this.frameshift.isPresent(mutations) ?
           Optional.of(this.frameshift.buildMatch(mutations)) :
           (this.prematureStop.isPresent(aaAlignment) ?
            Optional.of(this.prematureStop.buildMatch(aaAlignment)) :
            Optional.empty());
  }
}
