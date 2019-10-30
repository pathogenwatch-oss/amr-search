package net.cgps.wgsa.paarsnp.core.snpar.codonmapping;

import net.cgps.wgsa.paarsnp.core.lib.utils.DnaSequence;
import net.cgps.wgsa.paarsnp.core.lib.utils.StreamGobbler;
import org.biojava.nbio.core.exceptions.CompoundNotFoundException;
import org.biojava.nbio.core.sequence.ProteinSequence;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class CreateAaAlignment implements BiFunction<String, String, Map.Entry<String, String>> {

  @Override
  public Map.Entry<String, String> apply(final String referenceAlignment, final String queryAlignment) {

    // Remove indels, remove incomplete codons from the end, translate and pairwise align
//    final var translateProcessBuilder = new ProcessBuilder("goalign", "translate", "--unaligned");

    final ProteinSequence transCleanRef;
    final ProteinSequence transCleanQuery;

    try {
      transCleanRef = new ProteinSequence(DnaSequence.translateMultiple(cleanSequence(referenceAlignment), 'X'));
      transCleanQuery = new ProteinSequence(DnaSequence.translateMultiple(cleanSequence(queryAlignment), 'X'));
    } catch (CompoundNotFoundException e) {
      e.printStackTrace();
    }

    try {
      final var translateProcess = translateProcessBuilder.start();
      final List<String> translation;
      try (
          final InputStream error = translateProcess.getErrorStream();
          final var translateWriter = new BufferedWriter(new OutputStreamWriter(translateProcess.getOutputStream()));
          final var translaterReader = new BufferedReader(new InputStreamReader(translateProcess.getInputStream()))) {

        new StreamGobbler(error, "ERROR").start();
        translateWriter.write(">ref");
        translateWriter.newLine();
        translateWriter.write(cleanSequence(referenceAlignment));
        translateWriter.newLine();
        translateWriter.write(">query");
        translateWriter.newLine();
        translateWriter.write(cleanSequence(queryAlignment));
        translateWriter.newLine();
        translateWriter.flush();
        translateWriter.close();

        translation = translaterReader.lines().collect(Collectors.toList());
      }

      final var alignProcessBuilder = new ProcessBuilder("goalign", "sw");
      final var alignmentProcess = alignProcessBuilder.start();

      try (final InputStream error = alignmentProcess.getErrorStream();
           final var alignWriter = new BufferedWriter(new OutputStreamWriter(alignmentProcess.getOutputStream()));
           final var alignReader = new BufferedReader(new InputStreamReader(alignmentProcess.getInputStream()))) {

        new StreamGobbler(error, "ERROR").start();

        for (final var line : translation) {
          alignWriter.write(line);
          alignWriter.newLine();
        }

        alignWriter.flush();
        alignWriter.close();

        final Map.Entry<String, String> alignmentResult = new ExtractAlignment().apply(alignReader.lines().collect(Collectors.joining("\n")));
        return alignmentResult;
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Removes indels and trims incomplete codon from the end of the sequence.
   *
   * @param sequence to be cleaned
   * @return Codon-complete and indel free String.
   */
  public static String cleanSequence(final String sequence) {
    final var noIndels = sequence.replace("-", "");
    return noIndels.substring(0, noIndels.length() - noIndels.length() % 3);
  }
}
