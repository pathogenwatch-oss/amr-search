package net.cgps.wgsa.paarsnp.builder;

import net.cgps.wgsa.paarsnp.core.models.variants.implementations.AaRegionInsert;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ParseVariantTest {

  private final Logger logger = LoggerFactory.getLogger(ParseVariantTest.class);

  @Test
  public void applyAminoAcidMutation() {
    final String test1 = "E67I";

    this.logger.info("Testing {}", test1);

    assertTrue(this.compare(this.generator('E', 'I', 67), new ParseMutation().apply(test1)));
  }

  private boolean compare(final Map.Entry<Integer, Map.Entry<String, String>> reference, final Map.Entry<Integer, Map.Entry<String, String>> test) {
    return reference.getKey().equals(test.getKey())
        && reference.getValue().getKey().equals(test.getValue().getKey())
        && reference.getValue().getValue().equals(test.getValue().getValue());
  }

  private Map.Entry<Integer, Map.Entry<String, String>> generator(final char a, final char b, final Integer position) {
    return this.generator(Character.toString(a), Character.toString(b), position);
  }

  private Map.Entry<Integer, Map.Entry<String, String>> generator(final String a, final String b, final Integer position) {
    return new ImmutablePair<>(position, new ImmutablePair<>(a, b));
  }

  @Test
  public void applyAAInsert() {
    final String test1 = "ins67I";

    this.logger.info("Testing {}", test1);

    assertTrue(this.compare(this.generator('-', 'I', 67), new ParseMutation().apply(test1)));
  }

  @Test
  public void applyAAInsertAlternativeForm() {
    final String test1 = "-67I";

    this.logger.info("Testing {}", test1);

    assertTrue(this.compare(this.generator('-', 'I', 67), new ParseMutation().apply(test1)));
  }

  @Test
  public void applyMultiAAInsert() {
    final String test1 = "ins67IE";

    this.logger.info("Testing {}", test1);

    assertTrue(this.compare(this.generator("--", "IE", 67), new ParseMutation().apply(test1)));
  }

  @Test
  public void applyAADeletion() {
    final String test1 = "E67del";

    this.logger.info("Testing {}", test1);

    assertTrue(this.compare(this.generator('E', '-', 67), new ParseMutation().apply(test1)));
  }

  @Test
  public void applyAADeletionAlternativeForm() {
    final String test1 = "E67-";

    this.logger.info("Testing {}", test1);

    assertTrue(this.compare(this.generator('E', '-', 67), new ParseMutation().apply(test1)));
  }

  @Test
  public void applyMultiAAInsertAlternativeForm() {
    final String test1 = "-67IE";

    this.logger.info("Testing {}", test1);

    assertTrue(this.compare(this.generator("--", "IE", 67), new ParseMutation().apply(test1)));
  }

  @Test
  public void applyPromoterSnp() {
    final String test1 = "t-15a";

    this.logger.info("Testing {}", test1);

    final Map.Entry<Integer, Map.Entry<String, String>> parsed = new ParseMutation().apply(test1);

    assertTrue(this.compare(this.generator('t', 'a', -15), parsed));
  }

  @Test
  public void applyPromoterInsert() {
    final String test1 = "--15a";

    this.logger.info("Testing {}", test1);

    assertTrue(this.compare(this.generator('-', 'a', -15), new ParseMutation().apply(test1)));
  }

  @Test
  public void applyPromoterInsertAlternativeForm() {
    final String test1 = "ins-15a";

    this.logger.info("Testing {}", test1);

    assertTrue(this.compare(this.generator('-', 'a', -15), new ParseMutation().apply(test1)));
  }

  @Test
  public void insertRangeTest() {
    assertEquals(new AaRegionInsert("test", 15, 30));
  }
}