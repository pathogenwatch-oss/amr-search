package net.cgps.wgsa.paarsnp.core.models.results;

import net.cgps.wgsa.paarsnp.core.lib.AbstractJsonnable;
import net.cgps.wgsa.paarsnp.core.models.ResistanceSet;
import net.cgps.wgsa.paarsnp.core.models.SetMember;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class SetResult extends AbstractJsonnable {

  private final Set<SetMember> foundMembers;
  private final Set<Modifier> foundModifiers;
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

  public Set<SetMember> getFoundMembers() {
    return this.foundMembers;
  }

  public Set<Modifier> getFoundModifiers() {
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

  public boolean containsAll() {
    return this.getFoundMembers().equals(new HashSet<>(this.set.getMembers()));
  }

}
