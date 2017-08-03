package net.cgps.wgsa.paarsnp.core.snpar;

import net.cgps.wgsa.paarsnp.core.lib.ResistanceSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Works out the partial and complete resistance sets present.
 */
public class ProcessSnparMatchData implements Function<SnparMatchData, ProcessSnparMatchData.ProcessedSets> {

  private final Logger logger = LoggerFactory.getLogger(ProcessSnparMatchData.class);
  private final Collection<ResistanceSet> resistanceSets;

  public ProcessSnparMatchData(final Collection<ResistanceSet> resistanceSets) {

    this.resistanceSets = resistanceSets;
  }

  @Override
  public ProcessedSets apply(final SnparMatchData snparMatchData) {

    final Set<String> seenIds = snparMatchData.getSnpResistanceElements()
                                              .stream()
                                              .map(SnpResistanceElement::getResistanceMutation)
                                              .map(ResistanceMutation::getName)
                                              .collect(Collectors.toSet());

    final Collection<ResistanceSet> completeSets = new HashSet<>(20);
    final Collection<ResistanceSet> partialSets = new HashSet<>(20);

    this.resistanceSets
        .forEach(resistanceSet -> {
          final long seenElementCount = resistanceSet.getElementIds()
                                                     .stream()
                                                     .filter(seenIds::contains)
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

  public static class ProcessedSets {

    private final Collection<ResistanceSet> completeSets;
    private final Collection<ResistanceSet> partialSets;
    private final Set<String> seenIds;

    public ProcessedSets(final Collection<ResistanceSet> completeSets, final Collection<ResistanceSet> partialSets, final Set<String> seenIds) {

      this.completeSets = completeSets;
      this.partialSets = partialSets;
      this.seenIds = seenIds;
    }

    public Collection<ResistanceSet> getCompleteSets() {

      return this.completeSets;
    }

    public Collection<ResistanceSet> getPartialSets() {

      return this.partialSets;
    }

    public Set<String> getSeenIds() {

      return this.seenIds;
    }
  }
}
