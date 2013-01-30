#!/usr/bin/env python

import cards


def main():
    ak12inorder = cards.process_cards_file(
            '/home/bmaupin/Desktop/ak12inorder.tsv',
            '\t')
    ak12linguastep = cards.process_cards_file(
            '/home/bmaupin/Desktop/ak12linguastep.tsv',
            '\t',
            parts_of_speech=True)
#    print ak12inorder[0].english
#    print ak12linguastep[0].english


    for card in ak12inorder:
#        print '%s\t%s\t%s' % (card.english, card.arabic, card.chapter)
        '''for card_linguastep in ak12linguastep:
            if cards.compare_strings(card.arabic, card_linguastep.arabic):
                print cards.compare_strings(card.english, card_linguastep.english)
                print '%s\t%s\t%s' % (card.english, cards.prep_arabic(card.arabic), card.chapter)
                print '%s\t%s\t%s\t%s' % (card_linguastep.english, cards.prep_arabic(card_linguastep.arabic), card_linguastep.part, card_linguastep.chapter)
        '''
        '''
        # we need the index so we can remove the card
        for n in range(len(ak12linguastep)):
            # if arabic matches
            if cards.compare_strings(card.arabic, ak12linguastep[n].arabic):
                # if english matches
                if cards.compare_strings(card.english, ak12linguastep[n].english):
#                    print '%s\t%s\t%s' % (card.english, cards.prep_arabic(card.arabic), card.chapter)
#                    print '%s\t%s\t%s\t%s' % (ak12linguastep[n].english, cards.prep_arabic(ak12linguastep[n].arabic), ak12linguastep[n].part, ak12linguastep[n].chapter)
                    del ak12linguastep[n]
        '''
        for card_linguastep in ak12linguastep[:]:
            # if arabic matches
            if cards.compare_strings(card.arabic, card_linguastep.arabic):
                # if english matches
                if cards.compare_strings(card.english, card_linguastep.english):
                    # remove the card
                    ak12linguastep.remove(card_linguastep)
                    # add the part of speech to the inorder card
                    card.part = card_linguastep.part
                    # go on to the next card
                    break
                # if arabic matches but english doesn't
                else:
                    # print them both
                    print '%s\t%s\t%s' % (card.english, cards.prep_arabic(card.arabic), card.chapter)
                    print '%s\t%s\t%s\t%s' % (card_linguastep.english, cards.prep_arabic(card_linguastep.arabic), card_linguastep.part, card_linguastep.chapter)
                    match = raw_input('Is this a match (y/n)? ')
                    if match.strip().lower == 'y':
                        # remove the card
                        ak12linguastep.remove(card_linguastep)
                        # add the part of speech to the inorder card
                        card.part = card_linguastep.part
                        # go on to the next card
                        break
            # if the arabic doesn't match and it's the last card in ak12linguastep
            elif card_linguastep == ak12linguastep[-1]:
                pass
#                    print '%s\t%s\t%s' % (card.english, cards.prep_arabic(card.arabic), card.chapter)
#                    print '%s\t%s\t%s\t%s' % (card_linguastep.english, cards.prep_arabic(card_linguastep.arabic), card_linguastep.part, card_linguastep.chapter) 
                
                 

        
    print len(ak12linguastep)
#    for card_linguastep in ak12linguastep:
#        print '%s\t%s\t%s\t%s' % (card_linguastep.english, cards.prep_arabic(card_linguastep.arabic), card_linguastep.part, card_linguastep.chapter)

    
    
    
'''
print_arabic(arabic):
    arabic = arabic.decode('utf8')
    arabic_to_print = ''
    for char in arabic:
        arabic_to_.print
        sys.stdout.write()
for char in 
'''



if __name__ == '__main__':
    main()