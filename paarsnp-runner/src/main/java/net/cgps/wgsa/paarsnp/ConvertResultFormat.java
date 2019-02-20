package net.cgps.wgsa.paarsnp;

import net.cgps.wgsa.paarsnp.core.lib.ConvertSetDescription;
import net.cgps.wgsa.paarsnp.core.models.Phenotype;
import net.cgps.wgsa.paarsnp.core.models.ResistanceSet;
import net.cgps.wgsa.paarsnp.core.models.SetMember;
import net.cgps.wgsa.paarsnp.core.models.results.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConvertResultFormat implements Function<PaarsnpResult, PathogenWatchFormat> {

  @Override
  public PathogenWatchFormat apply(final PaarsnpResult paarsnpResult) {

    final Collection<String> paarElementIds = new ArrayList<>(50);
    final Collection<String> snparElementIds = new ArrayList<>(100);

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
                .map(variant -> foundElement + "_" + variant)
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
        .forEach(match -> elementIdToSetNames
            .get(match.getSearchStatistics().getLibrarySequenceId())
            .forEach(set -> {
              if (match.getSnpResistanceElements().isEmpty() && paarElementIds.contains(match.getSearchStatistics().getLibrarySequenceId())) {
                matches.add(this.buildMatchFormat(set.getName(), match, "PAAR"));
              } else if (!match.getSnpResistanceElements().isEmpty()) {
                matches.add(this.buildMatchFormat(set.getName(), match, "SNPAR"));
                variants.addAll(match
                    .getSnpResistanceElements()
                    .stream()
                    .flatMap(variant -> variant
                        .getCausalMutations()
                        .stream()
                        .map(causalVariant -> new PathogenWatchFormat.VariantJson(
                            set.getPhenotypes().stream().map(Phenotype::getProfile).flatMap(Collection::stream).collect(Collectors.toList()),
                            match.getSearchStatistics().getQuerySequenceId(),
                            match.getSearchStatistics().isReversed(),
                            causalVariant.getQueryLocation(),
                            causalVariant.getReferenceLocation(),
                            variant.getResistanceMutation().getName(),
                            match.getSearchStatistics().getLibrarySequenceStart()
                        )))
                    .collect(Collectors.toList())
                );
              }
            }));

    final Function<ResistanceSet, Stream<OldStyleSetDescription>> convertSetFormat = new ConvertSetDescription();

    return new PathogenWatchFormat(paarsnpResult.getAssemblyId(),
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

  public PathogenWatchFormat.CdsJson buildMatchFormat(final String setName, final MatchJson match, final String sourceString) {
    return new PathogenWatchFormat.CdsJson(
        setName,
        sourceString,
        match.getSearchStatistics().isReversed(),
        match.getSearchStatistics().getEvalue(),
        match.getSearchStatistics().getPercentIdentity(),
        new PathogenWatchFormat.CdsLocation(
            match.getSearchStatistics().getLibrarySequenceStart(),
            match.getSearchStatistics().getLibrarySequenceStop(),
            match.getSearchStatistics().getLibrarySequenceLength(),
            match.getSearchStatistics().getLibrarySequenceId()
        ),
        new PathogenWatchFormat.CdsLocation(
            match.getSearchStatistics().getQuerySequenceStart(),
            match.getSearchStatistics().getQuerySequenceStop(),
            match.getSearchStatistics().getQuerySequenceLength(),
            match.getSearchStatistics().getQuerySequenceId()
        )
    );
  }

  private Integer countSnparSetSize(final ResistanceSet resistanceSet) {
    return resistanceSet.getMembers().stream().map(SetMember::getVariants).mapToInt(Collection::size).sum();
  }
}
