package net.cgps.wgsa.paarsnp.core;

public class InputData {

  private final String assemblyId;
  private final String speciesId;
  private final String sequenceFile;

  public InputData(final String assemblyId, final String speciesId, final String sequenceFile) {

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

  public String getSequenceFile() {

    return this.sequenceFile;
  }
}
