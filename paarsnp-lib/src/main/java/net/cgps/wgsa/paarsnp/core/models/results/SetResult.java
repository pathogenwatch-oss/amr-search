package net.cgps.wgsa.paarsnp.core.models.results;

import net.cgps.wgsa.paarsnp.core.lib.AbstractJsonnable;
import net.cgps.wgsa.paarsnp.core.models.ResistanceSet;
import net.cgps.wgsa.paarsnp.core.models.SetMember;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

public class SetResult extends AbstractJsonnable {

  private final Collection<SetMember> foundMembers;
  private final Collection<Modifier> foundModifiers;
  private final ResistanceSet set;

  @SuppressWarnings("unused")
  private SetResult() {
    this(Collections.emptyList(), Collections.emptyList(), null);
  }

  public SetResult(final Collection<SetMember> foundMembers, final Collection<Modifier> foundModifiers, final ResistanceSet set) {
    this.foundMembers = new HashSet<>(foundMembers);
    this.foundModifiers = new HashSet<>(foundModifiers);
    this.set = set;
  }

  public Collection<SetMember> getFoundMembers() {
    return this.foundMembers;
  }

  public Collection<Modifier> getFoundModifiers() {
    return this.foundModifiers;
  }

  public ResistanceSet getSet() {
    return this.set;
  }

  @Override
  public String toString() {
    return this.toJson();
  }

  public boolean containsModifier(final Modifier modifier) {
    return this.foundModifiers.contains(modifier);
  }
}
