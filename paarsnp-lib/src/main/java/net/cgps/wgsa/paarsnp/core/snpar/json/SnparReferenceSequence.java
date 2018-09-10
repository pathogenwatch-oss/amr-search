package net.cgps.wgsa.paarsnp.core.snpar.json;

import net.cgps.wgsa.paarsnp.core.lib.SequenceType;
import net.cgps.wgsa.paarsnp.core.lib.json.AbstractJsonnable;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

public class SnparReferenceSequence extends AbstractJsonnable {

  private final String name;
  private final SequenceType type;
  private final float pid;
  private final float coverage;
  private final Collection<String> variants;
  private Collection<ResistanceMutation> mappedVariants = null;

  public SnparReferenceSequence(final String name, final SequenceType type, final float pid, final float coverage, final Collection<String> variants) {

    this.name = name;
    this.type = type;
    this.pid = pid;
    this.coverage = coverage;
    this.variants = variants;
  }

  public Collection<ResistanceMutation> getResistanceMutations() {

    if (null == this.mappedVariants) {
      switch (this.type) {
        case DNA:
          this.mappedVariants = this.variants.stream().map(ResistanceMutation.parseSnp()).collect(Collectors.toList());
          break;
        case PROTEIN:
          this.mappedVariants = this.variants.stream().map(ResistanceMutation.parseAaVariant()).collect(Collectors.toList());
          break;
      }
    }
    return Collections.unmodifiableCollection(this.mappedVariants);
  }

  public String getName() {

    return this.name;
  }

  public SequenceType getType() {

    return this.type;
  }

  public float getPid() {

    return this.pid;
  }

  public float getCoverage() {
    return this.coverage;
  }
}
