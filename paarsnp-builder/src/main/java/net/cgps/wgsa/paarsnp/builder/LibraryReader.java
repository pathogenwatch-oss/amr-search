package net.cgps.wgsa.paarsnp.builder;

import com.moandjiezana.toml.Toml;
import net.cgps.wgsa.paarsnp.core.PaarsnpLibrary;
import net.cgps.wgsa.paarsnp.core.lib.SequenceType;
import net.cgps.wgsa.paarsnp.core.lib.json.AntimicrobialAgent;
import net.cgps.wgsa.paarsnp.core.lib.json.Phenotype;
import net.cgps.wgsa.paarsnp.core.lib.json.ResistanceSet;
import net.cgps.wgsa.paarsnp.core.paar.json.Paar;
import net.cgps.wgsa.paarsnp.core.paar.json.ResistanceGene;
import net.cgps.wgsa.paarsnp.core.snpar.json.SetMember;
import net.cgps.wgsa.paarsnp.core.snpar.json.Snpar;
import net.cgps.wgsa.paarsnp.core.snpar.json.SnparReferenceSequence;

import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class LibraryReader implements Function<Path, LibraryReader.PaarsnpLibraryAndSequences> {

  @Override
  public PaarsnpLibraryAndSequences apply(final Path path) {

    final Toml toml = new Toml().read(path.toFile());

    // Read the antibiotics
    final List<AntimicrobialAgent> antimicrobials = toml.getTables("antimicrobials")
        .stream()
        .map(LibraryReader.parseAntimicrobialAgent())
        .collect(Collectors.toList());

    // Construct the paar library

    final Map<String, String> paarSequences = new HashMap<>(500);

    final Paar paar = new Paar(
        toml.getTables("paar.genes")
            .stream()
            .peek(geneToml -> paarSequences.put(geneToml.getString("name"), geneToml.getString("sequence")))
            .map(LibraryReader.parsePaarGene())
            .collect(Collectors.toList()),
        toml.getTables("paar.sets")
            .stream()
            .map(LibraryReader.parsePaarSet())
            .collect(Collectors.toList()));

    final Map<String, String> snparSequences = new HashMap<>(500);

    final Snpar snpar = new Snpar(
        toml.getTables("snpar.genes")
            .stream()
            .peek(geneToml -> snparSequences.put(geneToml.getString("name"), geneToml.getString("sequence")))
            .map(LibraryReader.parseSnparGene())
            .collect(Collectors.toList()),
        toml.getTables("snpar.sets")
            .stream()
            .map(LibraryReader.parseSnparSet())
            .collect(Collectors.toList()));

    return new PaarsnpLibraryAndSequences(paarSequences, snparSequences, new PaarsnpLibrary(toml.getLong("label").toString(), antimicrobials, paar, snpar));
  }

  public static Function<Toml, ResistanceSet> parseSnparSet() {
    return toml -> {
      final List<Phenotype> phenotypes = toml.getTables("phenotypes")
          .stream()
          .map(LibraryReader.parsePhenotype())
          .collect(Collectors.toList());

      return new ResistanceSet(
          toml.getString("name"),
          phenotypes,
          toml.getTables("members")
              .stream()
              .map(LibraryReader.parseSnparMember())
              .collect(Collectors.toList()));
    };
  }

  public static Function<Toml, SetMember> parseSnparMember() {
    return toml -> toml.to(SetMember.class);
  }

  public static Function<Toml, ResistanceSet> parsePaarSet() {
    return toml -> {
      final List<Phenotype> phenotypes = toml.getTables("phenotypes")
          .stream()
          .map(LibraryReader.parsePhenotype())
          .collect(Collectors.toList());

      return new ResistanceSet(
          toml.getString("name"),
          phenotypes,
          toml.<String>getList("members")
              .stream()
              .map(parsePaarMember())
              .collect(Collectors.toList()));
    };
  }

  public static Function<Toml, Phenotype> parsePhenotype() {
    return toml -> toml.to(Phenotype.class);
  }

  public static Function<Toml, AntimicrobialAgent> parseAntimicrobialAgent() {
    return toml -> toml.to(AntimicrobialAgent.class);
  }

  public static Function<Toml, ResistanceGene> parsePaarGene() {
    return geneToml -> new ResistanceGene(geneToml.getString("name"), geneToml.getDouble("coverage").floatValue(), geneToml.getDouble("pid").floatValue());
  }

  public static Function<String, SetMember> parsePaarMember() {
    return memberName -> new SetMember(memberName, Collections.emptyList());
  }

  public static Function<Toml, SnparReferenceSequence> parseSnparGene() {
    return toml -> new SnparReferenceSequence(
        toml.getString("name"),
        SequenceType.valueOf(toml.getString("type").toUpperCase()),
        toml.getDouble("pid").floatValue(),
        toml.getDouble("coverage").floatValue(),
        toml.getList("variants")
    );
  }

  public static class PaarsnpLibraryAndSequences {
    private final Map<String, String> paarSequences;
    private final Map<String, String> snparSequences;
    private final PaarsnpLibrary paarsnpLibrary;

    public PaarsnpLibraryAndSequences(final Map<String, String> paarSequences, final Map<String, String> snparSequences, final PaarsnpLibrary paarsnpLibrary) {
      this.paarSequences = paarSequences;
      this.snparSequences = snparSequences;
      this.paarsnpLibrary = paarsnpLibrary;
    }

    public Map<String, String> getPaarSequences() {
      return this.paarSequences;
    }

    public Map<String, String> getSnparSequences() {
      return this.snparSequences;
    }

    public PaarsnpLibrary getPaarsnpLibrary() {
      return this.paarsnpLibrary;
    }

  }

}
