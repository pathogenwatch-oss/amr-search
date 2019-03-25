package net.cgps.wgsa.paarsnp;

import com.fasterxml.jackson.annotation.JsonInclude;
import net.cgps.wgsa.paarsnp.core.lib.AbstractJsonnable;
import net.cgps.wgsa.paarsnp.core.models.LibraryMetadata;
import net.cgps.wgsa.paarsnp.core.models.results.OldStyleAntibioticProfile;

import java.util.*;

public class PathogenWatchFormat extends AbstractJsonnable implements Result {

  private final String assemblyId;
  private final Collection<String> paarElementIds;
  private final Collection<String> snparElementIds;
  private final Collection<OldStyleAntibioticProfile> resistanceProfile;
  private final Collection<CdsJson> matches;
  private final Collection<VariantJson> variantMatches;
  private final LibraryMetadata library;

  @SuppressWarnings("unused")
  private PathogenWatchFormat() {

    this("", null, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
  }

  public PathogenWatchFormat(final String assemblyId, final LibraryMetadata version, final Collection<String> snparResult, final Collection<String> paarResult, final Collection<OldStyleAntibioticProfile> resistanceProfile, final Collection<CdsJson> matches, final Collection<VariantJson> variantMatches) {

    this.assemblyId = assemblyId;
    this.library = version;
    this.snparElementIds = snparResult;
    this.paarElementIds = paarResult;
    this.resistanceProfile = resistanceProfile;
    this.matches = matches;
    this.variantMatches = variantMatches;
  }

  public Collection<OldStyleAntibioticProfile> getResistanceProfile() {

    return this.resistanceProfile;
  }

  public String getAssemblyId() {

    return this.assemblyId;
  }

  public Collection<String> getPaarElementIds() {
    return this.paarElementIds;
  }

  public Collection<String> getSnparElementIds() {
    return this.snparElementIds;
  }

  public Collection<CdsJson> getMatches() {
    return this.matches;
  }

  public Collection<VariantJson> getVariantMatches() {
    return this.variantMatches;
  }

  public LibraryMetadata getLibrary() {
    return this.library;
  }

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class CdsJson extends AbstractJsonnable {
    private final String id;
    private final String source;
    private final String type = "CDS";
    private final boolean reversed;
    private final double evalue;
    private final double percentIdentity;
    private final CdsLocation library;
    private final CdsLocation query;
    private final Collection<String> agents;

    private CdsJson() {
      this("", "", false, 0.0, 0.0, null, null, Collections.emptyList());
    }

    public CdsJson(final String id, final String source, final boolean reversed, final double evalue, final double percentIdentity, final CdsLocation library, final CdsLocation query, final List<String> agents) {
      super();
      this.id = id;
      this.source = source;
      this.reversed = reversed;
      this.evalue = evalue;
      this.percentIdentity = percentIdentity;
      this.library = library;
      this.query = query;
      this.agents = agents;
    }

    public String getSource() {
      return this.source;
    }

    public String getType() {
      return this.type;
    }

    public boolean isReversed() {
      return this.reversed;
    }

    public double getEvalue() {
      return this.evalue;
    }

    public String getId() {
      return this.id;
    }

    public CdsLocation getQuery() {
      return this.query;
    }

    public CdsLocation getLibrary() {
      return this.library;
    }

    public double getPercentIdentity() {
      return this.percentIdentity;
    }

    public Collection<String> getAgents() {
      return this.agents;
    }
  }

  public static class CdsLocation extends AbstractJsonnable {
    private final int start;
    private final int stop;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final Integer length;
    private final String id;

    private CdsLocation() {
      this(0, 0, 0, "");
    }

    public CdsLocation(final int start, final int stop, final Integer length, final String id) {
      this.start = start;
      this.stop = stop;
      this.length = length;
      this.id = id;
    }

    public int getStart() {
      return this.start;
    }

    public int getStop() {
      return this.stop;
    }

    public Integer getLength() {
      return this.length;
    }

    public String getId() {
      return this.id;
    }
  }

  public static class VariantJson extends AbstractJsonnable {
    private final Collection<String> agents;
    private final String id;
    private final String source = "WGSA_SNPAR";
    private final String type = "point_mutation";
    private final boolean reversed;
    private final int queryLocation;
    private final int referenceLocation;
    private final String name;
    private final int libraryStart;

    private VariantJson() {
      this(Collections.emptyList(), "", false, 0, 0, "", 0);
    }

    public VariantJson(final Collection<String> agents, final String id, final boolean reversed, final int queryLocation, final int referenceLocation, final String name, final int libraryStart) {
      this.agents = agents;
      this.id = id;
      this.reversed = reversed;
      this.queryLocation = queryLocation;
      this.referenceLocation = referenceLocation;
      this.name = name;
      this.libraryStart = libraryStart;
    }

    public Collection<String> getAgents() {
      return this.agents;
    }

    public String getId() {
      return this.id;
    }

    public String getSource() {
      return this.source;
    }

    public boolean isReversed() {
      return this.reversed;
    }

    public int getQueryLocation() {
      return this.queryLocation;
    }

    public int getReferenceLocation() {
      return this.referenceLocation;
    }

    public String getName() {
      return this.name;
    }

    public int getLibraryStart() {
      return this.libraryStart;
    }

    @Override
    public boolean equals(final Object o) {
      if (this == o) return true;
      if (o == null || this.getClass() != o.getClass()) return false;
      final VariantJson that = (VariantJson) o;
      return this.reversed == that.reversed &&
          this.queryLocation == that.queryLocation &&
          this.referenceLocation == that.referenceLocation &&
          this.libraryStart == that.libraryStart &&
          new HashSet<>(this.agents).equals(new HashSet<>(that.agents)) &&
          Objects.equals(this.id, that.id) &&
          Objects.equals(this.source, that.source) &&
          Objects.equals(this.name, that.name);
    }

    @Override
    public int hashCode() {
      return Objects.hash(this.agents, this.id, this.source, this.reversed, this.queryLocation, this.referenceLocation, this.name, this.libraryStart);
    }

    public String getType() {
      return this.type;
    }
  }
}
