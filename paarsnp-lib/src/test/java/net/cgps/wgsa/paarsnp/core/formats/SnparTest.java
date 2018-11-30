package net.cgps.wgsa.paarsnp.core.formats;

import net.cgps.wgsa.paarsnp.core.lib.PhenotypeEffect;
import net.cgps.wgsa.paarsnp.core.lib.json.Phenotype;
import net.cgps.wgsa.paarsnp.core.lib.json.ResistanceSet;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.junit.Assert.*;

public class SnparTest {

  @Test
  public void addRecords() {
    final Snpar snpar = new Snpar(Collections.emptyList(), Arrays.asList(
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
    snpar.addRecords(Collections.singletonMap("set1", update));

    Collection<Phenotype> phenotypes = snpar.getSets().get("set1").getPhenotypes();
    Collection<Phenotype> phenotypes2 = update.getPhenotypes();
    Assert.assertEquals(phenotypes, phenotypes2);
  }
}