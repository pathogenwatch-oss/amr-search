import glob

for file in glob.glob('*_ids.txt'):
    name = file.replace('_ids.txt', '')
    with open(file, 'r') as in_f:
        for line in in_f.readlines():
            print(line.rstrip().split(' ')[0], name)
