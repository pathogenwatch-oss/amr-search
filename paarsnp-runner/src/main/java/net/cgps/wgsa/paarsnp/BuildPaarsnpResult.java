package net.cgps.wgsa.paarsnp;

import net.cgps.wgsa.paarsnp.core.lib.ElementEffect;
import net.cgps.wgsa.paarsnp.core.lib.PhenotypeEffect;
import net.cgps.wgsa.paarsnp.core.lib.ProfileAggregator;
import net.cgps.wgsa.paarsnp.core.lib.ResistanceState;
import net.cgps.wgsa.paarsnp.core.lib.json.*;
import net.cgps.wgsa.paarsnp.core.paar.json.PaarResult;
import net.cgps.wgsa.paarsnp.core.snpar.json.SnparResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
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

    this.logger.debug("Building paarsnp result from match data for assemblyId={}", paarsnpResultData.assemblyId);

    // Now build the profiles.
    final ProfileAggregator profileAggregator = ProfileAggregator.initialise(this.agents);

    final Map<String, Collection<String>> resistanceSets = this.agents.keySet().stream().collect(Collectors.toMap(Function.identity(), (agent) -> new ArrayList<>()));

    // Start with resolving PAAR
    paarsnpResultData.paarResult.getSetResults()
        .stream()
        .filter(setResult -> !setResult.getFoundMembers().isEmpty())
        .forEach(setResult -> {

          final Completeness completeness = setResult.getFoundMembers().size() == setResult.getSet().getMembers().size() ? Completeness.COMPLETE : Completeness.PARTIAL;

          setResult.getSet().getPhenotypes()
              .forEach(phenotype -> this.determineResistanceState(
                  phenotype,
                  phenotype.getModifiers().stream().filter(modifier -> setResult.getFoundModifiers().contains(modifier.getName())).collect(Collectors.toList()),
                  completeness)
                  .forEach(agentState -> {
                    this.logger.debug("{} {}", agentState.getKey(), agentState.getValue().name());
                    resistanceSets.get(agentState.getKey()).add(setResult.getSet().getName());
                    profileAggregator.addPhenotype(agentState.getKey(), agentState.getValue());
                  }));
        });

    // Then add the SNPAR result
    paarsnpResultData.snparResult.getSetResults()
        .stream()
        .filter(setResult -> !setResult.getFoundMembers().isEmpty())
        .forEach(setResult -> {

          final Completeness completeness = setResult.getFoundMembers().size() == setResult.getSet().getMembers().size() ? Completeness.COMPLETE : Completeness.PARTIAL;

          setResult.getSet().getPhenotypes()
              .forEach(phenotype -> this.determineResistanceState(
                  phenotype,
                  phenotype.getModifiers().stream().filter(modifier -> setResult.getFoundModifiers().contains(modifier.getName())).collect(Collectors.toList()),
                  completeness)
              );
        });

    // Work out the profile in the specified order
    final Collection<AntibioticProfile> antibioticProfiles = paarsnpResultData.referenceProfile
        .stream()
        .map(agent -> new AntibioticProfile(
            this.agents.get(agent),
            profileAggregator.getProfileMap().get(agent),
            resistanceSets.get(agent)
                .stream()
                .map(this.resistanceSetMap::get)
                .collect(Collectors.toList())))
        .collect(Collectors.toList());

    // Sorts the profile and adds the antibiotics with no matches.
    return new PaarsnpResult(paarsnpResultData.assemblyId, paarsnpResultData.snparResult, paarsnpResultData.paarResult, antibioticProfiles);
  }

  private Stream<Map.Entry<String, ResistanceState>> determineResistanceState(final Phenotype phenotype, final List<Modifier> phenotypeModifiers, final BuildPaarsnpResult.Completeness completeness) {

    // NB At the moment only the first modifier is dealt with (assumes only one allowed modifier at a time)
    final ElementEffect modifierEffect = phenotypeModifiers.isEmpty() ? ElementEffect.RESISTANCE : phenotypeModifiers.get(0).getEffect();

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
