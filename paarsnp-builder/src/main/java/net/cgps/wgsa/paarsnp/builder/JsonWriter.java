package net.cgps.wgsa.paarsnp.builder;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.util.function.Function;

public class JsonWriter implements Function<Object, String> {

  private final ObjectMapper mapper;

  public JsonWriter() {
    this.mapper = new ObjectMapper();
  }

  @Override
  public String apply(final Object objectToWrite) {
    // Create a StringWriter to write the JSON string to
    final StringWriter writer = new StringWriter();

    try {
      this.mapper.writerWithDefaultPrettyPrinter().writeValue(writer, objectToWrite);
    } catch (final IOException e) {
      // I don't think this can happen since there is no IO...
      LoggerFactory.getLogger(objectToWrite.getClass()).error("IOException thrown when writing JSON string.", e);
    }

    return writer.toString();
  }
}
