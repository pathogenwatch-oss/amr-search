package net.cgps.wgsa.paarsnp.core.models.variants.implementations;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import net.cgps.wgsa.paarsnp.core.lib.AbstractJsonnable;
import net.cgps.wgsa.paarsnp.core.lib.blast.Mutation;
import net.cgps.wgsa.paarsnp.core.models.Location;
import net.cgps.wgsa.paarsnp.core.models.ResistanceMutationMatch;
import net.cgps.wgsa.paarsnp.core.models.variants.Variant;
import net.cgps.wgsa.paarsnp.core.snpar.AaAlignment;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.cgps.wgsa.paarsnp.core.lib.blast.Mutation.MutationType.D;
import static net.cgps.wgsa.paarsnp.core.lib.blast.Mutation.MutationType.I;

@JsonDeserialize(as = Frameshift.class)
public class Frameshift extends AbstractJsonnable implements Variant {

  @SuppressWarnings("FieldCanBeLocal")
  private final String name;

  public Frameshift() {
    this.name = "frameshift";
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public boolean isPresent(final Map<Integer, Collection<Mutation>> mutations, final AaAlignment aaAlignment) {

    var deletionFrameshifts = this.selectFrameshiftingDeletions(Mutation.select(D, mutations));

    var insertionFrameshifts = mutations.values().stream()
        .flatMap(Collection::stream)
        .filter(mutation -> mutation.getMutationType() == I)
        .filter(mutation -> mutation.getMutationSequence().length() % 3 != 0)
        .collect(Collectors.toList());

    // Sum the lengths of the inserts and deletions
    return !deletionFrameshifts.isEmpty() || !insertionFrameshifts.isEmpty();
  }

  private Map<Integer, Mutation> selectFrameshiftingDeletions(final Map<Integer, Mutation> deletions) {

    if (deletions.isEmpty()) {
      return Collections.emptyMap();
    }

    final List<Integer> positions = deletions.keySet()
        .stream()
        .sorted(Comparator.comparingInt(key -> key))
        .collect(Collectors.toList());

    final List<Integer> sequenceLength = new ArrayList<>(10);

    sequenceLength.add(positions.get(0));

    final Set<Integer> filter = new HashSet<>(90);

    for (int i = 1; i < positions.size(); i++) {
      if (positions.get(i) - 1 != positions.get(i - 1)) {
        // Extend the sequence of adjacent inserts/deletions
        if (0 < sequenceLength.size()) {
          if (sequenceLength.size() % 3 == 0) {
            filter.addAll(sequenceLength);
          }
          sequenceLength.clear();
        }
      }
      sequenceLength.add(positions.get(i));
    }

    if (0 < sequenceLength.size()) {
      if (sequenceLength.size() % 3 == 0) {
        filter.addAll(sequenceLength);
        sequenceLength.clear();
      }
    }

    final Map<Integer, Mutation> filteredMap = new HashMap<>(deletions);

    filter.forEach(filteredMap::remove);

    return filteredMap;
  }

  @Override
  public ResistanceMutationMatch buildMatch(final Map<Integer, Collection<Mutation>> mutations, final AaAlignment resourceB) {

    return new ResistanceMutationMatch(
        this,
        Stream.concat(
            Mutation.select(I, mutations)
                .values()
                .stream()
                .filter(mutation -> mutation.getMutationSequence().length() % 3 != 0),
            this.selectFrameshiftingDeletions(Mutation.select(D, mutations))
                .values()
                .stream())
            .map(mutation -> new Location(mutation.getQueryLocation(), mutation.getReferenceLocation()))
            .collect(Collectors.toList()));
  }

  @Override
  public boolean isWithinBoundaries(final int start, final int stop) {
    return true;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    return o != null && this.getClass() == o.getClass();
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.name);
  }
}
