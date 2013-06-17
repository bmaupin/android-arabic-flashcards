#!/usr/bin/env python

import sys

import cards

def main():
#    if len(sys.argv) < 4:
#        sys.exit('usage: %s ordered_file file_to_order output_file')
    
    def compare_to_unordered(english):
#        for card in unordered_cards:
        for index, card in enumerate(unordered_cards):
            # only compare cards from the current chapter
            if chapter != card.chapter:
                continue
            
            match = cards.compare_strings(english, card.english)
            if match == True:
#                print 'possible match:'
#                print '\tordered: %s' % (english)
#                print '\tmatch: %s' % (card.english)
#                response = ''
#                while response.lower() != 'y' and response.lower() != 'n':
#                    response = raw_input('\tMatch? (y/n): ')
#                if response.lower() == 'y':
                # use english from ordered chapters, since it should be right
                card.english = english
                output_cards.append(card)
                del unordered_cards[index]
                return
            
            match = cards.compare_strings(english, card.english, partial = True)
            if match == True:
                print 'possible match:'
                print '\tordered: %s' % (english)
                print '\tmatch: %s' % (card.english)
                response = ''
                while response.lower() != 'y' and response.lower() != 'n':
                    response = raw_input('\tMatch? (y/n): ')
                if response.lower() == 'y':
                    # use english from ordered chapters, since it should be right
                    card.english = english
                    output_cards.append(card)
                    del unordered_cards[index]
                    return
        
        # if we get this far, no match was found
        card = cards.Card()
        card.english = english
        card.arabic = ''
        card.chapter = chapter
        output_cards.append(card)


    order = 0
    lines = 0
    leftover_cards = []
    ordered_chapters = {}
    output_cards = []
    infile1 = open(sys.argv[1])
    
    # populate ordered_chapters
    for line in infile1:
        if line.strip != '':
            lines += 1
#        english, arabic, chapter = line.split('\t')
#        english, chapter, ignore = line.split('\t')
        english, chapter = line.split('\t')
        english = english.strip()
#        arabic = arabic.strip()
        chapter = chapter.strip()
        # if chapter is empty, use the last chapter (so I can be lazy in the 
        # spreadsheet and only write the chapter once)
        if chapter == '':
            chapter = old_chapter
        else:
            old_chapter = chapter
        if chapter not in ordered_chapters:
#            ordered_chapters[chapter] = {}
            ordered_chapters[chapter] = []
            order = 1
        else:
            order += 1
        ordered_chapters[chapter].append(english)
#        ordered_chapters[chapter][english] = {'arabic': arabic,
#                                              'order': order}
#        ordered_chapters[chapter][english] = {'order': order}
    infile1.close()
    
    # populate unordered cards
    unordered_cards = cards.process_cards_file(sys.argv[2], '\t', 
            categories = False, chapters = True, genders = False, 
            parts_of_speech = True, plurals = False)
    
    for chapter in ordered_chapters:
# DEBUG
        if chapter != '1': continue
        for english in ordered_chapters[chapter]:
            compare_to_unordered(english)
    
    for index, card in enumerate(output_cards):
        print('%s: %s\t%s\t%s' % (
                index + 1,
                card.english,
                cards.prep_arabic(card.arabic),
                card.chapter))
    
    for index, card in enumerate(unordered_cards):
# DEBUG
        if card.chapter != '1': break
        print('%s: %s\t%s\t%s' % (
                index + 1,
                card.english,
                cards.prep_arabic(card.arabic),
                card.chapter))


# calls the main() function when the script runs
if __name__ == '__main__':
    main()
