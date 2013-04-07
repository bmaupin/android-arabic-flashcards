#/usr/bin/env python
# coding=utf8

import re

import pyfribidi

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
'''


class Card(object):
    def __str__(self):
        return '%s\t%s' % (self.english, prep_arabic(self.arabic))
        

def process_cards_file(file_name, separator, categories = False, 
        chapters = False, genders = False, parts_of_speech = False, 
        plurals = False):
    '''Input file: english should come first, then arabic, then part of speech 
    (optional), then category (optional), then gender (optional), then plural 
    (optional), then chapter (optional)
    '''
    cards = []
    first_line_processed = False
    
    columns = ['english', 'arabic']
    if parts_of_speech == True:
        columns.append('part')
    if categories == True:
        columns.append('category')
    if genders == True:
        columns.append('gender')
    if plurals == True:
        columns.append('plural')
    if chapters == True:
        columns.append('chapter')
    
    infile = open(file_name)
    for line in infile:
        card = Card()
        
        ''' strip whitespace of the line, split it by the separator, then 
        enumerate through the generated list, returning the index of each item 
        and the item
        '''
        for index, item in enumerate(line.strip().split(separator)):
            ''' for each value of the input file line, set the attribute of the 
            card object, removing whitespace
            '''
            setattr(card, columns[index], item.strip())

        '''
        if parts_of_speech == True:
            if plurals == True:
                card.english, card.arabic, card.part, card.chapter, card.plural = line.strip().split(separator)
            else:
                card.english, card.arabic, card.part, card.chapter = line.strip().split(separator)
        else:
            if plurals == True:
                card.english, card.arabic, card.chapter, card.plural = line.strip().split(separator)
            else:
                card.english, card.arabic, card.chapter = line.strip().split(separator)
        
        # iterate through each card attribute and remove whitespace
        for attr in card.__dict__:
            card.__setattr__(attr, card.__getattribute__(attr).strip())
        '''
        if first_line_processed == False:
            if (card.english.lower() == 'english' and 
                str(card.arabic.lower()) == 'arabic'):
                # don't add headings to the list of cards
                continue
        cards.append(card)
        
    infile.close()
        
    return cards


def compare_strings(string1, string2, strip_vowels = False, partial = False):
    '''Compare two strings
    If partial is True, look for partial matches as well 
    '''
    # handle blanks in case for some reason we're comparing empty fields
    if string1 == '' and string2 == '':
        return False
    # strip whitespace just in case
    string1 = string1.strip()
    string2 = string2.strip()
    # convert both strings to unicode for comparison
    if not isinstance(string1, unicode):
        string1 = string1.decode('utf8')
    if not isinstance(string2, unicode):
        string2 = string2.decode('utf8')
    
    if strip_vowels == True:
        # get rid of arabic vowels if the word is arabic
        string1 = strip_arabic_vowels(string1)
        string2 = strip_arabic_vowels(string2)
    # save ourselves some work :P
    if string1 == string2:
        return True
    # skip 'ون' (plural ending by itself)
# TODO: somehow or other this keeps throwing warnings, as if string1 and 2 aren't unicode
    print string1
    
    if string1 == u'\u0648\u0646' and string2 == u'\u0648\u0646':
        return False
    
    if partial:
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


def prep_arabic(string):
    '''Prepares strings with arabic in them for being printed to the terminal
    '''
    return pyfribidi.log2vis(string)


'''TODO: there's a much better way to do this using re.split()
something like this:
re.split('[;,]', "hour; o'clock; clock, watch")

then run str.strip() on the individual words

or re.split('[;, ]'...
and drop words that == ''
'''
def split_string_old(string, separators):
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


def split_string(string, separators):
    separators = '[%s]' % (separators)
    split = []
    for item in re.split(separators, string):
        if item != '':
            split.append(item)
    
    return split


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
    # don't try to decode a string that's already been decoded
    if not isinstance(line_with_vowels, unicode):
        line_with_vowels = line_with_vowels.decode('utf8')
    for char in line_with_vowels:
        if char not in diacritics:
            line_without_vowels += char
    
    return line_without_vowels.encode('utf8')
