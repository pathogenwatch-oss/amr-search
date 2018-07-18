package net.cgps.wgsa.paarsnp.core.lib.blast;

import net.cgps.wgsa.paarsnp.core.lib.blast.ncbi.BlastOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import java.io.BufferedReader;
import java.math.BigInteger;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Handles a BLAST XML format stream and parses out mutations.
 */
public class MutationReader implements Function<BufferedReader, Stream<MutationSearchResult>> {

    private final Logger logger = LoggerFactory.getLogger(MutationReader.class);

    private static double calculatePid(final BigInteger hspIdentity, final BigInteger hspAlignLen) {

        return ((double) hspIdentity.intValue() / (double) hspAlignLen.intValue()) * 100;
    }

    private BlastOutput getBlastOutput(final BufferedReader inputStream) throws SAXException, JAXBException {

        final Unmarshaller unmarshaller = JAXBContext.newInstance(BlastOutput.class.getPackage().getName()).createUnmarshaller();

        final XMLReader xmlreader = XMLReaderFactory.createXMLReader();
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
        final InputSource input = new InputSource(inputStream);
        final Source source = new SAXSource(xmlreader, input);
        return (BlastOutput) unmarshaller.unmarshal(source);
    }

    /**
     * Returns a list of {@link MutationSearchResult} objects, keyed by query sequence ID.
     */
    private Stream<MutationSearchResult> mapMatches(final BlastOutput blastOutput) {

        this.logger.debug("Mapping matches");

        // An "iteration" in blast speak is the result for a contig search (i.e. a single fasta record in a multi-fasta). So
        // a single sequence fasta will only have one iteration.
        return blastOutput.getBlastOutputIterations()
                .getIteration()
                .parallelStream()
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
                                    final boolean reversed = hsp.getHspHitFrom().intValue() > hsp.getHspHitTo().intValue();

                                    final MutationBuilder mutationBuilder = new MutationBuilder(hit.getHitAccession());

                                    // Extract the list of mutations
                                    final SequenceProcessingResult sequenceProcessingResult = new SequenceProcessor(hsp.getHspHseq(), hsp.getHspHitFrom().intValue(), reversed, hsp.getHspQseq(), hsp.getHspQueryFrom().intValue(), mutationBuilder).call();

                                    // Add the match w/ mutations to the collection.
                                    return new MutationSearchResult(
                                            hit.getHitAccession(),
                                            hsp.getHspHitFrom().intValue(),
                                            hsp.getHspHitTo().intValue(),
                                            iteration.getIterationQueryDef(),
                                            hsp.getHspQueryFrom().intValue(),
                                            hsp.getHspQueryTo().intValue(),
                                            hsp.getHspQseq(),
                                            hsp.getHspHseq(),
                                            calculatePid(hsp.getHspIdentity(), hsp.getHspAlignLen()),
                                            hsp.getHspEvalue(),
                                            reversed,
                                            sequenceProcessingResult.getMutations(),
                                            hit.getHitLen().intValue()
                                    );
                                })
                        )
                );
    }

    @Override
    public Stream<MutationSearchResult> apply(final BufferedReader blastOutputStream) {
        final BlastOutput blastOutput;
        try {
            blastOutput = this.getBlastOutput(blastOutputStream);
        } catch (final SAXException | JAXBException e) {
            this.logger.error("Failed to parse BLAST stream.");
            throw new RuntimeException(e);
        }

        this.logger.trace(blastOutput.toString());

        return this.mapMatches(blastOutput);
    }
}
