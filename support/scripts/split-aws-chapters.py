#!/usr/bin/env python

# $Id$


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

    
    infile = open(infile_name)
    new_words = open(new_words_name, 'w')
    
    count = 1
    dupe_count = 0
    # chapter, card id
    aws_cards = []
    
    for line in infile:
        values = line.strip().split('|')
        # make sure there's a value for the aws chapter
        if values[6] != '':
            if represents_int(values[0]) and values[1] == '':
                aws_cards.append([values[6], int(values[0]) - dupe_count])
                dupe_count += 1
            else:
                aws_cards.append([values[6], count - dupe_count])
                new_words.write(line)
        count += 1
    
    new_words.close()
    
    aws_chapters = open(aws_chapters_name, 'w')
    for item in aws_cards:
        aws_chapters.write('%s|%s\n' % (item[0], item[1]))
    
    aws_chapters.close()
        
        
#        line.find('|')
        
#        line = line.strip()
        
        
    


# calls the main() function when the script runs
if __name__ == '__main__':
    main()
