package net.cgps.wgsa.paarsnp.core.models.variants.implementations;

import net.cgps.wgsa.paarsnp.core.lib.blast.Mutation;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FrameshiftTest {

  @Test
  public void isPresent() {
    final var mutation1 = new Mutation(Mutation.MutationType.D, 4, "-", "T", 4);
    final var mutation2 = new Mutation(Mutation.MutationType.I, 11, "AAA", "---", 12);

    final Map<Integer, Collection<Mutation>> mutations = new HashMap<>();
    mutations.put(mutation1.getReferenceLocation(), Collections.singletonList(mutation1));
    mutations.put(mutation2.getReferenceLocation(), Collections.singletonList(mutation2));

    assertTrue(new Frameshift().isPresent(mutations));
  }

  @Test
  public void isNotPresent() {
    var mutationList = Arrays.asList(
        new Mutation(Mutation.MutationType.D, 4, "-", "T", 4),
        new Mutation(Mutation.MutationType.D, 4, "-", "T", 5),
        new Mutation(Mutation.MutationType.D, 4, "-", "T", 6),
        new Mutation(Mutation.MutationType.I, 11, "AAA", "---", 12));

    final Map<Integer, Collection<Mutation>> mutations = mutationList.stream()
        .collect(Collectors.toMap(Mutation::getReferenceLocation, Collections::singletonList));

    assertFalse(new Frameshift().isPresent(mutations));
  }

}