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

    for card in ak12inorder[:1]:
#        print '%s\t%s\t%s' % (card.english, card.arabic, card.chapter)
        for card_linguastep in ak12linguastep:
            if cards.compare_strings(card.arabic, card_linguastep.arabic):
                print cards.compare_strings(card.english, card_linguastep.english)
                print '%s\t%s\t%s' % (card.english, card.arabic, card.chapter)
                print '%s\t%s\t%s\t%s' % (card_linguastep.english, card_linguastep.arabic, card_linguastep.part, card_linguastep.chapter)


    
    
    
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