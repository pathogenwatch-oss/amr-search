package net.cgps.wgsa.paarsnp.core.lib;

import java.nio.file.Path;

public class InputData {

  private final String assemblyId;
  private final String speciesId;
  private final Path sequenceFile;

  public InputData(final String assemblyId, final String speciesId, final Path sequenceFile) {

    this.assemblyId = assemblyId;
    this.speciesId = speciesId;
    this.sequenceFile = sequenceFile;
  }

  public String getAssemblyId() {

    return this.assemblyId;
  }

  public String getSpeciesId() {

    return this.speciesId;
  }

  public Path getSequenceFile() {

    return this.sequenceFile;
  }
}
