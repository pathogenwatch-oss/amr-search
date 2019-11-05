package net.cgps.wgsa.paarsnp.builder;

import net.cgps.wgsa.paarsnp.core.models.variants.implementations.AaRegionInsert;
import net.cgps.wgsa.paarsnp.core.models.variants.implementations.ResistanceMutation;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ParseVariantTest {

  private final Logger logger = LoggerFactory.getLogger(ParseVariantTest.class);

  @Test
  public void applyAminoAcidMutation() {
    final String test1 = "E67I";

    this.logger.info("Testing {}", test1);

    assertEquals(
        new ResistanceMutation(test1, "E", 199, "I", ResistanceMutation.TYPE.AA, 67),
        new ParseVariant(300).apply(test1));
  }

  @Test
  public void applyAAInsertAlternativeForm() {
    final String test1 = "-67I";

    this.logger.info("Testing {}", test1);

    assertEquals(
        new ResistanceMutation(test1, "-", 201, "I", ResistanceMutation.TYPE.AA, 67),
        new ParseVariant(300).apply(test1));
  }

  @Test
  public void applyPromoterSnp() {
    final String test1 = "t-15a";

    this.logger.info("Testing {}", test1);

    final Map.Entry<Integer, Map.Entry<String, String>> parsed = new ParseMutation().apply(test1);

    assertEquals(new ResistanceMutation(test1, "T", 35, "A", ResistanceMutation.TYPE.DNA, -15),
        new ParseVariant(99).apply(test1));
  }

  @Test
  public void applyPromoterInsert() {
    final String test1 = "ins-15a";

    this.logger.info("Testing {}", test1);

    assertEquals(
        new ResistanceMutation(test1, "-", 35, "A", ResistanceMutation.TYPE.DNA, -15),
        new ParseVariant(99).apply(test1));
  }

  @Test
  public void applyPromoterInsertAlternativeForm() {

    this.logger.info("Testing {}", "ins-15a");

    assertEquals(
        new ResistanceMutation("ins-15a", "-", 35, "A", ResistanceMutation.TYPE.DNA, -15),
        new ParseVariant(99).apply("ins-15a"));
  }

  @Test
  public void insertRangeTest() {

    final var encoding = "aa_insert_15-30";
    assertEquals(new AaRegionInsert(encoding, 15, 30), new ParseVariant(99).apply(encoding));
  }
}