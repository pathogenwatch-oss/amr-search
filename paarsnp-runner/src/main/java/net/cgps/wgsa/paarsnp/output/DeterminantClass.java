package net.cgps.wgsa.paarsnp.output;

import net.cgps.wgsa.paarsnp.core.models.ElementEffect;

public enum DeterminantClass {
  RESISTANCE, CONTRIBUTES, REDUCES, INDUCED, OTHER;

  public static DeterminantClass fromModifierEffect(final ElementEffect elementEffect) {
    switch (elementEffect) {
      case SUPPRESSES:
        return REDUCES;
      case INDUCED:
        return INDUCED;
      case NONE:
        return OTHER;
    }
    return OTHER;
  }
}
