package net.cgps.wgsa.paarsnp.core.models;

import net.cgps.wgsa.paarsnp.core.lib.AbstractJsonnable;

public class LibraryMetadata extends AbstractJsonnable {
  private final LibraryMetadata.Source source;
  private final String version;
  private final String label;

  private LibraryMetadata() {
    this(Source.PUBLIC, "", "");
  }

  public LibraryMetadata(final Source source, final String version, final String label) {
    this.source = source;
    this.version = version;
    this.label = label;
  }

  public Source getSource() {
    return this.source;
  }

  public String getVersion() {
    return this.version;
  }

  public String getLabel() {
    return this.label;
  }

  public enum Source {
    PUBLIC, TESTING
  }
}
