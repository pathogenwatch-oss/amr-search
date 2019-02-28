from Bio import SeqIO
from collections import defaultdict

records = list(SeqIO.parse("../build/databases/573_paarsnp.fna", "fasta"))

reverse_seqs = defaultdict(list)

for record in records:
    reverse_seqs[str(record.seq)].append(record.id)

for lists in reverse_seqs.values():
    if len(lists) > 1:
        print(lists, file=sys.stdout)
