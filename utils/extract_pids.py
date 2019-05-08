import glob
import json
import sys
from collections import defaultdict

import matplotlib.pyplot as plt
import pandas as pd

plt.close('all')

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
    match_df.to_csv(gene + '_matches.csv', index_label='MATCH ID')

    plt.figure()
    pid_df = match_df[['PID']].groupby('PID').size()
    ax = pid_df.plot(kind='bar', fontsize=12, figsize=(15, 10))
    ax.set_xlabel("Percent Identity Against Reference", fontsize=12)
    ax.set_ylabel("Size", fontsize=12)
    plt.savefig(gene + '_pid.png')
    plt.close()

# print(gene, match_df.describe())
