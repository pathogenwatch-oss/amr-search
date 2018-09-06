package net.cgps.wgsa.paarsnp.core.lib;

import net.cgps.wgsa.paarsnp.core.lib.json.AntibioticProfile;
import net.cgps.wgsa.paarsnp.core.lib.json.Modifier;
import net.cgps.wgsa.paarsnp.core.lib.json.Phenotype;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

public class ProfileAggregator {
  final Map<String, AntibioticProfile> profileMap;

  public ProfileAggregator() {
    this.profileMap = new HashMap<>(50);
  }

  public void addPhenotype(final Phenotype phenotype, final Predicate<Modifier> selector) {
    final Optional<Modifier> selectedModifier = phenotype.getModifiers()
        .stream()
        .filter(selector)
        .findFirst();

  }
}
