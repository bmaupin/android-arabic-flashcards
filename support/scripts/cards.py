#/usr/bin/env python
# coding=utf8

class Card(object):
    def __init__(self):
        pass


def compare_strings(string1, string2):
    # handle blanks in case for some reason we're comparing empty fields
    if string1 == '' and string2 == '':
        return False
    # get rid of arabic vowels if the word is arabic
    string1 = strip_arabic_vowels(string1)
    string2 = strip_arabic_vowels(string2)
    # save ourselves some work :P
    if string1 == string2:
        return True
    # skip 'ون' (plural ending by itself)
    if string1 == '\xd9\x88\xd9\x86' and string2 == '\xd9\x88\xd9\x86':
        return False
    
    separators = [',', ';', '(', ')', ' ']
    
    list1 = split_string(string1, separators)
    list2 = split_string(string2, separators)
    
#    print list1
#    print list2
    
    for item in list1:
        if item in list2:
            # match found
            return True
    
    return False


def split_string(string, separators):
    '''Splits a string by all of the provided separators, returning a list of
    the separated items
    '''
    def split_strings_recursive(strings, separators):
        if len(separators) > 0:
            new_strings = []
            separator = separators.pop()
            for string in strings:
                new_strings.extend(string.split(separator))
            
            return split_strings_recursive(new_strings, separators)
        
        else:
            new_strings = []
            for string in strings:
                # get rid of leading and trailing whitespace
                string = string.strip()
                if string != '':
                    new_strings.append(string)
            
            return new_strings
    
    # don't modify the original separators list
    return split_strings_recursive([string], list(separators))
    

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
