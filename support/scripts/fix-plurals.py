#!/usr/bin/env python
# coding=utf8

import cards

debug = True
plurals_to_fix = ['-ات',
                  '-ون',
                  '-ون/ين',
                  '-ون / ين',
                  ]
prepositions_to_ignore = [' عن',
                          ' من',
                          ]

def main():
    ak12inorder = cards.process_cards_file(
            '/home/bmaupin/Desktop/ak12vocab.tsv',
            '\t',
            chapters = True,
            parts_of_speech = True,
            plurals = True,
            )
    outfile = open('/home/bmaupin/Desktop/ak12vocab-fixed.tsv', 'w')
    
    index = -1
    for card in ak12inorder:
        index += 1
        
        if card.arabic in plurals_to_fix:
            card = fix_arabic_plural(card, ak12inorder[index - 1])

        outfile.write('\t'.join((card.english, card.arabic, card.part, 
                card.chapter, card.plural)))
#        outfile.write('\t'.join((card.english, card.arabic, card.part, 
#                card.plural, card.chapter)))
        outfile.write('\n')
    
    outfile.close()   


def fix_arabic_plural(plural, singular):
    preposition_to_add = False
    
    print 'current word: %s\t%s' % (plural.english, 
            cards.prep_arabic(plural.arabic))
    print 'previous word: %s\t%s' % (singular.english, 
            cards.prep_arabic(singular.arabic))
    response = raw_input('fix? (y/n): ')

    if response.lower() == 'y':
#    if True:
        for preposition in prepositions_to_ignore:
            if singular.arabic.endswith(preposition):
                # chop off the preposition
                singular.arabic = singular.arabic[:-len(preposition)]
                preposition_to_add = preposition
        
        if plural.arabic == '-ات' or plural.arabic == '-ون':
            if plural.arabic == '-ات':
                singular = singular.arabic.decode('utf8')
                # if the arabic doesn't end in ة
                if singular[-1] != u'\u0629':
                    plural.arabic = singular.encode('utf8') + plural.arabic[1:]
                else:
                    # drop the ة
                    plural.arabic = singular[:-1].encode('utf8') + plural.arabic[1:]
                
            elif plural.arabic == '-ون':
                plural.arabic = singular.arabic + plural.arabic[1:]
                  
        elif plural.arabic == '-ون/ين' or plural.arabic == '-ون / ين':
            plural.arabic = singular.arabic + 'ون' + '/' + singular.arabic + 'ين'
    
    # if there's a preposition to add back, do it
    if preposition_to_add:
        plural.arabic += preposition_to_add
    
    if debug:
        print '\tplural: %s' % (cards.prep_arabic(plural.arabic))
    
    return plural
    

# calls the main() function when the script runs
if __name__ == '__main__':
    main()
