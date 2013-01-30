#!/usr/bin/env python

import sys

def main():
#    if len(sys.argv) < 4:
#        sys.exit('usage: %s ordered_file file_to_order output_file')

    order = 0
    lines = 0
    ordered_chapters = {}
    infile1 = open(sys.argv[1])
    for line in infile1:
        if line.strip != '':
            lines += 1
        english, arabic, chapter = line.split('\t')
        english = english.strip()
        arabic = arabic.strip()
        chapter = chapter.strip()
        if chapter not in ordered_chapters:
            ordered_chapters[chapter] = {}
            order = 1
        else:
            order += 1
        ordered_chapters[chapter][english] = {'arabic': arabic,
                                              'order': order}
    infile1.close()
    
    infile2 = open(sys.argv[2])
    for line in infile2:
        
    
    output_cards = []
    
    
    
    print lines
    
        
    


# calls the main() function when the script runs
if __name__ == '__main__':
    main()
