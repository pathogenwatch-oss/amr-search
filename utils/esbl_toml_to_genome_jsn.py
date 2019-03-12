import sys
from collections import defaultdict

import json
import toml

with open(sys.argv[1], 'r') as library_file:
    library = toml.loads(library_file.read())

paar_profiles = defaultdict(set)
snpar_profiles = defaultdict(set)

for mechanism in library['mechanisms']:
    for phenotype in mechanism['phenotypes']:
        for am in phenotype['profile']:
            paar_profiles[am].update(mechanism['members'])

processed_paar = dict()

for am in paar_profiles:
    processed_paar[am] = list()
    for member in paar_profiles[am]:
        processed_paar[am].append({'element': member, 'effect': 'RESISTANT'})

paarsnp_library = dict()

paarsnp_library['paar'] = processed_paar
paarsnp_library['snp'] = {}

paarsnp_library['antibiotics'] = list()

for am in library['antimicrobials']:
    new_am = dict()
    new_am['key'] = am['key']
    new_am['fullName'] = am['name']
    new_am['displayName'] = am['name']
    new_am['antimicrobialClass'] = am['type']
    paarsnp_library['antibiotics'].append(new_am)

print(json.dumps(paarsnp_library))
