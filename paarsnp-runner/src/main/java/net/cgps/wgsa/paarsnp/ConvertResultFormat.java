package net.cgps.wgsa.paarsnp;

import net.cgps.wgsa.paarsnp.core.lib.SetResult;
import net.cgps.wgsa.paarsnp.core.lib.json.OldStyleAntibioticProfile;
import net.cgps.wgsa.paarsnp.core.lib.json.OldStyleSetDescription;
import net.cgps.wgsa.paarsnp.core.lib.json.ResistanceSet;
import net.cgps.wgsa.paarsnp.core.lib.utils.ConvertSetDescription;
import net.cgps.wgsa.paarsnp.core.paar.json.OldStylePaarResult;
import net.cgps.wgsa.paarsnp.core.snpar.json.OldStyleSnparResult;
import net.cgps.wgsa.paarsnp.core.snpar.json.SetMember;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConvertResultFormat implements Function<PaarsnpResult, OldStylePaarsnpResult> {

  @Override
  public OldStylePaarsnpResult apply(final PaarsnpResult paarsnpResult) {

    final Function<ResistanceSet, Stream<OldStyleSetDescription>> convertSetFormat = new ConvertSetDescription();

    final Function<Collection<ResistanceSet>, Collection<OldStyleSetDescription>> converter = sets -> sets.stream().flatMap(convertSetFormat::apply).collect(Collectors.toList());

    final List<OldStyleSetDescription> snparModifiedSets = paarsnpResult.getSnparResult().getSetResults().stream()
        .filter(setResult -> !setResult.getFoundModifiers().isEmpty())
        .map(SetResult::getSet)
        .flatMap(convertSetFormat)
        .collect(Collectors.toList());

    final Set<String> seenSnparSets = snparModifiedSets.stream().map(OldStyleSetDescription::getResistanceSetName).collect(Collectors.toSet());

    final List<OldStyleSetDescription> snparCompleteSets = paarsnpResult.getSnparResult().getSetResults().stream()
        .filter(setResult -> seenSnparSets.contains(setResult.getSet().getName()))
        .filter(setResult -> setResult.getFoundMembers().size() == this.countSnparSetSize(setResult.getSet()))
        .map(SetResult::getSet)
        .flatMap(convertSetFormat)
        .collect(Collectors.toList());

    final List<OldStyleSetDescription> snparPartialSets = paarsnpResult.getSnparResult().getSetResults().stream()
        .filter(setResult -> seenSnparSets.contains(setResult.getSet().getName()))
        .filter(setResult -> 0 < setResult.getFoundMembers().size())
        .filter(setResult -> setResult.getFoundMembers().size() < this.countSnparSetSize(setResult.getSet()))
        .map(SetResult::getSet)
        .flatMap(convertSetFormat)
        .collect(Collectors.toList());


    final List<OldStyleSetDescription> paarModifiedSets = paarsnpResult.getPaarResult().getSetResults().stream()
        .filter(setResult -> !setResult.getFoundModifiers().isEmpty())
        .map(SetResult::getSet)
        .flatMap(convertSetFormat)
        .collect(Collectors.toList());

    final Set<String> seenPaarSets = paarModifiedSets.stream().map(OldStyleSetDescription::getResistanceSetName).collect(Collectors.toSet());

    final List<OldStyleSetDescription> paarCompleteSets = paarsnpResult.getPaarResult().getSetResults().stream()
        .filter(setResult -> setResult.getFoundMembers().size() == setResult.getSet().getMembers().size())
        .filter(setResult -> seenPaarSets.contains(setResult.getSet().getName()))
        .map(SetResult::getSet)
        .flatMap(convertSetFormat)
        .collect(Collectors.toList());

    final List<OldStyleSetDescription> paarPartialSets = paarsnpResult.getPaarResult().getSetResults().stream()
        .filter(setResult -> 0 < setResult.getFoundMembers().size())
        .filter(setResult -> setResult.getFoundMembers().size() < setResult.getSet().getMembers().size())
        .map(SetResult::getSet)
        .flatMap(convertSetFormat)
        .collect(Collectors.toList());

    return new OldStylePaarsnpResult(

        paarsnpResult.getAssemblyId(),

        new OldStyleSnparResult(
            paarsnpResult.getSnparResult().getSetResults()
                .stream()
                .map(SetResult::getFoundMembers)
                .flatMap(Collection::stream)
                .collect(Collectors.toList()),
            snparCompleteSets,
            snparPartialSets,
            snparModifiedSets,
            paarsnpResult.getSnparResult().getBlastMatches()
        ),

        new OldStylePaarResult(
            paarCompleteSets,
            paarPartialSets,
            paarModifiedSets,
            paarsnpResult.getPaarResult().getBlastMatches(),
            paarsnpResult.getPaarResult().getPaarElementIds()
        ),

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
            .collect(Collectors.toList())
    );
  }

  private Integer countSnparSetSize(final ResistanceSet resistanceSet) {
    return resistanceSet.getMembers().stream().map(SetMember::getVariants).mapToInt(Collection::size).sum();
  }
}
