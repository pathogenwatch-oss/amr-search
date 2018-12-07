package net.cgps.wgsa.paarsnp.core.models.variants.implementations;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import net.cgps.wgsa.paarsnp.core.lib.blast.Mutation;
import net.cgps.wgsa.paarsnp.core.models.ResistanceMutationMatch;
import net.cgps.wgsa.paarsnp.core.models.variants.Variant;
import net.cgps.wgsa.paarsnp.core.snpar.CodonMap;

import java.util.*;
import java.util.stream.Collectors;

import static net.cgps.wgsa.paarsnp.core.lib.blast.Mutation.MutationType.D;
import static net.cgps.wgsa.paarsnp.core.lib.blast.Mutation.MutationType.I;

@JsonDeserialize(as = Frameshift.class)
public class Frameshift implements Variant {

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
  public boolean isPresent(final Map<Integer, Collection<Mutation>> mutations, final CodonMap codonMap) {

    final Map<Integer, Mutation> inserts = this.keepFrameshiftingMutations(Mutation.select(I, mutations));
    final Map<Integer, Mutation> deletions = this.keepFrameshiftingMutations(Mutation.select(D, mutations));

    return !inserts.isEmpty() || !deletions.isEmpty();
  }

  @Override
  public ResistanceMutationMatch buildMatch(final Map<Integer, Collection<Mutation>> mutations, final CodonMap resourceB) {
    final Map<Integer, Mutation> inserts = this.keepFrameshiftingMutations(Mutation.select(I, mutations));
    final Map<Integer, Mutation> deletions = this.keepFrameshiftingMutations(Mutation.select(D, mutations));
    final Collection<Mutation> causalMutations = new ArrayList<>(50);
    causalMutations.addAll(inserts.values());
    causalMutations.addAll(deletions.values());
    return new ResistanceMutationMatch(this, causalMutations);
  }

  private Map<Integer, Mutation> keepFrameshiftingMutations(final Map<Integer, Mutation> indels) {

    if (indels.isEmpty()) {
      return Collections.emptyMap();
    }

    final List<Integer> positions = indels.keySet()
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

    final Map<Integer, Mutation> filteredMap = new HashMap<>(indels);

    filter.forEach(filteredMap::remove);

    return filteredMap;
  }

  @Override
  public boolean isWithinBoundaries(final int start, final int stop) {
    return true;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || this.getClass() != o.getClass()) return false;
    return true;
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.name);
  }
}
