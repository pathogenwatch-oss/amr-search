package net.cgps.wgsa.paarsnp.core.lib.blast;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Class representing a single row of results from table format output. Used when mutation locations aren't needed. Also contains a set of utility methods.
 */
@SuppressWarnings({"ClassWithTooManyFields", "ClassWithTooManyMethods"})
public class BlastMatch {

  private final String querySequenceId;
  private final String librarySequenceId;
  private final int querySequenceLength;
  private final int librarySequenceLength;
  private final double percentIdentity;
  private final int alignmentLength;
  private final int mismatches;
  private final int gapOpens;
  private final int querySequenceStart;
  private final int querySequenceStop;
  private final int librarySequenceStart;
  private final int librarySequenceStop;
  private final double evalue;
  private final double bitScore;
  private final boolean reversed;

  protected BlastMatch(final String querySequenceId, final String librarySequenceId, final int querySequenceLength, final int librarySequenceLength, final double percentIdentity, final Integer alignmentLength, final Integer mismatches, final Integer gapOpens, final Integer querySequenceStart, final Integer querySequenceStop, final Integer subjectStartIn, final Integer subjectEndIn, final double evalue, final double bitscore, final boolean isForward) {

    this.querySequenceId = querySequenceId;
    this.librarySequenceId = librarySequenceId;
    this.querySequenceLength = querySequenceLength;
    this.librarySequenceLength = librarySequenceLength;
    this.percentIdentity = percentIdentity;
    this.alignmentLength = alignmentLength;
    this.mismatches = mismatches;
    this.gapOpens = gapOpens;
    this.querySequenceStart = querySequenceStart;
    this.querySequenceStop = querySequenceStop;
    this.librarySequenceStart = subjectStartIn;
    this.librarySequenceStop = subjectEndIn;
    this.evalue = evalue;
    this.bitScore = bitscore;
    this.reversed = !isForward;
  }

  /**
   * tests if two sets of match coordinates overlap by more than the allowed threshold. If matches are on opposite
   * strands then there is no overlap.
   *
   * @return true if there is a significant overlap.
   */
  public static boolean significantOverlap(final int start1, final int end1, final boolean isForward1, final int start2, final int end2, final boolean isForward2, double overlapThreshold) {

    final Logger logger = LoggerFactory.getLogger(BlastMatch.class);

    // Check that the matches are on the same strand
    if (!sameStrand(isForward1, isForward2)) {
      logger.debug("No overlap - forward1: {} forward2: {}", isForward1, isForward2);
      return false;
    }

    final List<Integer> startStop1 = new ArrayList<>();
    startStop1.add(start1);
    startStop1.add(end1);

    Collections.sort(startStop1);

    final List<Integer> startStop2 = new ArrayList<>();
    startStop2.add(start2);
    startStop2.add(end2);

    Collections.sort(startStop2);

    int overlap = 0;

    if ((startStop1.get(0) <= startStop2.get(1)) && (startStop1.get(1) >= startStop2.get(0))) {
      overlap = startStop1.get(0) > startStop2.get(0) ? (startStop2.get(1) - startStop1.get(0) + 1) : (startStop1
          .get(1) - startStop2
          .get(0) + 1);
    }

    if (0 != overlap) {
      logger.debug("Overlap: {} {}-{} {}-{}", overlap, startStop1.get(0), startStop1.get(1), startStop2.get(0), startStop2.get(1));
    }

    return overlap > overlapThreshold;
  }

  /**
   * Checks if the start-end coordinates indicate that two matches are on the same strand.
   *
   * @param isForward1 - direction of strand 1 (boolean, reversed -> true, reverse -> false)
   * @param isForward2 - direction of strand 2 (boolean, reversed -> true, reverse -> false)
   * @return matches are on the same strand = true
   */
  public static boolean sameStrand(final boolean isForward1, final boolean isForward2) {

    return isForward1 == isForward2;
  }

  @SuppressWarnings("unused")
  public int getQuerySequenceLength() {

    return this.querySequenceLength;
  }

  public int getLibrarySequenceLength() {

    return this.librarySequenceLength;
  }

  @Override
  public String toString() {

    return "BlastMatch{" +
        "querySequenceId='" + this.querySequenceId + '\'' +
        ", librarySequenceId='" + this.librarySequenceId + '\'' +
        ", querySequenceLength=" + this.querySequenceLength +
        ", librarySequenceLength=" + this.librarySequenceLength +
        ", percentIdentity=" + this.percentIdentity +
        ", alignmentLength=" + this.alignmentLength +
        ", mismatches=" + this.mismatches +
        ", gapOpens=" + this.gapOpens +
        ", querySequenceStart=" + this.querySequenceStart +
        ", querySequenceStop=" + this.querySequenceStop +
        ", librarySequenceStart=" + this.librarySequenceStart +
        ", librarySequenceStop=" + this.librarySequenceStop +
        ", evalue=" + this.evalue +
        ", bitScore=" + this.bitScore +
        ", isReversed=" + this.reversed +
        '}';
  }

  @SuppressWarnings("unused")
  public int getAlignmentLength() {

    return this.alignmentLength;
  }

  @SuppressWarnings("unused")
  public double getBitScore() {

    return this.bitScore;
  }

  @SuppressWarnings("unused")
  public int getGapOpens() {

    return this.gapOpens;
  }

  @SuppressWarnings("unused")
  public int getMismatches() {

    return this.mismatches;
  }


  public String getLibrarySequenceId() {

    return this.librarySequenceId;
  }


  public int getLibrarySequenceStart() {

    return this.librarySequenceStart;
  }


  public int getLibrarySequenceStop() {

    return this.librarySequenceStop;
  }


  public String getQuerySequenceId() {

    return this.querySequenceId;
  }


  public int getQuerySequenceStart() {

    return this.querySequenceStart;
  }


  public int getQuerySequenceStop() {

    return this.querySequenceStop;
  }


  public double getPercentIdentity() {

    return this.percentIdentity;
  }


  @SuppressWarnings("unused")
  public double getEvalue() {

    return this.evalue;
  }


  public boolean isReversed() {

    return this.reversed;
  }

  @JsonIgnore
  public final int getSubjectMatchLength() {

    return calculateSubjectMatchLength(this.getLibrarySequenceStart(), this.getLibrarySequenceStop());
  }

  private static int calculateSubjectMatchLength(final int coordinateA, final int coordinateB) {

    final List<Integer> startStop = new ArrayList<>(2);
    startStop.add(coordinateA);
    startStop.add(coordinateB);

    // To allow for reversed matches.
    Collections.sort(startStop);

    return (startStop.get(1) - startStop.get(0)) + 1;
  }

  // Specifies position of fields.
  public enum FORMAT {
    QSEQID(0),
    SSEQID(1),
    QLEN(2),
    SLEN(3),
    PIDENT(4),
    LENGTH(5),
    MISMATCH(6),
    GAPOPEN(7),
    QSTART(8),
    QEND(9),
    SSTART(10),
    SEND(11),
    EVALUE(12),
    BITSCORE(13),
    SSTRAND(14);
    public static final int FIELD_COUNT = 15;
    private final int column;

    FORMAT(final int index) {

      this.column = index;
    }

    public int index() {

      return this.column;
    }
  }

}
