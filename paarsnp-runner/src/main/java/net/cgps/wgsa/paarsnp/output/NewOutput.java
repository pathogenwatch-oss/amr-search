package net.cgps.wgsa.paarsnp.output;

import net.cgps.wgsa.paarsnp.core.lib.AbstractJsonnable;
import net.cgps.wgsa.paarsnp.core.models.LibraryMetadata;

import java.util.Collection;
import java.util.Collections;

public class NewOutput extends AbstractJsonnable {

  private final String assemblyId;
  private final DeterminantsProfile allDeterminants;
  private final LibraryMetadata library;
  private final Collection<Match> matches;
  private final ResistanceProfile resistanceProfile;

  private NewOutput() {
    this("", null, null, Collections.emptyList(), null);
  }

  public NewOutput(final String assemblyId, final ResistanceProfile resistanceProfile, final DeterminantsProfile allDeterminants, final Collection<Match> matches, final LibraryMetadata library) {
    this.assemblyId = assemblyId;
    this.resistanceProfile = resistanceProfile;
    this.allDeterminants = allDeterminants;
    this.matches = matches;
    this.library = library;
  }

  private String getAssemblyId() {
    return assemblyId;
  }

  private DeterminantsProfile getAllDeterminants() {
    return allDeterminants;
  }

  private LibraryMetadata getLibrary() {
    return library;
  }

  private Collection<Match> getMatches() {
    return matches;
  }

  private ResistanceProfile getResistanceProfile() {
    return resistanceProfile;
  }

}
