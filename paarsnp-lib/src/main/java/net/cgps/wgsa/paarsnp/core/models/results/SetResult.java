package net.cgps.wgsa.paarsnp.core.models.results;

import net.cgps.wgsa.paarsnp.core.lib.AbstractJsonnable;
import net.cgps.wgsa.paarsnp.core.models.ResistanceSet;
import net.cgps.wgsa.paarsnp.core.models.SetMember;

import java.util.Collection;
import java.util.Collections;

public class SetResult extends AbstractJsonnable {

  private final Collection<SetMember> foundMembers;
  private final Collection<SetMember> foundModifiers;
  private final ResistanceSet set;

  @SuppressWarnings("unused")
  private SetResult() {
    this(Collections.emptyList(), Collections.emptyList(), null);
  }

  public SetResult(final Collection<SetMember> foundMembers, final Collection<SetMember> foundModifiers, final ResistanceSet set) {
    this.foundMembers = foundMembers;
    this.foundModifiers = foundModifiers;
    this.set = set;
  }

  public Collection<SetMember> getFoundMembers() {
    return this.foundMembers;
  }

  public Collection<SetMember> getFoundModifiers() {
    return this.foundModifiers;
  }

  public boolean modifierIsPresent(final String name) {
    return this.foundMembers
        .stream()
        .anyMatch(setMember -> setMember.getGene().equals(name));
  }

  public ResistanceSet getSet() {
    return this.set;
  }
}
