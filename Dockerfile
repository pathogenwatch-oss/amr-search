FROM maven:3.9.4-eclipse-temurin-21 AS builder

ARG SPECIES_CODE=""
ARG AMR_LIBRARY_VERSION=""
ARG MAVEN_VERSION=3.9.4
ARG USER_HOME_DIR="/root"
ARG BASE_URL=https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries

# Download & install BLAST
RUN mkdir /opt/blast \
      && curl ftp://ftp.ncbi.nlm.nih.gov/blast/executables/blast+/2.11.0/ncbi-blast-2.11.0+-x64-linux.tar.gz \
      | tar -zxC /opt/blast --strip-components=1

ENV PATH=/opt/blast/bin:$PATH

RUN mkdir paarsnp-runner \
    && mkdir paarsnp-builder \
    && mkdir pw-config-utils \
    && mkdir paarsnp-lib

COPY ./pom.xml ./pom.xml

COPY ./paarsnp-lib/pom.xml ./paarsnp-lib/pom.xml

COPY ./paarsnp-builder/pom.xml ./paarsnp-builder/pom.xml

COPY ./paarsnp-runner/pom.xml ./paarsnp-runner/pom.xml

COPY ./pw-config-utils/pom.xml ./pw-config-utils/pom.xml

COPY ./pw-genome-config/pom.xml ./pw-genome-config/pom.xml

RUN ["mvn", "package", "--fail-never"]

# Start of improving the caching of maven builds, but it's complicated by the issue discussed in:
# https://stackoverflow.com/questions/14694139/how-to-resolve-dependencies-between-modules-within-multi-module-project
# https://issues.apache.org/jira/browse/MDEP-516

COPY ./pw-genome-config/ ./pw-genome-config/

COPY ./pw-config-utils/src/ ./pw-config-utils/src/

COPY ./paarsnp-runner/src/ ./paarsnp-runner/src/

COPY ./paarsnp-builder/src/ ./paarsnp-builder/src/

COPY ./paarsnp-lib/src/ ./paarsnp-lib/src/

COPY ./resources ./resources

COPY ./libraries ./libraries

# When supplied by build.sh, record the exact AMR library revision in the
# generated PAARSNP library metadata. This only changes the image build layer.
RUN if [ -n "$AMR_LIBRARY_VERSION" ]; then \
      sed -i '/"source": "PUBLIC"/!b;n;c\    "version": "'"$AMR_LIBRARY_VERSION"'"' resources/libraries.json; \
    fi

# A blank species code retains every library, preserving the all-species image.
# The builder discovers species from numeric TOML filenames, so remove the other
# species definitions before Maven generates the databases.
RUN if [ -n "$SPECIES_CODE" ]; then \
      test -f "/libraries/amr-libraries/${SPECIES_CODE}.toml" \
        || test -f "/libraries/amr-test-libraries/${SPECIES_CODE}.toml"; \
      find /libraries/amr-libraries /libraries/amr-test-libraries \
        -maxdepth 1 -type f -regextype posix-extended \
        -regex '.*/[0-9]+\.toml' ! -name "${SPECIES_CODE}.toml" -delete; \
    fi

RUN mkdir -p /build

RUN mvn verify

RUN mkdir /paarsnp/ \
    && mv ./build/paarsnp.jar /paarsnp/paarsnp.jar \
    && mv ./build/databases /paarsnp \
    && rm -f /paarsnp/databases/*.fna \
    && mv ./resources/taxid.map /paarsnp/databases/

FROM eclipse-temurin:21

RUN mkdir -p /opt/blast/bin \
    && mkdir /data

COPY --from=builder /opt/blast/bin/blastn /opt/blast/bin

COPY --from=builder /paarsnp /paarsnp

ENV PATH=/opt/blast/bin:$PATH

WORKDIR /data

ENTRYPOINT ["java","-jar","/paarsnp/paarsnp.jar"]
