FROM maven:3 AS builder

RUN apt-get update && apt-get install -y --no-install-recommends \
		curl \
		python \
	&& rm -rf /var/lib/apt/lists/*

# Download & install BLAST
RUN mkdir /opt/blast \
      && curl ftp://ftp.ncbi.nlm.nih.gov/blast/executables/blast+/2.2.30/ncbi-blast-2.2.30+-x64-linux.tar.gz \
      | tar -zxC /opt/blast --strip-components=1

ENV PATH /opt/blast/bin:$PATH

ADD *.sh /usr/local/bin/

COPY settings.template.xml /root/.m2/settings.xml

RUN /usr/local/bin/run_replace.sh

RUN mkdir paarsnp-runner \
    && mkdir paarsnp-builder \
    && mkdir paarsnp-lib \
    && mkdir external-fetcher

COPY ./pom.xml ./pom.xml

COPY ./external-fetcher/ ./external-fetcher/

COPY ./paarsnp-runner/ ./paarsnp-runner/

COPY ./paarsnp-builder/ ./paarsnp-builder/

COPY ./paarsnp-lib/ ./paarsnp-lib/

COPY ./build/ ./build/

RUN mvn package dependency:go-offline -U

RUN mkdir /paarsnp/ \
    && mv ./build/paarsnp.jar /paarsnp/paarsnp.jar \
    && mv ./build/databases /paarsnp

FROM openjdk:8-jre

RUN mkdir -p /opt/blast/bin

COPY --from=builder /opt/blast/bin/blastn /opt/blast/bin

ENV PATH /opt/blast/bin:$PATH

COPY --from=builder /paarsnp /paarsnp

RUN mkdir /data

WORKDIR /data

ENTRYPOINT ["java","-jar","/paarsnp/paarsnp.jar"]
