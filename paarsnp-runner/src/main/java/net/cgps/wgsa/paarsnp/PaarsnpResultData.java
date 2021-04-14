package net.cgps.wgsa.paarsnp;

import net.cgps.wgsa.paarsnp.core.models.LibraryMetadata;
import net.cgps.wgsa.paarsnp.core.models.results.SearchResult;

import java.util.Collection;

public class PaarsnpResultData {

  public final LibraryMetadata version;
  public final String assemblyId;
  public final SearchResult searchResult;
  public final Collection<String> referenceProfile;

  public PaarsnpResultData(final LibraryMetadata version, final String assemblyId, final SearchResult searchResult, final Collection<String> referenceProfile) {
    this.version = version;
    this.assemblyId = assemblyId;
    this.searchResult = searchResult;
    this.referenceProfile = referenceProfile;
  }
}
