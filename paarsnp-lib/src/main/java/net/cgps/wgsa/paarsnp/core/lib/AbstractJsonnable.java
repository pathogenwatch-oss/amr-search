package net.cgps.wgsa.paarsnp.core.lib;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

/**
 * The base class for Jsonnable objects. Provides JSON serialisation/deserialisation
 */
public abstract class AbstractJsonnable implements Jsonnable {

  public static <T extends Jsonnable> T fromJson(final String object, final Class<T> objectClass) {
    try {
      return new ObjectMapper().readValue(object.getBytes(), objectClass);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String toString() {
    return this.toPrettyJson();
  }

  /**
   * Method is specific to classes that are expected to be serialised as JSON
   *
   * @param jsonnableObject - an object that conforms to the Jackson requirements for serialisation.
   * @return A pretty representation of the JSON string.
   */
  public static String toPrettyJson(final Jsonnable jsonnableObject) {

    final ObjectMapper mapper = new ObjectMapper();

    // Create a StringWriter to write the JSON string to
    final StringWriter writer = new StringWriter();

    try {
      mapper.writerWithDefaultPrettyPrinter().writeValue(writer, jsonnableObject);
    } catch (final IOException e) {
      // I don't think this can happen since there is no IO...
      LoggerFactory.getLogger(jsonnableObject.getClass()).error("IOException thrown when writing JSON string.", e);
    }

    return writer.toString();
  }

  @JsonIgnore
  @Override
  public final String toJson() {

    return toJson(this);
  }

  @Override
  public final String toPrettyJson() {

    return toPrettyJson(this);
  }

  public static String toJson(final AbstractJsonnable jsonnableObject) {

    final StringWriter writer = new StringWriter();

    try {
      new ObjectMapper().writer().writeValue(writer, jsonnableObject);
    } catch (final IOException e) {
      // I don't think this can happen since there is no IO...
      LoggerFactory.getLogger(jsonnableObject.getClass()).error("IOException thrown when writing JSON string.", e);
    }
    return writer.toString();

  }

  public static <T extends Jsonnable> T fromJsonFile(final File jsonFile, final Class<T> messageClass) {

    try {

      return new ObjectMapper().readValue(jsonFile, messageClass);

    } catch (final IOException | NullPointerException e) {
      LoggerFactory.getLogger(AbstractJsonnable.class).error("Json mapping exception for file {} to type {}", jsonFile.getAbsolutePath(), messageClass);
      LoggerFactory.getLogger(AbstractJsonnable.class).error("Message: ", e);

      throw new RuntimeException(e);
    }
  }
}
