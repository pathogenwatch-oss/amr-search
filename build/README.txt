Getting Started
---------------

To create the Paarsnp runner container, run:

docker build -t paarsnp -f runner.DockerFile .

Usage
-----

To run paarsnp on a single FASTA file in the local directory using the container:

docker run -v $PWD:/data paarsnp java -jar paarsnp.jar -i assembly.fa -s 90370

To run paarsnp on all FASTA files in the local directory:

docker run -v $PWD:/data paarsnp java -jar paarsnp.jar -i . -s 90370 -o

NB "/data" is a blessed folder in paarsnp

To get the results to STDOUT rather than file:

docker run -v $PWD:/data paarsnp java -jar paarsnp.jar -i assembly.fa -s 90370 -o

NB not pretty printed, one record per line

