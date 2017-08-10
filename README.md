# paarsnp
PAARSNP AMR resistance predictor from the CGPS and part of the WGSA public health genomics platform.

PAARSNP provides antimicrobial resistance predictions for a curated set of medically important pathogens from assembled genome sequences (FASTA files). Resistance predictions are based on the presence of both resistance genes (PAAR) and mutations (SNPAR).

[Getting Started](#getting-started)

[With Docker](#running-with-docker)

[Without Docker](#running-directly)

[Building From]

## Getting Started

PAARSNP can be run in several ways, including via Docker. We will provide a public Docker container soon.

It's also possible extend the current AMR libraries or to generate your own AMR libraries to use in PAARSNP (e.g. for currently unsupported species).

### Currently supported method



### Running with Docker


### Running Directly

PAARSNP is a command line tool and currently requires Linux or MacOS, as well as JAVA 8 or higher.

* Get options and help: `java -jar paarsnp-runner.jar`

* Download the build folder

### Requirements for running directly

1. JAVA 8 or higher
1. blastn

### Optional
1. makeblastdb (for compiling from scratch)
