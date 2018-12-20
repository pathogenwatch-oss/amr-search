import csv
import os
import re
import sys
from pathlib import Path


class Set:

    def __init__(self, name, source):
        self.name = name
        self.source = source
        self.phenotypes = dict()
        self.members = list()

    def as_toml(self) -> str:
        phenotype_str = '[' + ',\n              '.join(
            [phenotype.as_toml() for phenotype in self.phenotypes.values()]) + ']'
        members_str = ', '.join([member.as_toml() for member in set(self.members)])
        return '[[' + self.source + \
               '.sets]]\nname = "' + self.name + \
               '"\nphenotypes = ' + phenotype_str + \
               '\nmembers = [' + members_str + ']'

    def add_member(self, member_name):
        if type(member_name) == str:
            self.members.append(TomlString(member_name))
        else:
            self.members.append(member_name)


class TomlString:

    def __init__(self, string):
        self.string = string

    def __eq__(self, other):
        """Overrides the default implementation"""
        if isinstance(other, TomlString):
            return self.string == other.string
        return False

    def __hash__(self):
        return hash(self.string)

    def as_toml(self) -> str:
        return '"' + self.string + '"'


class SnparMember:

    def __init__(self, gene, variants):
        self.gene = gene
        self.variants = set(variants)

    def as_toml(self):
        return '{gene="' + self.gene + '", variants=[' + \
               ','.join([variant.as_toml() for variant in self.variants]) + ']}'


class Phenotype:

    def __init__(self, effect, profile):
        self.effect = effect
        self.profile = profile
        self.modifiers = []

    def as_toml(self) -> str:
        return '{effect = "' + self.effect + '", profile = [' + ','.join(
            [am.as_toml() for am in self.profile]) + '], modifiers = [' + ','.join(
            [mod.as_toml() for mod in self.modifiers]) + ']}'


class Modifier:

    def __init__(self, name, effect):
        self.name = name
        self.effect = effect

    def as_toml(self) -> str:
        return '{name = "' + self.name + '", effect = "' + self.effect + '"}'


class GeneInfo:

    def __init__(self, name, source, pid, coverage, seq_type):
        self.source = source
        self.seq_type = seq_type
        self.coverage = coverage
        self.pid = pid
        self.name = name
        self.variants = set()
        self.sequence = ''

    def as_toml(self):
        variants_str = '"' if 0 == len(self.variants) else '"\nvariants = [ ' + ','.join(
            variant.as_toml() for variant in self.variants) + ' ]'

        return '[[' + self.source + \
               '.genes]]\nname = "' + self.name + \
               '"\npid = ' + self.pid + \
               '\ncoverage = ' + self.coverage + \
               '\ntype = "' + self.seq_type + \
               variants_str + \
               '\nsequence = "' + self.sequence + \
               '"'


# Deals with multinucleotide mutations
def parse_snpar_mutation(mutation_str: str):
    # print(mutation_str, file=sys.stderr)
    matches = re.search('^([-A-Z]+)(\d+)([-A-Z]+)$', mutation_str)
    wildtype = matches.group(1)
    position = matches.group(2)
    replacement = matches.group(3)

    length = len(wildtype)

    mutation_strings = list()
    for i in range(0, length):
        mut_str = wildtype[i] + str(int(position) + i) + replacement[i]
        # print(mut_str, file=sys.stderr)
        mutation_strings.append(TomlString(mut_str))
    return mutation_strings


def process_paar():
    print('\n[paar]\n')

    sets = dict()
    genes = dict()

    with open(genes_csv, 'r') as genes_fh:
        genes_reader = csv.DictReader(genes_fh)
        for row in genes_reader:

            set_name = row['Resistance Group'] if len(row['Resistance Group']) != 0 else row['Gene Name']

            if set_name not in sets:
                # Initialise the set
                sets[set_name] = Set(set_name, 'paar')

            if row['Resistance Profile'] not in sets[set_name].phenotypes.keys():
                # Initialise new phenotype
                profile_obs = [TomlString(am) for am in row['Resistance Profile'].split(',')]
                sets[set_name].phenotypes[row['Resistance Profile']] = Phenotype(row['Set Effect'], profile_obs)

            if 'RESISTANCE' == row['Effect']:
                # Add resistance element to the set
                sets[set_name].add_member(row['Gene Name'])
            else:
                # Add modifier element (assumes set will have already been initialised.
                sets[set_name].phenotypes[row['Resistance Profile']].modifiers.append(Modifier(row['Gene Name'],
                                                                                               row['Effect']))

            if row['Gene Name'] not in genes.keys():
                # Initialise the gene metadata
                genes[row['Gene Name']] = GeneInfo(row['Gene Name'], 'paar', row['PID Threshold'],
                                                   row['Coverage Threshold'], 'Protein')

    # Now read in sequences from FASTA
    with open(genes_fna, 'r') as gene_seqs_fh:
        current_id = ''

        for line in gene_seqs_fh.readlines():
            if line.startswith('>'):
                current_id = line.rstrip().replace('>', '')
                if current_id not in genes.keys():
                    print(current_id, 'not in csv file but is in FASTA.', file=sys.stderr)
                    raise KeyError
            else:
                genes[current_id].sequence = line.rstrip()

    for resistance_set in sets.keys():
        print(sets[resistance_set].as_toml(), end='\n\n')

    for gene in genes.keys():
        print(genes[gene].as_toml(), end='\n\n')


def process_snpar():
    print('\n[snpar]\n')

    snpar_sets = dict()
    snpar_genes = dict()

    with open(snps_csv, 'r') as snps_fh:
        snps_reader = csv.DictReader(snps_fh)
        for row in snps_reader:

            set_name = row['Resistance Group'] if 0 != len(row['Resistance Group']) else row['Gene Name'] + '_' + \
                                                                                         row['Mutation']

            if set_name not in snpar_sets:
                # Initialise the set
                snpar_sets[set_name] = Set(set_name, 'snpar')

            if row['Resistance Profile'] not in snpar_sets[set_name].phenotypes.keys():
                # Initialise new phenotype
                profile_obs = [TomlString(am) for am in row['Resistance Profile'].split(',')]
                snpar_sets[set_name].phenotypes[row['Resistance Profile']] = Phenotype(row['Set Effect'], profile_obs)

            if 'RESISTANCE' == row['Effect']:
                # Add resistance element to the set
                # At the moment, assuming 1 gene per resistance group (this is true)
                if 0 == len(snpar_sets[set_name].members):
                    snpar_sets[set_name].add_member(
                        SnparMember(row['Gene Name'], parse_snpar_mutation(row['Mutation'])))
                else:
                    snpar_sets[set_name].members[0].variants.update(parse_snpar_mutation(row['Mutation']))
            else:
                # snpar_sets[set_name].members[0].variants.append(TomlString(row['Mutation']))
                # Add modifier element (assumes set will have already been initialised.
                snpar_sets[set_name].phenotypes[row['Resistance Profile']].modifiers.append(Modifier(row['Gene Name'],
                                                                                                     row['Effect']))

            if row['Gene Name'] not in snpar_genes.keys():
                # Initialise the gene metadata
                snpar_genes[row['Gene Name']] = GeneInfo(row['Gene Name'], 'snpar', '80.0', '60.0', 'Protein')
            snpar_genes[row['Gene Name']].variants.update(parse_snpar_mutation(row['Mutation']))

    # Now read in sequences from FASTA
    with open(snps_fna, 'r') as snp_seqs_fh:
        current_id = ''

        for line in snp_seqs_fh.readlines():
            if line.startswith('>'):
                current_id = line.rstrip().replace('>', '')
                if current_id not in snpar_genes.keys():
                    print(current_id, 'not in csv file but is in FASTA.', file=sys.stderr)
                    raise KeyError
            else:
                snpar_genes[current_id].sequence = line.rstrip()

    for resistance_set in snpar_sets.keys():
        print(snpar_sets[resistance_set].as_toml(), end='\n\n')

    for snpar_gene in snpar_genes.keys():
        print(snpar_genes[snpar_gene].as_toml(), end='\n\n')


# Main script
source_dir = Path(sys.argv[1])

label = source_dir.parts[-1]

print('Processing', label, file=sys.stderr)

print('label', '=', label)

# Open the files to map.
am_file = Path(source_dir, Path('ar_agents.csv'))
genes_csv = Path(source_dir, Path('resistance_genes.csv'))
genes_fna = Path(source_dir, Path('resistance_genes.fa'))
snps_csv = Path(source_dir, Path('resistance_variants.csv'))
snps_fna = Path(source_dir, Path('resistance_variants.fa'))

antimicrobials = list()

with open(am_file, 'r') as am_fh:
    am_reader = csv.DictReader(am_fh)
    for row in am_reader:
        antimicrobials.append(
            '{key = "' + row['Name'] + '", type = "' + row['Type'] + '", name = "' + row['Full Name'] + '"}')

am_str = ',\n                  '.join(antimicrobials)

print('antimicrobials = [', am_str, ']', sep='')

if os.path.isfile(genes_csv):
    process_paar()

if os.path.isfile(snps_csv):
    process_snpar()
