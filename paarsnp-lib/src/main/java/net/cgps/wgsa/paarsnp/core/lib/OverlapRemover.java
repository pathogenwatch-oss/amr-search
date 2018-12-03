package net.cgps.wgsa.paarsnp.core.lib;

import net.cgps.wgsa.paarsnp.core.lib.blast.BlastMatch;
import net.cgps.wgsa.paarsnp.core.lib.blast.BlastSearchStatistics;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collector;

public class OverlapRemover<T extends BlastMatch> implements Collector<T, List<T>, Collection<T>> {

  private final Comparator<BlastSearchStatistics> statComparer = Comparator
      // Highest PID first
      .comparingDouble(BlastSearchStatistics::getPercentIdentity)
      .reversed()
      .thenComparing(BlastSearchStatistics::getLibrarySequenceId)
      .thenComparing(BlastSearchStatistics::getQuerySequenceId)
      .thenComparingDouble(BlastSearchStatistics::getQuerySequenceStart);

  private final Comparator<BlastMatch> matchComparator = (o1, o2) -> this.statComparer.compare(o1.getBlastSearchStatistics(), o2.getBlastSearchStatistics());

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
      return a;
    };
  }

  @Override
  public Function<List<T>, Collection<T>> finisher() {
    // Complete matches checked against themselves for overlaps
    return (matchList) -> {

      if (matchList.isEmpty()) {
        return matchList;
      }

      // Remove overlaps, keeping best first and removing subsequent overlapping matches.
      matchList.sort(this.matchComparator);

      final List<T> filtered = new ArrayList<>(matchList.size());
      filtered.add(matchList.get(0)); // Add the best hit.

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

  public static class TestForOverlap implements BiPredicate<BlastMatch, BlastMatch> {


    private final int overlapThreshold;

    @Override
    public boolean test(final BlastMatch match1, final BlastMatch match2) {

      if (!match1.getBlastSearchStatistics().getQuerySequenceId().equals(match2.getBlastSearchStatistics().getQuerySequenceId())) {
        return false;
      }

      // The query coordinates are never reversed.
      final int queryStart1 = match1.getBlastSearchStatistics().getQuerySequenceStart();
      final int queryStop1 = match1.getBlastSearchStatistics().getQuerySequenceStop();

      final int queryStart2 = match2.getBlastSearchStatistics().getQuerySequenceStart();
      final int queryStop2 = match2.getBlastSearchStatistics().getQuerySequenceStop();

      return overlapCheck(queryStart1, queryStop1, queryStart2, queryStop2, this.overlapThreshold);
    }

    TestForOverlap(final int overlapThreshold) {

      this.overlapThreshold = overlapThreshold;
    }

    public static boolean overlapCheck(int queryStart1, int queryStop1, int queryStart2, int queryStop2, int overlapThreshold) {
      return queryStop2 >= queryStart1 + overlapThreshold && queryStop1 >= queryStart2 + overlapThreshold;

    }
  }
}
