package net.cgps.wgsa.paarsnp.pwgenomeconfig;

import net.cgps.wgsa.paarsnp.builder.LibraryReader;
import net.cgps.wgsa.paarsnp.core.models.PaarsnpLibrary;
import net.cgps.wgsa.paarsnp.core.models.Phenotype;
import net.cgps.wgsa.paarsnp.core.models.PhenotypeEffect;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class CreateConfigMain {
  public static void main(final String[] args) {
    final Path tomlFile = Paths.get(args[0]);
    final Path outputFile = Paths.get(args[1]);
    if (Files.exists(tomlFile)) {
      try {
        new CreateConfigMain().run(tomlFile, outputFile);
      } catch (final Exception e) {
        System.err.println(e.getMessage());
        e.printStackTrace();
        System.exit(1);
      }
    } else {
      System.err.println("AMR library " + args[0] + " does not exist.");
    }
  }

  private void run(final Path libraryFile, final Path outputFile) {
    final PaarsnpLibrary paarsnpLibrary = new LibraryReader().apply(libraryFile).getPaarsnpLibrary();

    final Collection<PwGenomeJson.PwAgent> antibiotics = paarsnpLibrary.getAntimicrobials()
        .stream()
        .map(agent -> new PwGenomeJson.PwAgent(agent.getKey(), agent.getKey(), agent.getName(), agent.getType()))
        .collect(Collectors.toList());

    final Map<String, Set<PwGenomeJson.PwPaarRecord>> paarRecordMap = this.initialiseAmrMap(antibiotics, HashSet::new);
    final Map<String, Map<String, Set<PwGenomeJson.PwSnpRecord>>> snparRecordMap = this.initialiseAmrMap(antibiotics, HashMap::new);

    paarsnpLibrary.getSets()
        .values()
        .forEach(set -> {
          final Collection<PwGenomeJson.PwPaarRecord> paarRecords = set
              .getMembers()
              .stream()
              .filter(setMember -> setMember.getVariants().isEmpty())
              .map(setMember -> new PwGenomeJson.PwPaarRecord(setMember.getGene(), PhenotypeEffect.RESISTANT))
              .collect(Collectors.toSet());

          final Map<String, Set<PwGenomeJson.PwSnpRecord>> snpRecords = set.getMembers()
              .stream()
              .filter(setMember -> !setMember.getVariants().isEmpty())
              .map(setMember -> new AbstractMap.SimpleImmutableEntry<>(
                  setMember.getGene(),
                  setMember.getVariants()
                      .stream()
                      .map(name -> new PwGenomeJson.PwSnpRecord(name, PhenotypeEffect.RESISTANT))
                      .collect(Collectors.toSet())))
              .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

          set.getPhenotypes()
              .stream()
              .map(Phenotype::getProfile)
              .flatMap(Collection::stream)
              .forEach(antibiotic -> {
                if (!paarRecords.isEmpty()) {
                  paarRecordMap.get(antibiotic).addAll(paarRecords);
                }
                if (!snpRecords.isEmpty()) {
                  snpRecords.keySet()
                      .stream()
                      .filter(key -> !snparRecordMap.get(antibiotic).containsKey(key))
                      .forEach(key -> snparRecordMap.get(antibiotic).put(key, new HashSet<>()));
                  snpRecords
                      .forEach((key, value) -> snparRecordMap.get(antibiotic).get(key).addAll(value));
                }
              });
        });

    final PwGenomeJson json = new PwGenomeJson(
        antibiotics,
        snparRecordMap
            .entrySet()
            .stream()
            .filter(entry -> !entry.getValue().isEmpty())
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)),
        paarRecordMap.entrySet().stream().filter(entry -> !entry.getValue().isEmpty()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
    );

    try {
      Files.write(outputFile, json.toJson().getBytes());
    } catch (
        final IOException e) {
      throw new RuntimeException(e);
    }
  }

  private <T> Map<String, T> initialiseAmrMap(final Collection<PwGenomeJson.PwAgent> agents, final Supplier<T> supplier) {
    return agents
        .stream()
        .map(PwGenomeJson.PwAgent::getKey)
        .collect(Collectors.toMap(Function.identity(), (agent) -> supplier.get()));
  }
}
