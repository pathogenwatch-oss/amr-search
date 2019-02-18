package net.cgps.wgsa.paarsnp.core.snpar;

import net.cgps.wgsa.paarsnp.core.models.Mechanisms;
import net.cgps.wgsa.paarsnp.core.models.Phenotype;
import net.cgps.wgsa.paarsnp.core.models.PhenotypeEffect;
import net.cgps.wgsa.paarsnp.core.models.ResistanceSet;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class MechanismsTest {

  @Test
  public void addRecords() {
    final Mechanisms mechanisms = new Mechanisms(Collections.emptyList(), Arrays.asList(
        new ResistanceSet("set1",
            Arrays.asList(new Phenotype(
                PhenotypeEffect.RESISTANT,
                Arrays.asList("AAA", "BBB", "CCC"),
                Collections.emptyList()
            )),
            Collections.emptyList())
    ));

    final ResistanceSet update = new ResistanceSet(
        "set1",
        Arrays.asList(new Phenotype(
                PhenotypeEffect.RESISTANT,
                Arrays.asList("AAA", "BBB"),
                Collections.emptyList()
            ),
            new Phenotype(
                PhenotypeEffect.INTERMEDIATE,
                Arrays.asList("CCC"),
                Collections.emptyList()
            )),
        Collections.emptyList()
    );
    mechanisms.addRecords(Collections.singletonMap("set1", update));

    Collection<Phenotype> phenotypes = mechanisms.getSets().get("set1").getPhenotypes();
    Collection<Phenotype> phenotypes2 = update.getPhenotypes();
    Assert.assertEquals(phenotypes, phenotypes2);
  }
}