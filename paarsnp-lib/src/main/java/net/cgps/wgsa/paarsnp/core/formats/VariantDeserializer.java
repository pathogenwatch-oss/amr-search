package net.cgps.wgsa.paarsnp.core.formats;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;

public class VariantDeserializer extends JsonDeserializer {
  @Override
  public Object deserialize(final JsonParser p, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
    ObjectMapper mapper = (ObjectMapper) p.getCodec();
    ObjectNode root = mapper.readTree(p);
    /*write you own condition*/
    if (root.has("type")) {
      return mapper.readValue(root.toString(), ResistanceMutation.class);
    }
    return mapper.readValue(root.toString(), PrematureStop.class);
  }
}
