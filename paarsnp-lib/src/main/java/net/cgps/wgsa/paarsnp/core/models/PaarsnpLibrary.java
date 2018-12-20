package net.cgps.wgsa.paarsnp.core.models;

import net.cgps.wgsa.paarsnp.core.lib.AbstractJsonnable;
import net.cgps.wgsa.paarsnp.core.models.results.AntimicrobialAgent;

import java.util.ArrayList;
import java.util.List;

public class PaarsnpLibrary extends AbstractJsonnable {

  private final String label;
  private final List<AntimicrobialAgent> antimicrobials;
  private final Paar paar;
  private final Snpar snpar;

  @SuppressWarnings("unused")
  private PaarsnpLibrary() {
    this("");
  }

  public PaarsnpLibrary(final String label) {
    this(label, new ArrayList<>(50), new Paar(), new Snpar());
  }

  public PaarsnpLibrary(final String label, final List<AntimicrobialAgent> antimicrobials, final Paar paar, final Snpar snpar) {
    this.label = label;
    this.antimicrobials = antimicrobials;
    this.paar = paar;
    this.snpar = snpar;
  }

  public Paar getPaar() {
    return this.paar;
  }

  public Snpar getSnpar() {
    return this.snpar;
  }

  public String getLabel() {
    return this.label;
  }

  public List<AntimicrobialAgent> getAntimicrobials() {
    return this.antimicrobials;
  }
}
