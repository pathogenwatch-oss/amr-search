package net.cgps.wgsa.paarsnp.core.lib;

import net.cgps.wgsa.paarsnp.core.lib.blast.BlastMatch;
import net.cgps.wgsa.paarsnp.core.models.PaarsnpLibrary;
import net.cgps.wgsa.paarsnp.core.models.ReferenceSequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class FilterByIndividualThresholds implements Predicate<BlastMatch> {

  private final Logger logger = LoggerFactory.getLogger(FilterByIndividualThresholds.class);
  private final Map<String, Float> pidThresholds;
  private final Map<String, Float> coverageThresholds;

  public FilterByIndividualThresholds(final Map<String, Float> pidThresholds, final Map<String, Float> coverageThresholds) {
    this.pidThresholds = pidThresholds;
    this.coverageThresholds = coverageThresholds;
  }

  public static FilterByIndividualThresholds build(final PaarsnpLibrary mechanismsLibrary) {
    return new FilterByIndividualThresholds(
        mechanismsLibrary.getGenes()
            .values()
            .stream()
            .collect(Collectors.toMap(ReferenceSequence::getName, ReferenceSequence::getPid)),
        mechanismsLibrary.getGenes()
            .values()
            .stream()
            .collect(Collectors.toMap(ReferenceSequence::getName, ReferenceSequence::getCoverage))
    );
  }

  @Override
  public boolean test(final BlastMatch match) {
    this.logger.debug("Filtering {}", match.getBlastSearchStatistics().getReferenceId());
    this.logger.debug("{} {} coverage", match.getBlastSearchStatistics().getReferenceId(), this.coverageThresholds.get(match.getBlastSearchStatistics().getReferenceId()));
    this.logger.debug("{} {} pid", match.getBlastSearchStatistics().getReferenceId(), this.pidThresholds.get(match.getBlastSearchStatistics().getReferenceId()));
    return this.coverageThresholds.get(match.getBlastSearchStatistics().getReferenceId()) <= match.calculateCoverage()
        &&
        this.pidThresholds.get(match.getBlastSearchStatistics().getReferenceId()) <= match.getBlastSearchStatistics().getPercentIdentity();
  }
}