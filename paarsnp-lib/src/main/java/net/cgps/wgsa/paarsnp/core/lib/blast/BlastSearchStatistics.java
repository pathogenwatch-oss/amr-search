package net.cgps.wgsa.paarsnp.core.lib.blast;

import net.cgps.wgsa.paarsnp.core.lib.AbstractJsonnable;
import net.cgps.wgsa.paarsnp.core.lib.utils.DnaSequence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

public class BlastSearchStatistics extends AbstractJsonnable {

  private final String refId;
  private final int refStart;
  private final String queryId;
  private final int queryStart;
  private final double pid;
  private final double evalue;
  private final DnaSequence.Strand strand;
  private final boolean reversed;
  private final int refStop;
  private final int queryStop;
  private final int refLength;
  private final Collection<VariantMatch> resistanceVariants;

  @SuppressWarnings("unused")
  private BlastSearchStatistics() {

    this("", 0, 0, 0, "", 0, 0, 0.0, 0.0, DnaSequence.Strand.FORWARD);
  }

  public BlastSearchStatistics(final String referenceId, final int refStart, final int refStop, final int refLength, final String querySequenceId, final int querySequenceStart, final int querySequenceStop, final double evalue, final double pid, final DnaSequence.Strand strand) {

    this.refId = referenceId;
    this.queryId = querySequenceId;
    this.queryStart = querySequenceStart;
    this.pid = (double) Math.round(pid * 100) / 100;
    this.evalue = evalue;
    this.strand = strand;
    this.queryStop = querySequenceStop;
    this.refLength = refLength;
    this.resistanceVariants = new HashSet<>(20);

    if (DnaSequence.Strand.FORWARD == strand) {
      this.refStart = refStart;
      this.refStop = refStop;
      this.reversed = false;
    } else {
      this.refStart = refStop;
      this.refStop = refStart;
      this.reversed = true;
    }
  }

  public String getRefId() {

    return this.refId;
  }

  public int getRefStart() {

    return this.refStart;
  }

  public int getRefStop() {

    return this.refStop;
  }

  public int getRefLength() {

    return this.refLength;
  }

  public String getQueryId() {

    return this.queryId;
  }

  public int getQueryStart() {

    return this.queryStart;
  }

  public int getQueryStop() {

    return this.queryStop;
  }

  public double getPid() {

    return this.pid;
  }

  @SuppressWarnings("unused")
  public double getEvalue() {

    return this.evalue;
  }

  public DnaSequence.Strand getStrand() {

    return this.strand;
  }

  public Collection<VariantMatch> getResistanceVariants() {
    return this.resistanceVariants;
  }

  public void addVariants(final Collection<VariantMatch> variants) {
    this.resistanceVariants.addAll(variants);
  }

  @Override
  public String toString() {

    return "BlastSearchStatistics{" +
        "librarySequenceId='" + this.refId + '\'' +
        ", librarySequenceStart=" + this.refStart +
        ", querySequenceId='" + this.queryId + '\'' +
        ", querySequenceStart=" + this.queryStart +
        ", percentIdentity=" + this.pid +
        ", evalue=" + this.evalue +
        ", strand=" + this.strand +
        ", librarySequenceStop=" + this.refStop +
        ", querySequenceStop=" + this.queryStop +
        ", librarySequenceLength=" + this.refLength +
        '}';
  }

  @SuppressWarnings("unused")
  public boolean isReversed() {
    return this.reversed;
  }
}
