package net.cgps.wgsa.paarsnp.output;

import net.cgps.wgsa.paarsnp.core.models.ElementEffect;

public enum DeterminantClass {
  RESISTANCE, CONTRIBUTES, REDUCES, OTHER;

  public static DeterminantClass fromModifierEffect(final ElementEffect elementEffect) {
    switch (elementEffect) {
      case SUPPRESSES:
        return REDUCES;
      case INDUCED:
      case NONE:
        return OTHER;
    }
    return OTHER;
  }
}
