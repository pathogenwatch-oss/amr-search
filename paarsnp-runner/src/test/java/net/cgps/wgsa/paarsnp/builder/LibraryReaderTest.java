package net.cgps.wgsa.paarsnp.builder;

import com.moandjiezana.toml.Toml;
import net.cgps.wgsa.paarsnp.core.lib.PhenotypeEffect;
import net.cgps.wgsa.paarsnp.core.lib.json.AntimicrobialAgent;
import net.cgps.wgsa.paarsnp.core.lib.json.Phenotype;
import net.cgps.wgsa.paarsnp.core.lib.json.ResistanceSet;
import net.cgps.wgsa.paarsnp.core.paar.json.ResistanceGene;
import net.cgps.wgsa.paarsnp.core.snpar.json.SetMember;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

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
        "type = \"Protein\"\n" +
        "sequence = \"ATGGCTAAAATGAGAATATCACCGGAATTGAAAAAACTGATCGAAAAATACCGCTGCGTAAAAGATACGGAAGGAATGTCTCCTGCTAAGGTATATAAGCTGGTGGGAGAAAATGAAAACCTATATTTAAAAATGACGGACAGCCGGTATAAAGGGACCACCTATGATGTGGAACGGGAAAAGGACATGATGCTATGGCTGGAAGGAAAGCTGCCTGTTCCAAAGGTCCTGCACTTTGAACGGCATGATGGCTGGAGCAATCTGCTCATGAGTGAGGCCGATGGCGTCCTTTGCTCGGAAGAGTATGAAGATGAACAAAGCCCTGAAAAGATTATCGAGCTGTATGCGGAGTGCATCAGGCTCTTTCACTCCATCGACATATCGGATTGTCCCTATACGAATAGCTTAGACAGCCGCTTAGCCGAATTGGATTACTTACTGAATAACGATCTGGCCGATGTGGATTGCGAAAACTGGGAAGAAGACACTCCATTTAAAGATCCGCGCGAGCTGTATGATTTTTTAAAGACGGAAAAGCCCGAAGAGGAACTTGTCTTTTCCCACGGCGACCTGGGAGACAGCAACATCTTTGTGAAAGATGGCAAAGTAAGTGGCTTTATTGATCTTGGGAGAAGCGGCAGGGCGGACAAGTGGTATGACATTGCCTTCTGCGTCCGGTCGATCAGGGAGGATATCGGGGAAGAACAGTATGTCGAGCTATTTTTTGACTTACTGGGGATCAAGCCTGATTGGGAGAAAATAAAATATTATATTTTACTGGATGAATTGTTTTAG\"\n")
        .getTables("paar.genes").get(0);
    final ResistanceGene resistanceGene = new ResistanceGene("aph_3prime_III_1_M26832", 80.0f, 80.0f);
    Assert.assertEquals(resistanceGene, LibraryReader.parsePaarGene().apply(geneToml));
  }

  @Test
  public void parsePhenotype() {
    final Toml toml = new Toml().read("phenotypes = [{effect = \"RESISTANT\", profile = [\"TCY\"], modifiers = []}]\n")
        .getTables("phenotypes")
        .get(0);
    final Phenotype phenotype = new Phenotype(PhenotypeEffect.RESISTANT, Collections.singletonList("TCY"), Collections.emptyList());
    Assert.assertEquals(phenotype, LibraryReader.parsePhenotype().apply(toml));
  }

  @Test
  public void parsePaarSet() {
    final Toml toml = new Toml().read("[[paar.sets]]\n" +
        "name = \"tetM_8\"\n" +
        "phenotypes = [{effect = \"RESISTANT\", profile = [\"TCY\"], modifiers = []}]\n" +
        "members = [\"tetM_8_X04388\"]\n")
        .getTables("paar.sets")
        .get(0);
    final ResistanceSet resistanceSet = new ResistanceSet("tetM_8", Collections.singletonList(new Phenotype(PhenotypeEffect.RESISTANT, Collections.singletonList("TCY"), Collections.emptyList())), Collections.singletonList(new SetMember("tetM_8_X04388", Collections.emptyList())));
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
}