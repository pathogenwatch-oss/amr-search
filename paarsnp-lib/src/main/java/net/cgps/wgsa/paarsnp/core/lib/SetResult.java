package net.cgps.wgsa.paarsnp.core.lib;

import net.cgps.wgsa.paarsnp.core.lib.json.AbstractJsonnable;
import net.cgps.wgsa.paarsnp.core.lib.json.ResistanceSet;

import java.util.Collection;
import java.util.Collections;

public class SetResult extends AbstractJsonnable {

  private final Collection<String> foundMembers;
  private final Collection<String> foundModifiers;
  private final ResistanceSet set;

  @SuppressWarnings("unused")
  private SetResult() {
    this(Collections.emptyList(), Collections.emptyList(), null);
  }

  public SetResult(final Collection<String> foundMembers, final Collection<String> foundModifiers, final ResistanceSet set) {
    this.foundMembers = foundMembers;
    this.foundModifiers = foundModifiers;
    this.set = set;
  }

  public Collection<String> getFoundMembers() {
    return this.foundMembers;
  }

  public Collection<String> getFoundModifiers() {
    return this.foundModifiers;
  }

  public ResistanceSet getSet() {
    return this.set;
  }
}
