package net.cgps.wgsa.paarsnp.core.snpar;

import net.cgps.wgsa.paarsnp.core.models.ProcessedMatch;
import net.cgps.wgsa.paarsnp.core.models.ResistanceMutationMatch;
import net.cgps.wgsa.paarsnp.core.models.variants.Variant;

import java.util.Collection;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ExtractResistanceVariants implements Function<ProcessedMatch, Set<String>> {

  final Collection<String> queryVariants;

  public ExtractResistanceVariants(final Collection<String> queryVariants) {
    this.queryVariants = queryVariants;
  }

  @Override
  public Set<String> apply(final ProcessedMatch processedMatch) {
    return processedMatch
        .getSnpResistanceElements()
        .stream()
        .map(ResistanceMutationMatch::getName)
        .filter(this.queryVariants::contains)
        .collect(Collectors.toSet());
  }
}
