FROM openjdk:8-jre

RUN apt-get update && apt-get install -y --no-install-recommends \
		curl \
		python \
	&& rm -rf /var/lib/apt/lists/*

# Download & install BLAST
RUN mkdir /opt/blast \
      && curl ftp://ftp.ncbi.nlm.nih.gov/blast/executables/blast+/2.2.30/ncbi-blast-2.2.30+-x64-linux.tar.gz \
      | tar -zxC /opt/blast --strip-components=1

ENV PATH /opt/blast/bin:$PATH

RUN mkdir -p /paarsnp/databases

COPY paarsnp.jar /paarsnp
COPY databases /paarsnp/databases

RUN cd /opt/blast/ && find . ! -name 'blastn' -type f -exec rm -f {} +
RUN cd /opt/blast/ && rm -rf doc

WORKDIR /paarsnp

CMD ["bash"]
