#!/usr/bin/env python

'''Use this script to find possible duplicates in one arabic words source file
'''


infile_name = '../words/arabic-words.csv'
outfile_name = 'arabic-words-separated-vowels.csv'


def main():
    infile = open(infile_name)
    outfile = open(outfile_name, 'w')
    
    index = 0
    for line in infile:
        index += 1
#        line = line.strip()
        english, arabic, plural, part, category, gender = line.split('|')

        if plural == '':
            outfile.write(line)
        else:
            # write out the singular line
            outfile.write(('|').join([english, arabic, part, category, gender]))

            english_plural = make_english_plural(english)
            # write out the plural line
            outfile.write(('|').join([english_plural, plural, part, category, gender]))

    outfile.close()
    infile.close()

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
    elif english.endswith('y'):
        return english[:-1] + 'ies'
    elif english.endswith('s'):
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
