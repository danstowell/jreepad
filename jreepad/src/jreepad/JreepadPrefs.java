/*
           Jreepad - personal information manager.
           Copyright (C) 2004 Dan Stowell

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

The full license can be read online here:

           http://www.gnu.org/copyleft/gpl.html
*/

package jreepad;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Vector;

/*

A class to hold Jreepad's preferences - and hopefully to store them on disk in a nice 
permanent way which can be carried across from different versions

*/

public class JreepadPrefs implements Serializable
{
  File openLocation, saveLocation, importLocation, exportLocation, backupLocation;
  
  int autoSavePeriod;
  boolean autoSave;
  
  public static final int VIEW_BOTH = 0;
  public static final int VIEW_TREE = 1;
  public static final int VIEW_ARTICLE = 2;
  int viewWhich;
  
  boolean viewToolbar;
  
  int searchMaxNum;

  boolean autoDateInArticles;

  boolean loadLastFileOnOpen;
  
  String webSearchName;
  String webSearchPrefix;
  String webSearchPostfix;
  
  int defaultSearchMode;
  
  boolean wikiBehaviourActive;
  
  static final String[] characterEncodings = 
                 new String[]{"ISO-8859-1", "ISO-8859-15", "UTF-8", "UTF-16", "US-ASCII", "8859_15"};
  int fileEncoding;
  String getEncoding()
  {    return characterEncodings[fileEncoding];  }
  
  Vector openRecentList;
  int openRecentListLength;
  File getMostRecentFile() // This is used, specifically, by the command-line "find" tool
  {
    if(openRecentList.size()==0)
      return null;
    else
      return (File)openRecentList.get(0);
  }  
  
  Font treeFont;
  Font articleFont;
  
  int characterWrapWidth;
  
  boolean wrapToWindow;
  
  int windowLeft, windowTop, windowWidth, windowHeight;
  
  public static final int LINEBREAK_WIN=0;
  public static final int LINEBREAK_MAC=1;
  public static final int LINEBREAK_UNIX=2;
  int linebreakType = LINEBREAK_WIN;
  public String linebreak()
  {
    switch(linebreakType)
    {
      case LINEBREAK_WIN: return "\r\n";
      case LINEBREAK_MAC: return "\r";
      case LINEBREAK_UNIX: return "\n";
    }
    return "\r\n";
  }
  
  TreePathCollection treePathCollection;
  
  int htmlExportArticleType;
  boolean htmlExportUrlsToLinks;
  int htmlExportAnchorLinkType;
  
  int dividerLocation;
  
  JreepadPrefs(Dimension wndSize)
  {
    openLocation = new File(System.getProperty("user.home"));
    
    autoSavePeriod = 10;
    autoSave = false;
    
    viewWhich = 0;
    
    viewToolbar = true;
    
    searchMaxNum = 200;
    
    autoDateInArticles = true;
    
    loadLastFileOnOpen = true;

    webSearchName = "Google search for highlighted text";
    webSearchPrefix = "www.google.co.uk/search?q=";
    webSearchPostfix = "&hl=en";
    
    defaultSearchMode = 0;
    
    wikiBehaviourActive = true;
    
    fileEncoding = 0;
    
    openRecentList = new Vector();
    openRecentListLength = 10;
    
    treeFont = (new JTree()).getFont();
    articleFont = (new JEditorPane()).getFont();
    
    characterWrapWidth = 80;
    
    wrapToWindow = true;

  //  Toolkit theToolkit = Toolkit.getDefaultToolkit();
  //  Dimension wndSize = theToolkit.getScreenSize();
    windowWidth = (int)(wndSize.getWidth() * 0.6f);
    windowHeight = (int)(wndSize.getHeight() * 0.6f);

    // This bit attempts to ensure that the Jreepad view doesn't get too wide 
    //   (e.g. for people with dual-screen systems)
    //   - it limits the width/height proportion to the golden ratio!
    // Can't seem to find anything in the Toolkit which would automatically give us multi-screen info
    if(windowWidth > (int)(((float)windowHeight)*1.618034f) )
      windowWidth = (int)(((float)windowHeight)*1.618034f);
    else if(windowHeight > (int)(((float)windowWidth)*1.618034f) )
      windowHeight = (int)(((float)windowWidth)*1.618034f);

    windowTop = windowHeight/3;
    windowLeft = windowWidth/3;    
    
    treePathCollection = new TreePathCollection(new javax.swing.tree.TreePath[0]);
    
    htmlExportArticleType = 0;
    htmlExportUrlsToLinks = true;
    htmlExportAnchorLinkType = 1;
    
    dividerLocation = -1;
  }
  
  // We override the serialization routines so that different versions of our class can read 
  // each other's serialized states.
  private void writeObject(java.io.ObjectOutputStream out)
     throws IOException
  {
    out.writeObject(openLocation);
    out.writeObject(saveLocation);
    out.writeObject(importLocation);
    out.writeObject(exportLocation);
    out.writeObject(backupLocation);
    
    out.writeInt(autoSavePeriod);
    out.writeBoolean(autoSave);
    
    out.writeInt(viewWhich);
    
    out.writeBoolean(viewToolbar);
    
    out.writeInt(searchMaxNum);
    
    out.writeBoolean(autoDateInArticles);
    
    out.writeBoolean(loadLastFileOnOpen);

    out.writeObject(webSearchName);
    out.writeObject(webSearchPrefix);
    out.writeObject(webSearchPostfix);

    out.writeInt(defaultSearchMode);
    
    out.writeBoolean(wikiBehaviourActive);

    out.writeInt(fileEncoding);

    out.writeObject(openRecentList);
    out.writeInt(openRecentListLength);

    out.writeObject(treeFont);
    out.writeObject(articleFont);
    
    out.writeInt(characterWrapWidth);
    
    out.writeBoolean(wrapToWindow);

    out.writeInt(windowLeft);
    out.writeInt(windowTop);
    out.writeInt(windowWidth);
    out.writeInt(windowHeight);

    out.writeInt(linebreakType);
    
    out.writeObject(treePathCollection);
    
    out.writeInt(htmlExportArticleType);
    out.writeBoolean(htmlExportUrlsToLinks);
    out.writeInt(htmlExportAnchorLinkType);
    
    out.writeInt(dividerLocation);
  }
  private void readObject(java.io.ObjectInputStream in)
     throws IOException, ClassNotFoundException
  {
   try
   {
    openLocation = (File)in.readObject();
    saveLocation = (File)in.readObject();
    importLocation = (File)in.readObject();
    exportLocation = (File)in.readObject();
    backupLocation = (File)in.readObject();
    
    autoSavePeriod = in.readInt();
    autoSave = in.readBoolean();
    
    viewWhich = in.readInt();
    
    viewToolbar = in.readBoolean();
    
    searchMaxNum = in.readInt();
    
    autoDateInArticles = in.readBoolean();
    
    loadLastFileOnOpen = in.readBoolean();

    webSearchName = (String)in.readObject();
    webSearchPrefix = (String)in.readObject();
    webSearchPostfix = (String)in.readObject();

    defaultSearchMode = in.readInt();
    
    wikiBehaviourActive = in.readBoolean();
 
    fileEncoding = in.readInt();
    
    openRecentList = (Vector)in.readObject();
    openRecentListLength = in.readInt();

    treeFont = (Font)in.readObject();
    articleFont = (Font)in.readObject();
    
    characterWrapWidth = in.readInt();
    
    wrapToWindow = in.readBoolean();
    
    windowLeft = in.readInt();
    windowTop = in.readInt();
    windowWidth = in.readInt();
    windowHeight = in.readInt();

    linebreakType = in.readInt();
    
    treePathCollection = (TreePathCollection)in.readObject();
    
    htmlExportArticleType = in.readInt();
    htmlExportUrlsToLinks = in.readBoolean();
    htmlExportAnchorLinkType = in.readInt();
    
    dividerLocation = in.readInt();
   }
   catch(IOException e)
   {
   }
 } 
}