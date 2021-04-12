package net.cgps.wgsa.paarsnp.output;

import net.cgps.wgsa.paarsnp.core.lib.AbstractJsonnable;
import net.cgps.wgsa.paarsnp.core.models.LibraryMetadata;

import java.util.Collection;
import java.util.Collections;

public class NewOutput extends AbstractJsonnable {

  private final String assemblyId;
  private final LibraryMetadata library;
  private final Collection<Match> matches;
  private final ResistanceProfile resistanceProfile;

  private NewOutput() {
    this("", null, Collections.emptyList(), null);
  }

  public NewOutput(final String assemblyId, final ResistanceProfile resistanceProfile, final Collection<Match> matches, final LibraryMetadata library) {
    this.assemblyId = assemblyId;
    this.resistanceProfile = resistanceProfile;
    this.matches = matches;
    this.library = library;
  }

  private String getAssemblyId() {
    return assemblyId;
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
