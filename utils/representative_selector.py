import re
import sys
from collections import defaultdict

from sortedcontainers import SortedSet

families = set()
family_data = defaultdict(lambda: defaultdict(list))

with open(sys.argv[1], 'r') as f:
    for line in f.readlines():
        row = re.split(r'\s+', line.rstrip())
        familyA = re.sub(r'_\d+$', '', row[0])
        familyB = re.sub(r'_\d+$', '', row[1])
        families.update({familyA, familyB})
        row[2] = float(row[2])
        family_data[familyA][familyB].append(row[0:3])

representatives = defaultdict(set)
distances = defaultdict(dict)

for family1 in family_data.keys():

    if 1 == len(family_data[family1][family1]):
        representatives[family1].add(family_data[family1][family1][0][0])
        nearest_non_family = 0.0
        for family2 in family_data[family1].keys():
            if family1 != family2:
                for row in family_data[family1][family2]:
                    if nearest_non_family < row[2]:
                        nearest_non_family = row[2]

        distances[family_data[family1][family1][0][0]]['NEAREST_OTHER'] = nearest_non_family
    else:
        for row in family_data[family1][family1]:
            representatives[family1].add(row[0])
            if row[0] != row[1]:
                if row[2] < distances[row[0]].get('FURTHEST_SAME', 100.0):
                    distances[row[0]]['FURTHEST_SAME'] = row[2]

        for family2 in family_data[family1].keys():
            if family1 != family2:
                for row in family_data[family1][family2]:
                    if distances[row[0]].get('NEAREST_OTHER', 0.0) < row[2]:
                        distances[row[0]]['NEAREST_OTHER'] = row[2]

for family in SortedSet(representatives.keys()):
    if len(representatives[family]) == 1:
        rep = representatives[family].pop()
        print(
            rep,
            str(distances[rep].get('FURTHEST_SAME', '-')),
            str(distances[rep].get('NEAREST_OTHER', '-')),
            'Y',
            sep=',')
    else:
        for rep in representatives[family]:
            print(
                rep,
                str(distances[rep].get('FURTHEST_SAME', '-')),
                str(distances[rep].get('NEAREST_OTHER', '-')),
                '-',
                sep=',')
