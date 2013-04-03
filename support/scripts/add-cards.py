#!/usr/bin/env python

import sqlite3
import sys

import cards

cards_db = '/home/bmaupin/Documents/personal/android/android-arabic-flashcards/support/cards/cards.db'

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
        
        for new_card in new_cards:
            # look for duplicates
            # HERE: we need code that will search using SQL; this will include
#            for existing_card in existing_cards:
#                if cards.compare_strings(new_card.arabic, existing_card.arabic):
#                    pass
            
            # look for matches in old_cards
            for old_card in old_cards:
                if cards.compare_strings(new_card.arabic, existing_card.arabic, partial = True):
                    pass
    
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
    
    
                    
    # other files?
    
    # 1. two modes: manual input and input file
    
    # 1. check cards for duplicates
    # 2. see if we can fill in any details from old_cards
    
    
    

    conn.commit()
    conn.close()

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
