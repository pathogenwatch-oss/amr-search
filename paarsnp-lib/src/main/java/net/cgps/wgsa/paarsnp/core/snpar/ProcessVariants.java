package net.cgps.wgsa.paarsnp.core.snpar;

import net.cgps.wgsa.paarsnp.core.lib.blast.BlastMatch;
import net.cgps.wgsa.paarsnp.core.lib.blast.Mutation;
import net.cgps.wgsa.paarsnp.core.lib.blast.MutationBuilder;
import net.cgps.wgsa.paarsnp.core.lib.blast.SequenceProcessor;
import net.cgps.wgsa.paarsnp.core.models.ReferenceSequence;
import net.cgps.wgsa.paarsnp.core.models.ResistanceMutationMatch;
import net.cgps.wgsa.paarsnp.core.models.Snpar;
import net.cgps.wgsa.paarsnp.core.models.SnparMatchData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ProcessVariants implements Function<BlastMatch, SnparMatchData> {

  private final Logger logger = LoggerFactory.getLogger(ProcessVariants.class);

  private final Snpar snparLibrary;

  public ProcessVariants(final Snpar snparLibrary) {

    this.snparLibrary = snparLibrary;
  }

  @Override
  public SnparMatchData apply(final BlastMatch match) {

    final ReferenceSequence referenceSequence = this.snparLibrary.getGenes().get(match.getBlastSearchStatistics().getLibrarySequenceId());
    final Map<Integer, Collection<Mutation>> mutations = new SequenceProcessor(match.getReferenceMatchSequence(), match.getBlastSearchStatistics().getLibrarySequenceStart(), match.getBlastSearchStatistics().getStrand(), match.getForwardQuerySequence(), match.getBlastSearchStatistics().getQuerySequenceStart(), new MutationBuilder()).call();

    final CodonMap codonMap = new CodonMapper().apply(match);

    final Collection<ResistanceMutationMatch> resistanceMutations = referenceSequence
        .getVariants()
        .stream()
        .peek(mutation -> this.logger.debug("Testing resistance mutation {}", mutation.getName()))
        .filter(mutation -> mutation.isWithinBoundaries(
            match.getBlastSearchStatistics().getLibrarySequenceStart(),
            match.getBlastSearchStatistics().getLibrarySequenceStop()))
        .filter(resistanceMutation -> resistanceMutation.isPresent(mutations, codonMap))
        .map(resistanceMutation -> resistanceMutation.buildMatch(mutations, codonMap))
        .collect(Collectors.toList());

    return new SnparMatchData(match.getBlastSearchStatistics(), resistanceMutations);
  }
}
