import glob
import json
import sys
from collections import defaultdict

import pandas as pd

json_dir = sys.argv[1]

matches = defaultdict(list)

for json_file in glob.glob(json_dir + '/*paarsnp.jsn'):

    with open(json_file, 'r') as jf:
        paarsnp = json.load(jf)

    for match in paarsnp['matches']:
        pid = match['percentIdentity']
        coverage = ((match['library']['stop'] - match['library']['start'] + 1) / match['library']['length']) * 100
        name = match['library']['id']
        matches[name].append((pid, coverage))

for gene in matches:
    match_df = pd.DataFrame(matches[gene], columns=['PID', 'COVERAGE %'])
    print(gene, match_df.describe())
