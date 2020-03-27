import sys
from Bio import SeqIO

fasta = sys.argv[1]
contig_id = sys.argv[2]
location = int(sys.argv[3])
replacement = sys.argv[4]

with open(fasta, 'r') as f_fh:
    sequences = list(SeqIO.parse(f_fh, 'fasta'))

for sequence in sequences:
    if sequence.id == contig_id:
        new_seq = sequence.seq.tomutable()
        print(f'{sequence.id} mutating {location} {new_seq[location]}', file=sys.stderr)
        new_seq[location] = replacement
        sequence.seq = new_seq.toseq()
    SeqIO.write(sequence, sys.stdout, 'fasta')
