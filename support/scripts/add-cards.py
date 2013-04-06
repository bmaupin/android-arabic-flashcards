#!/usr/bin/env python

import sqlite3
import sys

import cards

debug = True
#cards_db = '/home/bmaupin/Documents/personal/android/android-arabic-flashcards/support/cards/cards.db'
cards_db = '/home/user/workspace/android-arabic-flashcards/support/cards/cards.db'

def main():
    conn = sqlite3.connect(cards_db)
    c = conn.cursor()

    curriculum_table = select_curriculum(c)
    
    # read in cards
    existing_cards = []
    for row in c.execute('SELECT * from cards'):
        card = cards.Card()
        card._id, card.english, card.arabic, card.part, card.category, card.gender, card.plural = row
        existing_cards.append(card)
    
    # read in old_cards
    old_cards = []
    for row in c.execute('SELECT * from old_cards'):
        card = cards.Card()
        card._id, card.english, card.arabic, card.part, card.category, card.gender, card.plural = row
        old_cards.append(card)
        
    # read in file for that curriculum
    response = raw_input('Do you wish to read in an additional file for this curriculum? (y/n): ')
    if response == 'y':
        input_filename = raw_input('Enter the full path to the file: ')
        separator = raw_input('What is the separator for this file: ')
        parts_of_speech = raw_input('Does this file contain parts of speech? (y/n): ')
        if parts_of_speech == 'y':
            parts_of_speech = True
        categories = raw_input('Does this file contain categories? (y/n): ')
        if categories == 'y':
            categories = True
        genders = raw_input('Does this file contain genders? (y/n): ')
        if genders == 'y':
            genders = True
        plurals = raw_input('Does this file contain plurals? (y/n): ')
        if plurals == 'y':
            plurals = True
        
        new_cards = cards.process_cards_file(input_filename, separator, 
                categories = categories, chapters = True, genders = genders, 
                parts_of_speech = parts_of_speech, plurals = plurals)
        
        '''
        count = 0
        for card in new_cards:
            if count == 10: break
            count += 1
            print( '%s\t%s\t%s\t%s\t%s' % (card.english, card.arabic, card.part, card.gender, card.chapter))
        '''

        
        for index, new_card in enumerate(new_cards):
            duplicate = False
            
            # look for duplicates
            print 'Searching for duplicates...'
            for existing_card in existing_cards:
                match = compare_cards(new_card, existing_card)
                # duplicate found 
                if match != False:
                    # update the new card to add with the updated card
                    new_cards[index] = match
                    # update the card in memory
                    new_card = match
                    duplicate = existing_card._id
                    # don't compare to any more existing cards
                    break
            
            # look for matches in old_cards
            for old_card in old_cards:
                match = compare_cards(new_card, old_card)
                # match found 
                if match != False:
                    # update the new card to add with the updated card
                    new_cards[index] = match
                    # update the card in memory
                    new_card = match
                    # don't compare to any more old cards
                    break
            
            if duplicate != False:
# TODO
                # code here to update the database using the id in the duplicate
                # variable and the data from new_card
                new_card._id = duplicate
            
            else:
# TODO
                # code here to add the new card to the database
                # then get the id of the card just added
#                new_card._id =
                # add the new card to existing_cards to help find dupes within
                # the same curriculum
                existing_cards.append(new_card)

# TODO            
            # code here to add the card ID and chapter to curriculum
            # new_card.chapter, card id from duplicate or card_id


                
    # other files?
    
    # 1. two modes: manual input and input file
    
    # 1. check cards for duplicates
    # 2. see if we can fill in any details from old_cards

    conn.commit()
    conn.close()
    
    
    ''' comparison process:
    look for an exact arabic match
        yes: look for an exact english match
            yes: prompt to fill in other details
            no: show cards, prompt user for judgment
                yes: prompt to fill in other details
                no: move on
        no: look for a partial arabic match
            yes: show cards, prompt user for judgment
                yes: prompt to fill in other details
                no: move on
            no: move on
    '''
def compare_cards(new, other):
    '''
    Returns: the new card (possibly updated) if a match was found, or False if 
    no match was found
    '''
    
    def fill_in_details():
        # iterate through the attributes of the other card
        for attr in other.__dict__:
            # if the new card doesn't have those attributes or if they are blank
            if attr not in new.__dict__ or getattr(new, attr) == '':
                # we don't want to update the chapter since it will be unique to
                #  each curriculum or _id since we'll handle that elsewhere
                if attr == 'chapter' or attr == '_id':
                    continue
                # copy them from the other card to the new card
                if debug:
                    print('adding %s: %s to new card' % (attr, getattr(other, attr)))
                setattr(new, attr, getattr(other, attr))
        # if we got here, we found a match
#        match = True
        return new
                    
    def possible_match():
        print 'possible match:'
        print '\tnew: %s\t%s' % (new.arabic, new.english)
        print '\tmatch: %s\t%s' % (other.arabic, other.english)
        response = raw_input('\tMatch? (y/n): ')
        if response.lower() == 'y':
            # don't ask this if arabic is identical
            if not cards.compare_strings(new.arabic, other.arabic):
                print('1: Use arabic from new\n'
                      '2: Use arabic from match\n'
                      '3: Flag arabic')
                response = raw_input('Selection: ')
                if response == '2':
                    new.arabic = other.arabic
                elif response == '3':
                    new.arabic += ' FLAG ' + other.arabic
            # don't ask this if english is identical
            if not cards.compare_strings(new.english, other.english):
                print('1: Use english from new\n'
                      '2: Use english from match\n'
                      '3: Flag english')
                response = raw_input('Selection: ')
                if response == '2':
                    new.english = other.english
                elif response == '3':
                    new.english += ' FLAG ' + other.english
            print 'Filling in details...'
            fill_in_details()

    # first, compare arabic without stripping vowels
    if cards.compare_strings(new.arabic, other.arabic):
        if cards.compare_strings(new.english, other.english):
            print 'positive match:'
            print '\tnew: %s\t%s' % (new.arabic, new.english)
            print '\tmatch: %s\t%s' % (other.arabic, other.english)
            response = raw_input('\tFill in details? (y/n): ')
            if response.lower() == 'y':
                fill_in_details()
        else:
            possible_match()
    # then strip arabic vowels and try again
    elif cards.compare_strings(new.arabic, other.arabic, strip_vowels = True):
        possible_match()
    elif cards.compare_strings(new.english, other.english):
        possible_match()
    else:
        if cards.compare_strings(new.arabic, other.arabic, partial = True):
            possible_match()
    
    return False
                    


def select_curriculum(c):
    # get all the tables
    c.execute('select name from sqlite_master where type = "table" and name '
              'LIKE "%_chapters"')
    result = c.fetchall()
    
    tables = []
    
    for row in result:
        table = row[0]
        if table != 'old_aws_chapters':
            tables.append(table)
    
    tables.sort()
    
    print('1. Create a new curriculum\n'
          'Or add to an existing curriculum:')
    item = 2
    for table in tables:
        print ('%s. %s' % (item, table))
        item += 1
    
    response = raw_input('Please choose an option: ')
    if response != '1':
        return tables[int(response) - 2]
        #sys.exit('ERROR: not implemented.  table=%s\n' % (table))
    
    else:
        response = raw_input('Please enter the table name of the new curriculum\n'
                             '(curriculumpartedition, ex: ak12, ab2, aws12): ')
    
    curriculum_table = '%s_chapters' % (response)
    c.execute('CREATE TABLE %s ('
            '"_ID" INTEGER PRIMARY KEY AUTOINCREMENT, '
            '"chapter" INTEGER NOT NULL, '
            '"card_ID" INTEGER NOT NULL '
            ');' % (curriculum_table))
    
    return select_curriculum(c)

# calls the main() function when the script runs
if __name__ == '__main__':
    main()

''' TODO:
 - make sure once we add a card, we add it to original_cards for searching dupes
 - output a replacement file for new cards (in case we stop partially through so
   we don't lose progress)
 - add functionality to deal with dupes
 - add functionality to deal with matches...
'''
