package net.cgps.wgsa.paarsnp.core.lib.blast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.function.Function;

public class BlastRowParser implements Function<String[], Optional<BlastMatch>> {

  @Override
  public Optional<BlastMatch> apply(final String[] data) {

    if (BlastMatch.FORMAT.FIELD_COUNT != data.length) {
      final Logger logger = LoggerFactory.getLogger(BlastMatch.class);
      final StringBuilder sb = new StringBuilder();
      for (final String el : data) {
        sb.append(el).append(",");
      }
      logger.error("Incomplete BLAST line " + sb);
      return Optional.empty();
    }

    return Optional.of(new BlastMatch(data[BlastMatch.FORMAT.QSEQID.index()], data[BlastMatch.FORMAT.SSEQID.index()],
                                      Integer.valueOf(data[BlastMatch.FORMAT.QLEN.index()]),
                                      Integer.valueOf(data[BlastMatch.FORMAT.SLEN.index()]),
                                      Double.valueOf(data[BlastMatch.FORMAT.PIDENT.index()]),
                                      Integer.valueOf(data[BlastMatch.FORMAT.LENGTH.index()]),
                                      Integer.valueOf(data[BlastMatch.FORMAT.MISMATCH.index()]),
                                      Integer.valueOf(data[BlastMatch.FORMAT.GAPOPEN.index()]),
                                      Integer.valueOf(data[BlastMatch.FORMAT.QSTART.index()]),
                                      Integer.valueOf(data[BlastMatch.FORMAT.QEND.index()]),
                                      Integer.valueOf(data[BlastMatch.FORMAT.SSTART.index()]),
                                      Integer.valueOf(data[BlastMatch.FORMAT.SEND.index()]),
                                      Double.valueOf(data[BlastMatch.FORMAT.EVALUE.index()]),
                                      Double.valueOf(data[BlastMatch.FORMAT.BITSCORE.index()]),
                                      Boolean.valueOf(data[BlastMatch.FORMAT.SSTRAND.index()].replace("plus", "true")
                                                                                             .replace("minus", "false")
                                                     )
    ));
  }
}
