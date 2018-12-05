package net.cgps.wgsa.paarsnp.core.models.variants;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import net.cgps.wgsa.paarsnp.core.models.variants.implementations.Frameshift;
import net.cgps.wgsa.paarsnp.core.models.variants.implementations.PrematureStop;
import net.cgps.wgsa.paarsnp.core.models.variants.implementations.ResistanceMutation;

import java.io.IOException;

public class VariantJsonDeserializer extends JsonDeserializer {

  @Override
  public Object deserialize(final JsonParser p, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
    ObjectMapper mapper = (ObjectMapper) p.getCodec();
    ObjectNode root = mapper.readTree(p);

    if (root.has("type")) {
      return mapper.readValue(root.toString(), ResistanceMutation.class);
    } else if ("truncated".equals(root.get("name").asText())) {
      return mapper.readValue(root.toString(), PrematureStop.class);
    } else {
      return mapper.readValue(root.toString(), Frameshift.class);
    }
  }
}
