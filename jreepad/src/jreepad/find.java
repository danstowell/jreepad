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

/*
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Enumeration;
import java.awt.print.*;
*/
import java.io.*;

import javax.swing.tree.TreePath;
import java.util.Vector;

import jreepad.io.AutoDetectReader;
import jreepad.io.JreepadReader;
import jreepad.io.JreepadWriter;
import jreepad.io.TreepadWriter;
import jreepad.io.XmlWriter;

/*

The "Find" class is a command-line utility for searching a single HJT file.

Examples of usage:

java jreepad.find -f ../hjts/notes.hjt "Wood Green"
java jreepad.find -f ../hjts/notes.hjt -t "Wood Green"
java jreepad.find -f ../hjts/notes.hjt -t "Wood Green" -a "020"
java jreepad.find -f ../hjts/notes.hjt "Wood Green" -a "020"

The "-f" argument indicates the file to load.
The remaining arguments are the search terms,
  which by default are found in EITHER the node title or the article.
A "-t" indicates that what follows should only be in the title.
A "-a" indicates that what follows should only be in the article.

"-h" is for help, of course
"-m" is for manual, i.e. more detailed than -h

"-l 200" limit the results to a maximum of 200


Output... hmm...

Default format should be easy to read but should also be processible.

Perhaps it should be plain old HJT format?

Or perhaps it should be something like

-- Node title: Wood Green stuff --
Blah blah blonky bloo isn't this a nice thing
Don't forget to water the flowers by the way
----------------------------------

Perhaps output can be specified by
-oh = Output should be .HJT format
-on = Output should be simple texty format (default)
-ot = Output should be just the node titles
-ox = Simple XML format
-ob = Brief texty format

It needs to pick up the file format from the Jreepad prefs file (the encoding).
It could also pick up the default file to search, i.e. the last one loaded.

*/

public class find
{
  private static final int PARAMMODE_SEARCHANY = 1;
  private static final int PARAMMODE_FILENAME = 2;
  private static final int PARAMMODE_SEARCHTITLE = 3;
  private static final int PARAMMODE_SEARCHARTICLE = 4;
  private static final int PARAMMODE_SEARCHLIMIT = 5;

  private static final int OUTPUT_HJT = 1;
  private static final int OUTPUT_TEXT = 2;
  private static final int OUTPUT_TITLES = 3;
  private static final int OUTPUT_BRIEF = 4;
  private static final int OUTPUT_XML = 5;

  private static final File prefsFile = new File(System.getProperty("user.home"), ".jreepref");

  public static void main(String[] args) throws IOException
  {
    int maxResults = 200; // A default which can be overridden
    boolean caseSensitive = false;
    boolean orNotAnd = true;
    File userFile = null;
    int outputFormat = OUTPUT_XML;

    Vector titleSearches = new Vector();
    Vector articleSearches = new Vector();

    // Load the arguments and check them out
    if(args.length==0 || (args.length==1 && (args[0].startsWith("-h") || args[0].startsWith("--h")) ))
    {
      printUsage();
      System.exit(1);
    }
    int paramMode = PARAMMODE_SEARCHANY;
    for(int i=0; i<args.length; i++)
    {
      if(args[i].equals("-t"))
        paramMode = PARAMMODE_SEARCHTITLE;
      else if(args[i].equals("-a"))
        paramMode = PARAMMODE_SEARCHARTICLE;
      else if(args[i].equals("-l"))
        paramMode = PARAMMODE_SEARCHLIMIT;
      else if(args[i].equals("-f"))
        paramMode = PARAMMODE_FILENAME;
      else if(args[i].startsWith("-o"))
      {
        if(args[i].equals("-oh"))
          outputFormat = OUTPUT_HJT;
        else if(args[i].equals("-on"))
          outputFormat = OUTPUT_TEXT;
        else if(args[i].equals("-ot"))
          outputFormat = OUTPUT_TITLES;
        else if(args[i].equals("-ob"))
          outputFormat = OUTPUT_BRIEF;
        else if(args[i].equals("-ox"))
          outputFormat = OUTPUT_XML;
      }
      else
      {
        switch(paramMode)
        {
          case PARAMMODE_FILENAME:
            userFile = new File(args[i]);
            paramMode = PARAMMODE_SEARCHANY;
            break;
          case PARAMMODE_SEARCHTITLE:
            titleSearches.add(args[i]);
            break;
          case PARAMMODE_SEARCHLIMIT:
            maxResults = Integer.parseInt(args[i]);
            paramMode = PARAMMODE_SEARCHANY;
            break;
          case PARAMMODE_SEARCHARTICLE:
            articleSearches.add(args[i]);
            break;
          case PARAMMODE_SEARCHANY:
          default:
            titleSearches.add(args[i]);
            articleSearches.add(args[i]);
            break;
        }
      }
    }

    String inNodes = "";
    for(int i=0; i<titleSearches.size(); i++)
    {
      inNodes += " " + (String)titleSearches.get(i);
    }
    String inArticles = "";
    for(int i=0; i<articleSearches.size(); i++)
    {
      inArticles += " " + (String)articleSearches.get(i);
    }
    if(titleSearches.size()>1 || articleSearches.size()>1)
    {
      System.out.println("Warning: complicated searches (more than 1 item to find in article, or more than 1 article to find in title) don't currently work properly!");
    }

    String encoding = "UTF-8";
    File prefsLastFile = null;
    // Load the preferences file (if exists)
    if(prefsFile.exists())
    {
     try
     {
      // De-serialize the prefs file (i.e. load it)...
      JreepadPrefs jreepref;
	  ObjectInputStream prefsLoader = new ObjectInputStream(new FileInputStream(prefsFile));
	  jreepref = (JreepadPrefs)prefsLoader.readObject();
	  prefsLoader.close();

      // ...and take some data from it
      encoding = jreepref.getEncoding();
      prefsLastFile = jreepref.getMostRecentFile();
      // then dump it!
      jreepref = null;
     }
     catch(Exception err){} // Exceptions are ignored - just ignore the prefs file if you get an error
    }
    else
    {
    }

    // If no file specified by user, attempt to take it from the preferences file
    if(userFile==null)
      userFile = prefsLastFile;

    // If file unspecified or not found, exit with error
    if(userFile==null)
    {
      System.out.println("No Treepad/Jreepad file specified, and none found in the recently-used files list!");
      System.exit(1);
    }
    else if(!userFile.exists())
    {
      System.out.println("File not found!");
      System.exit(1);
    }

    // Load the file to be searched
    JreepadNode root = new JreepadNode();
	try
	{
        InputStream in = new FileInputStream(userFile);
        JreepadReader reader = new AutoDetectReader(encoding, false);
        root = reader.read(in);
	}
	catch(IOException err)
	{
	  System.out.println("File input error: " + err);
	  System.exit(1);
	}


    // Carry out the search
    JreepadSearcher searcher = new JreepadSearcher(root);
    searcher.performSearch(inNodes, inArticles, new TreePath(root),
  							orNotAnd, caseSensitive, maxResults);
    JreepadSearcher.JreepadSearchResult[] res = searcher.getSearchResults();

    if(res.length==0)
    {
	  System.out.println("No matches.");
	  System.exit(1);
    }

    // Output the results
    JreepadNode resultsParent;
    switch(outputFormat)
    {
      case OUTPUT_TEXT:
        for(int i=0; i<res.length; i++)
          System.out.println(formatResultSimpleText(res[i]));
        break;
      case OUTPUT_BRIEF:
        for(int i=0; i<res.length; i++)
          System.out.println(formatResultBrieferText(res[i]));
        break;
      case OUTPUT_XML:
      case OUTPUT_HJT:
        resultsParent = new JreepadNode("Search results","");
        for(int i=0; i<res.length; i++)
          resultsParent.add(res[i].getNode());
        String outputEncoding = "ISO-8859-1"; // FIXME: What should the encoding be?
        JreepadWriter writer;
        if (outputFormat == OUTPUT_XML)
            writer= new XmlWriter(outputEncoding);
        else
            writer= new TreepadWriter(outputEncoding);
        writer.write(System.out, resultsParent);
        break;
      case OUTPUT_TITLES:
        for(int i=0; i<res.length; i++)
          System.out.println(res[i].getNode().getTitle());
        break;
    }
  } // End of main()


  private static String formatResultSimpleText(JreepadSearcher.JreepadSearchResult res)
  {
    JreepadNode n = res.getNode();
    return "-- " + n.getArticle().getTitle() + " --\n" + n.getArticle().getContent();
  }
  private static String formatResultBrieferText(JreepadSearcher.JreepadSearchResult res)
  {
    JreepadNode n = res.getNode();
    return n.getTitle() + "\n    " + res.getArticleQuote().replace('\n', ' ');
  }
  /*
  private static String formatResultXml(JreepadSearcher.JreepadSearchResult res)
  {
    JreepadNode n = res.getNode();
    //FIXME: What the heck should the encoding be? Does it matter?
    return n.toXmlNoHeader("ISO-8859-1", 1, false);
//    return "<node title=\"" + n.getTitle() + "\">" + n.getContent() + "</node>";
  }
  */

  private static void printUsage()
  {
    System.out.println("Usage: java jreepad.find -f mynotes.hjt \"Text to find\"");
    System.out.println("For more information use \"-m\" for a brief manual.");
  }

  private static void printManual()
  {
    System.out.println("Jreepad (c) Dan Stowell - open source under the Gnu Public Licence");
  }

}