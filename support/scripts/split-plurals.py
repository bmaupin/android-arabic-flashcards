#!/usr/bin/env python
# coding=utf8

''' Splits out arabic plurals into separate cards
'''

import re
import sys

import cards


def main():
    if len(sys.argv) != 3:
        sys.exit('usage: %s input_file output_file' % 
                 (sys.argv[0]))
    input_filename = sys.argv[1]
    output_filename = sys.argv[2]
    
    input_cards = cards.process_cards_file(input_filename, '\t')
    output_file = open(output_filename, 'w')
    
    for card in input_cards:
        if card.arabic.find(' \xd8\xac. ') != -1:
            if len(card.arabic.split(' \xd8\xac. ')) != 2:
                sys.exit('ERROR: more than one plural'
                         '\t%s' % (card))
            else:
                arabic, arabic_plural = card.arabic.split(' \xd8\xac. ')
                
                english_plural = make_english_words_plural(card.english)
                print 'plural:\t%s' % (english_plural)
                response = raw_input('Press enter if this is correct, '
                        'otherwise enter the correct plural: ')
                if response == 'n':
                    response = raw_input('Press enter if this is correct, '
                            'otherwise enter the correct plural: ')
                if response != '':
                    english_plural = response
                
                output_file.write('%s\t%s\t%s\t%s\n' % (
                        card.english,
                        arabic,
                        card.chapter,
                        'n'))
                output_file.write('%s\t%s\t%s\t%s\n' % (
                        english_plural,
                        arabic_plural,
                        card.chapter,
                        'y'))
                
#            print card
#            sys.exit()
        else:
            output_file.write('%s\t%s\t%s\t%s\n' % (
                    card.english,
                    card.arabic,
                    card.chapter,
                    'n'))
    
    output_file.close()


def make_english_words_plural(english):
    def count_spaces_at_start(word, spaces = 0):
        if word.startswith(' '):
            spaces += 1
            return count_spaces_at_start(word[1:], spaces)
        else:
            return spaces
        
    def count_spaces_at_end(word, spaces = 0):
        if word.endswith(' '):
            spaces += 1
            return count_spaces_at_end(word[:-1], spaces)
        else:
            return spaces
        
    #separators = ' ;,/()'
    separators = ';,/()'
    
    # list to hold pluralized pieces
    new_pieces = []
    # if we're currently processing words in between parentheses
    parentheses = False
    old_pieces = re.split('([%s])' % (separators), english)
    for piece in old_pieces:
        if piece == '':
            continue
        if piece == '(':
            parentheses = True
        elif piece == ')':
            parentheses = False
        
        if piece in separators or parentheses:
        # don't pluralize separators or words between parentheses
        #elif piece in separators or (
        #        piece.startswith('(') and piece.endswith(')')):
            new_pieces.append(piece)
        else:
            # this is some horribly convoluted code to preserve spaces at the 
            # beginning and end of words.  look away!
            spaces_start = count_spaces_at_start(piece)
            spaces_end = count_spaces_at_end(piece)
            
            if spaces_start == 0:
                spaces_start_index = None
            else:
                spaces_start_index = spaces_start
            if spaces_end == 0:
                spaces_end_index = None
            else:
                # make it negative for index
                spaces_end_index = -spaces_end
            
            new_piece = make_english_word_plural(piece[spaces_start_index:spaces_end_index])
            new_pieces.append(' ' * spaces_start + new_piece + ' ' * spaces_end)
            
            '''
            if spaces > 0:
                new_piece = make_english_word_plural(piece[:-spaces])
                new_pieces.append(new_piece + ' ' * spaces)
            else:
                new_pieces.append(make_english_word_plural(piece))
            '''
    
    return ''.join(new_pieces)


def make_english_word_plural(english):
    '''need to fix these:
once, (one) time

graduate fellow; teaching assistant
student (male)
son, boy; child
friend (male), boyfriend
cousin (male, paternal)
college, school (in a university)
grandfather/ ancestor
hour; o'clock; clock, watch

class, classroom; season
area, region
classmate; colleague (male)
friends (female)
other (female)
match, game (sports)
state, province

day off
(an) experience
(a) group of
too busy (to have time) for
young men
(piece of) advice
    '''
    # these are words that we can't make a plural out of in english, but a 
    # plural exists in arabic
    non_plurals = ['actual',
                   'big, large; old (in age)',
                   'different',
                   'eyeglasses',
                   'famous',
                   'first',
                   'foreign',
                   'green',
                   'handsome',
                   'important',
                   'intelligent',
                   'late',
                   'literature',
                   'many',
                   'married',
                   'new',
                   'news',
                   "o'clock",
                   'old, ancient',
                   'once',
                   'outstanding',
                   'powerful, strong',
                   'real',
                   'science',
                   'series',
                   'short',
                   'smart',
                   'snow',
                   'sole, only',
                   'superior',
                   'tall',
                   'that',
                   'true',
                   'weather',
                   'which',
                  ]
    
    special_plurals = {'child': 'children',
                       'man': 'men',
                       'person': 'people',
                       'woman': 'women',
                       'wife': 'wives',
                      }
    
    if english in special_plurals:
        return special_plurals[english]
    '''
    for word in special_plurals:
        if english.find(word) != -1:
            return english.replace(word, special_plurals[word])
    '''
    if english in non_plurals:
        return '%s (pl)' % (english)
    elif english.endswith('y') and english[-2] != 'a' and english[-2] != 'e' \
            and english[-2] != 'i' and english[-2] != 'o' and english[-2] != \
            'u':
        return english[:-1] + 'ies'
    elif (english.endswith('s') or english.endswith('sh') or 
          english.endswith('ch') or english.endswith('x')):
        return english + 'es'
    else:
        return english + 's'
    
    
if __name__ == '__main__':
    main()