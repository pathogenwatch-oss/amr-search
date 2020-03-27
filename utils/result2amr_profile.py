import csv
import glob
import json

import sys

in_dir = sys.argv[1]

profiles = list()
for jsonf in glob.glob(in_dir + '/*.jsn'):
    with open(jsonf, 'r') as jf:
        data = json.load(jf)
    profile = {
        'AssemblyId': data['assemblyId'],
    }
    for agent_profile in data['resistanceProfile']:
        profile[agent_profile['agent']['fullName']] = agent_profile['resistanceState']
    profiles.append(profile)

headers = set(profiles[0].keys())
headers.remove('AssemblyId')
headers = sorted(headers)
headers.insert(0, 'AssemblyId')

writer = csv.DictWriter(sys.stdout, fieldnames=headers)
writer.writeheader()
writer.writerows(profiles)
