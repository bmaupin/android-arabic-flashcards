#!/usr/bin/env python
# coding=utf8

'''Find all instances of ون and ات and add them to the singular to make a plural
'''


infile_name = '../../words/arabic-words.csv'
outfile_name = 'arabic-words-new.csv'


def main():
    current_word = {}
    previous_word = {}
    possible_dupes = 0
    infile = open(infile_name)
    outfile = open(outfile_name, 'w')

    index = 0
    for line in infile:
        index += 1
        current_word['english'], current_word['arabic'], \
                current_word['part'], current_word['category'], \
                current_word['gender'], current_word['plural'] = \
                line.split('|')

        if current_word['arabic'] == 'ون' or current_word['arabic'] == 'ات':
            # do some sanity checks
            if current_word['part'] != previous_word['part'] or \
                    current_word['category'] != previous_word['category'] or \
                    current_word['gender'] != previous_word['gender'] or \
                    current_word['plural'] == previous_word['plural']:
                print 'WARNING:'
                print '%s:\t%s' % (index, current_word['english'])
                print '\t%s' % (previous_word['english'])
                print '\t%s' % (previous_word['arabic'])
            else:
                print '%s:\t%s' % (index, current_word['english'])
                print '\t%s' % (previous_word['english'])
                print '\t%s' % (previous_word['arabic'])

                if current_word['arabic'] == 'ون':
                    print '\t%s' % (previous_word['arabic'] + current_word['arabic'])

                    outfile.write('|'.join((current_word['english'], \
                            previous_word['arabic'] + current_word['arabic'], \
                            current_word['part'], current_word['category'], \
                            current_word['gender'], current_word['plural'])))
                elif current_word['arabic'] == 'ات':
                    singular = previous_word['arabic'].decode('utf8')
                    # make sure the last letter of the singular is ة
                    if singular[-1] != u'\u0629':
                        print 'WARNING:'
                        print '%s:\t%s' % (index, current_word['english'])
                        print '\t%s' % (previous_word['english'])
                        print '\t%s' % (previous_word['arabic'])
                        print ord(singular[-1])
                        continue
                    else:
                        # drop the ة
                        plural = singular[:-1].encode('utf8') + current_word['arabic']
                        print '\t%s' % (plural)

                    outfile.write('|'.join((current_word['english'], plural,
                            current_word['part'], current_word['category'], \
                            current_word['gender'], current_word['plural'])))

        else:
            outfile.write(line)

        previous_word = current_word.copy()



if __name__ == '__main__':
    main()
