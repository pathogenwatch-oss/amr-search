package net.cgps.wgsa.paarsnp.core.lib.blast;

import com.fasterxml.jackson.annotation.JsonIgnore;
import net.cgps.wgsa.paarsnp.core.snpar.json.Mutation;

import java.util.Collection;

public interface BlastMatch {

  public BlastSearchStatistics getBlastSearchStatistics();

  public String getQueryMatchSequence();

  public String getReferenceMatchSequence();

  @JsonIgnore
  public boolean isComplete();

  public Collection<Mutation> getMutations();

}
