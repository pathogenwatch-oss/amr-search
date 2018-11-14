package net.cgps.wgsa.paarsnp.core.snpar;

import net.cgps.wgsa.paarsnp.core.formats.*;
import net.cgps.wgsa.paarsnp.core.lib.blast.BlastMatch;
import net.cgps.wgsa.paarsnp.core.lib.blast.MutationBuilder;
import net.cgps.wgsa.paarsnp.core.lib.blast.SequenceProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
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
        .getMappedVariants()
        .stream()
        .peek(mutation -> this.logger.debug("Resistance mutation {}", mutation.getName()))
        .filter(mutation -> mutation.isWithin(match))
        .map(resistanceMutation -> resistanceMutation.isPresent(mutations, codonMap))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toList());

    return new SnparMatchData(match.getBlastSearchStatistics(), resistanceMutations);
  }
}
