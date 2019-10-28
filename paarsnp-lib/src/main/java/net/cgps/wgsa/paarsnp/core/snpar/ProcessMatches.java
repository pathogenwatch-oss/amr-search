package net.cgps.wgsa.paarsnp.core.snpar;

import net.cgps.wgsa.paarsnp.core.lib.blast.BlastMatch;
import net.cgps.wgsa.paarsnp.core.lib.blast.Mutation;
import net.cgps.wgsa.paarsnp.core.lib.blast.MutationBuilder;
import net.cgps.wgsa.paarsnp.core.lib.blast.SequenceProcessor;
import net.cgps.wgsa.paarsnp.core.models.PaarsnpLibrary;
import net.cgps.wgsa.paarsnp.core.models.ProcessedMatch;
import net.cgps.wgsa.paarsnp.core.models.ReferenceSequence;
import net.cgps.wgsa.paarsnp.core.models.ResistanceMutationMatch;
import net.cgps.wgsa.paarsnp.core.snpar.codonmapping.CodonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
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

    final ReferenceSequence referenceSequence = this.mechanismsLibrary.getGenes().get(match.getBlastSearchStatistics().getLibrarySequenceId());

    // if the mechanisms don't use SNPs just return match data with empty variants
    if (referenceSequence.getVariants().isEmpty()) {
      return new ProcessedMatch(match.getBlastSearchStatistics(), Collections.emptyList());
    }

    // Extract mutations from sequence
    final Map<Integer, Collection<Mutation>> mutations = new SequenceProcessor(match.getReferenceMatchSequence(), match.getBlastSearchStatistics().getLibrarySequenceStart(), match.getBlastSearchStatistics().getStrand(), match.getForwardQuerySequence(), match.getBlastSearchStatistics().getQuerySequenceStart(), new MutationBuilder()).call();

    final CodonMap codonMap = new CodonMapper().apply(match);

    final Collection<ResistanceMutationMatch> resistanceMutations = referenceSequence
        .getVariants()
        .stream()
        .peek(mutation -> this.logger.trace("Testing resistance mutation {}", mutation.getName()))
        .filter(mutation -> mutation.isWithinBoundaries(
            match.getBlastSearchStatistics().getLibrarySequenceStart(),
            match.getBlastSearchStatistics().getLibrarySequenceStop()))
        .filter(resistanceMutation -> resistanceMutation.isPresent(mutations, codonMap))
        .map(resistanceMutation -> resistanceMutation.buildMatch(mutations, codonMap))
        .collect(Collectors.toList());

    return new ProcessedMatch(match.getBlastSearchStatistics(), resistanceMutations);
  }
}
