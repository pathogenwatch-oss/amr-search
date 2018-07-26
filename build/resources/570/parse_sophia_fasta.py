import re
import sys

id_regex = re.compile('^(.*)_\w+([_.]\d+)?$')

fasta_file = sys.argv[1]

fasta_out = 'resistance_genes.fa'
csv_out = 'resistance_genes.csv'

aggregated_antimicrobials = set()

with open(fasta_file, 'r') as fasta_fh, open(fasta_out, 'w') as f_out, open(csv_out, 'w') as c_out:
    print('Gene Name,Resistance Group,Set Effect,Effect,Resistance Profile,PID Threshold,Coverage Threshold,Source',
          file=c_out)
    current_id = ''
    current_seq = ''
    for line in fasta_fh:
        if line.startswith('>'):
            if current_id != '':
                print('>' + current_id, current_seq, sep='\n', file=f_out)
                current_seq = ''

            data = line.rstrip().replace('>', '').split(' ')

            print(data[0])
            current_id = id_regex.match(data[0]).group(1)
            # current_id = '_'.join(data[0].split('_')[0:-1])
            if current_id.startswith('cf'):
                continue
            if len(data) < 2:
                print(current_id, 'is broken')
                continue
            amr_list = data[1].split('_')
            aggregated_antimicrobials.update(amr_list)

            print(current_id,
                  '',
                  'RESISTANT,RESISTANCE',
                  '"' + ','.join(amr_list) + '"',
                  '80.0,80.0',
                  '',
                  sep=',',
                  end='\n',
                  file=c_out
                  )
        else:
            current_seq += line.rstrip().replace('-', '')

with open('ar_agents.csv', 'w') as ar_out:
    print('Name,Type,Full Name', file=ar_out)
    for am in aggregated_antimicrobials:
        print(am, am, am, sep=',', file=ar_out)
