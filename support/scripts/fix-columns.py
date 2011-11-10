#!/usr/bin/env python

import sys

infile_name = 'arabic-words-separated-vowels.csv'
outfile_name = 'arabic-words-separated-vowels2.csv'

def main():
    infile = open(infile_name)
    outfile = open(outfile_name, 'w')

    for line in infile:
        if len(line.split('|')) == 6:
            outfile.write(line)
        elif len(line.split('|')) == 7 and line.split('|')[2] == '':
            outfile.write('|'.join(line.split('|')[0:2] + line.split('|')[3:]))
        else:
            sys.exit('error: line %s' % (line))

    outfile.close()
    infile.close()


if __name__ == '__main__':
    main()
