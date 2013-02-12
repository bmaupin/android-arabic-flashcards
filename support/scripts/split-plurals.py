#!/usr/bin/env python
# coding=utf8

''' Splits out arabic plurals into separate cards
'''

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
                
                english_plural = make_english_plural(card.english)
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

    


def make_english_plural(english):
    '''need to fix these:
student (male)
son, boy; children
friend (male), boyfriend
class, classroom; season
area, region
cousin (male, paternal)
college, school (in a university)
classmate; colleague (male)
friends (female)
other (female)
match, game (sports)
state, province
graduate fellow; teaching assistants
grandfather/ ancestors
hour; o'clock; clock, watches
late
wife
science
    '''
    # these are words that we can't make a plural out of in english, but a 
    # plural exists in arabic
    non_plurals = ['news',
                   'eyeglasses',
                   'literature',
                   'married',
                   'first',
                   'sole, only',
                   'new',
                   'tall',
                   'short',
                   'old, ancient',
                   'big, large; old (in age)',
                   'powerful, strong',
                   'handsome',
                   'green',
                   'snow',
                   'weather',
                  ]

# FIXME, replaces many with meny    
    special_plurals = {'woman': 'women',
                       'man': 'men',
                       'child': 'children',
                      }
    
    for word in special_plurals:
        if english.find(word) != -1:
            return english.replace(word, special_plurals[word])
    
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