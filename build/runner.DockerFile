FROM paarsnp-base:latest

RUN mkdir -p /paarsnp/databases

COPY paarsnp.jar /paarsnp
COPY databases /paarsnp/databases

RUN cd /opt/blast/ && find . ! -name 'blastn' -type f -exec rm -f {} +
RUN cd /opt/blast/ && rm -rf doc

WORKDIR /paarsnp
