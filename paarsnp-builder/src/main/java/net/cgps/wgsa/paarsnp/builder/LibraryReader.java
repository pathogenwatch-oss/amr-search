package net.cgps.wgsa.paarsnp.builder;

import com.moandjiezana.toml.Toml;
import net.cgps.wgsa.paarsnp.core.models.PaarsnpLibrary;
import net.cgps.wgsa.paarsnp.core.models.ReferenceSequence;
import net.cgps.wgsa.paarsnp.core.models.SetMember;
import net.cgps.wgsa.paarsnp.core.models.results.AntimicrobialAgent;
import net.cgps.wgsa.paarsnp.core.models.Phenotype;
import net.cgps.wgsa.paarsnp.core.models.ResistanceSet;
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

    // Read in the new set of genes
    final Map<String, Pair<String, ReferenceSequence>> newGenes = Optional.ofNullable(toml.getTables("genes"))
        .orElse(Collections.emptyList())
        .stream()
        .map(geneToml -> new ImmutablePair<>(
            ">" + geneToml.getString("name") + "\n" + geneToml.getString("sequence") + "\n",
            LibraryReader.parseGene().apply(geneToml)))
        .map(geneInfo -> new ImmutablePair<>(geneInfo.getRight().getName(), geneInfo))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    // Construct the paar library
    baseLibrary.getPaarsnpLibrary().getPaar().addRecords(
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
            .filter(nameToSeq -> !baseLibrary.getPaarSequences().containsKey(nameToSeq.getKey()))
            .map(Map.Entry::getValue)
            .collect(Collectors.toMap(ReferenceSequence::getName, Function.identity(), (p1, p2) -> p1)));

    // Store the sequences for the FASTA
    baseLibrary.getPaarsnpLibrary().getPaar().getGenes().keySet()
        .stream()
        .filter(geneId -> !baseLibrary.getPaarSequences().containsKey(geneId))
        .forEach(key -> baseLibrary.getPaarSequences().put(key, newGenes.get(key).getKey()));

    // Construct SNPAR
    baseLibrary.getPaarsnpLibrary().getMechanisms().addRecords(
        Optional.ofNullable(toml.getTables("snpar"))
            .orElse(Collections.emptyList())
            .stream()
            .map(LibraryReader.parseSnparSet())
            .collect(Collectors.toMap(ResistanceSet::getName, Function.identity()))
    );

    final Map<String, Collection<String>> sequenceIdToVariants = baseLibrary.getPaarsnpLibrary().getMechanisms().getSets()
        .values()
        .stream()
        .map(ResistanceSet::getMembers)
        .flatMap(Collection::stream)
        .map(member -> new ImmutablePair<String, Collection<String>>(member.getGene(), new ArrayList<>(member.getVariants()))
        )
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> {
          a.addAll(b);
          return a;
        }));

    // Add the genes
    // Need to update all the SNPs
    baseLibrary.getPaarsnpLibrary().getMechanisms().addResistanceGenes(
        baseLibrary.getPaarsnpLibrary().getMechanisms().getSets().values()
            .stream()
            .map(ResistanceSet::getMembers)
            .flatMap(Collection::stream)
            .map(SetMember::getGene)
            .map(gene -> newGenes.containsKey(gene) ? newGenes.get(gene).getValue() : baseLibrary.getPaarsnpLibrary().getMechanisms().getGenes().get(gene))
            .peek(snparReferenceSequence -> snparReferenceSequence.addVariants(sequenceIdToVariants.get(snparReferenceSequence.getName())))
            .collect(Collectors.toMap(ReferenceSequence::getName, Function.identity(), (p1, p2) -> p1)));

    // Store the sequences for the FASTA
    baseLibrary.getPaarsnpLibrary().getMechanisms().getGenes().keySet()
        .stream()
        .filter(geneId -> !baseLibrary.getSnparSequences().containsKey(geneId))
        .forEach(key -> baseLibrary.getSnparSequences().put(key, newGenes.get(key).getKey()));

    return baseLibrary;
  }

  private PaarsnpLibraryAndSequences readParents(final String label, final List<String> parentLibrary, final Path libraryDirectory) {

    return parentLibrary.stream()
        .map(libName -> Paths.get(libraryDirectory.toString(), libName + ".toml"))
        .map(libPath -> new LibraryReader().apply(libPath))
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

  public static Function<Toml, ReferenceSequence> parseGene() {
    return toml -> new ReferenceSequence(
        toml.getString("name"),
        toml.getString("sequence").length(),
        toml.getDouble("pid").floatValue(),
        toml.getDouble("coverage").floatValue()
    );
  }

  public static class PaarsnpLibraryAndSequences {
    private final Map<String, String> sequences;
    private final PaarsnpLibrary paarsnpLibrary;

    public PaarsnpLibraryAndSequences(final String label) {
      this(new HashMap<>(), new PaarsnpLibrary(label));
    }

    public PaarsnpLibraryAndSequences(final Map<String, String> sequences, final PaarsnpLibrary paarsnpLibrary) {
      this.sequences = sequences;
      this.paarsnpLibrary = paarsnpLibrary;
    }

    public Map<String, String> getSequences() {
      return this.sequences;
    }

    public PaarsnpLibrary getPaarsnpLibrary() {
      return this.paarsnpLibrary;
    }

    public PaarsnpLibraryAndSequences merge(final PaarsnpLibraryAndSequences that) {

      this.addAntibiotics(that.paarsnpLibrary.getAntimicrobials());

      this.sequences.putAll(that.sequences);

      this.paarsnpLibrary.getMechanisms().addResistanceGenes(that.paarsnpLibrary.getMechanisms().getGenes());

      this.paarsnpLibrary.getMechanisms().addRecords(that.paarsnpLibrary.getMechanisms().getSets());

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
