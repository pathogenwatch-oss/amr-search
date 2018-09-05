package net.cgps.wgsa.paarsnp;

import net.cgps.wgsa.paarsnp.core.lib.ElementEffect;
import net.cgps.wgsa.paarsnp.core.lib.ResistanceState;
import net.cgps.wgsa.paarsnp.core.lib.PhenotypeEffect;
import net.cgps.wgsa.paarsnp.core.lib.json.AntibioticProfile;
import net.cgps.wgsa.paarsnp.core.lib.json.AntimicrobialAgent;
import net.cgps.wgsa.paarsnp.core.paar.PaarResult;
import net.cgps.wgsa.paarsnp.core.snpar.json.SnparResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BuildPaarsnpResult implements Function<BuildPaarsnpResult.PaarsnpResultData, PaarsnpResult> {

  private final Logger logger = LoggerFactory.getLogger(BuildPaarsnpResult.class);

  private final Map<String, AntimicrobialAgent> agents;

  public BuildPaarsnpResult(Map<String, AntimicrobialAgent> agents) {
    this.agents = agents;
  }

  @Override
  public PaarsnpResult apply(final PaarsnpResultData paarsnpResultData) {

    this.logger.debug("Building net.cgps.wgsa.paarsnp result from match data for assemblyId={}", paarsnpResultData.assemblyId);

    // Now build the profiles.
    final Map<String, AntibioticProfile> antibioticProfiles = new LinkedHashMap<>(50);

    // Start with resolving the complete resistance sets.
    Stream.concat(paarsnpResultData.snparResult.getCompleteSets()
            .stream(),
        paarsnpResultData.paarResult.getCompleteResistanceSets()
            .stream()
    )
        .forEach(resistanceSet -> resistanceSet.getAgents()
            .forEach(
                agent -> {

                  // For each set go through each agent it encodes resistance for.
                  if (!antibioticProfiles.containsKey(agent)) {
                    // New antibiotic.

                    // Check if it's a modified set (Sets that confer resistance but contain a modifier element)
                    if (paarsnpResultData.paarResult.getModifiedSets().containsKey(resistanceSet.getName())) {

                      // This isn't complete as it does't handle all possibilities, with the resistance set effects, but currently we don't have any modifiers in intermediate effect sets.

                      final ResistanceState rs;
                      final ElementEffect modification = paarsnpResultData.paarResult.getModifiedSets().get(resistanceSet.getName());

                      if (ElementEffect.MODIFIES_INDUCED == modification) {
                        rs = ResistanceState.INDUCIBLE;
                      } else if (ElementEffect.MODIFIES_SUPPRESSES == modification){
                        rs = ResistanceState.NOT_FOUND;
                      } else {
                        rs = ResistanceState.NOT_FOUND;
                      }

                      antibioticProfiles.put(agent, new AntibioticProfile(agents.get(agent), rs, new ArrayList<>(10)));

                    } else {

                      final ResistanceState rs;

                      if (PhenotypeEffect.RESISTANT == resistanceSet.getEffect()) {
                        rs = ResistanceState.RESISTANT;
                      } else if (PhenotypeEffect.INTERMEDIATE_NOT_ADDITIVE == resistanceSet.getEffect()) {
                        rs = ResistanceState.INTERMEDIATE;
                      } else if (PhenotypeEffect.INTERMEDIATE_ADDITIVE == resistanceSet.getEffect()) {
                        rs = ResistanceState.RESISTANT;
                      } else {
                        rs = ResistanceState.INDUCIBLE;
                      }

                      antibioticProfiles.put(agent, new AntibioticProfile(agents.get(agent), rs, new ArrayList<>(10)));
                    }
                  } else if (!paarsnpResultData.paarResult.getModifiedSets().containsKey(resistanceSet.getName()) && (ResistanceState.INDUCIBLE == antibioticProfiles.get(agent).getResistanceState())) {

                    // Already contains an inducible profile, and current one is not, so convert to resistant
                    antibioticProfiles.put(agent, new AntibioticProfile(agents.get(agent), ResistanceState.RESISTANT, antibioticProfiles.get(agent).getResistanceSets()));
                  } else {

                    // If the previous assignment was intermediate and the current is resistant, then replace the profile
                    if (ResistanceState.INTERMEDIATE == antibioticProfiles.get(agent).getResistanceState()
                        &&
                        (PhenotypeEffect.RESISTANT == resistanceSet.getEffect()) || (PhenotypeEffect.INTERMEDIATE_ADDITIVE == resistanceSet.getEffect())) {
                      antibioticProfiles.put(agent, new AntibioticProfile(agents.get(agent), ResistanceState.RESISTANT, antibioticProfiles.get(agent).getResistanceSets()));

                    }
                  }
                  // finally add the set.
                  antibioticProfiles.get(agent).addSet(resistanceSet);
                })
        );

    // Now go through the partial sets and check if any effect new antibiotics.
    Stream.concat(paarsnpResultData.snparResult.getPartialSets().stream(), paarsnpResultData.paarResult.getPartialResistanceSets().stream())
        .forEach(partialSet -> partialSet.getAgents()
            .forEach(agent -> {

              if (!antibioticProfiles.containsKey(agent)) {

                // agent has not been previously annotated, so check if a partial set confers (e.g.) intermediate resistance

                final ResistanceState rs;

                if (PhenotypeEffect.INTERMEDIATE_ADDITIVE == partialSet.getEffect()) {
                  // These sets provide intermediate resistance when not complete. All other sets are sensitive.
                  rs = ResistanceState.INTERMEDIATE;
                  antibioticProfiles.put(agent, new AntibioticProfile(agents.get(agent), rs, new ArrayList<>(10)));
                  antibioticProfiles.get(agent).addSet(partialSet);
                }

              }
            })
        );

    // Sorts the profile and adds the antibiotics with no matches.
    final Collection<AntibioticProfile> sortedAntibioticProfiles = paarsnpResultData.referenceProfile.stream().map(agent -> antibioticProfiles.getOrDefault(agent, AntibioticProfile.buildDefault(agents.get(agent)))).collect(Collectors.toList());

    return new PaarsnpResult(paarsnpResultData.assemblyId, paarsnpResultData.snparResult, paarsnpResultData.paarResult, sortedAntibioticProfiles);
  }

  public static class PaarsnpResultData {

    final String assemblyId;
    final SnparResult snparResult;
    final PaarResult paarResult;
    final Collection<String> referenceProfile;

    public PaarsnpResultData(final String assemblyId, final SnparResult snparResult, final PaarResult paarResult, final Collection<String> referenceProfile) {

      this.assemblyId = assemblyId;
      this.snparResult = snparResult;
      this.paarResult = paarResult;
      this.referenceProfile = referenceProfile;
    }
  }
}
