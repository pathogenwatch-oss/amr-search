package net.cgps.wgsa.paarsnp;

import net.cgps.wgsa.paarsnp.core.lib.ConvertSetDescription;
import net.cgps.wgsa.paarsnp.core.models.Phenotype;
import net.cgps.wgsa.paarsnp.core.models.ResistanceSet;
import net.cgps.wgsa.paarsnp.core.models.results.MatchJson;
import net.cgps.wgsa.paarsnp.core.models.results.OldStyleAntibioticProfile;
import net.cgps.wgsa.paarsnp.core.models.results.OldStyleSetDescription;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConvertResultFormat implements Function<PaarsnpResult, PathogenWatchFormat> {

  @Override
  public PathogenWatchFormat apply(final PaarsnpResult paarsnpResult) {

    final var paarElementIds = new HashSet<String>(50);
    final var snparElementIds = new HashSet<String>(100);

    paarsnpResult.getSearchResult()
        .getSetResults()
        .stream()
        .flatMap(set -> Stream.of(set.getFoundMembers(), set.getFoundModifiers()))
        .flatMap(Collection::stream)
        .forEach(foundElement -> {
          if (foundElement.getVariants().isEmpty()) {
            paarElementIds.add(foundElement.getGene());
          } else {
            snparElementIds.addAll(foundElement
                .getVariants()
                .stream()
                .map(variant -> foundElement.getGene() + "_" + variant)
                .collect(Collectors.toList())
            );
          }
        });

    final Collection<PathogenWatchFormat.CdsJson> matches = new ArrayList<>(50);
    final Collection<PathogenWatchFormat.VariantJson> variants = new ArrayList<>(100);

    final Map<String, List<ResistanceSet>> elementIdToSetNames = paarsnpResult
        .getSearchResult()
        .getSetResults()
        .stream()
        .flatMap(setResult -> Stream.of(setResult.getFoundMembers(), setResult.getFoundModifiers())
            .flatMap(Collection::stream)
            .map(setMember -> new AbstractMap.SimpleImmutableEntry<>(setMember.getGene(), setResult.getSet())))
        .collect(Collectors.groupingBy(AbstractMap.SimpleImmutableEntry::getKey, Collectors.mapping(Map.Entry::getValue, Collectors.toList())));

    paarsnpResult
        .getSearchResult()
        .getBlastMatches()
        .stream()
        .filter(match -> elementIdToSetNames.containsKey(match.getStatistics().getReferenceId()))
        .forEach(match -> elementIdToSetNames
            .get(match.getStatistics().getReferenceId())
            .forEach(set -> {
              final List<String> setAntimicrobials = set.getPhenotypes().stream().map(Phenotype::getProfile).flatMap(Collection::stream).collect(Collectors.toList());

              if (match.getResistanceVariants().isEmpty() && paarElementIds.contains(match.getStatistics().getReferenceId())) {
                matches.add(this.buildMatchFormat(set.getName(), setAntimicrobials, match, "WGSA_PAAR"));
              } else if (!match.getResistanceVariants().isEmpty()) {
                // Check if the found variants are also in the set
                matches.add(this.buildMatchFormat(set.getName(), null, match, "WGSA_SNPAR"));
                variants.addAll(match
                    .getResistanceVariants()
                    .stream()
                    .filter(variant -> set.contains(match.getStatistics().getReferenceId(), variant.getName()))
                    .flatMap(variant -> variant
                        .getLocations()
                        .stream()
                        .map(causalVariant -> new PathogenWatchFormat.VariantJson(
                            setAntimicrobials,
                            match.getStatistics().getQueryId(),
                            match.getStatistics().isReversed(),
                            causalVariant.getQueryIndex(),
                            causalVariant.getReferenceIndex(),
                            variant.getName(),
                            match.getStatistics().getReferenceStart()
                        )))
                    .collect(Collectors.toList())
                );
              }
            }));

    final Function<ResistanceSet, Stream<OldStyleSetDescription>> convertSetFormat = new ConvertSetDescription();

    return new PathogenWatchFormat(paarsnpResult.getAssemblyId(),
        paarsnpResult.getVersion(),
        snparElementIds,
        paarElementIds,
        paarsnpResult.getResistanceProfile()
            .stream()
            .map(profile -> new OldStyleAntibioticProfile(
                    profile.getAgent(),
                    profile.getResistanceState(),
                    profile.getResistanceSets()
                        .stream()
                        .flatMap(convertSetFormat)
                        .collect(Collectors.toList())
                )
            )
            .collect(Collectors.toList()),
        matches,
        variants);
  }

  public PathogenWatchFormat.CdsJson buildMatchFormat(final String setName, final List<String> agents, final MatchJson match, final String sourceString) {
    return new PathogenWatchFormat.CdsJson(
        setName,
        sourceString,
        match.getStatistics().isReversed(),
        match.getStatistics().getEvalue(),
        Double.parseDouble(String.format("%.2f", match.getStatistics().getPercentIdentity())),
        new PathogenWatchFormat.CdsLocation(
            match.getStatistics().getReferenceStart(),
            match.getStatistics().getReferenceStop(),
            match.getStatistics().getReferenceLength(),
            match.getStatistics().getReferenceId()
        ),
        new PathogenWatchFormat.CdsLocation(
            match.getStatistics().getQueryStart(),
            match.getStatistics().getQueryStop(),
            null,
            match.getStatistics().getQueryId()
        ),
        agents
    );
  }
}
