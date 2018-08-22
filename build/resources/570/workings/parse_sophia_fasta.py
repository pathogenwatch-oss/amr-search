import re
import sys

id_regex = re.compile('^(.*)_\w+([_.]\d+)?$')

agent_map = {'betalactamase gene': 'Betalactamase',
             'class A beta-lactamase': 'Class A Betalactamase',
             'beta_lactamase': 'Betalactamase',
             'betalactamase': 'Betalactamase',
             'carbapenemase': 'Carbapenemase',
             'subclass_B3_metallo-beta-lactamase': 'Subclass B3 Betalactamase',
             'class B betalactamase': 'Class B Betalactamase',
             'metallobetalactamase': 'Metallobetalactamase',
             'beta-lactamase': 'Betalactamase',
             'metallobetalactamase gene': 'Metallobetalactamase',
             'Betalactamase': 'Betalactamase'}

# class_map = {
#     'Betalactamase': 'Betalactamase',
#     'Class A Betalactamase': 'Betalactamase',
#     'Class B Betalactamase': 'Class B Betalactamase',
#     'Subclass B3 Betalactamase': 'Betalactamase',
#     'Metallobetalactamase': 'Metallobetalactamase',
#     'Carbapenemase': 'Carbapenemase'
# }
class_map = dict()

fasta_file = sys.argv[1]
id_list = sys.argv[2]

fasta_out = 'resistance_genes.fa'
csv_out = 'resistance_genes.csv'

# with open('name_to_class_map.lst', 'r') as class_fh:
#     for line in class_fh.readlines():
#         data = line.rstrip().replace('>', '').split(' ')
#         print(data[0])
#         class_map[id_regex.match(data[0]).group(1)] = data[1]

ids = list()
with open(id_list, 'r') as ids_fh:
    for line in ids_fh.readlines():
        # print(line.rstrip())
        # print(part1)
        ids.append(line.rstrip())
        # ids.append(id_regex.match(line.rstrip().replace('>', '').split(' ')[0]).group(1))

aggregated_antimicrobials = set()

# with open(fasta_file, 'r') as fasta_fh, open(fasta_out, 'w') as f_out, open(csv_out, 'w') as c_out:
with open(fasta_file, 'r') as fasta_fh, open(fasta_out, 'w') as f_out:
    # print('Gene Name,Resistance Group,Set Effect,Effect,Resistance Profile,PID Threshold,Coverage Threshold,Source',
    #       file=c_out)
    current_id = ''
    current_seq = ''
    skip = False
    for line in fasta_fh:
        if line.startswith('>'):
            if current_id != '' and current_id in ids:
                print('>' + current_id, current_seq, sep='\n', file=f_out)
            current_seq = ''

            data = line.rstrip().replace('>', '').split(' ')

            # print(data[0])
            current_id = id_regex.match(data[0]).group(1)
            # current_id = '_'.join(data[0].split('_')[0:-1])
            if current_id.startswith('cf'):
                continue
            # if len(data) < 2:
            #     print(current_id, 'is broken')
            #     continue

            if current_id in ids:
                amr_list = current_id
                aggregated_antimicrobials.add(amr_list)
                # print(current_id,
                #       '',
                #       'RESISTANT,RESISTANCE',
                #       '"' + amr_list + '"',
                #       '80.0,80.0',
                #       '',
                #       sep=',',
                #       end='\n',
                #       file=c_out
                #       )
        else:
            current_seq += line.rstrip().replace('-', '').upper()

    if current_id != '' and current_id in ids:
        print('>' + current_id, current_seq, sep='\n', file=f_out)
        current_seq = ''

# with open('ar_agents.csv', 'w') as ar_out:
#     print('Name,Type,Full Name', file=ar_out)
#     for am in aggregated_antimicrobials:
#         print(am, class_map[am], am, sep=',', file=ar_out)
