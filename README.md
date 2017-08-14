# paarsnp
PAARSNP AMR resistance predictor from the CGPS and part of the WGSA public health genomics platform.

PAARSNP provides antimicrobial resistance predictions for a curated set of medically important pathogens from assembled genome sequences (FASTA files). Resistance predictions are based on the presence of both resistance genes (PAAR) and mutations (SNPAR).

[Current Species](#current-species)

[Getting Started](#getting-started)

[With Docker](#running-with-docker)

[Without Docker](#running-directly)

[Output Format](#output-format)

[Extending the AMR databases](#extending-paarsnp)

## Current Species

| Species | NCBI Code |
|---|---|
| Neisseria gonorrhoeae | 485 |
| Staphylococcus aureus | 1280 |
| Salmonella _Typhi_ | 90370 |
| Streptococcus pneumoniae | 1313 |

## Getting Started

PAARSNP can be run in several ways, including via Docker. We will provide a public Docker container soon.

It's also possible extend the current AMR libraries or to generate your own AMR libraries to use in PAARSNP (e.g. for currently unsupported species).

### Docker-based Build (recommended)

Requires:
* Docker
* That is all

```
git clone https://github.com/ImperialCollegeLondon/paarsnp.git
cd paarsnp
docker run -it --rm --name paarsnp -v /var/run/docker.sock:/var/run/docker.sock -v "$(pwd)":/usr/src/mymaven -v maven-repo:/root/.m2 -v ~/.docker:/root/.docker -w /usr/src/mymaven paarsnp-builder mvn package
```

At this point you can use [Docker](#running-with-docker) or run it directly from the [terminal](#running-directly) (requires JAVA 8 to be installed as well).

### Maven Build

Requires:
* git, maven, java 8, makeblastdb (on $PATH)

Optional:
* blastn on $PATH (for running the unit tests)

```
git clone https://github.com/ImperialCollegeLondon/paarsnp.git
cd paarsnp
mvn -Dmaven.test.skip=true install
# (or leave out -Dmaven.test.skip=true if blastn is available)
```

This will configure the BLAST databases and resources that PAARSNP needs.

At this point you can use [Docker](#running-with-docker) or run it directly from the [terminal](#running-directly).

### Running with Docker

To create the Paarsnp runner container, run:

1. cd build
1. docker build -t paarsnp -f runner.DockerFile .

Usage
-----

To run paarsnp on a single Salmonella _Typhi_ (`90370`) FASTA file in the local directory using the container. An output file `assembly_paarsnp.jsn` is created:

`docker run -v $PWD:/data paarsnp java -jar paarsnp.jar -i assembly.fa -s 90370`

To run paarsnp on all FASTA files in the local directory, with an output file for each one:

`docker run -v $PWD:/data paarsnp java -jar paarsnp.jar -i . -s 90370`

NB "/data" is a protected folder for paarsnp, and is ideally used to mount the local drive.

To get the results to STDOUT rather than file:

`docker run -v $PWD:/data paarsnp java -jar paarsnp.jar -i assembly.fa -s 90370 -o`

NB not pretty printed, one record per line

### Running Directly

* The JAR file is `build/paarsnp.runner.jar` and can be moved anywhere. It assumes the database directory is in the same directory, but this can be specified with the `-d` command line option.
* Get options and help: `java -jar paarsnp.jar`
* e.g. A _Staphylococcus aureus_ assembly `java -jar paarsnp.jar -i saureus_assembly.fa -s 1280`

### Output Format

### Extending PAARSNP

#### Naming Docker Builds

Container tags are automatically generated during the build phase by Maven using https://github.com/jgitver/jgitver.

To create a "release tag" (i.e. not appended with "-SNAPSHOT") and push the resulting container to a remote Docker repository:
```
git tag -a -s -m "My message" v1.0.0-RC4
docker run -it --rm --name paarsnp -v /var/run/docker.sock:/var/run/docker.sock -v "$(pwd)":/usr/src/mymaven -v maven-repo:/root/.m2 -v ~/.docker:/root/.docker -w /usr/src/mymaven paarsnp-builder mvn install
```

The Docker repository can be changed from the CGPS default by editing the `<paarsnp.docker-repository>` property in the top level `pom.xml`.
