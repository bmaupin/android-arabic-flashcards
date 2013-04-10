#!/usr/bin/env python
# coding=utf8

import wx

class Frame(wx.Frame):
    def __init__(self):
        super(Frame, self).__init__(title='', parent=None)
        gridSizer = wx.GridSizer(rows=5, cols=3, hgap=5, vgap=5)
        
        
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
        
        updatedArabicControl = wx.TextCtrl(self)
        updatedArabicControl.SetFont(font)
        newArabicControl = wx.Button(self, 1, new_arabic)
        newArabicControl.SetFont(font)
        matchArabicControl = wx.Button(self, 1, match_arabic)
        matchArabicControl.SetFont(font)
        

        
        updatedEnglishControl = wx.TextCtrl(self)
        newEnglishControl = wx.Button(self, 1, new_english)
        matchEnglishControl = wx.Button(self, 1, match_english)
        
        if new_arabic == match_arabic:
            updatedArabicControl.Disable()
            updatedArabicControl.SetValue(new_arabic)
            newArabicControl.Disable()
            matchArabicControl.Disable()
        
        if new_english == match_english:
            updatedEnglishControl.Disable()
            updatedEnglishControl.SetValue(new_english)
            newEnglishControl.Disable()
            matchEnglishControl.Disable()
        
        vbox1 = wx.BoxSizer(wx.VERTICAL)
        hbox1 = wx.BoxSizer(wx.HORIZONTAL)
        vbox1.Add(updatedArabicControl, 1, wx.EXPAND)
        hbox1.Add(vbox1, 1, wx.ALIGN_CENTER)
        
        vbox2 = wx.BoxSizer(wx.VERTICAL)
        hbox2 = wx.BoxSizer(wx.HORIZONTAL)
        vbox2.Add(updatedEnglishControl, 1, wx.EXPAND)
        hbox2.Add(vbox2, 1, wx.ALIGN_CENTER)

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
                (wx.Button(self, 1, 'OK'), 0, wx.ALIGN_CENTER),
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
        self.SetSizer(gridSizer)
        self.Layout()


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
 - get the new and match buttons working
 - need to get whole column to stretch if text is long
 - pass and retrieve values
'''