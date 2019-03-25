package net.cgps.wgsa.paarsnp.builder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.cgps.wgsa.paarsnp.core.models.LibraryMetadata;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.function.Function;

public class LibraryConfigReader implements Function<Path, Collection<LibraryMetadata>> {

  @Override
  public Collection<LibraryMetadata> apply(final Path input) {
    final ObjectMapper om = new ObjectMapper();

    try {
      return om.readValue(Paths.get(input.toString(), "libraries.json").toFile(), new TypeReference<Collection<LibraryMetadata>>() {
      });
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }
}
