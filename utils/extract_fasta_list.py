import sys

from Bio import SeqIO

list_file = sys.argv[1]
fasta_file = sys.argv[2]

records = SeqIO.to_dict(SeqIO.parse(fasta_file, "fasta"))

with open(list_file, 'r') as lf:
    ids = [a.rstrip() for a in lf.readlines()]

selected = [records[name] for name in ids]

SeqIO.write(selected, sys.stdout, 'fasta')
