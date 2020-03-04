FROM openjdk:13-jdk-oracle AS builder

ARG MAVEN_VERSION=3.6.3
ARG USER_HOME_DIR="/root"
ARG SHA=c35a1803a6e70a126e80b2b3ae33eed961f83ed74d18fcd16909b2d44d7dada3203f1ffe726c17ef8dcca2dcaa9fca676987befeadc9b9f759967a8cb77181c0
ARG BASE_URL=https://apache.osuosl.org/maven/maven-3/${MAVEN_VERSION}/binaries

RUN mkdir -p /usr/share/maven /usr/share/maven/ref \
  && curl -fsSL -o /tmp/apache-maven.tar.gz ${BASE_URL}/apache-maven-${MAVEN_VERSION}-bin.tar.gz \
  && echo "${SHA}  /tmp/apache-maven.tar.gz" | sha512sum -c - \
  && tar -xzf /tmp/apache-maven.tar.gz -C /usr/share/maven --strip-components=1 \
  && rm -f /tmp/apache-maven.tar.gz \
  && ln -s /usr/share/maven/bin/mvn /usr/bin/mvn

ENV MAVEN_HOME /usr/share/maven
ENV MAVEN_CONFIG "$USER_HOME_DIR/.m2"

#COPY mvn-entrypoint.sh /usr/local/bin/mvn-entrypoint.sh
#COPY settings-docker.xml /usr/share/maven/ref/

#RUN dnf install -y curl

# Download & install BLAST
RUN mkdir /opt/blast \
      && curl ftp://ftp.ncbi.nlm.nih.gov/blast/executables/blast+/2.2.30/ncbi-blast-2.2.30+-x64-linux.tar.gz \
      | tar -zxC /opt/blast --strip-components=1

ENV PATH /opt/blast/bin:$PATH

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

RUN mkdir -p /build

RUN mvn verify

RUN mkdir /paarsnp/ \
    && mv ./build/paarsnp.jar /paarsnp/paarsnp.jar \
    && mv ./build/databases /paarsnp \
    && rm -f /paarsnp/databases/*.fna \
    && mv ./resources/taxid.map /paarsnp/databases/

#FROM openjdk:11-jre-slim as db_builder
#
#RUN mkdir -p /opt/blast/bin \
#    && mkdir -p /build/databases
#
#COPY --from=builder /opt/blast/bin/blastn /opt/blast/bin
#
#COPY --from=builder /build/paarsnp-builder.jar /build/
#
#RUN

FROM openjdk:13-slim

RUN mkdir -p /opt/blast/bin \
    && mkdir /data

COPY --from=builder /opt/blast/bin/blastn /opt/blast/bin

COPY --from=builder /paarsnp /paarsnp

ENV PATH /opt/blast/bin:$PATH

WORKDIR /data

ENTRYPOINT ["java","-jar","/paarsnp/paarsnp.jar"]
