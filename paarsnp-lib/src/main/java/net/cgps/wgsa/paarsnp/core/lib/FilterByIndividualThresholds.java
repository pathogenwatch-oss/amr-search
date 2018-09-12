package net.cgps.wgsa.paarsnp.core.lib;

import net.cgps.wgsa.paarsnp.core.lib.blast.BlastMatch;
import net.cgps.wgsa.paarsnp.core.paar.json.Paar;
import net.cgps.wgsa.paarsnp.core.paar.json.ResistanceGene;
import net.cgps.wgsa.paarsnp.core.snpar.json.Snpar;
import net.cgps.wgsa.paarsnp.core.snpar.json.SnparReferenceSequence;

import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class FilterByIndividualThresholds implements Predicate<BlastMatch> {
  private final Map<String, Float> pidThresholds;
  private final Map<String, Float> coverageThresholds;

  public FilterByIndividualThresholds(final Map<String, Float> pidThresholds, final Map<String, Float> coverageThresholds) {
    this.pidThresholds = pidThresholds;
    this.coverageThresholds = coverageThresholds;
  }

  public static FilterByIndividualThresholds build(final Paar paarLibrary) {
    return new FilterByIndividualThresholds(
        paarLibrary
            .getGenes()
            .values()
            .stream()
            .collect(Collectors.toMap(ResistanceGene::getFamilyName, ResistanceGene::getPid)),
        paarLibrary
            .getGenes()
            .values()
            .stream()
            .collect(Collectors.toMap(ResistanceGene::getFamilyName, ResistanceGene::getCoverage)));
  }

  public static FilterByIndividualThresholds build(final Snpar snparLibrary) {
    return new FilterByIndividualThresholds(
        snparLibrary.getGenes()
            .values()
            .stream()
            .collect(Collectors.toMap(SnparReferenceSequence::getName, SnparReferenceSequence::getPid)),
        snparLibrary.getGenes()
            .values()
            .stream()
            .collect(Collectors.toMap(SnparReferenceSequence::getName, SnparReferenceSequence::getCoverage))
    );
  }

  @Override
  public boolean test(final BlastMatch match) {

    return this.coverageThresholds.get(match.getBlastSearchStatistics().getLibrarySequenceId()) <= match.calculateCoverage()
        &&
        this.pidThresholds.get(match.getBlastSearchStatistics().getLibrarySequenceId()) <= match.getBlastSearchStatistics().getPercentIdentity();
  }
}