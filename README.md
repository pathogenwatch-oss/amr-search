# paarsnp
PAARSNP AMR resistance predictor from the CGPS and part of the WGSA public health genomics platform.

PAARSNP provides antimicrobial resistance predictions for a curated set of medically important pathogens from assembled genome sequences (FASTA files). Resistance predictions are based on the presence of both resistance genes (PAAR) and mutations (SNPAR). Paarsnp predictions allow for more than one SNP or gene to be required to confer resistance, as well as suppressors. Resistance can also be classed as "Intermediate" or "Full".

[Current Species](#current-species)

[Getting Started](#getting-started)

[With Docker](#running-with-docker)

[Without Docker](#running-directly)

[Output Format](#output-format)

[Extending the AMR databases](#extending-paarsnp)

[Contributing data to PAARSNP](#contributing-to-paarsnp)

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
docker build -t paarsnp-builder -f Dockerfile .
# The next command actually builds paarsnp as a JAR and as a container
docker run -it --rm --name paarsnp -v /var/run/docker.sock:/var/run/docker.sock -v "$(pwd)":/usr/src/mymaven -v ~/.docker:/root/.docker -w /usr/src/mymaven paarsnp-builder mvn clean package
```
Or, for faster future builds, first create a docker volume (1st command) and use it for future builds (third command):
```
docker volume create --name maven-repo
docker build -t paarsnp-builder -f Dockerfile .
# Only use this command to update paarsnp in the future.
docker run -it --rm --name paarsnp -v /var/run/docker.sock:/var/run/docker.sock -v "$(pwd)":/usr/src/mymaven -v maven-repo:/root/.m2 -v ~/.docker:/root/.docker -w /usr/src/mymaven paarsnp-builder mvn clean
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

To create the Paarsnp runner container, run:

1. cd build
1. docker build -t paarsnp -f DockerFile .

### Running with Docker

Usage
-----

To run paarsnp on a single Salmonella _Typhi_ (`90370`) FASTA file in the local directory using the container. An output file `assembly_paarsnp.jsn` is created.

NB If you used the recommended docker build process, substitute `paarsnp` for `registry.gitlab.com/cgps/wgsa-tasks/paarsnp`.

`docker run --rm -v $PWD:/data paarsnp -i assembly.fa -s 90370`

To run paarsnp on all FASTA files in the local directory, with an output file for each one:

`docker run --rm -v $PWD:/data paarsnp -i . -s 90370`

NB "/data" is a protected folder for paarsnp, and is normally used to mount the local drive.

To get the results to STDOUT rather than file:

`docker run --rm -v $PWD:/data paarsnp -i assembly.fa -s 90370 -o`

NB not pretty printed, one record per line

### Running Directly

* The JAR file is `build/paarsnp.runner.jar` and can be moved anywhere. It assumes the database directory is in the same directory, but this can be specified with the `-d` command line option.
* Get options and help: `java -jar paarsnp.jar`
* e.g. A _Staphylococcus aureus_ assembly `java -jar paarsnp.jar -i saureus_assembly.fa -s 1280`

### Output Format

Currently only a JSON output is supported.

#### JSON

A complete example of the JSON format can be found in [here](/examples/output.jsn)

The key field of interest to most will be the `resistanceProfile` field. For each antibiotic, the resistance state (e.g. `RESISTANT`) and resistance groups that have been found are listed.

The individual results and detailed BLAST data can be found in the `paarResult` & `snparResult` fields.

```
{
      "resistanceState": "RESISTANT",
      "resistanceSets": [
        {
          "effect": "RESISTANT",
          "resistanceSetName": "TEM-1",
          "agents": [
            "AMP"
          ],
          "elementIds": [
            "TEM-1"
          ],
          "modifiers": {}
        }
      ],
      "agent": {
        "name": "AMP",
        "fullName": "Ampicillin",
        "type": "Beta-lactam"
      }
    },
```

### Extending PAARSNP

To use your own AMR libraries, the simplest way is to modify the files in the [resources folder](/build/resources).

Each species has a set of plain text CSV files that define the resistance genes and SNPs. Current species can be extended by modifying the files, while new species can be add by creating a new directory corresponding to the species code.

Then run one of the build processes described in [Getting Started](#getting-started) to generate the required inputs & BLAST databases for PAARSNP. Alternatively, you can run the database builder directly (e.g.) `java -jar build/paarsnp-builder.jar -i build/resources -o build/databases`.

 Full documentation on the file formats can be found in the [linked documentation](/docs/INPUT_FORMATS.md)

### Contributing to PAARSNP

The "best" way to contribute new resistance genes and SNPs to PAARSNP is to edit the files in the [resources folder](/build/resources), and then submit a pull request. Use the instructions in [Extending Paarsnp](#extending-paarsnp) and the [linked documentation](/docs/INPUT_FORMATS.md) to add and test the new resistance markers.

Alternatively, create a GitHub issue with the request or contact CGPS directly.

#### Naming Docker Builds

Container tags are automatically generated during the build phase by Maven using [jgitver](https://github.com/jgitver/jgitver).

To create a "release tag" (i.e. not appended with "-SNAPSHOT") and push the resulting container to a remote Docker repository:
```
git tag -a -m "My message" v1.0.0-RC4
docker run -it --rm --name paarsnp -v /var/run/docker.sock:/var/run/docker.sock -v "$(pwd)":/usr/src/mymaven -v maven-repo:/root/.m2 -v ~/.docker:/root/.docker -w /usr/src/mymaven paarsnp-builder mvn install
```

The Docker repository can be changed from the CGPS default by editing the `<paarsnp.docker-repository>` property in the top level `pom.xml`.
