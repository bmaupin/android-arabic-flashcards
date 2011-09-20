#!/usr/bin/env python

# $Id$

'''Finds sheddas in words in our database and shows which letter occurs after, 
since shedda + harakat combinations aren't working due to Android arabic issues.
'''

infile_name = '../words/arabic-words.csv'


def main():
    after_shedda = {}
    infile = open(infile_name)
    
    def get_after_shedda(word):
        shedda = word.find(u'\u0651')
        if shedda != -1:
            if shedda == len(word) - 1:
                return
            after = word[shedda + 1]
            
            if after not in after_shedda:
                after_shedda[after] = [0, []]
            after_shedda[after][0] += 1
#            if len(after_shedda[after][1]) < 20:
            after_shedda[after][1].append(count)
            
#                after_shedda[after] = 0
#            after_shedda[after] += 1
            
#            if ord(after) > 1610 and ord(after) != 1614 and ord(after) != 1616:
#                print '%s\t%s\t%s' % (count, word, hex(ord(after)))
    
    count = 0
    for line in infile:
        count += 1
        values = line.strip().split('|')
        # arabic
        get_after_shedda(values[1].decode('utf8'))
        # arabic plural
        get_after_shedda(values[2].decode('utf8'))
        
    for letter in sorted(after_shedda):
#        print '%s\t%s\t%s' % (letter, hex(ord(letter)), after_shedda[letter])
        print '%s\t%s\t%s' % (letter, hex(ord(letter)), after_shedda[letter][0])
        if ord(letter) > 1610:
            print '\t%s' % (after_shedda[letter][1])


# calls the main() function when the script runs
if __name__ == '__main__':
    main()
