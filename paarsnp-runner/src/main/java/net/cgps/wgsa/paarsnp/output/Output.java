package net.cgps.wgsa.paarsnp.output;

import com.fasterxml.jackson.annotation.JsonInclude;
import net.cgps.wgsa.paarsnp.core.lib.AbstractJsonnable;
import net.cgps.wgsa.paarsnp.core.lib.blast.BlastSearchStatistics;
import net.cgps.wgsa.paarsnp.core.models.LibraryMetadata;
import net.cgps.wgsa.paarsnp.core.models.results.MatchJson;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public class Output extends AbstractJsonnable implements ResultJson  {

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String assemblyId;
  private final LibraryMetadata library;
  private final Set<String> acquired;
  private final Set<String> variants;
  private final Collection<BlastSearchStatistics> matches;
  private final Collection<ResistanceProfile> resistanceProfile;

  private Output() {
    this("", null, null, null, Collections.emptyList(), null);
  }

  public Output(final String assemblyId, final Collection<ResistanceProfile> resistanceProfile, final Set<String> acquired, final Set<String> variants, final Collection<BlastSearchStatistics> matches, final LibraryMetadata library) {
    this.assemblyId = assemblyId;
    this.resistanceProfile = resistanceProfile;
    this.acquired = acquired;
    this.variants = variants;
    this.matches = matches;
    this.library = library;
  }

  public String getAssemblyId() {
    return assemblyId;
  }

  public LibraryMetadata getLibrary() {
    return library;
  }

  public Collection<BlastSearchStatistics> getMatches() {
    return matches;
  }

  public Collection<ResistanceProfile> getResistanceProfile() {
    return resistanceProfile;
  }

  public Set<String> getAcquired() {
    return acquired;
  }

  public Set<String> getVariants() {
    return variants;
  }

  @Override
  public void unsetAssemblyId() {
    this.assemblyId = null;
  }
}
