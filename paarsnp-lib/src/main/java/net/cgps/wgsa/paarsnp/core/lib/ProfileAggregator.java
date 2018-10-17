package net.cgps.wgsa.paarsnp.core.lib;

import net.cgps.wgsa.paarsnp.core.lib.json.AntimicrobialAgent;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ProfileAggregator {

  private final Map<String, ResistanceState> profileMap;

  private ProfileAggregator(final Map<String, ResistanceState> profileMap) {
    this.profileMap = profileMap;
  }

  public void addPhenotype(final String agent, final ResistanceState resistanceState) {
    if (resistanceState.getRank() < this.profileMap.get(agent).getRank()) {
      this.profileMap.put(agent, resistanceState);
    }
  }

  public Map<String, ResistanceState> getProfileMap() {
    return this.profileMap;
  }

  public static ProfileAggregator initialise(final Map<String, AntimicrobialAgent> agents) {
    return new ProfileAggregator(agents.keySet().stream().collect(Collectors.toMap(Function.identity(), (agent) -> ResistanceState.NOT_FOUND)));
  }
}
