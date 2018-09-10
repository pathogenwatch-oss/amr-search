package net.cgps.wgsa.paarsnp.core.lib;

import java.util.Map;

public class ProfileAggregator {

  private final Map<String, ResistanceState> profileMap;

  public ProfileAggregator(final Map<String, ResistanceState> profileMap) {
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
}
