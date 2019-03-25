FROM maven:3.5-jdk-11 AS builder

RUN apt-get update && apt-get install -y --no-install-recommends \
		curl \
		python \
	&& rm -rf /var/lib/apt/lists/*

# Download & install BLAST
RUN mkdir /opt/blast \
      && curl ftp://ftp.ncbi.nlm.nih.gov/blast/executables/blast+/2.2.30/ncbi-blast-2.2.30+-x64-linux.tar.gz \
      | tar -zxC /opt/blast --strip-components=1

ENV PATH /opt/blast/bin:$PATH

RUN mkdir paarsnp-runner \
    && mkdir paarsnp-builder \
    && mkdir paarsnp-lib \
    && mkdir external-fetcher

# Start of improving the caching of maven builds, but it's complicated by the issue discussed in:
# https://stackoverflow.com/questions/14694139/how-to-resolve-dependencies-between-modules-within-multi-module-project
# https://issues.apache.org/jira/browse/MDEP-516
COPY ./pom.xml ./pom.xml

COPY ./pw-genome-config/ ./pw-genome-config/

COPY ./pw-config-utils/ ./pw-config-utils/

COPY ./paarsnp-runner/ ./paarsnp-runner/

COPY ./paarsnp-builder/ ./paarsnp-builder/

COPY ./paarsnp-lib/ ./paarsnp-lib/

COPY ./resources ./resources

COPY ./libraries ./libraries

RUN mkdir -p /build

RUN mvn clean package dependency:go-offline -U

RUN mkdir /paarsnp/ \
    && mv ./build/paarsnp.jar /paarsnp/paarsnp.jar \
    && mv ./build/databases /paarsnp \
    && mv ./resources/taxid.map /paarsnp/databases/

FROM openjdk:11-jre

RUN mkdir -p /opt/blast/bin

COPY --from=builder /opt/blast/bin/blastn /opt/blast/bin

ENV PATH /opt/blast/bin:$PATH

COPY --from=builder /paarsnp /paarsnp

RUN mkdir /data

WORKDIR /data

ENTRYPOINT ["java","-jar","/paarsnp/paarsnp.jar"]
