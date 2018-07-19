package net.cgps.wgsa.paarsnp.core.lib.blast;

import net.cgps.wgsa.paarsnp.core.lib.blast.ncbi.BlastOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.*;
import org.xml.sax.helpers.XMLReaderFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import java.io.BufferedReader;
import java.util.function.Function;

public class BlastXmlReader implements Function<BufferedReader, BlastOutput> {

  private final Logger logger = LoggerFactory.getLogger(BlastXmlReader.class);

  public BlastOutput apply(final BufferedReader inputStream) {

    final Unmarshaller unmarshaller;
    final XMLReader xmlreader;

    try {
      unmarshaller = JAXBContext.newInstance(BlastOutput.class.getPackage().getName()).createUnmarshaller();
      xmlreader = XMLReaderFactory.createXMLReader();
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

    } catch (final JAXBException | SAXException e) {
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
