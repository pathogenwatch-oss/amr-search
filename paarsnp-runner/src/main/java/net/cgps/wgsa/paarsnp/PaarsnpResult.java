package net.cgps.wgsa.paarsnp;

import net.cgps.wgsa.paarsnp.core.lib.AbstractJsonnable;
import net.cgps.wgsa.paarsnp.core.models.LibraryMetadata;
import net.cgps.wgsa.paarsnp.core.models.results.AntibioticProfile;
import net.cgps.wgsa.paarsnp.core.models.results.SearchResult;

import java.util.Collection;
import java.util.Collections;

public class PaarsnpResult extends AbstractJsonnable implements Result {

  private final SearchResult searchResult;
  private final Collection<AntibioticProfile> resistanceProfile;
  private final String assemblyId;
  private final LibraryMetadata version;

  @SuppressWarnings("unused")
  private PaarsnpResult() {
    this("", null, Collections.emptyList(), null);
  }

  public PaarsnpResult(final String assemblyId, final SearchResult searchResult, final Collection<AntibioticProfile> resistanceProfile, final LibraryMetadata version) {

    this.assemblyId = assemblyId;
    this.searchResult = searchResult;
    this.resistanceProfile = resistanceProfile;
    this.version = version;
  }

  public Collection<AntibioticProfile> getResistanceProfile() {
    return this.resistanceProfile;
  }

  public String getAssemblyId() {
    return this.assemblyId;
  }


  public SearchResult getSearchResult() {
    return this.searchResult;
  }

  public LibraryMetadata getVersion() {
    return this.version;
  }
}
