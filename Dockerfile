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

COPY . /usr/src/mymaven/

WORKDIR /usr/src/mymaven/

# Run docker build with -v "$(pwd)":/usr/src/mymaven
RUN mvn clean package

RUN mkdir /paarsnp/ \
    && mv /usr/src/mymaven/build/paarsnp.jar /paarsnp/paarsnp.jar \
    && mv /usr/src/mymaven/build/databases /paarsnp

FROM openjdk:8-jre

RUN mkdir -p /opt/blast/bin

COPY --from=builder /opt/blast/bin/blastn /opt/blast/bin

ENV PATH /opt/blast/bin:$PATH

COPY --from=builder /paarsnp /paarsnp

RUN mkdir /data

WORKDIR /data

ENTRYPOINT ["java","-jar","/paarsnp/paarsnp.jar"]
