#!/usr/bin/env python

import sqlite3
import sys

import wx

import cards

debug = True
#cards_db = '/home/user/Documents/personal/android/android-arabic-flashcards/support/cards/cards.db'
#cards_db = '/home/user/workspace/android-arabic-flashcards/support/cards/cards.db'
cards_db = '/home/user/Desktop/cards.db'

CHAPTERS_CHAPTER = 'chapter'
CHAPTERS_CARDID = 'card_ID'

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
            duplicate_id = False
            
            # look for duplicates
            print 'Searching for duplicates...'
            for existing_card in existing_cards:
                match = compare_cards(new_card, existing_card)
                # duplicate_id found 
                if match != False:
                    # update the new card to add with the updated card
                    new_cards[index] = match
                    # update the card in memory
                    new_card = match
                    duplicate_id = existing_card._id
                    # don't compare to any more existing cards
                    break
            
            # look for matches in old_cards
            print 'Searching for matches...'
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
            
            # card is a duplicate, so update the existing card
            if duplicate_id != False:
                # update the database using the id in the duplicate_id
                # variable and the data from new_card
                for attr in cards.Card.ATTRIBUTES:
                    # of course we don't want to update _id, and chapter is 
                    # handled elsewhere
                    if attr == 'chapter' or attr == '_id':
                        continue
                    if hasattr(new_card, attr):
                        # don't bother adding blank attributes
                        if getattr(new_card, attr) != '':
                            c.execute('UPDATE cards SET %s = "%s" WHERE _id = '
                                    '"%s"' % (attr, 
                                    getattr(new_card, attr),
                                    duplicate_id))
                
                conn.commit()        
                new_card._id = duplicate_id
            
            # card is new, so add it to the database
            else:
                # add the required attributes for the new card to the database
                c.execute('INSERT INTO cards (english, arabic) VALUES ("%s", '
                        '"%s")' % (new_card.english, new_card.arabic))
                conn.commit()
                # get the id of the card just added
                c.execute('SELECT _id FROM cards ORDER BY _id DESC LIMIT 1')
                row = c.fetchone()
                new_card._id = row[0]
                
                # add optional attributes for the new card to the database
                for attr in cards.Card.ATTRIBUTES:
                    # of course we don't want to update _id, chapter is 
                    # handled elsewhere, and we've already added english and arabic
                    if (attr == 'chapter' or attr == '_id' or attr == 'english'
                            or attr == 'arabic'): 
                        continue
                    if hasattr(new_card, attr):
                        # don't bother adding blank attributes
                        if getattr(new_card, attr) != '':
                            c.execute('UPDATE cards SET %s = "%s" WHERE _id = '
                                    '"%s"' % (attr, 
                                    getattr(new_card, attr),
                                    new_card._id))
                conn.commit()   
                
                # add the new card to existing_cards to help find dupes within
                # the same curriculum
                existing_cards.append(new_card)

            c.execute('INSERT INTO %s (%s, %s) VALUES ("%s", "%s")' % (
                    curriculum_table,
                    CHAPTERS_CHAPTER,
                    CHAPTERS_CARDID,
                    new_card.chapter,
                    new_card._id
                    ))

# TODO            
            # code here to add the card ID and chapter to curriculum
            # new_card.chapter, card id from duplicate_id or card_id


                
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
        return new
                    
    def possible_match():
        print 'possible match:'
        print '\tnew: %s\t%s' % (cards.prep_arabic(new.arabic), new.english)
        print '\tmatch: %s\t%s' % (cards.prep_arabic(other.arabic), other.english)
        response = ''
        while response.lower() != 'y' and response.lower() != 'n':
            response = raw_input('\tMatch? (y/n): ')
        if response.lower() == 'y':
            app = App(new.arabic, new.english, other.arabic, other.english)
            new.arabic, new.english = app.getOutput()
            
            
            
            '''
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
            '''
            print 'Filling in details...'
            fill_in_details()
            
    # first, compare arabic without stripping vowels
    if cards.compare_strings(new.arabic, other.arabic):
        if cards.compare_strings(new.english, other.english):
            print 'positive match:'
            print '\tnew: %s\t%s' % (cards.prep_arabic(new.arabic), new.english)
            print '\tmatch: %s\t%s' % (cards.prep_arabic(other.arabic), other.english)
            response = ''
            while response.lower() != 'y' and response.lower() != 'n':
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


class Frame(wx.Frame):
    def __init__(self, new_arabic, new_english, match_arabic, match_english, 
                passBack, title = ''):
        super(Frame, self).__init__(title=title, parent=None)
        self.passBack = passBack
        
        gridSizer = wx.FlexGridSizer(rows=5, cols=3, hgap=10, vgap=10)
        # allow horizontal resizing (but not vertical)
        gridSizer.SetFlexibleDirection(wx.HORIZONTAL)
        
        # change the font size, providing default values for the rest
        font = wx.Font(14, wx.FONTFAMILY_DEFAULT, wx.FONTSTYLE_NORMAL,
                wx.FONTWEIGHT_NORMAL)
        
        updatedArabicControl = wx.TextCtrl(self)
        self.updatedArabicControl = updatedArabicControl
        updatedArabicControl.SetFont(font)
        newArabicControl = wx.Button(self, 1, new_arabic)
        newArabicControl.SetFont(font)
        matchArabicControl = wx.Button(self, 1, match_arabic)
        matchArabicControl.SetFont(font)

        updatedEnglishControl = wx.TextCtrl(self)
        self.updatedEnglishControl = updatedEnglishControl
        newEnglishControl = wx.Button(self, 1, new_english)
        matchEnglishControl = wx.Button(self, 1, match_english)
        
        if new_arabic == match_arabic:
            updatedArabicControl.Disable()
            updatedArabicControl.SetValue(new_arabic)
            newArabicControl.Disable()
            matchArabicControl.Disable()
        else:
            newArabicControl.language = 'arabic'
            matchArabicControl.language = 'arabic'
            newArabicControl.Bind(wx.EVT_BUTTON, self.onCardButtonClick)
            matchArabicControl.Bind(wx.EVT_BUTTON, self.onCardButtonClick)
        
        if new_english == match_english:
            updatedEnglishControl.Disable()
            updatedEnglishControl.SetValue(new_english)
            newEnglishControl.Disable()
            matchEnglishControl.Disable()
        else:
            newEnglishControl.language = 'english'
            matchEnglishControl.language = 'english'
            newEnglishControl.Bind(wx.EVT_BUTTON, self.onCardButtonClick)
            matchEnglishControl.Bind(wx.EVT_BUTTON, self.onCardButtonClick)
        
        vbox1 = wx.BoxSizer(wx.VERTICAL)
        hbox1 = wx.BoxSizer(wx.HORIZONTAL)
        vbox1.Add(updatedArabicControl, 1, wx.EXPAND)
        hbox1.Add(vbox1, 1, wx.ALIGN_CENTER)
        
        vbox2 = wx.BoxSizer(wx.VERTICAL)
        hbox2 = wx.BoxSizer(wx.HORIZONTAL)
        vbox2.Add(updatedEnglishControl, 1, wx.EXPAND)
        hbox2.Add(vbox2, 1, wx.ALIGN_CENTER)
        
        okButton = wx.Button(self, 1, 'OK')
        okButton.Bind(wx.EVT_BUTTON, self.onOKButtonClick)

        gridSizer.AddMany( [
                (0, 0),
                (wx.StaticText(self, 1, label="Arabic:"), 0, wx.ALIGN_CENTER),
                (wx.StaticText(self, 1, label="English:"), 0, wx.ALIGN_CENTER),
                (0, 0),
                #(wx.TextCtrl(self), 0, wx.ALIGN_CENTER),
                (hbox1, 1, wx.EXPAND),
                #(wx.TextCtrl(self), 1, wx.ALIGN_CENTER),
                (hbox2, 1, wx.EXPAND),
                (wx.StaticText(self, 1, label="new:"), 0, wx.ALIGN_CENTER),
                #(wx.StaticText(self, 1, label=new_arabic), 0, wx.ALIGN_CENTER),
                (newArabicControl, 0, wx.ALIGN_CENTER),
                #(wx.StaticText(self, 1, label=new_english), 0, wx.ALIGN_CENTER),
                (newEnglishControl, 0, wx.ALIGN_CENTER),
                (wx.StaticText(self, 1, label="match:"), 0, wx.ALIGN_CENTER),
                #(wx.StaticText(self, 1, label=match_arabic), 0, wx.ALIGN_CENTER),
                (matchArabicControl, 0, wx.ALIGN_CENTER),
                #(wx.StaticText(self, 1, label=match_english), 0, wx.ALIGN_CENTER),
                (matchEnglishControl, 0, wx.ALIGN_CENTER),
                ])
        
        # allow the sexond and third columns to grow (horizontally)
        gridSizer.AddGrowableCol(1)
        gridSizer.AddGrowableCol(2)
        
        # set the minimum size of the grid to the default size of the frame
        # problem: still smushes everything to one side of the frame
        #gridSizer.SetMinSize(self.GetSize())
        
        # one big sizer to fit everything else
        sizer = wx.BoxSizer( wx.VERTICAL)
        # add the gridsizer with a border (padding) of 20, allow it to expand
        sizer.Add(gridSizer, 1, wx.EXPAND|wx.ALL, 20)
        sizer.Add(okButton, 0, wx.ALIGN_CENTER|wx.ALL, 20)
        
        # sets the sizer of the frame and the size/fit of the frame to the size of the sizer
        self.SetSizerAndFit(sizer)
        self.Layout()
        
        # increase the width of the frame for extra space 
        self.SetSize(wx.Size(
                self.GetSize().GetWidth() + 200,
                self.GetSize().GetHeight()))
    
    def onCardButtonClick(self, event):
        btn = event.GetEventObject()
        if btn.language == 'arabic':
            self.updatedArabicControl.SetValue(btn.GetLabelText())
        elif btn.language == 'english':
            self.updatedEnglishControl.SetValue(btn.GetLabelText())
    
    def onOKButtonClick(self, event):
        self.passBack.updatedArabic = self.updatedArabicControl.GetValue()
        self.passBack.updatedEnglish = self.updatedEnglishControl.GetValue()
        
        self.Close()


class App(wx.App):
    def __init__ (self, new_arabic, new_english, match_arabic, match_english, 
                parent=None):
        wx.App.__init__(self, False)
        self.frame = Frame(new_arabic, new_english, match_arabic, match_english, 
                passBack=self, title = 'title') #Pass this app in
        self.outputFromFrame = "" #The output from my frame
        
    def getOutput(self):
        self.frame.Show()
        self.MainLoop()
        #return self.outputFromFrame
        return self.updatedArabic, self.updatedEnglish


# calls the main() function when the script runs
if __name__ == '__main__':
    main()

''' TODO:
 - make sure once we add a card, we add it to original_cards for searching dupes
 - output a replacement file for new cards (in case we stop partially through so
   we don't lose progress)
 - add functionality for adding chapters
 - improved functionality for match dialog
     - cancel button?
         - it could return false, and then the script could continue
'''
