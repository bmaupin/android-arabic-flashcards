#!/usr/bin/env python

import wx
class Frame(wx.Frame):
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
        self.frame = Frame(passBack=self, title = 'frrp') #Pass this app in
        self.outputFromFrame = "" #The output from my frame
        
    def getOutput(self):
        self.frame.Show()
        self.MainLoop()
        return self.outputFromFrame

app = App()
app.getOutput()

del(app)
app = App()
app.getOutput()
