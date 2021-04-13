package net.cgps.wgsa.paarsnp.output;

import net.cgps.wgsa.paarsnp.core.lib.AbstractJsonnable;
import net.cgps.wgsa.paarsnp.core.models.LibraryMetadata;
import net.cgps.wgsa.paarsnp.core.models.results.MatchJson;

import java.util.Collection;
import java.util.Collections;

public class NewOutput extends AbstractJsonnable implements ResultJson  {

  private final String assemblyId;
  private final LibraryMetadata library;
  private final Collection<MatchJson> matches;
  private final Collection<ResistanceProfile> resistanceProfile;

  private NewOutput() {
    this("", null, Collections.emptyList(), null);
  }

  public NewOutput(final String assemblyId, final Collection<ResistanceProfile> resistanceProfile, final Collection<MatchJson> matches, final LibraryMetadata library) {
    this.assemblyId = assemblyId;
    this.resistanceProfile = resistanceProfile;
    this.matches = matches;
    this.library = library;
  }

  public String getAssemblyId() {
    return assemblyId;
  }

  public LibraryMetadata getLibrary() {
    return library;
  }

  public Collection<MatchJson> getMatches() {
    return matches;
  }

  public Collection<ResistanceProfile> getResistanceProfile() {
    return resistanceProfile;
  }
}
