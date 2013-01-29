#/usr/bin/env python
# coding=utf8

import pyfribidi

'''
class ArabicString(str):
    def __init__(self):
        self._value = None
    def set(self, value):
        self._value = value
    def __repr__(self):
        return self._value
    def __str__(self):
        return pyfribidi.log2vis(self._value)


class Card(object):
    def __init__(self):
        self._arabic = ArabicString()
    @property
    def arabic(self):
        return self._arabic
    @arabic.setter
    def arabic(self, value):
        self._arabic.set(value)
'''
        
class ArabicString(str):
    def __init__(self, value):
        self._value = value
    def __repr__(self):
        return self._value
# TODO: this might cause us major trouble when we just want to write to a file...
    def __str__(self):
        return pyfribidi.log2vis(self._value)


class Card(object):
    @property
    def arabic(self):
        return self._arabic
    @arabic.setter
    def arabic(self, value):
        self._arabic = ArabicString(value)

#c = Card()
#c.arabic = 'الأَدَب'
#c.arabic
#print c.arabic


def process_cards_file(file_name, separator, parts_of_speech=False):
    '''Input file: english should come first, then arabic, then part of speech 
    (optional), then chapter
    '''
    cards = []
    first_line_processed = False
    
    infile = open(file_name)
    for line in infile:
        card = Card()
        if parts_of_speech:
            card.english, card.arabic, card.part, card.chapter = line.strip().split(separator)
        else:
            card.english, card.arabic, card.chapter = line.strip().split(separator)
        if first_line_processed == False:
            if (card.english.lower() == 'english' and 
                str(card.arabic.lower()) == 'arabic'):
                # don't add headings to the list of cards
                continue
        cards.append(card)
        
    infile.close()
        
    return cards


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
