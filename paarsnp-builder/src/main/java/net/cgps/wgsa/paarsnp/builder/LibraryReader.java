package net.cgps.wgsa.paarsnp.builder;

import com.moandjiezana.toml.Toml;
import net.cgps.wgsa.paarsnp.core.PaarsnpLibrary;
import net.cgps.wgsa.paarsnp.core.lib.SequenceType;
import net.cgps.wgsa.paarsnp.core.lib.json.AntimicrobialAgent;
import net.cgps.wgsa.paarsnp.core.lib.json.Phenotype;
import net.cgps.wgsa.paarsnp.core.lib.json.ResistanceSet;
import net.cgps.wgsa.paarsnp.core.paar.json.ResistanceGene;
import net.cgps.wgsa.paarsnp.core.snpar.json.SetMember;
import net.cgps.wgsa.paarsnp.core.snpar.json.SnparReferenceSequence;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class LibraryReader implements Function<Path, LibraryReader.PaarsnpLibraryAndSequences> {

  @Override
  public PaarsnpLibraryAndSequences apply(final Path path) {

    final Toml toml = new Toml().read(path.toFile());

    final String label = toml.getString("label");

    // And down the recursive rabbit hole we go ...
    final PaarsnpLibraryAndSequences baseLibrary = this.readParents(label, toml.getList("extends", new ArrayList<>()), path.getParent());

    // Read the antibiotics
    final List<AntimicrobialAgent> antimicrobials = Optional.ofNullable(toml.getTables("antimicrobials"))
        .orElse(Collections.emptyList())
        .stream()
        .map(LibraryReader.parseAntimicrobialAgent())
        .collect(Collectors.toList());

    baseLibrary.addAntibiotics(antimicrobials);

    // Construct the paar library
    baseLibrary.getPaarsnpLibrary().getPaar().addResistanceGenes(
        Optional.ofNullable(toml.getTables("paar.genes"))
            .orElse(Collections.emptyList())
            .stream()
            .peek(geneToml -> baseLibrary.getPaarSequences().put(geneToml.getString("name"), geneToml.getString("sequence")))
            .map(LibraryReader.parsePaarGene())
            .collect(Collectors.toMap(ResistanceGene::getFamilyName, Function.identity())));

    baseLibrary.getPaarsnpLibrary().getPaar().addResistanceSets(
        Optional.ofNullable(toml.getTables("paar.sets"))
            .orElse(Collections.emptyList())
            .stream()
            .map(LibraryReader.parsePaarSet())
            .collect(Collectors.toMap(ResistanceSet::getName, Function.identity())));

    baseLibrary.getPaarsnpLibrary().getSnpar().addResistanceGenes(
        Optional.ofNullable(toml.getTables("snpar.genes"))
            .orElse(Collections.emptyList())
            .stream()
            .peek(geneToml -> baseLibrary.getSnparSequences().put(geneToml.getString("name"), geneToml.getString("sequence")))
            .map(LibraryReader.parseSnparGene())
            .collect(Collectors.toMap(SnparReferenceSequence::getName, Function.identity()))
    );

    baseLibrary.getPaarsnpLibrary().getSnpar().addResistanceSets(
        Optional.ofNullable(toml.getTables("snpar.sets"))
            .orElse(Collections.emptyList())
            .stream()
            .map(LibraryReader.parseSnparSet())
            .collect(Collectors.toMap(ResistanceSet::getName, Function.identity()))
    );

    return baseLibrary;
  }

  private PaarsnpLibraryAndSequences readParents(final String label, final List<String> parentLibrary, final Path libraryDirectory) {

    return parentLibrary.stream()
        .map(libName -> Paths.get(libraryDirectory.toString(), libName + ".toml"))
        .map(new LibraryReader())
        .collect(new Collector<PaarsnpLibraryAndSequences, PaarsnpLibraryAndSequences, PaarsnpLibraryAndSequences>() {

          @Override
          public Supplier<PaarsnpLibraryAndSequences> supplier() {
            return () -> new PaarsnpLibraryAndSequences(label);
          }

          @Override
          public BiConsumer<PaarsnpLibraryAndSequences, PaarsnpLibraryAndSequences> accumulator() {
            return PaarsnpLibraryAndSequences::merge;
          }

          @Override
          public BinaryOperator<PaarsnpLibraryAndSequences> combiner() {
            // only called on parallel stream, so should try to preserve order.
            return (a, b) -> {
              if (label.equals(a.getPaarsnpLibrary().getLabel()) || (parentLibrary.indexOf(a.getPaarsnpLibrary().getLabel()) < parentLibrary.indexOf(b.getPaarsnpLibrary().getLabel()))) {
                return a.merge(b);
              } else {
                return b.merge(a);
              }
            };
          }

          @Override
          public Function<PaarsnpLibraryAndSequences, PaarsnpLibraryAndSequences> finisher() {
            return Function.identity();
          }

          @Override
          public Set<Characteristics> characteristics() {
            return Collections.singleton(Characteristics.IDENTITY_FINISH);
          }
        });
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

    public PaarsnpLibraryAndSequences(final String label) {
      this(new HashMap<>(), new HashMap<>(), new PaarsnpLibrary(label));
    }

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

    public PaarsnpLibraryAndSequences merge(final PaarsnpLibraryAndSequences that) {

      this.addAntibiotics(that.paarsnpLibrary.getAntimicrobials());
      this.paarSequences.putAll(that.getPaarSequences());
      this.snparSequences.putAll(that.getSnparSequences());
      this.paarsnpLibrary.getPaar().addResistanceGenes(that.paarsnpLibrary.getPaar().getGenes());
      this.paarsnpLibrary.getPaar().addResistanceSets(that.paarsnpLibrary.getPaar().getSets());
      this.paarsnpLibrary.getSnpar().addResistanceGenes(that.paarsnpLibrary.getSnpar().getGenes());
      this.paarsnpLibrary.getSnpar().addResistanceSets(that.paarsnpLibrary.getSnpar().getSets());

      return this;
    }

    public void addAntibiotics(final Collection<AntimicrobialAgent> antimicrobials) {
      // Only add new antibiotics
      this.getPaarsnpLibrary().getAntimicrobials().addAll(
          antimicrobials
              .stream()
              .filter(antimicrobialAgent -> !this.getPaarsnpLibrary().getAntimicrobials().contains(antimicrobialAgent))
              .collect(Collectors.toList()));
    }
  }
}
