duplicates = set()
seen_seqs = dict()

with open('resistance_genes.fa') as in_fh:
    current_id = ''
    for line in in_fh.readlines():
        line = line.rstrip()
        if line.startswith('>'):
            current_id = line.replace('>', '')
        else:
            if line in seen_seqs:
                duplicates.add(seen_seqs[line] + ' - ' + current_id)
            else:
                seen_seqs[line] = current_id

for duplicate in duplicates:
    print(duplicate)
