#!/usr/bin/env python

import sys

infile_name = 'arabic-words-separated-vowels.csv'
outfile_name = 'arabic-words-separated-vowels2.csv'
aws_chapters_name = 'aws-chapters-new.csv'
aws_chapters_new_name = 'aws-chapters-new2.csv'


def main():
    if len(sys.argv) != 3:
        sys.exit('Error: must provide card IDs of the original and the duplicate')
    original_id = int(sys.argv[1])
    dupe_id = int(sys.argv[2])

    infile = open(infile_name)
    outfile = open(outfile_name, 'w')

    index = 0

    # first, remove the dupe from the arabic words file
    # go through all the lines in the input file
    for line in infile:
        index += 1
        # write them to the output file if it's not the dupe line
        if index != dupe_id:
            outfile.write(line)
        
    infile.close()
    outfile.close()

    infile = open(aws_chapters_name)
    outfile = open(aws_chapters_new_name, 'w')
    
    for line in infile:
        chapter, cardid = line.strip().split('|')
        cardid = int(cardid)
        # replace all occurrences of the dupe id with the original id
        if cardid == dupe_id:
            cardid = original_id
        # any id greater than the dupe id needs to be decremented
        elif cardid > dupe_id:
            cardid -= 1

        outfile.write('%s|%s\n' % (chapter, cardid))

    outfile.close()
    infile.close() 


if __name__ == '__main__':
    main()

