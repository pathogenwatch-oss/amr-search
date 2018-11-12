package net.cgps.wgsa.paarsnp.builder;

import com.moandjiezana.toml.Toml;
import net.cgps.wgsa.paarsnp.core.lib.ElementEffect;
import net.cgps.wgsa.paarsnp.core.lib.PhenotypeEffect;
import net.cgps.wgsa.paarsnp.core.lib.json.AntimicrobialAgent;
import net.cgps.wgsa.paarsnp.core.lib.json.Modifier;
import net.cgps.wgsa.paarsnp.core.lib.json.Phenotype;
import net.cgps.wgsa.paarsnp.core.lib.json.ResistanceSet;
import net.cgps.wgsa.paarsnp.core.formats.ReferenceSequence;
import net.cgps.wgsa.paarsnp.core.formats.SetMember;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class LibraryReaderTest {

  @Test
  public void parseAntimicrobialAgent() {
    final Toml agentToml = new Toml().read("antimicrobials = [{key = \"KAN\", type = \"Aminoglycosides\", name = \"Kanamycin\"}]")
        .getTables("antimicrobials").get(0);
    final AntimicrobialAgent agent = new AntimicrobialAgent("KAN", "Aminoglycosides", "Kanamycin");
    Assert.assertEquals(agent, LibraryReader.parseAntimicrobialAgent().apply(agentToml));
  }

  @Test
  public void parsePaarGene() {
    final Toml geneToml = new Toml().read("[[paar.genes]]\n" +
        "name = \"aph_3prime_III_1_M26832\"\n" +
        "pid = 80.0\n" +
        "coverage = 80.0\n" +
        "sequence = \"ATGGCTAAAATGAGAATATCACCGGAATTGAAAAAACTGATCGAAAAATACCGCTGCGTAAAAGATACGGAAGGAATGTCTCCTGCTAAGGTATATAAGCTGGTGGGAGAAAATGAAAACCTATATTTAAAAATGACGGACAGCCGGTATAAAGGGACCACCTATGATGTGGAACGGGAAAAGGACATGATGCTATGGCTGGAAGGAAAGCTGCCTGTTCCAAAGGTCCTGCACTTTGAACGGCATGATGGCTGGAGCAATCTGCTCATGAGTGAGGCCGATGGCGTCCTTTGCTCGGAAGAGTATGAAGATGAACAAAGCCCTGAAAAGATTATCGAGCTGTATGCGGAGTGCATCAGGCTCTTTCACTCCATCGACATATCGGATTGTCCCTATACGAATAGCTTAGACAGCCGCTTAGCCGAATTGGATTACTTACTGAATAACGATCTGGCCGATGTGGATTGCGAAAACTGGGAAGAAGACACTCCATTTAAAGATCCGCGCGAGCTGTATGATTTTTTAAAGACGGAAAAGCCCGAAGAGGAACTTGTCTTTTCCCACGGCGACCTGGGAGACAGCAACATCTTTGTGAAAGATGGCAAAGTAAGTGGCTTTATTGATCTTGGGAGAAGCGGCAGGGCGGACAAGTGGTATGACATTGCCTTCTGCGTCCGGTCGATCAGGGAGGATATCGGGGAAGAACAGTATGTCGAGCTATTTTTTGACTTACTGGGGATCAAGCCTGATTGGGAGAAAATAAAATATTATATTTTACTGGATGAATTGTTTTAG\"\n")
        .getTables("paar.genes").get(0);
    final ReferenceSequence resistanceGene = new ReferenceSequence("aph_3prime_III_1_M26832", 80.0f, 80.0f);
    Assert.assertEquals(resistanceGene, LibraryReader.parseSnparGene().apply(geneToml));
  }

  @Test
  public void parsePhenotype() {

    final List<Phenotype> phenotypes = Arrays.asList(
        new Phenotype(PhenotypeEffect.RESISTANT, Collections.singletonList("ERY"), Collections.emptyList()),
        new Phenotype(PhenotypeEffect.RESISTANT, Collections.singletonList("CLI"), Collections.singletonList(new Modifier("ermC_LP", ElementEffect.MODIFIES_INDUCED)))
    );

    Assert.assertEquals(
        phenotypes,
        new Toml().read("phenotypes = [{effect = \"RESISTANT\", profile = [\"ERY\"], modifiers = []},\n" +
            "              {effect = \"RESISTANT\", profile = [\"CLI\"], modifiers = [{name = \"ermC_LP\", effect = \"MODIFIES_INDUCED\"}]}]\n")
            .getTables("phenotypes")
            .stream()
            .map(LibraryReader.parsePhenotype())
            .collect(Collectors.toList()));
  }

  @Test
  public void parsePaarSet() {
    final Toml toml = new Toml().read("[[paar.sets]]\n" +
        "name = \"tetM_8\"\n" +
        "phenotypes = [{effect = \"RESISTANT\", profile = [\"TCY\"], modifiers = []}]\n" +
        "members = [\"tetM_8_X04388\"]\n")
        .getTables("paar.sets")
        .get(0);
    final ResistanceSet resistanceSet = ResistanceSet.build(Optional.of("tetM_8"), Collections.singletonList(new Phenotype(PhenotypeEffect.RESISTANT, Collections.singletonList("TCY"), Collections.emptyList())), Collections.singletonList(new SetMember("tetM_8_X04388", Collections.emptyList())));
    Assert.assertEquals(resistanceSet, LibraryReader.parsePaarSet().apply(toml));
  }

  @Test
  public void parsePaarMember() {

    final List<SetMember> members = Collections.singletonList(new SetMember("tetM_8_X04388", Collections.emptyList()));
    final List<String> membersTest = new Toml()
        .read("members = [\"tetM_8_X04388\"]\n")
        .getList("members");

    Assert.assertEquals(members, Collections.singletonList(LibraryReader.parsePaarMember().apply(membersTest.get(0))));
  }

  @Test
  public void parseSnparSet() {

    Assert.assertEquals(

        ResistanceSet.build(
            Optional.of("penA_I312M_G545S_V316T"),
            Arrays.asList(
                new Phenotype(PhenotypeEffect.INTERMEDIATE, Arrays.asList("CRO", "PEN"), Collections.emptyList()),
                new Phenotype(PhenotypeEffect.INTERMEDIATE_ADDITIVE, Collections.singletonList("CFM"), Collections.emptyList())),
            Collections.singletonList(new SetMember("penA", Arrays.asList("G545S", "I312M", "V316T")))
        ),

        LibraryReader.parseSnparSet().apply(new Toml()
            .read("[[snpar.sets]]\n" +
                "name = \"penA_I312M_G545S_V316T\"\n" +
                "phenotypes = [{effect = \"INTERMEDIATE\", profile = [\"CRO\",\"PEN\"], modifiers = []},\n" +
                "              {effect = \"INTERMEDIATE_ADDITIVE\", profile = [\"CFM\"], modifiers = []}]\n" +
                "members = [{gene=\"penA\", variants=[\"G545S\",\"I312M\",\"V316T\"]}]\n")
            .getTables("snpar.sets")
            .get(0)
        )
    );
  }

  @Test
  public void parseSnparMember() {

    Assert.assertEquals(
        Collections.singletonList(new SetMember("penA", Arrays.asList("G545S", "I312M", "V316T"))),
        Collections.singletonList(LibraryReader.parseSnparMember()
            .apply(new Toml()
                .read("members = [{gene=\"penA\", variants=[\"G545S\",\"I312M\",\"V316T\"]}]\n")
                .getTables("members")
                .get(0))
        )
    );
  }

  @Test
  public void parseSnparGene() {
    final ReferenceSequence referenceSequence = new ReferenceSequence(
        "penA",
        80.0f,
        60.0f
    );

//    referenceSequence.addVariants(Arrays.asList("A501P", "I312M", "V316P", "P551S", "A501V", "G545S", "G542S", "A311V", "V316T", "T483S"));

    Assert.assertEquals(
        referenceSequence,
        LibraryReader.parseSnparGene().apply(new Toml().read("[[snpar.genes]]\n" +
            "name = \"penA\"\n" +
            "pid = 80.0\n" +
            "coverage = 60.0\n" +
            "sequence = \"ATGTTGATT\"\n")
            .getTables("snpar.genes")
            .get(0))
    );
  }
}