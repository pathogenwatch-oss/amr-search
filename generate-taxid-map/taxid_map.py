import subprocess
import sys
import re
from pathlib import Path

matcher = re.compile('^(\d+).toml$')

1
def extract_genus_id(record):
    return record.lstrip().rstrip().split(' ')[0]


library_directory = Path(sys.argv[1])

taxonkit_path = sys.argv[2] if 2 < len(sys.argv) else 'taxonkit'

library_taxids = set()

for file in library_directory.glob('*.toml'):
    match = matcher.search(str(file.name))
    if match is not None:
        library_taxids.add(match.group(1))

# Genus example (one line from grep)
# mirko-air:bin coriny$ ./taxonkit list --ids 570 --show-rank --show-name | grep "\[genus\]"
# 570 [genus] Klebsiella
# Species example (no output from grep)
# mirko-air:bin coriny$ ./taxonkit list --ids 1280 --show-rank --show-name | grep "\[genus\]"
# mirko-air:bin coriny$
# High level example (lots of output)
# mirko-air:bin coriny$ ./taxonkit list --ids 1224 --show-rank --show-name | grep "\[genus\]" | head
# 262 [genus] Francisella
# 1234547 [genus] Candidatus Nebulobacter
# 1869285 [genus] Allofrancisella
# 330062 [genus] Candidatus Endoecteinascidia

id_map = dict()

for taxid in library_taxids:

    print(taxid, file=sys.stderr)
    command = [taxonkit_path, 'list', '--ids', taxid, '--show-rank', '--show-name']

    lookup_process = subprocess.Popen(command, stdout=subprocess.PIPE)

    genuses = list(filter(lambda x: '[genus]' in x,
                          [line.decode('UTF=8').lstrip().rstrip() for line in lookup_process.stdout.readlines()]))

    id_map[taxid] = taxid

    if len(genuses) == 0:
        # Is below the level of species and so maps to itself
        print('... Below genus', file=sys.stderr)
    else:
        # Is genus or higher level.
        # NB don't overwrite mappings already created above
        for line in genuses:
            print(line, file=sys.stderr)
            genus_id = extract_genus_id(line)
            if genus_id not in id_map:
                id_map[genus_id] = taxid

for taxid in id_map.keys():
    print(taxid, id_map[taxid], file=sys.stdout)
