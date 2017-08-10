package net.cgps.wgsa.paarsnp.builder;

import net.cgps.wgsa.paarsnp.core.lib.ResistanceType;
import net.cgps.wgsa.paarsnp.core.lib.SequenceType;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class MutationLine {

  static final String SEP_CHAR = "\t";
  static final String HEADER_START = "type";
  private final static Pattern snpIdPattern = Pattern.compile("^([\\-A-Z]+)([0-9]+)([A-Z\\-]+)$");

  private final String type;
  private final String name;
  private final String setName;
  private final String representativeId;
  private final String originalSequence;
  private final Integer position;
  private final String mutantSequence;
  private final String antibiotics;
  private final String source;
  private final SequenceType sequenceType;
  private final ResistanceType groupEffect;

  public MutationLine(final String type, final String name, final String setName, final String representativeId, final String originalSequence, final Integer position, final String mutantSequence, final String antibiotics, final String source, final SequenceType sequenceType, final ResistanceType groupEffect) {

    this.type = type;
    this.name = name;
    this.setName = setName;
    this.representativeId = representativeId;
    this.originalSequence = originalSequence;
    this.position = position;
    this.mutantSequence = mutantSequence;
    this.antibiotics = antibiotics;
    this.source = source;
    this.sequenceType = sequenceType;
    this.groupEffect = groupEffect;
  }

  /**
   * @param line - TSV line
   * @return The content if it's not a commented out (or blank) line.
   * @throws Exception If line parsing fails.
   */
  public static Optional<MutationLine> parseLine(final String line) throws Exception {

    final String[] data = line.split(SEP_CHAR);

    if (line.startsWith("#")) {
      LoggerFactory.getLogger(MutationLine.class).info("Skipping SNPAR data line {}", line);
      return Optional.empty();
    }

    if ((0 == data.length) || "#".equals(data[0])) {
      LoggerFactory.getLogger(MutationLine.class).info("Skipping SNPAR data line {}", line);
      return Optional.empty();
    }

    final String type = data[0];
    final SequenceType sequenceType = "SAP".equals(type) ? SequenceType.PROTEIN : SequenceType.DNA;
    final String originalSequence;
    final String mutantSequence;
    final int position;

    final String snpName = data[2].trim();
    final String representativeId = data[3].trim();
    // Allow for blank set names (auto-generate the name).
    final String setName = data[1].trim().isEmpty() ? (representativeId + "_" + snpName) : data[1].trim();

    final Matcher matcher = snpIdPattern.matcher(snpName);

    if (!matcher.find()) {
      throw new Exception("Unable to match SAP data: " + snpName);
    }

    originalSequence = matcher.group(1);
    mutantSequence = matcher.group(3);
    position = StringUtils.isNumeric(snpName) ? Integer.valueOf(snpName) : Integer.valueOf(matcher.group(2));

    final String antibiotics = data[4];
    final ResistanceType completeGroupEffect = ResistanceType.valueOf(data[5].toUpperCase());

    return Optional.of(new MutationLine(type, snpName, setName, representativeId, originalSequence, position, mutantSequence, antibiotics, "", sequenceType, completeGroupEffect));
  }

  @SuppressWarnings("unused")
  public String getSetName() {

    return this.setName;
  }

  public String getType() {

    return this.type;
  }

  public String getName() {

    return this.name;
  }

  public String getRepresentativeId() {

    return this.representativeId;
  }

  public String getOriginalSequence() {

    return this.originalSequence;
  }

  public Integer getPosition() {

    return this.position;
  }

  public String getMutantSequence() {

    return this.mutantSequence;
  }

  public String getAntibiotics() {

    return this.antibiotics;
  }

  public String getSource() {

    return this.source;
  }

  public SequenceType getSequenceType() {

    return this.sequenceType;
  }

  public ResistanceType getGroupEffect() {

    return this.groupEffect;
  }

}

