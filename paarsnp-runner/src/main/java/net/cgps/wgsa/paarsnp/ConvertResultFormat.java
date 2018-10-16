package net.cgps.wgsa.paarsnp;

import net.cgps.wgsa.paarsnp.core.lib.json.OldStyleAntibioticProfile;
import net.cgps.wgsa.paarsnp.core.lib.json.OldStyleSetDescription;
import net.cgps.wgsa.paarsnp.core.lib.json.ResistanceSet;
import net.cgps.wgsa.paarsnp.core.lib.utils.ConvertSetDescription;
import net.cgps.wgsa.paarsnp.core.paar.json.OldStylePaarResult;
import net.cgps.wgsa.paarsnp.core.snpar.json.OldStyleSnparResult;

import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConvertResultFormat implements Function<PaarsnpResult, OldStylePaarsnpResult> {

  @Override
  public OldStylePaarsnpResult apply(final PaarsnpResult paarsnpResult) {

    final Function<ResistanceSet, Stream<OldStyleSetDescription>> convertSetFormat = new ConvertSetDescription();

    final Function<Collection<ResistanceSet>, Collection<OldStyleSetDescription>> converter = sets -> sets.stream().flatMap(convertSetFormat::apply).collect(Collectors.toList());

    return new OldStylePaarsnpResult(
        paarsnpResult.getAssemblyId(),
        new OldStyleSnparResult(
            paarsnpResult.getSnparResult().getResistanceMutationIds(),
            converter.apply(paarsnpResult.getSnparResult().getCompleteSets()),
            converter.apply(paarsnpResult.getSnparResult().getPartialSets()),
            modifiedSets,
            paarsnpResult.getSnparResult().getBlastMatches()
        ),
        new OldStylePaarResult(
            converter.apply(paarsnpResult.getPaarResult().getCompleteResistanceSets()),
            converter.apply(paarsnpResult.getPaarResult().getPartialResistanceSets()),
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
}
