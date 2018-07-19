package net.cgps.wgsa.paarsnp.core.lib;

import net.cgps.wgsa.paarsnp.core.lib.blast.BlastMatch;
import net.cgps.wgsa.paarsnp.core.lib.blast.BlastSearchStatistics;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class OverlapRemover<T extends BlastMatch> implements Collector<T, List<T>, Collection<T>> {

  private final Comparator<BlastSearchStatistics> statComparer = Comparator
      // Highest PID first
      .comparingDouble(BlastSearchStatistics::getPercentIdentity)
      .reversed()
      .thenComparing(BlastSearchStatistics::getLibrarySequenceId)
      .thenComparing(BlastSearchStatistics::getQuerySequenceId)
      .thenComparingDouble(BlastSearchStatistics::getQuerySequenceStart);

  private final Comparator<BlastMatch> matchComparator = (o1, o2) -> statComparer.compare(o1.getBlastSearchStatistics(), o2.getBlastSearchStatistics());

  private final TestForOverlap testOverlap;

  public OverlapRemover(final int allowedOverlap) {

    this.testOverlap = new TestForOverlap(allowedOverlap);
  }

  @Override
  public Supplier<List<T>> supplier() {
    return ArrayList::new;
  }

  @Override
  public BiConsumer<List<T>, T> accumulator() {
    return Collection::add;
  }

  @Override
  public BinaryOperator<List<T>> combiner() {
    return (a, b) -> {
      a.addAll(b);
      return b;
    };
  }

  @Override
  public Function<List<T>, Collection<T>> finisher() {
    return (matchList) -> {
      // Complete matches checked against themselves for overlaps
      if (matchList.isEmpty()) {
        return matchList;
      }

      matchList.sort(matchComparator);

      final List<T> filtered = new ArrayList<>(matchList.size());
      filtered.add(matchList.get(0)); // Add the best hit.

      // Remove overlaps, keeping best first and removing subsequent overlapping matches.
      for (int i = 1; i < matchList.size(); i++) {
        boolean keep = true;
        for (final BlastMatch alreadySelected : filtered) {
          if (this.testOverlap.test(alreadySelected, matchList.get(i))) {
            keep = false;
            break;
          }
        }
        if (keep) {
          filtered.add(matchList.get(i));
        }
      }

      return filtered;
    };
  }

  @Override
  public Set<Characteristics> characteristics() {
    return Collections.singleton(Characteristics.UNORDERED);
  }
}
