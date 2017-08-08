package net.cgps.wgsa.paarsnp.core;

import net.cgps.wgsa.paarsnp.PaarsnpResult;
import net.cgps.wgsa.paarsnp.core.lib.AntibioticProfile;
import net.cgps.wgsa.paarsnp.core.lib.AntimicrobialAgent;
import net.cgps.wgsa.paarsnp.core.lib.ResistanceState;
import net.cgps.wgsa.paarsnp.core.lib.ResistanceType;
import net.cgps.wgsa.paarsnp.core.paar.PaarResult;
import net.cgps.wgsa.paarsnp.core.paar.ResistanceGene;
import net.cgps.wgsa.paarsnp.core.snpar.SnparResult;
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

  @Override
  public PaarsnpResult apply(final PaarsnpResultData paarsnpResultData) {

    this.logger.debug("Building net.cgps.wgsa.paarsnp result from match data for assemblyId={}", paarsnpResultData.assemblyId);

    // Build the old-style resistance profile.
    final Map<String, Map<String, ResistanceState>> sortedResistanceProfile = new BuildSimpleAmrProfile(paarsnpResultData.referenceProfile).apply(Stream.concat(paarsnpResultData.snparResult.getCompleteSets().stream(), paarsnpResultData.paarResult.getCompleteResistanceSets().stream()));

    // Now build the new profiles.
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
                                                       if (!antibioticProfiles.containsKey(agent.getName())) {
                                                         // New antibiotic.

                                                         // Check if it's a modifier set (Sets that confer resistance but contain a modifier element)
                                                         if (paarsnpResultData.paarResult.getModifiedSets().containsKey(resistanceSet.getResistanceSetName())) {

                                                           // This isn't complete as it does't handle all possibilities, with the resistance set effects, but currently we don't have any modifiers in intermediate effect sets.

                                                           final ResistanceState rs;
                                                           final ResistanceGene.EFFECT modification = paarsnpResultData.paarResult.getModifiedSets().get(resistanceSet.getResistanceSetName());

                                                           if (ResistanceGene.EFFECT.MODIFIES_INDUCED == modification) {
                                                             rs = ResistanceState.INDUCIBLE;
                                                           } else if (ResistanceGene.EFFECT.MODIFIES_RESISTANT == modification) {
                                                             rs = ResistanceState.RESISTANT;
                                                           } else {
                                                             rs = ResistanceState.UNKNOWN;
                                                           }

                                                           antibioticProfiles.put(agent.getName(), new AntibioticProfile(agent.getName(), rs, new ArrayList<>(10)));

                                                         } else {

                                                           final ResistanceState rs;

                                                           if (ResistanceType.RESISTANT == resistanceSet.getEffect()) {
                                                             rs = ResistanceState.RESISTANT;
                                                           } else if (ResistanceType.INTERMEDIATE_NOT_ADDITIVE == resistanceSet.getEffect()) {
                                                             rs = ResistanceState.INTERMEDIATE;
                                                           } else if (ResistanceType.INTERMEDIATE_ADDITIVE == resistanceSet.getEffect()) {
                                                             rs = ResistanceState.RESISTANT;
                                                           } else {
                                                             rs = ResistanceState.INDUCIBLE;
                                                           }

                                                           antibioticProfiles.put(agent.getName(), new AntibioticProfile(agent.getName(), rs, new ArrayList<>(10)));
                                                         }
                                                       } else if (!paarsnpResultData.paarResult.getModifiedSets().containsKey(resistanceSet.getResistanceSetName()) && (ResistanceState.INDUCIBLE == antibioticProfiles.get(agent.getName()).getResistanceState())) {

                                                         // Already contains an inducible profile, and current one is not, so convert to resistant
                                                         antibioticProfiles.put(agent.getName(), new AntibioticProfile(agent.getName(), ResistanceState.RESISTANT, antibioticProfiles.get(agent.getName()).getResistanceSets()));
                                                       } else {

                                                         // If the previous assignment was intermediate and the current is resistant, then replace the profile
                                                         if (ResistanceState.INTERMEDIATE == antibioticProfiles.get(agent.getName()).getResistanceState()
                                                             &&
                                                             (ResistanceType.RESISTANT == resistanceSet.getEffect()) || (ResistanceType.INTERMEDIATE_ADDITIVE == resistanceSet.getEffect())) {
                                                           antibioticProfiles.put(agent.getName(), new AntibioticProfile(agent.getName(), ResistanceState.RESISTANT, antibioticProfiles.get(agent.getName()).getResistanceSets()));

                                                         }

                                                       }

                                                       // finally add the set.
                                                       antibioticProfiles.get(agent.getName()).addSet(resistanceSet);
                                                     })
                  );

    // Now go through the partial sets and check if any effect new antibiotics.
    Stream.concat(paarsnpResultData.snparResult.getPartialSets().stream(), paarsnpResultData.paarResult.getPartialResistanceSets().stream())
          .forEach(partialSet -> partialSet.getAgents()
                                           .forEach(agent -> {

                                             if (!antibioticProfiles.containsKey(agent.getName())) {

                                               // agent has not been previously annotated, so check if a partial set confers (e.g.) intermediate resistance

                                               final ResistanceState rs;

                                               if (ResistanceType.INTERMEDIATE_ADDITIVE == partialSet.getEffect()) {
                                                 // These sets provide intermediate resistance when not complete. All other sets are sensitive.
                                                 rs = ResistanceState.INTERMEDIATE;
                                                 antibioticProfiles.put(agent.getName(), new AntibioticProfile(agent.getName(), rs, new ArrayList<>(10)));
                                                 antibioticProfiles.get(agent.getName()).addSet(partialSet);
                                               }

                                             }
                                           })
                  );


    // Sorts the profile and adds the antibiotics with no matches.
    final Collection<AntibioticProfile> sortedAntibioticProfiles = paarsnpResultData.referenceProfile.stream().map(agentId -> antibioticProfiles.getOrDefault(agentId.getName(), AntibioticProfile.buildDefault(agentId.getName()))).collect(Collectors.toList());

    return new PaarsnpResult(paarsnpResultData.assemblyId, paarsnpResultData.snparResult, paarsnpResultData.paarResult, sortedResistanceProfile, sortedAntibioticProfiles);
  }

  public static class PaarsnpResultData {

    final String assemblyId;
    final String speciesId;
    final SnparResult snparResult;
    final PaarResult paarResult;
    final Collection<AntimicrobialAgent> referenceProfile;

    public PaarsnpResultData(final String assemblyId, final String speciesId, final SnparResult snparResult, final PaarResult paarResult, final Collection<AntimicrobialAgent> referenceProfile) {

      this.assemblyId = assemblyId;
      this.speciesId = speciesId;
      this.snparResult = snparResult;
      this.paarResult = paarResult;
      this.referenceProfile = referenceProfile;
    }
  }
}
