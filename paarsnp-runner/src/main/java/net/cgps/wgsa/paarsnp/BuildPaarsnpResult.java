package net.cgps.wgsa.paarsnp;

import net.cgps.wgsa.paarsnp.core.lib.ElementEffect;
import net.cgps.wgsa.paarsnp.core.lib.PhenotypeEffect;
import net.cgps.wgsa.paarsnp.core.lib.ProfileAggregator;
import net.cgps.wgsa.paarsnp.core.lib.ResistanceState;
import net.cgps.wgsa.paarsnp.core.lib.json.*;
import net.cgps.wgsa.paarsnp.core.paar.PaarResult;
import net.cgps.wgsa.paarsnp.core.snpar.json.SnparResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BuildPaarsnpResult implements Function<BuildPaarsnpResult.PaarsnpResultData, PaarsnpResult> {

  private static final Modifier DEFAULT_MODIFIER = new Modifier("default", ElementEffect.RESISTANCE);

  private final Logger logger = LoggerFactory.getLogger(BuildPaarsnpResult.class);

  private final Map<String, AntimicrobialAgent> agents;
  private final Map<String, ResistanceSet> resistanceSetMap;

  public BuildPaarsnpResult(final Map<String, AntimicrobialAgent> agents, final Map<String, ResistanceSet> resistanceSetMap) {
    this.agents = agents;
    this.resistanceSetMap = resistanceSetMap;
  }

  @Override
  public PaarsnpResult apply(final PaarsnpResultData paarsnpResultData) {

    this.logger.debug("Building net.cgps.wgsa.paarsnp result from match data for assemblyId={}", paarsnpResultData.assemblyId);

    // Now build the profiles.
//    final Map<String, AntibioticProfile> antibioticProfiles = new LinkedHashMap<>(50);

    final ProfileAggregator profileAggregator = new ProfileAggregator(this.agents.keySet().stream().collect(Collectors.toMap(Function.identity(), (agent) -> ResistanceState.NOT_FOUND)));
    final Map<String, Collection<String>> resistanceSets = this.agents.keySet().stream().collect(Collectors.toMap(Function.identity(), (agent) -> new ArrayList<>()));

    final Predicate<Modifier> paarModSelector = modifier -> paarsnpResultData.paarResult.getBlastMatches().containsKey(modifier.getName());
    // Modifiers currently not supported for SNPAR
    final Predicate<Modifier> selector = modifier -> false;

    // Start with resolving PAAR
    paarsnpResultData.paarResult.getCompleteResistanceSets()
        .forEach(resistanceSet -> resistanceSet.getPhenotypes()
            .forEach(phenotype -> this.determineResistanceState(phenotype, paarModSelector, Completeness.COMPLETE)
                .forEach(agentToNewState -> {
                  resistanceSets.get(agentToNewState.getKey()).add(resistanceSet.getName());
                  profileAggregator.addPhenotype(agentToNewState.getKey(), agentToNewState.getValue());
                })));

    paarsnpResultData.paarResult.getPartialResistanceSets()
        .forEach(resistanceSet -> resistanceSet.getPhenotypes()
            .forEach(phenotype -> this.determineResistanceState(phenotype, paarModSelector, Completeness.PARTIAL)
                .forEach(agentToNewState -> {
                  // Only add to the sets if it can impact resistance
                  if (ResistanceState.NOT_FOUND != agentToNewState.getValue()) {
                    resistanceSets.get(agentToNewState.getKey()).add(resistanceSet.getName());
                  }
                  profileAggregator.addPhenotype(agentToNewState.getKey(), agentToNewState.getValue());
                })));

    // Now add SNPAR
    paarsnpResultData.snparResult.getCompleteSets()
        .forEach(resistanceSet -> resistanceSet.getPhenotypes()
            .forEach(phenotype -> this.determineResistanceState(phenotype, selector, Completeness.COMPLETE)
                .forEach(agentToNewState -> {
                  resistanceSets.get(agentToNewState.getKey()).add(resistanceSet.getName());
                  profileAggregator.addPhenotype(agentToNewState.getKey(), agentToNewState.getValue());
                })));


    paarsnpResultData.snparResult.getPartialSets()
        .forEach(resistanceSet -> resistanceSet.getPhenotypes()
            .forEach(phenotype -> this.determineResistanceState(phenotype, selector, Completeness.PARTIAL)
                .forEach(agentToNewState -> {
                  // Only add to the sets if it can impact resistance, as above
                  if (ResistanceState.NOT_FOUND != agentToNewState.getValue()) {
                    resistanceSets.get(agentToNewState.getKey()).add(resistanceSet.getName());
                  }
                  profileAggregator.addPhenotype(agentToNewState.getKey(), agentToNewState.getValue());
                })));

    // Work out the profile in the specified order
    final Collection<AntibioticProfile> antibioticProfiles = paarsnpResultData.referenceProfile
        .stream()
        .map(agent -> {
          Collection<ResistanceSet> combinedSets = resistanceSets.get(agent)
              .stream()
              .map(this.resistanceSetMap::get)
              .collect(Collectors.toList());
          return new AntibioticProfile(this.agents.get(agent), profileAggregator.getProfileMap().get(agent), combinedSets);
        })
        .collect(Collectors.toList());

    // Sorts the profile and adds the antibiotics with no matches.
    return new PaarsnpResult(paarsnpResultData.assemblyId, paarsnpResultData.snparResult, paarsnpResultData.paarResult, antibioticProfiles);
  }

  private Stream<Map.Entry<String, ResistanceState>> determineResistanceState(final Phenotype phenotype, final Predicate<Modifier> selector, final BuildPaarsnpResult.Completeness completeness) {

    final ElementEffect modifierEffect = phenotype.getModifiers()
        .stream()
        .filter(selector)
        .findFirst()
        .orElse(DEFAULT_MODIFIER).getEffect();

    return phenotype.getProfile()
        .stream()
        .map(agent -> {
          final ResistanceState resistanceState;
          switch (modifierEffect) {
            case RESISTANCE:
              if (phenotype.getEffect() == PhenotypeEffect.RESISTANT && Completeness.COMPLETE == completeness) {
                resistanceState = ResistanceState.RESISTANT;
              } else if (phenotype.getEffect() == PhenotypeEffect.INTERMEDIATE_ADDITIVE && Completeness.COMPLETE == completeness) {
                resistanceState = ResistanceState.RESISTANT;
              } else if (phenotype.getEffect() == PhenotypeEffect.INTERMEDIATE_NOT_ADDITIVE && Completeness.COMPLETE == completeness) {
                resistanceState = ResistanceState.INTERMEDIATE;
              } else {
                if (phenotype.getEffect() == PhenotypeEffect.INTERMEDIATE_ADDITIVE && Completeness.PARTIAL == completeness) {
                  resistanceState = ResistanceState.INTERMEDIATE;
                } else {
                  resistanceState = ResistanceState.NOT_FOUND;
                }
              }
              break;
            case MODIFIES_SUPPRESSES:
              resistanceState = ResistanceState.NOT_FOUND;
              break;
            case MODIFIES_INDUCED:
              if (phenotype.getEffect() == PhenotypeEffect.RESISTANT && Completeness.COMPLETE == completeness) {
                resistanceState = ResistanceState.INDUCIBLE;
              } else if (phenotype.getEffect() == PhenotypeEffect.INTERMEDIATE_ADDITIVE && Completeness.COMPLETE == completeness) {
                resistanceState = ResistanceState.INDUCIBLE;
              } else {
                resistanceState = ResistanceState.NOT_FOUND;
              }
              break;
            default:
              resistanceState = ResistanceState.NOT_FOUND;
          }
          return new AbstractMap.SimpleImmutableEntry<>(agent, resistanceState);
        });
  }

  public enum Completeness {
    COMPLETE, PARTIAL
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
