package net.cgps.wgsa.paarsnp.fetcher;

import ch.qos.logback.classic.Level;
import net.cgps.wgsa.paarsnp.core.lib.ElementEffect;
import net.cgps.wgsa.paarsnp.core.lib.SetResistanceType;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class FetcherMain {

  private final Logger logger = LoggerFactory.getLogger(FetcherMain.class);
  public static final float RESFINDER_OVERLAP_THRESHOLD = 0.4f;

  public static void main(final String[] args) {
    // Initialise the options parser
    final Options options = FetcherMain.myOptions();
    final CommandLineParser parser = new DefaultParser();

    if (args.length == 0) {
      final HelpFormatter formatter = new HelpFormatter();
      formatter.setWidth(200);
      formatter.printHelp("fetcher: ", options);
      System.exit(1);
    }

    try {

      final CommandLine commandLine = parser.parse(options, args);
      final ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
      root.setLevel(Level.valueOf(commandLine.getOptionValue('l', "INFO" )));

      final Path buildPath = Paths.get(commandLine.getOptionValue('b'));
      final Path outputPath = Paths.get(commandLine.getOptionValue('o'));
      final float pidThreshold = Float.valueOf(commandLine.getOptionValue('p'));

      new FetcherMain().run(buildPath, outputPath, pidThreshold);

    } catch (final Exception e) {
      LoggerFactory.getLogger(FetcherMain.class).error("Failed to run due to: ", e);
    }

  }

  private static Options myOptions() {

    final Option buildFolderPath = Option.builder("b" ).longOpt("build-dir" ).hasArg().argName("Build Directory Location" ).required().desc("Location of directory to store build data, e.g. the ResFinder git repository" ).build();

    final Option outputDirectoryOption = Option.builder("o" ).longOpt("output-dir" ).hasArg().argName("Output directory" ).desc("Optional: Output location for BLAST and paarsnp databases." ).required().build();

    final Option pidThreshold = Option.builder("p" ).longOpt("pid-threshold" ).hasArg().argName("Percent Identity Threshold" ).desc("Minimum percent identity threshold to use" ).required().build();

    final Option logLevel = Option.builder("l" ).longOpt("log-level" ).hasArg().argName("Logging level" ).desc("INFO, DEBUG etc" ).build();

    final Options options = new Options();
    options.addOption(outputDirectoryOption)
        .addOption(buildFolderPath)
        .addOption(pidThreshold)
        .addOption(logLevel);

    return options;
  }

  private void run(final Path resfinderGitPath, final Path outputParentDir, final float pidThreshold) {

    if (!Files.exists(outputParentDir)) {
      this.logger.error("Output directory {} does not exist", outputParentDir.toString());
      throw new RuntimeException("No output directory." );
    }

    // Checkout or update
    final Path gitPath = new InitialiseGit().apply(resfinderGitPath);

    // Generate the ar_agents.csv file
// Actually the config file is useless, and the agents will have to be built up from the notes.txt file

//    final Path configPath = Paths.get(gitPath.toString(), "config");


    try {
//      agents = Files.readAllLines(configPath)
//          .stream()
//          .filter(line -> !line.startsWith("#"))
//          .map(line -> line.split("\\s+"))
//          .map(dataArr -> new AntimicrobialAgent(dataArr[0], dataArr[1], dataArr[2]))
//          .collect(Collectors.toList());

      final Path resfinderOutDirectory = Paths.get(outputParentDir.toString(), "resfinder" );

      if (!Files.exists(resfinderOutDirectory)) {
        Files.createDirectories(resfinderOutDirectory);
      }

      // Read in the AMR mapping from notes.txt
      final Path notesPath = Paths.get(gitPath.toString(), "notes.txt" );

      final Map<String, Collection<String>> amrMappings = Files.readAllLines(notesPath)
          .stream()
          .filter(line -> !line.startsWith("#" ))
          .distinct() // Remove duplicate lines (why they are there, I don't know ...)
          .peek(line -> this.logger.trace("notes: {}", line))
          .map(line -> line.replace(" resistance", "" ))
          .map(line -> line.replace("and ", "" ))
          .map(line -> line.replaceAll("\\s+", "" ))
          .map(line -> line.split(":" ))
          .map(data -> new ImmutablePair<String, Collection<String>>(data[0], Arrays.asList(data[1].split("," ))))
          .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

      // Generate the unique set of antimicrobials
      final Collection<String> antimicrobials = amrMappings
          .values()
          .stream()
          .flatMap(Collection::stream)
          .collect(Collectors.toSet());

      // Write the ar agents file.
      final Path arAgents = Paths.get(resfinderOutDirectory.toString(), "ar_agents.csv" );
      final Path genesFasta = Paths.get(resfinderOutDirectory.toString(), "resistance_genes.fa" );
      final Path genesCsv = Paths.get(resfinderOutDirectory.toString(), "resistance_genes.csv" );
      final Path snpsFasta = Paths.get(resfinderOutDirectory.toString(), "ar_snps.fa" );
      final Path snpsCsv = Paths.get(resfinderOutDirectory.toString(), "ar_snps.csv" );


      try {
        Files.delete(arAgents);
        Files.delete(genesFasta);
        Files.delete(genesCsv);
        Files.delete(snpsFasta);
        Files.delete(snpsCsv);
      } catch (final NoSuchFileException e) {
        this.logger.trace("No old files to delete", e);
      }
      try (final BufferedWriter arWriter = Files.newBufferedWriter(arAgents, StandardOpenOption.CREATE)) {

        arWriter.append("Name,Type,Full Name" );
        arWriter.newLine();

        for (final String antimicrobial : antimicrobials) {
          arWriter.append(antimicrobial);
          arWriter.append("," );
          arWriter.append(antimicrobial);
          arWriter.append("," );
          arWriter.append(antimicrobial);
          arWriter.newLine();
        }
      }


      try (final BufferedWriter genesFastaBw = Files.newBufferedWriter(genesFasta, StandardOpenOption.CREATE); final BufferedWriter genesCsvBw = Files.newBufferedWriter(genesCsv, StandardOpenOption.CREATE)) {

        genesCsvBw.append("Gene Name,Resistance Group,Set Effect,Effect,Resistance Profile,PID Threshold,Coverage Threshold,Source" );
        genesCsvBw.newLine();

        Files
            .list(gitPath)
            .filter(file -> file.toString().endsWith(".fsa" ))
            .flatMap(file -> {

              final Collection<Map.Entry<String, String>> fastaRecords = new ArrayList<>(2000);

              try (final BufferedReader br = Files.newBufferedReader(file)) {

                String currentHeader = br.readLine().replace(">", "" );

                StringBuilder currentSequence = new StringBuilder(5000);

                String line;

                while ((line = br.readLine()) != null) {

                  if (line.startsWith(">" )) {

                    fastaRecords.add(new ImmutablePair<>(currentHeader, currentSequence.toString()));

                    currentHeader = line.replace(">", "" );
                    currentSequence.setLength(0); // reset the sequence.
                  } else {
                    currentSequence.append(line);
                  }
                }

                // Add the last record
                fastaRecords.add(new ImmutablePair<>(currentHeader, currentSequence.toString()));

              } catch (final IOException e) {
                throw new RuntimeException(e);
              }
              return fastaRecords.stream();
            })
            .distinct()
            .peek(record -> {
              // Write out the FASTA line to file
              try {
                genesFastaBw.write(">" + record.getKey() + "\n" + record.getValue() + "\n" );
              } catch (final IOException e) {
                throw new RuntimeException(e);
              }
            })
            .map(Map.Entry::getKey)
            .map(record -> {

              this.logger.debug("{}", record);

              // Generate the resistance_genes.csv file

              // The resistance group is from notes.txt and can be mapped to the gene name.
              final String resistanceGroupName = record.replaceFirst("_.*$", "" );

              final Collection<String> resistances = Optional.ofNullable(amrMappings.get(resistanceGroupName)).orElseGet(() -> {
                this.logger.error("Unable to find resistance group " + resistanceGroupName + " for " + record);
                return Collections.emptyList();
              });

              // Convert record to CSV line
              final StringBuilder line = new StringBuilder(200);

              line.append(record);
              line.append("," );
              line.append(record);
              line.append("," );
              line.append(SetResistanceType.RESISTANT.name());
              line.append("," );
              line.append(ElementEffect.RESISTANCE.name());
              line.append("," );
              line.append("\"" );
              line.append(String.join(",", resistances));
              line.append("\"" );
              line.append("," );
              line.append(String.valueOf(pidThreshold));
              line.append("," );
              line.append(String.valueOf(RESFINDER_OVERLAP_THRESHOLD));
              line.append("," );
              line.append("ResFinder" );

              return line.toString();
            })
            .forEach(line -> {
              // write to file.
              try {
                genesCsvBw.write(line);
                genesCsvBw.newLine();
              } catch (final IOException e) {
                throw new RuntimeException();
              }
            });
      }

      try {
        Files.createFile(snpsCsv);
        Files.createFile(snpsFasta);
      } catch (final FileAlreadyExistsException e) {
        this.logger.debug("SNP files already exist." );
      }
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }

  }
}
