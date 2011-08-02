#!/usr/bin/env python

# $Id$

'''
'''


infile1_name = '../words/arabic-words.csv'
infile2_name = '/home/bmaupin/Desktop/arabic-words-michelle.csv'
#outfile_name


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
        for item in list1:
            if item not in list2:
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
    
    
#    words = {}
    '''
    english1 = []
    arabic1 = []
    plural1 = []

    infile1 = open(infile1_name)
    index = 0
    for line in infile1:
        index += 1
        line = line.strip()
        english, arabic, plural, type, category, gender, aws_chapter = \
            line.split('|')
        if english != '':
            english1.append(english)
        if arabic != '':
            arabic1.append(arabic)
        if plural != '':
            plural1.append(plural)
#        words1[index] = {}
#        words1[index]['english'] = english
#        words1[index]['arabic'] = arabic
#        words1[index]['plural'] = plural
#        words1[index]['gender'] = gender

        ''
        for this_index in words:
            if this_index == index:
                continue
            if (english != '' and english == words[this_index]['english']) or \
                (arabic != '' and arabic == words[this_index]['arabic']) or \
                (arabic2 != '' and arabic2 != '\xd9\x88\xd9\x86' and arabic2 != '\xd8\xa7\xd8\xaa' and \
                arabic2 == words[this_index]['arabic2']):
#                if (words[this_index]['gender'] == '' or gender == '') or \
#                    words[this_index]['gender'] == gender:
                print '%s possible duplicate of %s' % (index, this_index)
        ''
    infile1.close()
    '''

if __name__ == '__main__':
    main()
