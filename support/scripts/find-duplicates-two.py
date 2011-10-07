#!/usr/bin/env python

# $Id$

'''Use this script to find possible duplicates in two arabic words source files
'''


infile1_name = '../words/arabic-words.csv'
infile2_name = '../words/al-kitaab part 1.csv'


def main():
#    words = {}
    infile1 = open(infile1_name)
    '''
    index = 0
    for line in infile1:
        index += 1
        line = line.strip()
        english, arabic, plural, part, category, gender = line.split('|')
        words[index] = {}
        words[index]['english'] = english
        words[index]['arabic'] = arabic
        words[index]['plural'] = plural
        words[index]['gender'] = gender
    '''
    
    english1 = []
    arabic1 = []
    plural1 = []
#    gender1 = []

    for line in infile1:
        line = line.strip()
        english, arabic, plural, part, category, gender = line.split('|')
        english1.append(english)
        arabic1.append(arabic)
        plural1.append(plural)
#        gender1.append(gender)
        

    infile1.close()
    infile2 = open(infile2_name)

    index = 0
    dupe_count = 0
    for line in infile2:
        duplicate = False
        index += 1
        line = line.strip()
        english, arabic, chapter = line.split('|')

#        if english in english1:
#            print 'possible duplicate (english)'
#            dupe_index = english1.index(english)
#            duplicate = True
#        if arabic in arabic1:
#            print 'possible duplicate (arabic)'
#            dupe_index = arabic1.index(arabic)
#            duplicate = True
        if arabic in plural1:
            print 'possible duplicate (plural1)'
            dupe_index = plural1.index(arabic)
            duplicate = True
        if duplicate == True:
            dupe_count += 1
            print '\t2: %s:\t%s\t%s\t%s' % (index, english, arabic, plural)
            print '\t1: %s:\t%s\t%s\t%s' % (dupe_index, english1[dupe_index], arabic1[dupe_index], plural1[dupe_index])
            
        '''
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
        '''

    infile1.close()

    print '\n%s possible duplicates' % (dupe_count)


if __name__ == '__main__':
    main()
