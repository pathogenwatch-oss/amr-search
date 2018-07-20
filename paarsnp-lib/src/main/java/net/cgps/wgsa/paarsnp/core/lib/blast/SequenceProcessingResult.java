package net.cgps.wgsa.paarsnp.core.lib.blast;

import net.cgps.wgsa.paarsnp.core.snpar.json.Mutation;

import java.util.Map;

/**
 * Created by cyeats on 28/04/15.
 */
public class SequenceProcessingResult {

  private final Map<Integer, Mutation> mutations;

  public SequenceProcessingResult(final Map<Integer, Mutation> mutations) {


    this.mutations = mutations;
  }

  public Map<Integer, Mutation> getMutations() {

    return this.mutations;
  }
}
