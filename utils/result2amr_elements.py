import csv
import glob
import json

import sys

in_dir = sys.argv[1]

elements = set()
profiles = list()
for jsonf in glob.glob(in_dir + '/*.jsn'):
    with open(jsonf, 'r') as jf:
        data = json.load(jf)
    profile = {
        'AssemblyId': data['assemblyId'],
    }
    elements.update(data['paarElementIds'])
    elements.update(data['snparElementIds'])

    for element in data['paarElementIds']:
        profile[element] = True
    for element in data['snparElementIds']:
        profile[element] = True

    profiles.append(profile)

headers = sorted(elements)
headers.insert(0, 'AssemblyId')

writer = csv.DictWriter(sys.stdout, fieldnames=headers)
writer.writeheader()
writer.writerows(profiles)
