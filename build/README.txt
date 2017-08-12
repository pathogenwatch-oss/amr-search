Getting Started With Running Paarsnp
------------------------------------


Building Paarsnp with Docker
----------------------------

```
# Optional but recommended to reduce future build times.
docker volume create --name maven-repo

# In the root of the project.
docker run -it --rm --name paarsnp -v /var/run/docker.sock:/var/run/docker.sock -v "$(pwd)":/usr/src/mymaven -v maven-repo:/root/.m2 -v ~/.docker:/root/.docker -w /usr/src/mymaven paarsnp-builder mvn clean install
```

This will populate the "build" folder with all the resources for building the Docker container

To create the Paarsnp runner container, run:

```
cd build
docker build -t paarsnp -f runner.DockerFile .
```

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


