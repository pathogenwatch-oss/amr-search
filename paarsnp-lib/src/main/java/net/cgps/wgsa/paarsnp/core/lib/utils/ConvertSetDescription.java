package net.cgps.wgsa.paarsnp.core.lib.utils;

import net.cgps.wgsa.paarsnp.core.lib.json.Modifier;
import net.cgps.wgsa.paarsnp.core.lib.json.OldStyleSetDescription;
import net.cgps.wgsa.paarsnp.core.lib.json.ResistanceSet;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConvertSetDescription implements Function<ResistanceSet, Stream<OldStyleSetDescription>> {

  @Override
  public Stream<OldStyleSetDescription> apply(final ResistanceSet resistanceSet) {

    final AtomicInteger nameCounter = new AtomicInteger(1);

    return resistanceSet
        .getPhenotypes()
        .stream()
        .map(phenotype -> {

          final String name = 1 == resistanceSet.getPhenotypes().size() ? resistanceSet.getName() : resistanceSet.getName() + "_" + String.valueOf(nameCounter.getAndAdd(1));

          final List<String> members = resistanceSet.getMembers().stream().flatMap(member -> {
            if (member.getVariants().isEmpty()) {
              return Stream.of(member.getGene());
            } else {
              return member.getVariants().stream().map(variant -> member.getGene() + "_" + variant);
            }
          }).collect(Collectors.toList());

          return new OldStyleSetDescription(name, phenotype.getProfile(), members, phenotype.getModifiers().stream().collect(Collectors.toMap(Modifier::getName, Modifier::getEffect)));
        });

  }

}
