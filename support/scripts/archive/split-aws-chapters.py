#!/usr/bin/env python

# $Id$

'''Split out the aws chapter column from arabic-words.csv
'''

infile_name = '../words/arabic-words.csv'
new_words_name = 'arabic-words.csv'
aws_chapters_name = 'aws-chapters.csv'


def main():
    def represents_int(s):
        try: 
            int(s)
            return True
        except ValueError:
            return False
    
    def correct_id(old_id):
#        if len(dupes) < 1:
#            return old_id
        count = 0
        for dupe_id in dupes:
            if old_id < dupe_id:
                return old_id - count
            count += 1
#        return old_id - len(dupes)
    
    current_id = 1
#    dupe_count = 0
    dupes = []
    # chapter, card id
    aws_cards = []
    
    infile = open(infile_name)
    new_words = open(new_words_name, 'w')
    
    for line in infile:
        values = line.strip().split('|')
        # make sure there's a value for the aws chapter
        if values[6] != '':
            # if it's a duplicate
            if represents_int(values[0]) and values[1] == '':
                dupes.append(current_id)
                aws_cards.append([values[6], correct_id(int(values[0]))])
#                aws_cards.append([values[6], int(values[0]) - dupe_count])
#                dupe_count += 1
            else:
                aws_cards.append([values[6], current_id - len(dupes)])
                # write the line, dropping the last column
                new_words.write('%s\n' % (line[:line.rfind('|')]))
#                aws_cards.append([values[6], count - dupe_count])
#                new_words.write(line)
        # we need to write the line even if there's no aws chapter value
        else:
            # write the line, dropping the last column
            new_words.write('%s\n' % (line[:line.rfind('|')]))
        current_id += 1
        
    new_words.close()
    
    aws_chapters = open(aws_chapters_name, 'w')
    for item in aws_cards:
        aws_chapters.write('%s|%s\n' % (item[0], item[1]))
    
    aws_chapters.close()
    
    dupes.sort()
    print '%s dupes:' % (len(dupes))
    print dupes
        
        
#        line.find('|')
        
#        line = line.strip()
        
        
    


# calls the main() function when the script runs
if __name__ == '__main__':
    main()
