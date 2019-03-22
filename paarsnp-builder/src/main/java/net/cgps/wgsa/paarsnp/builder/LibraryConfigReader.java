package net.cgps.wgsa.paarsnp.builder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.cgps.wgsa.paarsnp.core.models.LibraryVersion;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.function.Function;

public class LibraryConfigReader implements Function<Path, Collection<LibraryVersion>> {

  @Override
  public Collection<LibraryVersion> apply(final Path input) {
    final ObjectMapper om = new ObjectMapper();

    try {
      return om.readValue(Paths.get(input.toString(), "libraries.json").toFile(), new TypeReference<Collection<LibraryVersion>>() {
      });
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }
}
