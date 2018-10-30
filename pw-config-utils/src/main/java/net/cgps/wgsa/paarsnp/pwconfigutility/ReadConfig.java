package net.cgps.wgsa.paarsnp.pwconfigutility;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.function.Function;

public class ReadConfig implements Function<String, ObjectNode> {
  private final ObjectMapper mapper;

  public ReadConfig(final ObjectMapper mapper) {
    this.mapper = mapper;
  }

  public ObjectNode apply(final String filename) {


    try {
      return (ObjectNode) this.mapper.readTree(Files.readAllBytes(Paths.get(filename)));
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }
}
