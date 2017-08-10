package net.cgps.wgsa.paarsnp.core.lib;


import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class SortAmrProfile implements BiFunction<Collection<String>, Collection<String>, Map<String, Map<String, ResistanceState>>> {

  /**
   * Returns a collection of antibiotics ordered by the reference profile with yes/no resistance indicated (as according
   * to the resistance profile.
   *
   * @param referenceProfile  - The reference set of antibiotics in the wanted order
   * @param resistanceProfile - The set of antimicrobials that are resisted.
   * @return Each antimicrobial in the reference collection is noted as whether resisted or not and the results returned grouped by antimicrobial class.
   */
  @Override
  public Map<String, Map<String, ResistanceState>> apply(final Collection<String> referenceProfile, final Collection<String> resistanceProfile) {

    final Map<String, Map<String, ResistanceState>> orderedResistanceSet = new LinkedHashMap<>(30);

    // The reference profile defines the order & antimicrobials in the output profile.
    for (final String agent : referenceProfile) {

      if (!orderedResistanceSet.containsKey(agent)) {
        orderedResistanceSet.put(agent, new LinkedHashMap<>(5));
      }

      final ResistanceState resistanceState = resistanceProfile.contains(agent) ? ResistanceState.RESISTANT : ResistanceState.UNKNOWN;
      orderedResistanceSet.get(agent).put(agent, resistanceState);
    }

    return orderedResistanceSet;
  }
}