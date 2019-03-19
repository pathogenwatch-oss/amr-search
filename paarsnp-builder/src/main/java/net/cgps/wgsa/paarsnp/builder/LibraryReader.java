package net.cgps.wgsa.paarsnp.builder;

import com.moandjiezana.toml.Toml;
import net.cgps.wgsa.paarsnp.core.models.*;
import net.cgps.wgsa.paarsnp.core.models.results.AntimicrobialAgent;
import net.cgps.wgsa.paarsnp.core.models.results.Modifier;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class LibraryReader implements Function<Path, LibraryReader.LibraryDataAndSequences> {

  private static final Logger logger = LoggerFactory.getLogger(LibraryReader.class);
  private final Map<String, AntimicrobialAgent> antimicrobialDb;
  private final List<AntimicrobialAgent> selectedList;

  public LibraryReader(final Map<String, AntimicrobialAgent> antimicrobialDb) {
    this(antimicrobialDb, new ArrayList<>(50));
  }

  public LibraryReader(final Map<String, AntimicrobialAgent> antimicrobialDb, final List<AntimicrobialAgent> selectedList) {
    this.antimicrobialDb = antimicrobialDb;
    this.selectedList = selectedList;
  }

  @Override
  public LibraryDataAndSequences apply(final Path path) {

    // Initialise the final parts (e.g. not inherited from parent toml)
    final Toml toml = new Toml().read(path.toFile());
    final String label = toml.getString("label");

    // Initialise the AMR selection first time to use as filter for subsequent imports
    if (this.selectedList.isEmpty()) {
      this.selectedList.addAll(toml
          .getList("antimicrobials", new ArrayList<String>())
          .stream()
          .map(this.antimicrobialDb::get)
          .collect(Collectors.toList()));
    }

    // And down the recursive rabbit hole we go ...
    final LibraryDataAndSequences parentInfo = this.readParents(label, toml.getList("extends", new ArrayList<>()), path.getParent());

    final PaarsnpLibrary baseLibrary = parentInfo.getPaarsnpLibrary();
    baseLibrary.addAntimicrobials(this.selectedList);
    final Map<String, String> parentSequences = parentInfo.getSequences();

//    baseLibrary.addAntimicrobials(antimicrobialAgents);
    // Read in the new set of genes
    final Map<String, Pair<String, ReferenceSequence>> newGenes = Optional.ofNullable(toml.getTables("genes"))
        .orElse(Collections.emptyList())
        .stream()
        .map(geneToml -> new ImmutablePair<>(
            ">" + geneToml.getString("name") + "\n" + geneToml.getString("sequence") + "\n",
            LibraryReader.parseGene().apply(geneToml)))
        .map(geneInfo -> new ImmutablePair<>(geneInfo.getRight().getName(), geneInfo))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    // Construct SNPAR
    baseLibrary.addRecords(
        Optional.ofNullable(toml.getTables("mechanisms"))
            .orElse(Collections.emptyList())
            .stream()
            .map(LibraryReader.parseMechanisms())
            .collect(Collectors.toMap(ResistanceSet::getName, Function.identity()))
    );

    // Map the new variants to their sequence ID.
    final Map<String, Collection<String>> sequenceIdToVariants = baseLibrary
        .getSets()
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
    baseLibrary.addResistanceGenes(
        baseLibrary.getSets()
            .values()
            .stream()
            .map(ResistanceSet::getMembers)
            .flatMap(Collection::stream)
            .map(SetMember::getGene)
            .peek(logger::debug)
            .map(gene -> newGenes.containsKey(gene) ? newGenes.get(gene).getValue() : baseLibrary.getGenes().get(gene))
            .peek(snparReferenceSequence -> {
              logger.trace(snparReferenceSequence.getName());
              if (sequenceIdToVariants.containsKey(snparReferenceSequence.getName())) {
                // Need to update all the SNPs
                snparReferenceSequence.addVariants(sequenceIdToVariants.get(snparReferenceSequence.getName()));
              }
            })
            .collect(Collectors.toMap(ReferenceSequence::getName, Function.identity(), (p1, p2) -> p1)));

    // Add the modifiers to the gene list.
    baseLibrary.addResistanceGenes(
        baseLibrary.getSets()
            .values()
            .stream()
            .map(ResistanceSet::getPhenotypes)
            .flatMap(Collection::stream)
            .map(Phenotype::getModifiers)
            .flatMap(Collection::stream)
            .map(Modifier::getName)
            .map(name -> newGenes.get(name).getRight())
            .collect(Collectors.toMap(ReferenceSequence::getName, Function.identity(), (p1, p2) -> p1))
    );

    parentSequences.putAll(newGenes
        .keySet()
        .stream()
        .filter(geneId -> !parentSequences.containsKey(geneId))
        .collect(Collectors.toMap(Function.identity(), geneId -> newGenes.get(geneId).getKey())));

    return new LibraryDataAndSequences(parentSequences, baseLibrary);
  }

  private LibraryDataAndSequences readParents(final String label, final List<String> parentLibrary, final Path libraryDirectory) {

    return parentLibrary
        .stream()
        .map(libName -> Paths.get(libraryDirectory.toString(), libName + ".toml"))
        .map(this)
        .collect(new Collector<LibraryDataAndSequences, LibraryDataAndSequences, LibraryDataAndSequences>() {

          @Override
          public Supplier<LibraryDataAndSequences> supplier() {
            return () -> new LibraryDataAndSequences(new HashMap<>(500), new PaarsnpLibrary(label, LibraryReader.this.selectedList));
          }

          @Override
          public BiConsumer<LibraryDataAndSequences, LibraryDataAndSequences> accumulator() {
            return LibraryDataAndSequences::merge;
          }

          @Override
          public BinaryOperator<LibraryDataAndSequences> combiner() {
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
          public Function<LibraryDataAndSequences, LibraryDataAndSequences> finisher() {
            return Function.identity();
          }

          @Override
          public Set<Characteristics> characteristics() {
            return Collections.singleton(Characteristics.IDENTITY_FINISH);
          }
        });
  }

  public static Function<Toml, ResistanceSet> parseMechanisms() {
    return toml -> ResistanceSet.build(
        Optional.ofNullable(toml.getString("name")),
        toml.getTables("phenotypes")
            .stream()
            .map(LibraryReader.parsePhenotype())
            .collect(Collectors.toList()),
        parseMembers().apply(toml)
    );
  }

  public static Function<Toml, List<SetMember>> parseMembers() {
    return (toml) -> {
      try {
        final List<String> members = toml.getList("members", new ArrayList<>());
        return members
            .stream()
            .peek(member -> logger.trace("{}", member))
            .map(member -> new SetMember(member, Collections.emptyList()))
            .collect(Collectors.toList());
      } catch (final ClassCastException e) {
        return toml.getTables("members")
            .stream()
            .map(LibraryReader.parseMember())
            .collect(Collectors.toList());
      }
    };
  }

  public static Function<Toml, SetMember> parseMember() {
    return toml -> toml.to(SetMember.class);
  }

  public static Function<Toml, Phenotype> parsePhenotype() {
    return toml -> toml.to(Phenotype.class);
  }


  public static Function<Toml, ReferenceSequence> parseGene() {
    return toml -> new ReferenceSequence(
        toml.getString("name"),
        toml.getString("sequence").length(),
        toml.getDouble("pid").floatValue(),
        toml.getDouble("coverage").floatValue()
    );
  }

  public static class LibraryDataAndSequences {
    private final Map<String, String> sequences;
    private final PaarsnpLibrary paarsnpLibrary;

    public LibraryDataAndSequences(final Map<String, String> sequences, final PaarsnpLibrary paarsnpLibrary) {
      this.sequences = sequences;
      this.paarsnpLibrary = paarsnpLibrary;
    }

    public LibraryDataAndSequences merge(final LibraryDataAndSequences that) {
      this.sequences.putAll(that.sequences);
      this.paarsnpLibrary.merge(that.paarsnpLibrary);
      return this;
    }

    public PaarsnpLibrary getPaarsnpLibrary() {
      return this.paarsnpLibrary;
    }

    public Map<String, String> getSequences() {
      return this.sequences;
    }
  }
}
