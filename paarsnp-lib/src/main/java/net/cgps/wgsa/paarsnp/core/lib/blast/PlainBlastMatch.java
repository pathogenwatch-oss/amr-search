package net.cgps.wgsa.paarsnp.core.lib.blast;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Class representing a single row of results from table format output. Used when mutation locations aren't needed. Also contains a set of utility methods.
 */
public class PlainBlastMatch extends BaseBlastMatch {

  PlainBlastMatch(BlastSearchStatistics blastSearchStatistics, String queryMatchSequence, String referenceMatchSequence) {
    super(blastSearchStatistics, queryMatchSequence, referenceMatchSequence);
  }
}
