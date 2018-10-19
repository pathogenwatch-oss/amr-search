package net.cgps.wgsa.paarsnp.core.snpar;

import net.cgps.wgsa.paarsnp.core.lib.json.ResistanceSet;
import net.cgps.wgsa.paarsnp.core.snpar.json.ResistanceMutation;
import net.cgps.wgsa.paarsnp.core.snpar.json.ResistanceMutationMatch;
import net.cgps.wgsa.paarsnp.core.snpar.json.SnparMatchData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Works out the partial and complete resistance sets present.
 */
public class ProcessSnparMatchData implements Function<SnparMatchData, ProcessSnparMatchData.ProcessedSets> {

  private final Logger logger = LoggerFactory.getLogger(ProcessSnparMatchData.class);
  private final Collection<ResistanceSet> resistanceSets;

  ProcessSnparMatchData(final Collection<ResistanceSet> resistanceSets) {

    this.resistanceSets = resistanceSets;
  }

  @Override
  public ProcessedSets apply(final SnparMatchData snparMatchData) {

    final Set<String> seenIds = snparMatchData.getSnpResistanceElements()
        .stream()
        .map(ResistanceMutationMatch::getResistanceMutation)
        .map(ResistanceMutation::getName)
        .collect(Collectors.toSet());

    final Collection<ResistanceSet> completeSets = new HashSet<>(20);
    final Collection<ResistanceSet> partialSets = new HashSet<>(20);

    this.resistanceSets
        .forEach(resistanceSet -> {
          final long seenElementCount = resistanceSet.getElementIds()
              .stream()
              .peek(id -> this.logger.debug("Checking {}", id))
              .filter(seenIds::contains)
              .peek(id -> this.logger.debug("Kept {}", id))
              .count();
          this.logger.debug("Seen {} out of {} elements that belong to {}", seenElementCount, resistanceSet.getElementIds().size(), resistanceSet.getResistanceSetName());

          if (seenElementCount == 0) {
            this.logger.debug("No resistance mutations seen.");
            // nothing to do.
          } else if (resistanceSet.getElementIds().size() == seenElementCount) {
            completeSets.add(resistanceSet);
          } else {
            partialSets.add(resistanceSet);
          }
        });

    return new ProcessedSets(completeSets, partialSets, seenIds);
  }

  static class ProcessedSets {

    private final Collection<ResistanceSet> completeSets;
    private final Collection<ResistanceSet> partialSets;
    private final Set<String> seenIds;

    ProcessedSets(final Collection<ResistanceSet> completeSets, final Collection<ResistanceSet> partialSets, final Set<String> seenIds) {

      this.completeSets = completeSets;
      this.partialSets = partialSets;
      this.seenIds = seenIds;
    }

    ProcessedSets() {
      this(new HashSet<>(20), new HashSet<>(20), new HashSet<>(20));
    }

    void merge(final ProcessedSets... processedSets) {
      Stream.of(processedSets)
          .forEach(set -> {
            this.completeSets.addAll(set.completeSets);
            this.partialSets.addAll(set.partialSets);
            this.seenIds.addAll(set.seenIds);
          });
    }

    Collection<ResistanceSet> getCompleteSets() {

      return this.completeSets;
    }

    Collection<ResistanceSet> getPartialSets() {

      return this.partialSets;
    }

    Set<String> getSeenIds() {

      return this.seenIds;
    }
  }
}
