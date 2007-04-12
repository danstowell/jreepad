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
import java.util.prefs.Preferences;

/*

A class to hold Jreepad's preferences - and hopefully to store them on disk in a nice
permanent way which can be carried across from different versions

*/

public class JreepadPrefs //implements Serializable
{
  Preferences prefs;
  File openLocation, importLocation, exportLocation, backupLocation;

  boolean seenLicense;

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
  File getMostRecentFile()
  {
    if(openRecentList.size()==0)
      return null;
    else
      return (File)openRecentList.get(0);
  }

  Font treeFont;
  Font articleFont;

  int characterWrapWidth;

  public boolean wrapToWindow;

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

  boolean autoDetectHtmlArticles;

  public boolean addQuotesToCsvOutput;

  public static final int FILETYPE_XML = 0;
  public static final int FILETYPE_HJT = 1;
  int mainFileType;
  public static final String[] mainFileTypes = {"Jreepad XML","Treepad HJT"};

  static final int TOOLBAR_TEXT = 0;
  static final int TOOLBAR_ICON = 1;
  static final int TOOLBAR_OFF = 2;
  int toolbarMode;

  boolean showGreenStrip;

  /**
   * Date format string used to format inserted date.
   */
  public String dateFormat;

  JreepadPrefs(Dimension wndSize) {
    // Grab the prefs object from wherever Java's API has put it...
    prefs = Preferences.userNodeForPackage(this.getClass());

    openLocation = new File(prefs.get("OPENLOCATION", System.getProperty("user.home")));
    importLocation = new File(prefs.get("IMPORTLOCATION", System.getProperty("user.home")));
    exportLocation = new File(prefs.get("EXPORTLOCATION", System.getProperty("user.home")));
    backupLocation = new File(prefs.get("BACKUPLOCATION", System.getProperty("user.home")));

    seenLicense = prefs.getBoolean("SEENLICENSE", false);

    autoSavePeriod = prefs.getInt("AUTOSAVEPERIOD", 10);
    autoSave = prefs.getBoolean("AUTOSAVE", false);

    viewWhich = prefs.getInt("VIEWWHICH", VIEW_BOTH);

    viewToolbar = prefs.getBoolean("VIEWTOOLBAR", true);

    searchMaxNum = prefs.getInt("SEARCHMAXNUM", 200);

    autoDateInArticles = prefs.getBoolean("AUTODATEINARTICLES", true);

    loadLastFileOnOpen = prefs.getBoolean("LOADLASTFILEONOPEN", true);

    webSearchName = prefs.get("WEBSEARCHNAME",
          JreepadViewer.lang.getString("PREFS_DEFAULT_SEARCH_TEXT")); //"Google search for highlighted text";
    webSearchPrefix = prefs.get("WEBSEARCHPREFIX", "www.google.co.uk/search?q=");
    webSearchPostfix = prefs.get("WEBSEARCHPOSTFIX", "&hl=en");

    defaultSearchMode = prefs.getInt("DEFAULTSEARCHMODE", 0);

    wikiBehaviourActive = prefs.getBoolean("WIKIBEHAVIOURACTIVE", true);

    fileEncoding = prefs.getInt("FILEENCODING", 2); // Default to UTF-8

    openRecentList = new Vector();
    openRecentListLength = prefs.getInt("OPENRECENTLISTLENGTH", 10);
    String tempFileListItem;
    for(int i=0; i<100; i++){
        tempFileListItem = prefs.get("OPENRECENTLIST_"+i, "");
        if(!tempFileListItem.equals("")){
            openRecentList.add(new File(tempFileListItem));
        }else{
            break;
        }
    }

    String treeFontName = prefs.get("TREEFONTNAME", (new JTree()).getFont().getName());
    String articleFontName = prefs.get("ARTICLEFONTNAME", (new JEditorPane()).getFont().getName());
    int treeFontSize = prefs.getInt("TREEFONTSIZE", (new JTree()).getFont().getSize());
    int articleFontSize = prefs.getInt("ARTICLEFONTSIZE", (new JEditorPane()).getFont().getSize());

    treeFont = new Font(treeFontName, Font.PLAIN, treeFontSize);
    articleFont = new Font(articleFontName, Font.PLAIN, articleFontSize);

    characterWrapWidth = prefs.getInt("CHARACTERWRAPWIDTH", 80);

    wrapToWindow = prefs.getBoolean("WRAPTOWINDOW", true);

  //  Toolkit theToolkit = Toolkit.getDefaultToolkit();
  //  Dimension wndSize = theToolkit.getScreenSize();
    windowWidth  = prefs.getInt("WINDOWWIDTH", 0);
    windowHeight = prefs.getInt("WINDOWHEIGHT", 0);
    windowTop    = prefs.getInt("WINDOWTOP", 0);
    windowLeft   = prefs.getInt("WINDOWLEFT", 0);
    if(windowWidth==0 || windowWidth==0){

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

    }

    linebreakType   = prefs.getInt("LINEBREAKTYPE", LINEBREAK_WIN);


// THIS ISN'T CURRENTLY SAVED TO PREFERENCES IN ANY MEANINGFUL WAY.
// NEED SOME WAY OF STORING THE TREE STATE WHICH ACTUALLY WORKS.
treePathCollection = new TreePathCollection(new javax.swing.tree.TreePath[0]);

    htmlExportArticleType = prefs.getInt("HTMLEXPORTARTICLETYPE", 0);
    htmlExportUrlsToLinks = prefs.getBoolean("HTMLEXPORTURLSTOLINKS", true);
    htmlExportAnchorLinkType = prefs.getInt("HTMLEXPORTANCHORLINKTYPE", 1);

    dividerLocation = prefs.getInt("DIVIDERLOCATION", -1);

    autoDetectHtmlArticles = prefs.getBoolean("AUTODETECTHTMLINARTICLES", true);

    addQuotesToCsvOutput = prefs.getBoolean("ADDQUOTESTOCSVOUTPUT", false);

    mainFileType = prefs.getInt("MAINFILETYPE", FILETYPE_HJT);

    toolbarMode = prefs.getInt("TOOLBARMODE", TOOLBAR_ICON);

    showGreenStrip = prefs.getBoolean("SHOWGREENSTRIP", true);

    dateFormat = prefs.get("DATEFORMAT", "");
  }


  void save()
  {
    prefs.put("OPENLOCATION",""+openLocation);
    prefs.put("IMPORTLOCATION", ""+importLocation);
    prefs.put("EXPORTLOCATION", ""+exportLocation);
    prefs.put("BACKUPLOCATION", ""+backupLocation);

    prefs.putBoolean("SEENLICENSE", seenLicense);

    prefs.putInt("AUTOSAVEPERIOD", autoSavePeriod);
    prefs.putBoolean("AUTOSAVE", autoSave);

    prefs.putInt("VIEWWHICH", viewWhich);

    prefs.putBoolean("VIEWTOOLBAR", viewToolbar);

    prefs.putInt("SEARCHMAXNUM", searchMaxNum);

    prefs.putBoolean("AUTODATEINARTICLES", autoDateInArticles);

    prefs.putBoolean("LOADLASTFILEONOPEN", loadLastFileOnOpen);

    prefs.put("WEBSEARCHNAME", ""+webSearchName);
    prefs.put("WEBSEARCHPREFIX", ""+webSearchPrefix);
    prefs.put("WEBSEARCHPOSTFIX", ""+webSearchPostfix);

    prefs.putInt("DEFAULTSEARCHMODE", defaultSearchMode);

    prefs.putBoolean("WIKIBEHAVIOURACTIVE", wikiBehaviourActive);

    prefs.putInt("FILEENCODING",fileEncoding);

    for(int i=0; i<openRecentList.size(); i++) {
      prefs.put("OPENRECENTLIST_"+i, ""+((File)openRecentList.get(i)).toString());
    }
    prefs.putInt("OPENRECENTLISTLENGTH", openRecentListLength);

    prefs.put("TREEFONTNAME", treeFont.getName());
    prefs.putInt("TREEFONTSIZE", treeFont.getSize());
    prefs.put("ARTICLEFONTNAME", articleFont.getName());
    prefs.putInt("ARTICLEFONTSIZE", articleFont.getSize());

    prefs.putInt("CHARACTERWRAPWIDTH", characterWrapWidth);

    prefs.putBoolean("WRAPTOWINDOW", wrapToWindow);

    prefs.putInt("WINDOWLEFT", windowLeft);
    prefs.putInt("WINDOWTOP", windowTop);
    prefs.putInt("WINDOWWIDTH", windowWidth);
    prefs.putInt("WINDOWHEIGHT", windowHeight);

    prefs.putInt("LINEBREAKTYPE", linebreakType);

// HOW TO SERIALISE? prefs.put(""+treePathCollection);

    prefs.putInt("HTMLEXPORTARTICLETYPE", htmlExportArticleType);
    prefs.putBoolean("HTMLEXPORTURLSTOLINKS", htmlExportUrlsToLinks);
    prefs.putInt("HTMLEXPORTANCHORLINKTYPE", htmlExportAnchorLinkType);

    prefs.putInt("DIVIDERLOCATION", dividerLocation);

    prefs.putBoolean("AUTODETECTHTMLINARTICLES", autoDetectHtmlArticles);

    prefs.putBoolean("ADDQUOTESTOCSVOUTPUT", addQuotesToCsvOutput);

    prefs.putInt("MAINFILETYPE", mainFileType);

    prefs.putInt("TOOLBARMODE", toolbarMode);

    prefs.putBoolean("SHOWGREENSTRIP", showGreenStrip);

    prefs.put("DATEFORMAT", dateFormat);


    try{
      prefs.flush(); // Encourage the store to be saved
    }catch(Exception err){
    }
  }

/*
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

    out.writeBoolean(autoDetectHtmlArticles);

    out.writeBoolean(addQuotesToCsvOutput);

    out.writeInt(mainFileType);

    out.writeInt(toolbarMode);

    out.writeBoolean(showGreenStrip);
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

    autoDetectHtmlArticles = in.readBoolean();

    addQuotesToCsvOutput = in.readBoolean();

    mainFileType = in.readInt();

    toolbarMode = in.readInt();

    showGreenStrip = in.readBoolean();
   }
   catch(IOException e)
   {
   }
 }
 */
}