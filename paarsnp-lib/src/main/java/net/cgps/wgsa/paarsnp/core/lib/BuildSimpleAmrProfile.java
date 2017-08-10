package net.cgps.wgsa.paarsnp.core.lib;

import net.cgps.wgsa.paarsnp.core.lib.json.ResistanceSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BuildSimpleAmrProfile implements Function<Stream<ResistanceSet>, Map<String, Map<String, ResistanceState>>> {

  private final Logger logger = LoggerFactory.getLogger(BuildSimpleAmrProfile.class);
  private final SortAmrProfile amrSorter;
  private final Collection<String> referenceProfile;

  public BuildSimpleAmrProfile(final Collection<String> referenceProfile) {

    this.referenceProfile = referenceProfile;
    this.amrSorter = new SortAmrProfile();
  }

  @Override
  public Map<String, Map<String, ResistanceState>> apply(final Stream<ResistanceSet> resistanceSetStream) {

    return this.amrSorter.apply(resistanceSetStream.map(ResistanceSet::getAgents)
        .peek(antimicrobialAgents -> this.logger.debug("Adding {}", antimicrobialAgents.stream().collect(Collectors.joining(","))))
        .flatMap(Set::stream)
        .distinct()
        .collect(Collectors.toList()), this.referenceProfile);
  }
}
