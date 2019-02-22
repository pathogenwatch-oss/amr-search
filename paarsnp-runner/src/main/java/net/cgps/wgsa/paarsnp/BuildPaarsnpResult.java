package net.cgps.wgsa.paarsnp;

import net.cgps.wgsa.paarsnp.core.models.*;
import net.cgps.wgsa.paarsnp.core.models.ElementEffect;
import net.cgps.wgsa.paarsnp.core.models.PhenotypeEffect;
import net.cgps.wgsa.paarsnp.core.models.results.ResistanceState;
import net.cgps.wgsa.paarsnp.core.models.results.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BuildPaarsnpResult implements Function<BuildPaarsnpResult.PaarsnpResultData, PaarsnpResult> {

  private final Logger logger = LoggerFactory.getLogger(BuildPaarsnpResult.class);

  private final Map<String, AntimicrobialAgent> agents;
  private final Map<String, ResistanceSet> resistanceSetMap;

  public BuildPaarsnpResult(final Map<String, AntimicrobialAgent> agents, final Map<String, ResistanceSet> resistanceSetMap) {
    this.agents = agents;
    this.resistanceSetMap = resistanceSetMap;
  }

  @Override
  public PaarsnpResult apply(final PaarsnpResultData paarsnpResultData) {

    this.logger.debug("Building paarsnp result from match data for assemblyId={}", paarsnpResultData.assemblyId);

    // Now build the profiles.
    final ProfileAggregator profileAggregator = ProfileAggregator.initialise(this.agents);

    final Map<String, Collection<String>> resistanceSets = this.agents.keySet().stream().collect(Collectors.toMap(Function.identity(), (agent) -> new ArrayList<>()));

    paarsnpResultData.searchResult.getSetResults()
        .stream()
        .filter(setResult -> !setResult.getFoundMembers().isEmpty())
        .forEach(setResult -> {

          final boolean complete = setResult.getFoundMembers().size() == setResult
              .getSet()
              .size();

          setResult.getSet().getPhenotypes()
              .forEach(phenotype -> this.determineResistanceState(
                  phenotype,
                  phenotype.getModifiers().stream().filter(modifier -> setResult.modifierIsPresent(modifier.getName())).collect(Collectors.toList()),
                  complete)
                  .filter(state -> state.getValue() != ResistanceState.NOT_FOUND)
                  .forEach(agentState -> {
                    this.logger.debug("{} {}", agentState.getKey(), agentState.getValue().name());
                    resistanceSets.get(agentState.getKey()).add(setResult.getSet().getName());
                    profileAggregator.addPhenotype(agentState.getKey(), agentState.getValue());
                  }));
        });

    // Mangle the result into seperate PAAR & SNPAR results.
    // Check each member of each set. If PAAR
    // Add BLAST result to PAAR

    // Work out the profile in the specified order
    final Collection<AntibioticProfile> antibioticProfiles = paarsnpResultData
        .referenceProfile
        .stream()
        .map(agent -> new AntibioticProfile(
            this.agents.get(agent),
            profileAggregator.getProfileMap().get(agent),
            resistanceSets.get(agent)
                .stream()
                .map(this.resistanceSetMap::get)
                .map(set -> new ResistanceSet(set.getName(), set.getPhenotypes().stream().filter(phenotype -> phenotype.getProfile().contains(agent)).collect(Collectors.toList()), set.getMembers()))
                .collect(Collectors.toList())))
        .collect(Collectors.toList());

    // Sorts the profile and adds the antibiotics with no matches.
    return new PaarsnpResult(paarsnpResultData.assemblyId, paarsnpResultData.searchResult, antibioticProfiles);
  }

  private Stream<Map.Entry<String, ResistanceState>> determineResistanceState(final Phenotype phenotype, final List<Modifier> phenotypeModifiers, final boolean isComplete) {

    // NB At the moment only the first modifier is dealt with (assumes only one allowed modifier at a time)
    final ElementEffect modifierEffect = phenotypeModifiers.isEmpty() ? ElementEffect.RESISTANCE : phenotypeModifiers.get(0).getEffect();

    return phenotype.getProfile()
        .stream()
        .map(agent -> {
          final ResistanceState resistanceState;
          switch (modifierEffect) {
            case RESISTANCE:
              if (phenotype.getEffect() == PhenotypeEffect.RESISTANT && isComplete) {
                resistanceState = ResistanceState.RESISTANT;
              } else if (phenotype.getEffect() == PhenotypeEffect.INTERMEDIATE_ADDITIVE && isComplete) {
                resistanceState = ResistanceState.RESISTANT;
              } else if (phenotype.getEffect() == PhenotypeEffect.INTERMEDIATE && isComplete) {
                resistanceState = ResistanceState.INTERMEDIATE;
              } else {
                if (phenotype.getEffect() == PhenotypeEffect.INTERMEDIATE_ADDITIVE && !isComplete) {
                  resistanceState = ResistanceState.INTERMEDIATE;
                } else {
                  resistanceState = ResistanceState.NOT_FOUND;
                }
              }
              break;
            case SUPPRESSES:
              resistanceState = ResistanceState.NOT_FOUND;
              break;
            case INDUCED:
              if (phenotype.getEffect() == PhenotypeEffect.RESISTANT && isComplete) {
                resistanceState = ResistanceState.INDUCIBLE;
              } else if (phenotype.getEffect() == PhenotypeEffect.INTERMEDIATE_ADDITIVE && isComplete) {
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

  public static class PaarsnpResultData {

    public final String assemblyId;
    public final SearchResult searchResult;
    public final Collection<String> referenceProfile;

    public PaarsnpResultData(final String assemblyId, final SearchResult searchResult, final Collection<String> referenceProfile) {

      this.assemblyId = assemblyId;
      this.searchResult = searchResult;
      this.referenceProfile = referenceProfile;
    }
  }
}
