import sys
from collections import defaultdict

import toml
import json

with open(sys.argv[1], 'r') as library_file:
    library = toml.loads(library_file.read())

paar_profiles = defaultdict(set)

for amr_set in library['paar']['sets']:
    for phenotype in amr_set['phenotypes']:
        for am in phenotype['profile']:
            paar_profiles[am].update(amr_set['members'])

processed_paar = dict()

for am in paar_profiles:
    processed_paar[am] = list()
    for member in paar_profiles[am]:
        processed_paar[am].append({'element': member, 'effect': 'RESISTANT'})

paarsnp_library = dict()

paarsnp_library['paar'] = processed_paar

ams = list()

for am in library['antimicrobials']:
    new_am = dict()
    new_am['key'] = am['key']
    new_am['fullName'] = am['name']
    new_am['displayName'] = am['name']
    new_am['antimicrobialClass'] = am['type']
    ams.append(new_am)

paarsnp_library['antimicrobial'] = ams

paarsnp_library['snpar'] = {}

print(json.dumps(paarsnp_library))
