package net.cgps.wgsa.paarsnp.core.lib.blast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.function.Function;

public class BlastRowParser implements Function<String[], Optional<BaseBlastMatch>> {

  @Override
  public Optional<BaseBlastMatch> apply(final String[] data) {

    if (PlainBlastMatch.FORMAT.FIELD_COUNT != data.length) {
      final Logger logger = LoggerFactory.getLogger(PlainBlastMatch.class);
      final StringBuilder sb = new StringBuilder();
      for (final String el : data) {
        sb.append(el).append(",");
      }
      logger.error("Incomplete BLAST line " + sb);
      return Optional.empty();
    }

    return Optional.of(new PlainBlastMatch(data[PlainBlastMatch.FORMAT.QSEQID.index()], data[PlainBlastMatch.FORMAT.SSEQID.index()],
                                      Integer.valueOf(data[PlainBlastMatch.FORMAT.QLEN.index()]),
                                      Integer.valueOf(data[PlainBlastMatch.FORMAT.SLEN.index()]),
                                      Double.valueOf(data[PlainBlastMatch.FORMAT.PIDENT.index()]),
                                      Integer.valueOf(data[PlainBlastMatch.FORMAT.LENGTH.index()]),
                                      Integer.valueOf(data[PlainBlastMatch.FORMAT.MISMATCH.index()]),
                                      Integer.valueOf(data[PlainBlastMatch.FORMAT.GAPOPEN.index()]),
                                      Integer.valueOf(data[PlainBlastMatch.FORMAT.QSTART.index()]),
                                      Integer.valueOf(data[PlainBlastMatch.FORMAT.QEND.index()]),
                                      Integer.valueOf(data[PlainBlastMatch.FORMAT.SSTART.index()]),
                                      Integer.valueOf(data[PlainBlastMatch.FORMAT.SEND.index()]),
                                      Double.valueOf(data[PlainBlastMatch.FORMAT.EVALUE.index()]),
                                      Double.valueOf(data[PlainBlastMatch.FORMAT.BITSCORE.index()]),
                                      Boolean.valueOf(data[PlainBlastMatch.FORMAT.SSTRAND.index()].replace("plus", "true")
                                                                                             .replace("minus", "false")
                                                     )
    ));
  }
}
