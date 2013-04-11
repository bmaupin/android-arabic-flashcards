#!/usr/bin/env python
# coding=utf8

import wx

class Frame(wx.Frame):
    def __init__(self):
        super(Frame, self).__init__(title='', parent=None)
        gridSizer = wx.FlexGridSizer(rows=5, cols=3, hgap=10, vgap=10)
        # allow horizontal resizing (but not vertical)
        gridSizer.SetFlexibleDirection(wx.HORIZONTAL)
        
        new_arabic = 'أخبار'
        new_english = 'news'
        match_arabic = 'أخْبار'
        #match_arabic = new_arabic
        match_english = 'news stories'
        #match_english = new_english
        #match_english = 'news stories some really big huge string'
        
        '''
        vbox1 = wx.BoxSizer(wx.VERTICAL)
        hbox1 = wx.BoxSizer(wx.HORIZONTAL)
        hbox1.Add(wx.TextCtrl(self), 1, wx.EXPAND)
        vbox1.Add(hbox1, 1)
        '''
        
        font = wx.Font(14, wx.FONTFAMILY_DEFAULT, wx.FONTSTYLE_NORMAL,
                wx.FONTWEIGHT_NORMAL)
        
        # if we ever need to make this wider than the button:
        # http://stackoverflow.com/a/2455379/399105
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
                (0, 0),
                (0, 0),
                (0, 0),
                (0, 0),
                (okButton, 0, wx.ALIGN_CENTER),
                ])
        
        '''
        gridSizer.Add(wx.StaticText(self, 1, label="Arabic:"))
        gridSizer.Add(wx.StaticText(self, 1, label="English"))
        gridSizer.Add(wx.StaticText(self, 1))
        gridSizer.Add(wx.StaticText(self, 1))
        gridSizer.Add(wx.StaticText(self, 1))
        gridSizer.Add(wx.StaticText(self, 1))
        gridSizer.Add(wx.StaticText(self, 1, label="new:"))
        '''
        
        
        # set the minimum size of the grid to the default size of the frame
        # problem: still smushes everything to one side of the frame
        #gridSizer.SetMinSize(self.GetSize())
        
        # sets the sizer of the frame and the size/fit of the frame to the size of the sizer
        self.SetSizerAndFit(gridSizer)
        self.Layout()
    
    def onCardButtonClick(self, event):
        btn = event.GetEventObject()
        if btn.language == 'arabic':
            self.updatedArabicControl.SetValue(btn.GetLabelText())
        elif btn.language == 'english':
            self.updatedEnglishControl.SetValue(btn.GetLabelText())
    
    def onOKButtonClick(self, event):
        self.Close()
        


app = wx.App(False)
frame = Frame()
frame.Show(True)
app.MainLoop()

import sys
sys.exit()




class OldFrame(wx.Frame):
    def __init__(self, passBack, title = ''):
        super(Frame, self).__init__(title=title, parent=None, size=(-1,100))
        self.passBack = passBack
        
        v = wx.BoxSizer(wx.VERTICAL)
        s1 = wx.BoxSizer(wx.HORIZONTAL)
        s1.AddSpacer(10)
        self.txt = wx.TextCtrl(self)
        s1.Add(self.txt, 1, flag=wx.ALIGN_CENTER)
        s1.AddSpacer(10)
        v.Add(s1, 1, flag=wx.EXPAND)
        s2 = wx.BoxSizer(wx.HORIZONTAL)
        b = wx.Button(self, 1, "OK")
        b.Bind(wx.EVT_BUTTON, self.ok_button_click)
        s2.Add(b)
        v.Add(s2, 1, flag=wx.ALIGN_CENTER)
        self.SetSizer(v)
        self.Layout()
        
    def ok_button_click(self, event):
        self.passBack.outputFromFrame = self.txt.GetValue()
        #self.Destroy()
        self.Close()

class App(wx.App):
    def __init__ (self, parent=None):
        wx.App.__init__(self, False)
        self.frame = Frame(passBack=self, title = 'title') #Pass this app in
        self.outputFromFrame = "" #The output from my frame
        
    def getOutput(self):
        self.frame.Show()
        self.MainLoop()
        return self.outputFromFrame

app = App()
print app.getOutput()

del(app)
app = App()
print app.getOutput()

''' TODO:
 - pass and retrieve values
'''