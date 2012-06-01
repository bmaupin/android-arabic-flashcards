#!/usr/bin/env python

# $Id$

'''Use this script to find possible duplicates in one arabic words source file
'''


#infile_name = '../words/arabic-words.csv'
infile_name = 'arabic-words-separated-vowels.csv'


def main():
    words = {}
    possible_dupes = 0
    infile = open(infile_name)    

    index = 0
    for line in infile:
        index += 1
        line = line.strip()
        # some of the dupes might be due to vowel inconsistencies
        line = strip_arabic_vowels(line)
        english, arabic, part, category, gender, plural = \
            line.split('|')
        words[index] = {}
        words[index]['english'] = english
        words[index]['arabic'] = arabic
        words[index]['gender'] = gender

        for this_index in words:
            if this_index == index:
                continue

            possible_dupe = ''
            if compare_strings(arabic, words[this_index]['arabic']):
                possible_dupe = 'arabic'
#            elif compare_strings(english, words[this_index]['english']):
#                possible_dupe = 'english'

                '''

            english_split = split_string(words[this_index]['english']
            english2_split = []


            if (english != '' and english == words[this_index]['english']) or \
                (arabic != '' and arabic == words[this_index]['arabic']):
#                (arabic2 != '' and arabic2 != '\xd9\x88\xd9\x86' and arabic2 != '\xd8\xa7\xd8\xaa' and \
#                arabic2 == words[this_index]['arabic2']):
#                if (words[this_index]['gender'] == '' or gender == '') or \
#                    words[this_index]['gender'] == gender:
                '''

            if possible_dupe != '':
                possible_dupes += 1
                print 'possible duplicate (%s):' % (possible_dupe)
                print '\t%s:\t%s\t%s\t%s' % (index, english, gender, arabic)
                print '\t%s:\t%s\t%s\t%s' % (this_index, words[this_index]['english'], words[this_index]['gender'], words[this_index]['arabic'])


#                print '%s possible duplicate of %s' % (index, this_index)

    infile.close()

    print '%s possible duplicates found' % (possible_dupes)


def strip_arabic_vowels(line_with_vowels):
    diacritics = [u'\u064e',  # fatha, short a
                  u'\u064b',  # double fatha
                  u'\u0650',  # kasra, short i
                  u'\u064d',  # double kasra
                  u'\u064f',  # damma, short u
                  u'\u064c',  # double damma
                  u'\u0652',  # sukkun, nothing
                  u'\u0651',  # shedda, double
                 ]
    
    line_without_vowels = ''
    line_with_vowels = line_with_vowels.decode('utf8')
    for char in line_with_vowels:
        if char not in diacritics:
            line_without_vowels += char
    
    return line_without_vowels.encode('utf8')


def split_strings(strings, separators):
    if len(separators) > 0:
        new_strings = []
        separator = separators.pop()
        for string in strings:
            new_strings.extend(string.split(separator))
        
        return split_strings(new_strings, separators)
    
    else:
        new_strings = []
        for string in strings:
            # get rid of leading and trailing whitespace
            string = string.strip()
            if string != '':
                new_strings.append(string)
        
        return new_strings


def compare_strings(string1, string2):
    if string1 == '' and string2 == '':
        return False
    # skip 'ون' (plural ending by itself)
    if string1 == '\xd9\x88\xd9\x86' and string2 == '\xd9\x88\xd9\x86':
        return False
    
    separators = [',', ';', '(', ')', ' ']
    
    # don't modify the original separators list
    list1 = split_strings([string1], list(separators))
    list2 = split_strings([string2], list(separators))
    
#    print list1
#    print list2
    
    for item in list1:
        if item in list2:
            # match found
            return True
    
    return False

'''

def split_string(string):
    split_twice = []
    return_list = []
    
    split_once = string.split(',')    
    for item in split_once:
        split_twice.extend(item.split(';'))
    
    for item in split_twice:
        return_list.append(item.strip())
    
    return return_list


def compare_strings(string1, string2):
    if string1 == '' and string2 == '':
        return False
    if string1 == '\xd9\x88\xd9\x86' and string2 == '\xd9\x88\xd9\x86':
        return False

    list1 = split_string(string1)
    list2 = split_string(string2)

    for item in list1:
        if item in list2:
            # match found
            return True

    return False



se = list(se2) 
count = 0
def ss(strings, separators):
    if len(separators) > 0:
        new_strings = []
        separator = separators.pop()
        for string in strings:
            new_strings.extend(string.split(separator))
        
        return ss(new_strings, separators)
    
    else:
        new_strings = []
        for string in strings:
            new_strings.append(string.strip())
        
        return new_strings


   global count
    count += 1; print count
    #print strings
    if len(separators) > 0:
        new_strings = []
        separator = separators.pop()
        for string in strings:
            #count += 1
            #print count
            #print string
            new_strings.extend(string.split(separator))
        
        return ss(new_strings, separators)
    
    else:
        new_strings = []
        for string in strings:
            new_strings.append(string.strip())
        
        return new_strings
'''

if __name__ == '__main__':
    main()
