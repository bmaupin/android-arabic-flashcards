#!/usr/bin/env python

'''Find the longest english and arabic words and print them
'''


import re


infile_name = '../words/arabic-words.csv'
# number of longest values to print
number_arabic = 5
number_arabic_unicode = 5
number_english = 150


def main():
    longest_arabic = []
    # english equivalents of the longest arabic values so we can search for them more easily
    longest_arabic_english = []
    longest_arabic_unicode = []
    longest_arabic_unicode_english = []
    longest_english = []

    infile = open(infile_name)

    for line in infile:
        english, arabic = line.split('|')[0:2]

        for word in re.split('[ /]', english):
            if len(longest_english) == 0:
                longest_english.append(word)
            else:
                for index, item in enumerate(longest_english):
                    if len(word.decode('utf8')) > len(item):
                        longest_english.insert(index, word.decode('utf8'))
                        if len(longest_english) > number_english:
                            longest_english.pop()
                        break

        for word in re.split('[ /]', arabic):
            if len(longest_arabic) == 0:
                longest_arabic.append(word)
                longest_arabic_english.append(english)
            else:
                for index, item in enumerate(longest_arabic):
                    if len(word) > len(item):
                        longest_arabic.insert(index, word)
                        longest_arabic_english.insert(index, english)
                        if len(longest_arabic) > number_arabic:
                            longest_arabic.pop()
                            longest_arabic_english.pop()
                        break

            if len(longest_arabic_unicode) == 0:
                longest_arabic_unicode.append(word.decode('utf8'))
                longest_arabic_unicode_english.append(english)
            else:
                for index, item in enumerate(longest_arabic_unicode):
                    if len(word.decode('utf8')) > len(item):
                        longest_arabic_unicode.insert(index, word.decode('utf8'))
                        longest_arabic_unicode_english.insert(index, english)
                        if len(longest_arabic_unicode) > number_arabic_unicode:
                            longest_arabic_unicode.pop()
                            longest_arabic_unicode_english.pop()
                        break
        '''
        if len(longest_arabic_unicode) < number_arabic_unicode:
            longest_arabic_unicode.append(arabic.decode('utf8'))
            longest_arabic_unicode_english.append(english)
        else:
            for index, item in enumerate(longest_arabic_unicode):
                if len(arabic.decode('utf8')) > len(item):
                    longest_arabic_unicode[index] = arabic.decode('utf8')
                    longest_arabic_unicode_english[index] = english
                    break
        '''
    infile.close()

    print 'english:'
    for word in longest_english:
        print word.encode('utf8')
    print '\narabic:'
#    for word in longest_arabic:
#        print len(word)
    for word in longest_arabic_english:
        print word
#    for word in longest_arabic_unicode:
#        print len(word)
    for word in longest_arabic_unicode_english:
        print word

if __name__ == '__main__':
    main()
