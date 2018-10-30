package net.cgps.wgsa.paarsnp.pwconfigutility;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.cli.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ConfigUtility {

  public static void main(final String[] args) {

    // Initialise the options parser
    final Options options = ConfigUtility.myOptions();
    final CommandLineParser parser = new DefaultParser();

    if (args.length == 0) {
      final HelpFormatter formatter = new HelpFormatter();
      formatter.setWidth(200);
      formatter.printHelp("Options:", options);
      System.exit(1);
    }

    final CommandLine commandLine;
    try {
      commandLine = parser.parse(options, args);
    } catch (final ParseException e) {
      throw new RuntimeException(e);
    }

    final ch.qos.logback.classic.Logger rootLogger = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    rootLogger.setLevel(Level.valueOf(commandLine.getOptionValue('l', "INFO")));

    new ConfigUtility().updateConfig(commandLine.getOptionValue("i"), commandLine.getOptionValue("t"), commandLine.getOptionValue("v"), Optional.ofNullable(commandLine.getOptionValue("f")), Optional.ofNullable(commandLine.getOptionValue('j')), Optional.ofNullable(commandLine.getOptionValue('o')));
  }

  private static Options myOptions() {
    // Optional

    final Options options = new Options();
    options
        .addOption(Option.builder("i").longOpt("input-taxonids").hasArg().argName("Taxon ID File").desc("Required: Path to file containing list of taxon ids to modify. ").required().build())
        .addOption(Option.builder("t").longOpt("task").hasArg().argName("Task name").desc("Task name, e.g. 'core'.").required().build())
        .addOption(Option.builder("v").longOpt("version").hasArg().argName("New version").desc("Update to this version.").required().build())
        .addOption(Option.builder("f").longOpt("flag-name").hasArg().argName("Flag Name").desc("If the version is behind a feature flag, include that flag. If the flag is new for that ID then a new record will be added rather than the updated.").build())
        .addOption(Option.builder("j").longOpt("json").hasArg().argName("Original tasks.json").desc("Use an existing tasks.js file as the base to add to").build())
        .addOption(Option.builder("o").longOpt("outfile").hasArg().argName("Out filename").desc("Output file name").build())
        .addOption(Option.builder("l").longOpt("log-level").hasArg().argName("Logging level").desc("INFO, DEBUG etc").build());

    return options;

  }

  private void updateConfig(final String inputTaxidFile, final String task, final String version, final Optional<String> flagname, final Optional<String> originalConfig, final Optional<String> outputfilename) {

    final Set<String> taxids = new ReadTaxonList().apply(Paths.get(inputTaxidFile)).collect(Collectors.toSet());
    final Set<String> alreadyExist = new HashSet<>(5000);

    final ObjectMapper mapper = new ObjectMapper();


    final ObjectNode rootNode;
    if (originalConfig.isPresent()) {
      rootNode = new ReadConfig(mapper).apply(originalConfig.get());
    } else {
      rootNode = mapper.createObjectNode();
      rootNode.putObject("genome");
    }

    final ObjectNode genomes = (ObjectNode) rootNode.get("genome");

    genomes.fields().forEachRemaining(genomeNode -> {
      if (taxids.contains(genomeNode.getKey())) {

        alreadyExist.add(genomeNode.getKey());

        final Iterator<JsonNode> taskIter = genomeNode.getValue().elements();
        boolean updated = false;
        while (taskIter.hasNext() && !updated) {
          final JsonNode taskNode = taskIter.next();

          if (task.equals(taskNode.findValue("task").asText())) {

            if (!Optional.ofNullable(taskNode.get("flags")).isPresent() && !flagname.isPresent()) {
              ((ObjectNode) taskNode).put("version", version);
              updated = true;
            } else if (
                flagname.isPresent() &&
                    Optional.ofNullable(taskNode.get("flags")).isPresent() &&
                    taskNode.get("flags").has(flagname.get())) {
              ((ObjectNode) taskNode).put("version", version);
              updated = true;
            }
          }
        }

        if (!updated) {
          final ObjectNode newTask = ((ArrayNode) genomeNode.getValue()).addObject();
          newTask.put("task", task);
          newTask.put("version", version);
          flagname.ifPresent(flag -> {
            final ObjectNode flagsNode = newTask.putObject("flags");
            flagsNode.put(flag, true);
          });
        }
      }
    });

    // Add any missing tax ids.
    taxids.stream()
        .filter(taxId -> !alreadyExist.contains(taxId))
        .map(genomes::putArray)
        .map(ArrayNode::addObject)
        .forEach(newTask -> {
          newTask.put("task", task);
          newTask.put("version", version);
          flagname.ifPresent(flag -> {
            final ObjectNode flagsNode = newTask.putObject("flags");
            flagsNode.put(flag, true);
          });
        });

    // Write result
    try {
      mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
      mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
      final byte[] bytes = mapper.writeValueAsBytes(rootNode);
      if (outputfilename.isPresent()) {
        Files.write(Paths.get(outputfilename.get()), bytes);
      } else {
        System.out.print(new String(bytes));
      }
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }
}
