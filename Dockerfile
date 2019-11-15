FROM maven:3.6.2-jdk-11 AS builder

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

RUN ["mvn", "verify", "clean", "--fail-never"]

RUN apt-get update && apt-get install -y --no-install-recommends \
		curl \
	&& rm -rf /var/lib/apt/lists/*

# Download & install BLAST
RUN mkdir /opt/blast \
      && curl ftp://ftp.ncbi.nlm.nih.gov/blast/executables/blast+/2.2.30/ncbi-blast-2.2.30+-x64-linux.tar.gz \
      | tar -zxC /opt/blast --strip-components=1

ENV PATH /opt/blast/bin:$PATH

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

RUN mkdir -p /build

RUN mvn clean package

RUN mkdir /paarsnp/ \
    && mv ./build/paarsnp.jar /paarsnp/paarsnp.jar \
    && mv ./build/databases /paarsnp \
    && rm -f /paarsnp/databases/*.fna \
    && mv ./resources/taxid.map /paarsnp/databases/

FROM openjdk:11-jre-slim

RUN mkdir -p /opt/blast/bin \
    && mkdir /data

COPY --from=builder /opt/blast/bin/blastn /opt/blast/bin

COPY --from=builder /paarsnp /paarsnp

ENV PATH /opt/blast/bin:$PATH

WORKDIR /data

ENTRYPOINT ["java","-jar","/paarsnp/paarsnp.jar"]
