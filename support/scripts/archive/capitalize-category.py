#!/usr/bin/env python

'''Capitalize the card category
'''


infile_name = '../words/arabic-words.csv'
outfile_name = '../words/arabic-words-new.csv'


def main():
    words = {}
    infile = open(infile_name)
    outfile = open(outfile_name, 'w')
    
    for line in infile:
        split_line = line.split('|')
        split_line[3] = split_line[3].capitalize()
        outfile.write('|'.join(split_line))

    infile.close()
    outfile.close()


if __name__ == '__main__':
    main()
