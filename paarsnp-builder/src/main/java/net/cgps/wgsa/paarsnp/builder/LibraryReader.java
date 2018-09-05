package net.cgps.wgsa.paarsnp.builder;

import com.moandjiezana.toml.Toml;
import net.cgps.wgsa.paarsnp.core.PaarsnpLibrary;
import net.cgps.wgsa.paarsnp.core.lib.SequenceType;
import net.cgps.wgsa.paarsnp.core.lib.json.AntimicrobialAgent;
import net.cgps.wgsa.paarsnp.core.lib.json.ResistanceSet;
import net.cgps.wgsa.paarsnp.core.paar.json.Paar;
import net.cgps.wgsa.paarsnp.core.paar.json.ResistanceGene;
import net.cgps.wgsa.paarsnp.core.snpar.json.Snpar;
import net.cgps.wgsa.paarsnp.core.snpar.json.SnparMember;
import net.cgps.wgsa.paarsnp.core.snpar.json.SnparReferenceSequence;

import java.nio.file.Path;
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
    final List<AntimicrobialAgent> antimicrobials = toml.getList("antimicrobials");

    // Construct the paar library

    final Map<String, String> paarSequences = new HashMap<>(500);

    final List<ResistanceGene> resistanceGenes = toml.getTables("paar.genes")
        .stream()
        .peek(geneToml -> paarSequences.put(geneToml.getString("name"), geneToml.getString("sequence")))
        .map(geneToml -> new ResistanceGene(geneToml.getString("name"), geneToml.getDouble("coverage").floatValue(), geneToml.getDouble("pid").floatValue()))
        .collect(Collectors.toList());

    final List<ResistanceSet<String>> paarResistanceSets = toml.getList("paar.sets");

    final Paar paar = new Paar(resistanceGenes, paarResistanceSets);
    final Map<String, String> snparSequences = new HashMap<>(500);

    final List<ResistanceSet<SnparMember>> snparSets = toml.getList("snpar.sets");

    final List<SnparReferenceSequence> snparGenes = toml.getTables("snpar.genes")
        .stream()
        .peek(geneToml -> snparSequences.put(geneToml.getString("name"), geneToml.getString("sequence")))
        .map(geneToml -> new SnparReferenceSequence(geneToml.getString("name"), SequenceType.valueOf(geneToml.getString("type")), geneToml.getDouble("pid").floatValue(), geneToml.getDouble("coverage").floatValue(), geneToml.getList("variants")))
        .collect(Collectors.toList());

    final Snpar snpar = new Snpar(snparGenes, snparSets);
    return new PaarsnpLibraryAndSequences(paarSequences, snparSequences, new PaarsnpLibrary(toml.getString("label"), antimicrobials, paar, snpar));
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
