package net.cgps.wgsa.paarsnp.core.lib.blast;

import net.cgps.wgsa.paarsnp.core.models.ResistanceMutationMatch;

public class VariantMatch {

  private final String name;
  private final int refStart;
  private final int refStop;
  private final int queryStart;
  private final int queryStop;

  public VariantMatch(final String name, final int refStart, final int refStop, final int queryStart, final int queryStop) {
    this.name = name;
    this.refStart = refStart;
    this.refStop = refStop;
    this.queryStart = queryStart;
    this.queryStop = queryStop;
  }

  public static VariantMatch build(final ResistanceMutationMatch variant) {
    final var qmin = variant.getLocations().size() == 0 ?
                     variant.getLocations().get(0).getQueryIndex() :
                     Math.min(variant.getLocations().get(0).getQueryIndex(), variant.getLocations().get(variant.getLocations().size() - 1).getQueryIndex());
    final var qmax = variant.getLocations().size() == 0 ?
                     variant.getLocations().get(0).getQueryIndex() :
                     Math.max(variant.getLocations().get(0).getQueryIndex(), variant.getLocations().get(variant.getLocations().size() - 1).getQueryIndex());
    final var rmin = variant.getLocations().size() == 0 ?
                     variant.getLocations().get(0).getReferenceIndex() :
                     Math.min(variant.getLocations().get(0).getReferenceIndex(), variant.getLocations().get(variant.getLocations().size() - 1).getReferenceIndex());
    final var rmax = variant.getLocations().size() == 0 ?
                     variant.getLocations().get(0).getReferenceIndex() :
                     Math.max(variant.getLocations().get(0).getReferenceIndex(), variant.getLocations().get(variant.getLocations().size() - 1).getReferenceIndex());

    return new VariantMatch(variant.getName(), rmin, rmax, qmin, qmax);
  }

  public String getName() {
    return name;
  }

  public int getRefStart() {
    return refStart;
  }

  public int getRefStop() {
    return refStop;
  }

  public int getQueryStart() {
    return queryStart;
  }

  public int getQueryStop() {
    return queryStop;
  }
}
