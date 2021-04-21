package net.cgps.wgsa.paarsnp.core.snpar;

import net.cgps.wgsa.paarsnp.core.lib.blast.*;
import net.cgps.wgsa.paarsnp.core.models.PaarsnpLibrary;
import net.cgps.wgsa.paarsnp.core.models.ProcessedMatch;
import net.cgps.wgsa.paarsnp.core.models.ResistanceMutationMatch;
import net.cgps.wgsa.paarsnp.core.snpar.codonmapping.CodonMapper;
import net.cgps.wgsa.paarsnp.core.snpar.codonmapping.CreateFrameshiftFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ProcessMatches implements Function<BlastMatch, ProcessedMatch> {

  private final Logger logger = LoggerFactory.getLogger(ProcessMatches.class);

  private final PaarsnpLibrary mechanismsLibrary;

  public ProcessMatches(final PaarsnpLibrary mechanismsLibrary) {

    this.mechanismsLibrary = mechanismsLibrary;
  }

  @Override
  public ProcessedMatch apply(final BlastMatch match) {

    final var referenceSequence = this.mechanismsLibrary.getGenes().get(match.getBlastSearchStatistics().getRefId());

    // if the mechanisms don't use SNPs just return match data with empty variants
    if (referenceSequence.getVariants().isEmpty()) {
      return new ProcessedMatch(match.getBlastSearchStatistics(), Collections.emptyList());
    }

    // Extract mutations from sequence
    final var mutations = new SequenceProcessor(match.getReferenceMatchSequence(), match.getBlastSearchStatistics().getRefStart(), match.getBlastSearchStatistics().getStrand(), match.getForwardQuerySequence(), match.getBlastSearchStatistics().getQueryStart(), new MutationBuilder()).call();

    final var frameshiftFilter = new CreateFrameshiftFilter(referenceSequence.getLength()).apply(mutations.values().stream().flatMap(Collection::stream).filter(Mutation::isIndel).collect(Collectors.toList()));

    final var aaAlignment = new CodonMapper(frameshiftFilter).apply(match);

    final var resistanceMutations = referenceSequence
        .getVariants()
        .stream()
        .peek(mutation -> this.logger.trace("Testing resistance mutation {}", mutation.getName()))
        .filter(mutation -> mutation.isWithinBoundaries(
            match.getBlastSearchStatistics().getRefStart(),
            match.getBlastSearchStatistics().getRefStop()))
        .map(resistanceMutation -> resistanceMutation.match(mutations, aaAlignment)
            .map(locations -> new ResistanceMutationMatch(resistanceMutation.getName(), locations)))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toList());

    final var mutationList = resistanceMutations.stream().map(VariantMatch::build).collect(Collectors.toList());
    match.getBlastSearchStatistics().addVariants(mutationList);

    return new ProcessedMatch(match.getBlastSearchStatistics(), resistanceMutations);
  }
}
