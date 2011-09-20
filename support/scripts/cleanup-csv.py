#!/usr/bin/env python

# $Id$

'''Cleans up CSV files exported from OpenOffice spreadsheets, removing blank
lines and combining split lines
'''


infile_name = '/home/bmaupin/Desktop/al-kitaab part 1.csv'
outfile_name = '/home/bmaupin/Desktop/al-kitaab part 1 fixed.csv'

def main():
    lines = []
    infile = open(infile_name)
    split_line = False

    for line in infile:
        line = line.strip()
        if line == '':
            continue
        
        if line.endswith('|') or line.find('|') == -1:
            if split_line == True:
                lines[-1] += line
            else:
                split_line = True
                lines.append(line)
        elif split_line:
            split_line = False
            lines[-1] += line
        else:
            lines.append(line)

    infile.close()

    # see if any lines are messed up
    count = 0

    for line in lines:
        if len(line.split('|')) != 3:
            print '%s:\t%s' % (count, line)
        count += 1

    # manual fix for a couple lines
    
    lines[600] += lines[601]
    del lines[601]
    del lines[0]
    

    outfile = open(outfile_name, 'w')
    for line in lines:
        outfile.write('%s\n' % (line))
    outfile.close()

if __name__ == '__main__':
    main()
