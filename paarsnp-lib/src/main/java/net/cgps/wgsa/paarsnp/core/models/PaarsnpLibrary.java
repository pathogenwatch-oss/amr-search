package net.cgps.wgsa.paarsnp.core.models;

import net.cgps.wgsa.paarsnp.core.lib.AbstractJsonnable;
import net.cgps.wgsa.paarsnp.core.models.results.AntimicrobialAgent;

import java.util.ArrayList;
import java.util.List;

public class PaarsnpLibrary extends AbstractJsonnable {

  private final String label;
  private final List<AntimicrobialAgent> antimicrobials;
  private final Mechanisms mechanisms;

  @SuppressWarnings("unused")
  private PaarsnpLibrary() {
    this("");
  }

  public PaarsnpLibrary(final String label) {
    this(label, new ArrayList<>(50), new Mechanisms());
  }

  public PaarsnpLibrary(final String label, final List<AntimicrobialAgent> antimicrobials, final Mechanisms mechanisms) {
    this.label = label;
    this.antimicrobials = antimicrobials;
    this.mechanisms = mechanisms;
  }

  public Mechanisms getMechanisms() {
    return this.mechanisms;
  }

  public String getLabel() {
    return this.label;
  }

  public List<AntimicrobialAgent> getAntimicrobials() {
    return this.antimicrobials;
  }
}
