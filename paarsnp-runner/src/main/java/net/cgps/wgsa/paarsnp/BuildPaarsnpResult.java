package net.cgps.wgsa.paarsnp;

import net.cgps.wgsa.paarsnp.core.models.ElementEffect;
import net.cgps.wgsa.paarsnp.core.models.Phenotype;
import net.cgps.wgsa.paarsnp.core.models.PhenotypeEffect;
import net.cgps.wgsa.paarsnp.core.models.results.AntimicrobialAgent;
import net.cgps.wgsa.paarsnp.core.models.results.Modifier;
import net.cgps.wgsa.paarsnp.core.models.results.ResistanceState;
import net.cgps.wgsa.paarsnp.output.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BuildPaarsnpResult implements Function<PaarsnpResultData, ResultJson> {

  private final Logger logger = LoggerFactory.getLogger(BuildPaarsnpResult.class);

  private final Map<String, AntimicrobialAgent> agents;

  public BuildPaarsnpResult(final Map<String, AntimicrobialAgent> agents) {
    this.agents = agents;
  }

  @Override
  public ResultJson apply(final PaarsnpResultData paarsnpResultData) {
    this.logger.debug("Building paarsnp result from match data for assemblyId={}", paarsnpResultData.assemblyId);

    final var profileAggregator = ProfileAggregator.initialise(this.agents);

    // Agent key -> set name -> set
//    final var resistanceSets = this.agents.keySet().stream().collect(Collectors.toMap(Function.identity(), (agent) -> new HashMap<String, SetResult>()));
    // Agent key -> set name -> determinants
    final var acquiredDeterminants = this.agents.keySet().stream().collect(Collectors.toMap(Function.identity(), (agent) -> new HashMap<String, Collection<Determinant>>()));
    final var variantDeterminants = this.agents.keySet().stream().collect(Collectors.toMap(Function.identity(), (agent) -> new HashMap<String, Collection<Determinant>>()));
    final var determinantRules = this.agents.keySet().stream().collect(Collectors.toMap(Function.identity(), (agent) -> new HashMap<String, ResistanceState>()));


    paarsnpResultData.searchResult.getSetResults()
        .stream()
        .filter(setResult -> !setResult.getFoundMembers().isEmpty())
        .forEach(setResult -> setResult
            .getSet()
            .getPhenotypes()
            .forEach(phenotype -> {
              final var determinantClass = setResult.getSet().getMembers().size() == 1 && phenotype.getEffect() == PhenotypeEffect.RESISTANT && (setResult.getSet().getMembers().get(0).getVariants().isEmpty() || setResult.getSet().getMembers().get(0).getVariants().size() == 1) ? DeterminantClass.RESISTANCE : DeterminantClass.CONTRIBUTES;
              final var convertedAcquired = setResult.getFoundMembers().stream().filter(setMember -> setMember.getVariants().isEmpty()).map(member -> new Determinant(member.getGene(), null, determinantClass)).collect(Collectors.toSet());
              final var convertedVariants = setResult.getFoundMembers().stream().filter(setMember -> !setMember.getVariants().isEmpty()).flatMap(member -> member.getVariants().stream().map(variant -> new Determinant(member.getGene(), variant, determinantClass))).collect(Collectors.toSet());
                  final var modifiers = phenotype.getModifiers().stream().filter(setResult::containsModifier).collect(Collectors.toList());
                  final var convertedAcquiredModifiers = modifiers.stream().filter(modifier -> modifier.getVariants().isEmpty()).map(modifier -> new Determinant(modifier.getGene(), null, DeterminantClass.fromModifierEffect(modifier.getEffect()))).collect(Collectors.toSet());
                  final var convertedVariantModifiers = modifiers.stream().filter(modifier -> !modifier.getVariants().isEmpty()).flatMap(modifier -> modifier.getVariants().stream().map(variant -> new Determinant(modifier.getGene(), variant, DeterminantClass.fromModifierEffect(modifier.getEffect())))).collect(Collectors.toSet());

                  phenotype.getProfile().forEach(amKey -> {
                    final var foundAcquired = new HashSet<Determinant>(convertedAcquired.size() + convertedAcquiredModifiers.size());
                    foundAcquired.addAll(convertedAcquired);
                    foundAcquired.addAll(convertedAcquiredModifiers);
                    acquiredDeterminants.get(amKey).put(setResult.getSet().getName(), foundAcquired);
                    final var foundVariants = new HashSet<Determinant>(convertedAcquired.size() + convertedAcquiredModifiers.size());
                    foundVariants.addAll(convertedVariants);
                    foundVariants.addAll(convertedVariantModifiers);
                    variantDeterminants.get(amKey).put(setResult.getSet().getName(), foundVariants);
                  });

                  this.determineResistanceState(
                      phenotype,
                      modifiers,
                      setResult.containsAll())
                      .filter(state -> state.getValue() != ResistanceState.NOT_FOUND)
                      .forEach(agentState -> {
                        this.logger.debug("{} {}", agentState.getKey(), agentState.getValue().name());
                        determinantRules.get(agentState.getKey()).put(setResult.getSet().getName(), agentState.getValue());
                        profileAggregator.addPhenotype(agentState.getKey(), agentState.getValue());
                      });
                }
            ));

    final var resistanceProfile = paarsnpResultData.referenceProfile
        .stream()
        .map(agent -> new ResistanceProfile(
            this.agents.get(agent),
            profileAggregator.getProfileMap().get(agent),
            new DeterminantsProfile(
                acquiredDeterminants.get(agent).values().stream().flatMap(Collection::stream).collect(Collectors.toSet()),
                variantDeterminants.get(agent).values().stream().flatMap(Collection::stream).collect(Collectors.toSet())
            ),
            determinantRules.get(agent)
        ))
        .collect(Collectors.toList());

    final var matches = paarsnpResultData.searchResult.getBlastMatches();

    final var aggregatedAcquired = acquiredDeterminants.values().stream().map(Map::values).flatMap(Collection::stream).flatMap(Collection::stream).map(Determinant::getGene).collect(Collectors.toSet());
    final var aggregatedVariants = variantDeterminants.values().stream().map(Map::values).flatMap(Collection::stream).flatMap(Collection::stream).map(determinant -> determinant.getGene() + "_" + determinant.getVariant()).collect(Collectors.toSet());

    return new NewOutput(paarsnpResultData.assemblyId, resistanceProfile, aggregatedAcquired, aggregatedVariants, matches, paarsnpResultData.version);
  }

  private Stream<Map.Entry<String, ResistanceState>> determineResistanceState(final Phenotype phenotype, final List<Modifier> phenotypeModifiers, final boolean isComplete) {

    // NB At the moment only the first modifier is dealt with (assumes only one allowed modifier at a time)
    final var modifierEffect = phenotypeModifiers.stream().map(Modifier::getEffect).distinct().sorted().findFirst().orElse(ElementEffect.NONE);

    return phenotype.getProfile()
        .stream()
        .map(agent -> {
          final ResistanceState resistanceState;
          switch (modifierEffect) {
            case NONE:
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
              // Suppressed
              resistanceState = ResistanceState.NOT_FOUND;
          }
          return new AbstractMap.SimpleImmutableEntry<>(agent, resistanceState);
        });
  }

}
