import csv
import glob
import json

import sys

in_dir = sys.argv[1]

genes = set()
profiles = list()
for jsonf in glob.glob(in_dir + '/*.jsn'):
    with open(jsonf, 'r') as jf:
        data = json.load(jf)
    profile = {
        'AssemblyId': data['assemblyId'],
    }
    for match in data['matches']:
        genes.add(match['library']['id'])
        profile[match['library']['id']] = match['percentIdentity']

    profiles.append(profile)

headers = sorted(genes)
headers.insert(0, 'AssemblyId')

writer = csv.DictWriter(sys.stdout, fieldnames=headers)
writer.writeheader()
writer.writerows(profiles)
