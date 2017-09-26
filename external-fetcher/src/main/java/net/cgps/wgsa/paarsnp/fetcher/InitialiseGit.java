package net.cgps.wgsa.paarsnp.fetcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;

public class InitialiseGit implements Function<Path, Path> {

  public static final String RESFINDER_URL = "https://bitbucket.org/genomicepidemiology/resfinder_db.git";

  private final Logger logger = LoggerFactory.getLogger(InitialiseGit.class);

  @Override
  public Path apply(final Path parentPath) {

    try {

      if (!Files.exists(parentPath)) {

        Files.createDirectories(parentPath);

      }

      final Path gitPath = Paths.get(parentPath.toString(), "resfinder_db");

      if (Files.isDirectory(parentPath)) {

        this.logger.info("Using git path {}", gitPath.toAbsolutePath().toString());
        final String[] command;

        if (parentPath.toFile().list().length == 0) {

          this.logger.info("Cloning repository." );

          // clone the repo
          command = new String[]{
              "git",
              "-C",
              parentPath.toString(),
              "clone",
              "--depth",
              "1",
              RESFINDER_URL,
          };

        } else {

          this.logger.info("Updating the repository." );
          // Update the repo.
          command = new String[]{
              "git",
              "-C",
              gitPath.toString(),
              "pull",
          };
        }

        try {

          final Process p = new ProcessBuilder(command).start();

          final int result = p.waitFor();
          this.logger.debug("Git result = {}", result);
        } catch (final InterruptedException e) {
          this.logger.error("Failed to pull data from ResFinder using git.");
          throw new RuntimeException(e);
        }

      } else {
        throw new RuntimeException(parentPath.toAbsolutePath().toString() + " already exists but is not a directory.");
      }

      return gitPath;

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
