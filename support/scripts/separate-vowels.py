#!/usr/bin/env python

'''Use this script to find possible duplicates in one arabic words source file
'''

import sys


infile_name = '../words/arabic-words.csv'
outfile_name = 'arabic-words-separated-vowels.csv'
aws_chapters_name = '../words/aws-chapters.csv'
aws_chapters_new_name = 'aws-chapters-new.csv'


def main():
    def represents_int(s):
        try:
            int(s)
            return True
        except ValueError:
            return False

    max_cardid = 0
    chapters = {}
    aws_chapters = open(aws_chapters_name)

    for line in aws_chapters:
        line = line.strip()
        chapter, cardid = line.split('|')
        cardid = int(cardid)
        chapter = int(chapter)
        if cardid > max_cardid:
            max_cardid = cardid
        if chapter not in chapters:
            chapters[chapter] = []
        chapters[chapter].append(cardid)

    aws_chapters.close()

#    print chapters[4]
#    print max_cardid
#    sys.exit()

    '''
    card_chapters = {}
    aws_chapters = open(aws_chapters_name)

    for line in aws_chapters:
        line = line.strip()
        chapter, cardid = line.split('|')
        cardid = int(cardid)
        chapter = int(chapter)
        if cardid not in card_chapters:
            card_chapters[cardid] = []
        card_chapters[cardid].append(chapter)

    aws_chapters.close()
    '''

    infile = open(infile_name)
    outfile = open(outfile_name, 'w')
    
    index = 0
    plurals = 0
    for line in infile:
        index += 1
#        print index

#        if index == 690:
#            print chapters
#            sys.exit()
        line = line.strip()
        english, arabic, plural, part, category, gender = line.split('|')

        if plural == '':
            outfile.write(line + '|n\n')
        else:
#            print index
#            print plurals
#            print plurals
#            print len(card_chapters)
#            plurals += 1
            # write out the singular line
            outfile.write(('|').join([english, arabic, part, category, gender]) + '|n\n')

            for chapter in chapters:
#                print chapter
                for i in range(0, len(chapters[chapter])):
#                    print 'comparing %s' % (i)
                    if chapters[chapter][i] > index + plurals:
#                        print 'greater than'
                        chapters[chapter][i] = chapters[chapter][i] + 1
                try:
                    i = chapters[chapter].index(index + plurals)
                    chapters[chapter].insert(i + 1, index + plurals + 1)
                except ValueError, e:
                    pass

#                    elif chapters[chapter][i] == index:
#                        print 'equal'
#                        chapters[chapter].insert(i, index + 1)
           
            '''

                for cardid in chapters[chapter]:
                    if cardid > 

            for cardid in sorted(card_chapters, reverse=True):
                if cardid >= index + plurals:
            
                    
                    card_chapters[cardid + plurals + 1] = card_chapters[cardid + plurals]
                    
                try:
                    card_chapters[index + plurals + 1] = card_chapters[index + plurals]
                    break
                    
                    #try:
                    card_chapters[cardid + 1] = card_chapters[cardid]
                    
                    except KeyError:
                        print 'index: ' + str(index)
                        print 'plurals: ' + str(plurals)
                        print 'cardid: ' + str(cardid)
                        print 'card_chapters[cardid]: ' + str(card_chapters[cardid])
                        print 'card_chapters[cardid + 1]: ' + str(card_chapters[cardid + 1])
                        print 'card_chapters[index + plurals + 1]: ' + str(card_chapters[index + plurals + 1])
                        print 'card_chapters[index + plurals]: ' + str(card_chapters[index + plurals])
                        sys.exit()

            
#            card_chapters[index + plurals + 1] = 'test'

            '''

            if part != 'noun':
                english_plural = english + ' (pl)'
            else:
                #english_plural = make_english_plural(english)
                english_plural = []
                joined_commas = []
                joined_semicolons = []
                for split_commas in english.split(','):
                    if split_commas.find(';') == -1:
                        split_commas = make_english_plural(split_commas)
                        #joined_commas.append(split_commas)
                        english_plural.append(split_commas)
                    else:
                        for split_semicolons in split_commas.split(';'):
                            split_semicolons = make_english_plural(split_semicolons)
                            joined_semicolons.append(split_semicolons)
                        english_plural.append(';'.join(joined_semicolons))

                english_plural = ','.join(english_plural)
            # write out the plural line
            outfile.write(('|').join([english_plural, plural, part, category, gender]) + '|y\n')

            plurals += 1

    outfile.close()
    infile.close()

#    print chapters

    '''
    chapters = {}
    for cardid in card_chapters:
        for chapter in card_chapters[cardid]:
            if chapter not in chapters:
                chapters[chapter] = []
            chapters[chapter].append(cardid)
    print chapters
    '''

    aws_chapters_new = open(aws_chapters_new_name, 'w')

    for chapter in sorted(chapters):
        for cardid in chapters[chapter]:
            aws_chapters_new.write('%s|%s\n' % (chapter, cardid))

    aws_chapters_new.close()


def make_english_plural(english):
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
    
    '''
    special_plurals = {'woman': 'women',
                       'man': 'men',
                       'child': 'children',
                      }
    
    for word in special_plurals:
        if english.find(word) != -1:
            return english.replace(word, special_plurals[word]
    '''
    
    if english in non_plurals:
        return '%s (pl)' % (english)
    elif english.endswith('y') and english[-2] != 'a' and english[-2] != 'e' \
            and english[-2] != 'i' and english[-2] != 'o' and english[-2] != \
            'u':
        return english[:-1] + 'ies'
    elif english.endswith('s') or english.endswith('sh') or english.endswith('ch'):
        return english + 'es'
    else:
        return english + 's'

    '''
        words[index] = {}
        words[index]['english'] = english
        words[index]['arabic'] = arabic
        words[index]['arabic2'] = arabic2
        words[index]['gender'] = gender

        for this_index in words:
            if this_index == index:
                continue
            if (english != '' and english == words[this_index]['english']) or \
                (arabic != '' and arabic == words[this_index]['arabic']) or \
                (arabic2 != '' and arabic2 != '\xd9\x88\xd9\x86' and arabic2 != '\xd8\xa7\xd8\xaa' and \
                arabic2 == words[this_index]['arabic2']):
#                if (words[this_index]['gender'] == '' or gender == '') or \
#                    words[this_index]['gender'] == gender:
                print '%s possible duplicate of %s' % (index, this_index)

    infile.close()
    '''


if __name__ == '__main__':
    main()
