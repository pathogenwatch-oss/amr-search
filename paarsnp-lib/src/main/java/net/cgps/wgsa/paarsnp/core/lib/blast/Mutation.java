package net.cgps.wgsa.paarsnp.core.lib.blast;


import net.cgps.wgsa.paarsnp.core.lib.AbstractJsonnable;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

public class Mutation extends AbstractJsonnable {

  private final char originalSequence;
  private final int referenceLocation;
  private final char mutationSequence;
  private final MutationType mutationType;
  private final int queryLocation;

  @SuppressWarnings("unused")
  private Mutation() {

    this(MutationType.S, 0, 'a', 'a', 0);
  }

  public Mutation(final MutationType mutationType, final int queryLocation, final char mutationSequence, final char originalSequence, final int referenceLocation) {

    this.mutationType = mutationType;
    this.queryLocation = queryLocation;
    this.mutationSequence = mutationSequence;
    this.originalSequence = originalSequence;
    this.referenceLocation = referenceLocation;
  }

  public char getOriginalSequence() {

    return this.originalSequence;
  }

  public int getReferenceLocation() {

    return this.referenceLocation;
  }

  public char getMutationSequence() {

    return this.mutationSequence;
  }

  public MutationType getMutationType() {

    return this.mutationType;
  }

  public int getQueryLocation() {

    return this.queryLocation;
  }

  public static Map<Integer, Mutation> select(final MutationType type, final Map<Integer, Collection<Mutation>> mutations) {
    return mutations
        .entrySet()
        .stream()
        .flatMap(entry -> entry.getValue().stream().map(mutation -> new ImmutablePair<>(entry.getKey(), mutation)))
        .filter(mutation -> type == mutation.getRight().getMutationType())
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  public enum MutationType {

    S, I, D
  }

}
