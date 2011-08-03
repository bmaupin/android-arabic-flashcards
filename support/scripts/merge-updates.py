#!/usr/bin/env python

# $Id$

'''Find differences between file2 and file1
'''


infile1_name = '../words/arabic-words-r61.csv'
infile2_name = '/home/bmaupin/Desktop/arabic-words-michelle.csv'


def main():
    def parse_file(filename):
        arabic_list = []
        english_list = []
        plural_list = []
        infile = open(filename)
        index = 0
        for line in infile:
            index += 1
            line = line.strip()
            english, arabic, plural, type, category, gender, aws_chapter = \
                line.split('|')
            if english != '':
                english_list.append(english)
            if arabic != '':
                arabic_list.append(arabic)
            if plural != '':
                plural_list.append(plural)
        infile.close()
        
        return english_list, arabic_list, plural_list
    
    def compare_lists(list1, list2):
        '''Find items in list2 that aren't in list1 (but not vice-versa)
        '''
        
        count = 1
        for item in list2:
            if item not in list1:
                print '%s: %s' % (count, item) 
            count += 1
    
    english1, arabic1, plural1 = parse_file(infile1_name)
    english2, arabic2, plural2 = parse_file(infile2_name)
    
#    print len(english1)
#    print len(english2)
    
    compare_lists(english1, english2)
    print
    compare_lists(arabic1, arabic2)
    print
    compare_lists(plural1, plural2)
    print


if __name__ == '__main__':
    main()
