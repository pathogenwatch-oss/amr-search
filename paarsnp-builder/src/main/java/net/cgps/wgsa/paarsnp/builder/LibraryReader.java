package net.cgps.wgsa.paarsnp.builder;

import com.moandjiezana.toml.Toml;
import net.cgps.wgsa.paarsnp.core.formats.PaarsnpLibrary;
import net.cgps.wgsa.paarsnp.core.formats.ReferenceSequence;
import net.cgps.wgsa.paarsnp.core.formats.SetMember;
import net.cgps.wgsa.paarsnp.core.lib.json.AntimicrobialAgent;
import net.cgps.wgsa.paarsnp.core.lib.json.Phenotype;
import net.cgps.wgsa.paarsnp.core.lib.json.ResistanceSet;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

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

    final Map<String, Pair<String, ReferenceSequence>> newGenes = Optional.ofNullable(toml.getTables("genes"))
        .orElse(Collections.emptyList())
        .stream()
        .map(geneToml -> new ImmutablePair<>(
            ">" + geneToml.getString("name") + "\n" + geneToml.getString("sequence") + "\n",
            LibraryReader.parseSnparGene().apply(geneToml)))
        .map(gene -> new ImmutablePair<>(gene.getRight().getName(), gene))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    // Construct the paar library
    baseLibrary.getPaarsnpLibrary().getPaar().addResistanceSets(
        Optional.ofNullable(toml.getTables("paar"))
            .orElse(Collections.emptyList())
            .stream()
            .map(LibraryReader.parsePaarSet())
            .collect(Collectors.toMap(ResistanceSet::getName, Function.identity())));

    // Add the new paar genes
    baseLibrary.getPaarsnpLibrary().getPaar().addResistanceGenes(
        baseLibrary.getPaarsnpLibrary().getPaar().getSets().values()
            .stream()
            .map(ResistanceSet::getMembers)
            .flatMap(Collection::stream)
            .map(SetMember::getGene)
            .map(newGenes::get)
            .map(Optional::ofNullable)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(Map.Entry::getValue)
            .collect(Collectors.toMap(ReferenceSequence::getName, Function.identity(), (p1, p2) -> p1)));

    // Store the sequences for the FASTA
    baseLibrary.getPaarsnpLibrary().getPaar().getGenes().keySet()
        .stream()
        .filter(geneId -> !baseLibrary.getPaarSequences().containsKey(geneId))
        .forEach(key -> baseLibrary.getPaarSequences().put(key, newGenes.get(key).getKey()));

    // Construct SNPAR
    baseLibrary.getPaarsnpLibrary().getSnpar().addResistanceSets(
        Optional.ofNullable(toml.getTables("snpar"))
            .orElse(Collections.emptyList())
            .stream()
            .map(LibraryReader.parseSnparSet())
            .collect(Collectors.toMap(ResistanceSet::getName, Function.identity()))
    );

    final Map<String, Collection<String>> sequenceIdToVariants = baseLibrary.getPaarsnpLibrary().getSnpar().getSets()
        .values()
        .stream()
        .map(ResistanceSet::getMembers)
        .flatMap(Collection::stream)
        .map(member -> new ImmutablePair<String, Collection<String>>(member.getGene(), member.getVariants())
        )
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> {
          a.addAll(b);
          return a;
        }));

    // Add the genes
    // Need to update all the SNPs
    baseLibrary.getPaarsnpLibrary().getSnpar().addResistanceGenes(
        baseLibrary.getPaarsnpLibrary().getSnpar().getSets().values()
            .stream()
            .map(ResistanceSet::getMembers)
            .flatMap(Collection::stream)
            .map(SetMember::getGene)
            .map(newGene -> newGenes.containsKey(newGene) ? newGenes.get(newGene).getValue() : baseLibrary.getPaarsnpLibrary().getSnpar().getGenes().get(newGene))
            .peek(snparReferenceSequence -> snparReferenceSequence.addVariants(sequenceIdToVariants.get(snparReferenceSequence.getName())))
            .collect(Collectors.toMap(ReferenceSequence::getName, Function.identity(), (p1, p2) -> p1)));

    // Store the sequences for the FASTA
    baseLibrary.getPaarsnpLibrary().getSnpar().getGenes().keySet()
        .stream()
        .filter(geneId -> !baseLibrary.getSnparSequences().containsKey(geneId))
        .forEach(key -> baseLibrary.getSnparSequences().put(key, newGenes.get(key).getKey()));

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

      return ResistanceSet.build(
          Optional.ofNullable(toml.getString("name")),
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

      return ResistanceSet.build(
          Optional.ofNullable(toml.getString("name")),
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

  public static Function<String, SetMember> parsePaarMember() {
    return memberName -> new SetMember(memberName, Collections.emptyList());
  }

  public static Function<Toml, ReferenceSequence> parseSnparGene() {
    return toml -> new ReferenceSequence(
        toml.getString("name"),
        toml.getDouble("pid").floatValue(),
        toml.getDouble("coverage").floatValue()
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
      this.paarsnpLibrary.getSnpar().addResistanceGenes(that.paarsnpLibrary.getSnpar().getGenes());

      this.paarsnpLibrary.getPaar().addResistanceSets(that.paarsnpLibrary.getPaar().getSets());
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
