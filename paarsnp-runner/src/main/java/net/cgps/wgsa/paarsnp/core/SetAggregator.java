package net.cgps.wgsa.paarsnp.core;

import net.cgps.wgsa.paarsnp.core.lib.ResistanceSet;
import net.cgps.wgsa.paarsnp.core.snpar.ProcessSnparMatchData;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Creates a single unique list of complete & partial sets, as well as identified elements.
 */
public class SetAggregator implements Function<Collection<ProcessSnparMatchData.ProcessedSets>, ProcessSnparMatchData.ProcessedSets> {

  @Override
  public ProcessSnparMatchData.ProcessedSets apply(final Collection<ProcessSnparMatchData.ProcessedSets> processedSets) {

    final Set<String> seenIds = new HashSet<>(100);
    final Collection<ResistanceSet> completeSets = new HashSet<>(20);
    final Collection<ResistanceSet> unfPartialSets = new HashSet<>(20);

    processedSets
        .forEach(set -> {
          seenIds.addAll(set.getSeenIds());
          completeSets.addAll(set.getCompleteSets());
          unfPartialSets.addAll(set.getPartialSets());
        });

    // Any partial sets also in complete sets need filtering out.
    return new ProcessSnparMatchData.ProcessedSets(completeSets,
                                                   unfPartialSets.stream().filter(completeSets::contains).collect(Collectors.toList()),
                                                   seenIds
    );
  }
}
