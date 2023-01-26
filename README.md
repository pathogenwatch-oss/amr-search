# AMRsearch

## About

AMRsearch is primary tool used by Pathogenwatch to genotype and infer AMR resistance phenotype from assembled microbial
genomes.
For each supported species an in-house library of genotypes and inferred phenotypes has been curated in collaboration
with community experts.

Inferred resistance phenotypes are based on the presence of both resistance genes and mutations.
Paarsnp predictions allow for more than one SNP or gene to be required to confer resistance, as well as suppressors.
Resistance can also be classed as "Intermediate" or "Resistant" (i.e "S/I/R").

## Quick start

_Install & run with Docker in a terminal_
```
git clone --recursive --depth=1 https://github.com/pathogenwatch-oss/amr-search
cd amr-search
docker build -t amrsearch .
cd ~/path/to/my/genomes
docker run --rm -v $PWD:/data amrsearch -i my_typhi_genome.fa -s 90370
```

- `my_typhi_genome.fa`: FASTA file of (e.g.) Typhi assembly in local directory.
- `93070`: species code for Typhi.

### Internal only
Note: Between steps 2 & 3, Pathogenwatch developers should replace the git remote submodules URLS with the internal links.
```
git submodule set-url -- libraries/amr-libraries https://github.com/pathogenwatch/amr-libraries
git submodule set-url -- libraries/amr-test-libraries https://github.com/pathogenwatch/amr-test-libraries
```


## Useful Links

[Current Species](#current-species)

[Getting Started](#in-depth)

[Building With Docker](#running--installing-with-docker--recommended-)

[Building Without Docker](#running-directly)

[Output Format](#output-format)

[Extending the AMR databases](#extending-amrsearch)

[Contributing data to AMRsearch](#contributing-to-amrsearch)

## Current Species

| Species                      | NCBI Code |
|------------------------------|-----------|
| _Neisseria gonorrhoeae_      | 485       |
| _Staphylococcus aureus_      | 1280      |
| _Salmonella_ Typhi           | 90370     |
| _Streptococcus pneumoniae_   | 1313      |
| _Klebisiella_                | 570       |
| _Escherichia_                | 561       |
| _Mycobacterium tuberculosis_ | 1773      |
| _Candida auris_              | 498019    |
| _Vibrio cholerae_            | 666       |
| _Campylobacter_              | 194       |

## In depth

AMRsearch can be run in several ways and a fully automated build is provided. 
See instructions below for building AMRsearch with or without Docker

It's also possible extend the current AMR libraries or to generate your own AMR libraries to use in AMRsearch (e.g. for
currently unsupported species).

### Running & Installing with Docker (recommended)

Using Docker takes care of installing dependencies & setting up the databases. It should also run on Windows, Linux &
macOS, though Windows is not directly tested or supported.

To create and install the docker image follow the instructions provided [above](#quick-start).

### From Scratch

If you want to run AMRsearch without Docker, add to or modify the AMRsearch databases, or use an altered version of the
code, follow the instructions below to build AMRsearch with or without Docker.

Building AMRsearch and creating the BLAST databases is automatic using Maven. The Docker process actually runs the Maven
configuration within a container to generate the AMRsearch container.

Requires:

* git, maven, java 8, makeblastdb (on $PATH)

Optional:

* blastn on $PATH (for running the unit tests)

```
git clone --recursive --depth=1 https://github.com/pathogenwatch-oss/amr-search
cd amr-search
mvn -Dmaven.test.skip=true install
# (or leave out -Dmaven.test.skip=true if blastn is available)
```

This will configure the BLAST databases and resources that AMRsearch needs.

At this point you can use [Docker](#running-with-docker) or run it directly from the [terminal](#running-directly).

To create the AMRsearch runner container, run:

1. cd build
2. docker build -t amrsearch -f DockerFile .

#### From Scratch In Depth

The build process runs two internal scripts.

1. `external-fetcher` fetches external resources, such as ResFinder.
2. `paarsnp-builder` runs makeblastdb and assembles the internal database from the files in `build/resources`

* The Maven configuration automates running these scripts, while the Docker configuration provides a reliable
  environment to run them in.
* To completely remove Docker, and run as a usual JAVA programme, comment out the spotify docker-maven plugin in
  the `pom.xml` files.

### Running with Docker

Usage
-----

To run AMRsearch on a single Salmonella _Typhi_ FASTA file in the local directory using the container. An output
file `{assembly}_amrsearch.jsn` is created.

`docker run --rm -v $PWD:/data amrsearch -i assembly.fa -s 90370`

To run AMRsearch on all FASTA files in the local directory, with an output file for each one:

`docker run --rm -v $PWD:/data amrsearch -i . -s 90370`

To provide multiple taxonomic levels (e.g. genus & species) use `-s` for each. AMRsearch will choose the most precise
level it has a library for:

`docker run --rm -v $PWD:/data amrsearch -i . -s 570 -s 573`

If the FASTA folder is in a different directory you can mount it to docker as below.

`docker run --rm -v /full/path/to/FASTAS/:/data amrsearch -i . -s 90370`

NB "/data" is a protected folder for AMRsearch, and is normally used to mount the local drive.

To get the results to STDOUT rather than file:

`docker run --rm -v $PWD:/data amrsearch -i assembly.fa -s 90370 -o`

NB not pretty printed, one record per line

### Running Directly

* The JAR file is `build/paarsnp.runner.jar` and can be moved anywhere. It assumes the database directory is in the same
  directory, but this can be specified with the `-d` command line option.
* Get options and help: `java -jar paarsnp.jar`
* e.g. A _Staphylococcus aureus_ assembly `java -jar paarsnp.jar -i saureus_assembly.fa -s 1280`

### Output Format

Currently only a JSON output is supported.

#### JSON

A complete example of the JSON format can be found in [here](/examples/output.jsn)

The key field of interest to most will be the `resistanceProfile` field. For each antibiotic, the resistance state (
e.g. `RESISTANT`) and resistance groups that have been found are listed.

The individual results and detailed BLAST data can be found in the `paarResult` & `searchResult` fields.

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
        "name": "Ampicillin",
        "type": "Beta-lactam"
      }
    },
```

### Extending AMRsearch

To use your own AMR libraries, the simplest way is to modify the files in the [resources folder](/resources).

Each species has a set of plain text CSV files that define the resistance genes and SNPs. Current species can be
extended by modifying the files, while new species can be added by creating a new directory corresponding to the species
code.

Then run one of the build processes described  the [In depth](#in-depth) section to generate the required inputs &
BLAST databases for AMRsearch. Alternatively, you can run the database builder directly (
e.g.):
`java -jar build/paarsnp-builder.jar -i build/resources -o build/databases`.

Full documentation on the file formats can be found in the [linked documentation](/docs/INPUT_FORMATS.md)

### Contributing to AMRsearch

The "best" way to contribute new resistance genes and SNPs to PAARSNP is to edit the files in
the [resources folder](/resources), and then submit a pull request. Use the instructions
in [Extending AMRsearch](#extending-AMRsearch) and the [linked documentation](/docs/INPUT_FORMATS.md) to add and test the
new resistance markers.

Alternatively, create a GitHub issue with the request or contact the CGPS directly.
