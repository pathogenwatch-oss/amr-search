package net.cgps.wgsa.paarsnp.builder;

import com.moandjiezana.toml.Toml;
import net.cgps.wgsa.paarsnp.core.models.results.AntimicrobialAgent;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AntimicrobialDbReader implements Function<Path, Collection<AntimicrobialAgent>> {

  @Override
  public Collection<AntimicrobialAgent> apply(final Path input) {
    final Toml toml = new Toml().read(Paths.get(input.toString(), "antimicrobials.toml").toFile());

    return toml.getTables("antimicrobials")
        .stream()
        .map(AntimicrobialDbReader.parseAntimicrobialAgent())
        .collect(Collectors.toUnmodifiableList());
  }

  public static Function<Toml, AntimicrobialAgent> parseAntimicrobialAgent() {
    return toml -> toml.to(AntimicrobialAgent.class);
  }
}
