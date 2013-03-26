#!/usr/bin/env python
# coding=utf8

import cards

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
        if card.arabic == '-ات' or card.arabic == '-ون':
            print 'current word: %s\t%s' % (card.english, 
                    cards.prep_arabic(card.arabic))
            print 'previous word: %s\t%s' % (ak12inorder[index - 1].english, 
                    cards.prep_arabic(ak12inorder[index - 1].arabic))
            response = raw_input('fix? (y/n): ')
            
            if response.lower() == 'y':
                if card.arabic == '-ات':
                    singular = ak12inorder[index - 1].arabic.decode('utf8')
                    # make sure the last letter of the singular is ة
                    if singular[-1] != u'\u0629':
                        print 'WARNING: final character not taa marbuta'
                        print '\tcard not written to output file'
                        print '\t%s' % (ord(singular[-1]))
                        continue
                    else:
                        # drop the ة
                        plural = singular[:-1].encode('utf8') + card.arabic[1:]
                    
                    outfile.write('\t'.join((card.english, plural, card.part, 
                            card.chapter, card.plural)))
                    outfile.write('\n')
                    
                elif card.arabic == '-ون':
                    outfile.write('\t'.join((card.english, 
                            ak12inorder[index - 1].arabic + card.arabic[1:],
                            card.part, card.chapter, card.plural)))
                    outfile.write('\n')
                      
        elif card.arabic == '-ون/ين' or card.arabic == '-ون / ين':
            pass
        
        else:
            outfile.write('\t'.join((card.english, card.arabic, card.part, 
                    card.chapter, card.plural)))
            outfile.write('\n')
    
    outfile.close()   
        
    '''
        if card.arabic.find('-') != -1:
            if card.arabic not in plurals:
                plurals.append(card.arabic)
    
    print plurals
    plurals
    '''

# calls the main() function when the script runs
if __name__ == '__main__':
    main()


''' TODO:
 - verify plurals that are throwing warnings
 - ignore prepositions when converting to plural
        من
        
'''