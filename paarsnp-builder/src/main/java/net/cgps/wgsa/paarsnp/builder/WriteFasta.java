package net.cgps.wgsa.paarsnp.builder;

import net.cgps.wgsa.paarsnp.core.Constants;
import net.cgps.wgsa.paarsnp.core.lib.AbstractJsonnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.function.Consumer;

public class WriteFasta implements Consumer<LibraryReader.LibraryDataAndSequences> {

  private final Logger logger = LoggerFactory.getLogger(WriteFasta.class);
  private final Path outputDirectory;

  public WriteFasta(final Path outputDirectory) {
    this.outputDirectory = outputDirectory;
  }

  @Override
  public void accept(final LibraryReader.LibraryDataAndSequences libraryDataAndSequences) {

    final String speciesId = libraryDataAndSequences.getPaarsnpLibrary().getVersion().getLabel();

    final Path libraryFile = Paths.get(this.outputDirectory.toString(), speciesId + Constants.JSON_APPEND);
    final String snparLibraryName = speciesId + Constants.LIBRARY_APPEND;
    final Path snparFastaFile = Paths.get(this.outputDirectory.toString(), snparLibraryName + Constants.FASTA_APPEND);

    try (final BufferedOutputStream bw = new BufferedOutputStream(new FileOutputStream(libraryFile.toFile()))) {
      bw.write(AbstractJsonnable.toJson(libraryDataAndSequences.getPaarsnpLibrary()).getBytes());
    } catch (final IOException e) {
      throw new RuntimeException("Unable to serialise to " + libraryFile, e);
    }

    final MakeBlastDB makeBlastDB = new MakeBlastDB(this.outputDirectory);

    try {
      if (!libraryDataAndSequences.getSequences().isEmpty()) {
        Files.write(snparFastaFile, String.join("", libraryDataAndSequences.getSequences().values()).getBytes(), StandardOpenOption.CREATE);
        makeBlastDB.accept(snparLibraryName, snparFastaFile);
      }
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
    this.logger.info("{} files written.", speciesId);
  }
}
