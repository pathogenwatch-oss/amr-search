package net.cgps.wgsa.paarsnp.core.models;

import net.cgps.wgsa.paarsnp.core.lib.AbstractJsonnable;

public class LibraryVersion extends AbstractJsonnable {
  private final LibraryVersion.Source source;
  private final String version;

  private LibraryVersion() {
    this(Source.PUBLIC, "");
  }

  public LibraryVersion(final Source source, final String version) {
    this.source = source;
    this.version = version;
  }

  public Source getSource() {
    return this.source;
  }

  public String getVersion() {
    return this.version;
  }

  public enum Source {
    PUBLIC, TESTING
  }
}
