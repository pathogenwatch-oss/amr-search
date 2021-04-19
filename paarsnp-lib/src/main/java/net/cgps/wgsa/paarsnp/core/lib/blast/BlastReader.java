package net.cgps.wgsa.paarsnp.core.lib.blast;

import net.cgps.wgsa.paarsnp.core.lib.blast.ncbi.BlastOutput;
import net.cgps.wgsa.paarsnp.core.lib.utils.DnaSequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import java.io.BufferedReader;
import java.math.BigInteger;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Handles a BLAST XML format stream and parses out mutations.
 */
public class BlastReader implements Function<BlastOutput, Stream<BlastMatch>> {

  private final Logger logger = LoggerFactory.getLogger(BlastReader.class);

  /**
   * Returns a list of {@link BlastMatch} objects, keyed by query sequence ID.
   */
  public Stream<BlastMatch> apply(final BlastOutput blastOutput) {

    this.logger.debug("Mapping matches");

    // An "iteration" in blast speak is the result for a contig search (i.e. a single fasta record in a multi-fasta). So
    // a single sequence fasta will only have one iteration.
    return blastOutput.getBlastOutputIterations()
        .getIteration()
        .stream()
        .flatMap(iteration -> iteration
            .getIterationHits()
            .getHit()
            .stream()
            .flatMap(hit -> hit
                .getHitHsps()
                .getHsp()
                .stream()
                .map(hsp -> {
                  // Check if the match is reversed
                  final DnaSequence.Strand strand = hsp.getHspHitFrom().intValue() < hsp.getHspHitTo().intValue() ? DnaSequence.Strand.FORWARD : DnaSequence.Strand.REVERSE;

                  // Extract the list of mutations
                  final BlastSearchStatistics stats = new BlastSearchStatistics(
                      hit.getHitAccession(),
                      hsp.getHspHitFrom().intValue(),
                      hsp.getHspHitTo().intValue(), hit.getHitLen().intValue(), iteration.getIterationQueryDef(),
                      hsp.getHspQueryFrom().intValue(),
                      hsp.getHspQueryTo().intValue(), hsp.getHspEvalue(), calculatePid(hsp.getHspIdentity(), hsp.getHspAlignLen()),
                      strand);
                  // Add the match w/ mutations to the collection.
                  return new BlastMatch(stats, hsp.getHspQseq(), hsp.getHspHseq()
                  );
                })
            )
        );
  }

  private static double calculatePid(final BigInteger hspIdentity, final BigInteger hspAlignLen) {

    return ((double) hspIdentity.intValue() / (double) hspAlignLen.intValue()) * 100;
  }

  public static class BlastXmlReader implements Function<BufferedReader, BlastOutput> {

    private final Logger logger = LoggerFactory.getLogger(BlastXmlReader.class);

    public BlastOutput apply(final BufferedReader inputStream) {

      // This removes a deprecated optimiser that triggers an "Illegal Reflection" warning.
      // https://github.com/javaee/jaxb-v2/issues/1197
      System.setProperty("com.sun.xml.bind.v2.bytecode.ClassTailor.noOptimize", "true");

      final Unmarshaller unmarshaller;
      final XMLReader xmlreader;

      try {
        unmarshaller = JAXBContext.newInstance(BlastOutput.class.getPackage().getName()).createUnmarshaller();
        xmlreader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
        xmlreader.setFeature("http://xml.org/sax/features/namespaces", true);
        xmlreader.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
        xmlreader.setEntityResolver((publicId, systemId) -> {

              String file;
              if (systemId.contains("NCBI_BlastOutput.dtd")) {
                file = "/NCBI_BlastOutput.dtd";
              } else if (systemId.contains("NCBI_Entity.mod.dtd")) {
                file = "/NCBI_Entity.mod.dtd";
              } else if (systemId.contains("NCBI_BlastOutput.mod.dtd")) {
                file = "/NCBI_BlastOutput.mod.dtd";
              } else {
                throw new RuntimeException("No DTD found for parsing XML.");
              }
              return new InputSource(BlastOutput.class.getResourceAsStream(file));
            }
        );

      } catch (final JAXBException | SAXException | ParserConfigurationException e) {
        this.logger.error("Error constructing BLAST parser");
        throw new RuntimeException(e);
      }

      final InputSource input = new InputSource(inputStream);
      final Source source = new SAXSource(xmlreader, input);
      try {
        return (BlastOutput) unmarshaller.unmarshal(source);
      } catch (final JAXBException e) {
        this.logger.error("Malformed BLAST output.");
        throw new RuntimeException(e);
      }
    }
  }
}
