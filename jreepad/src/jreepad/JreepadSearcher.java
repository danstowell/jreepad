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
import javax.swing.event.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.awt.print.*;
*/
import java.util.Vector;
import javax.swing.tree.*;
import java.util.Enumeration;

/*

This class provides the functionality to search a Jreepad tree.

It's separate so that it can be used by the graphical Jreepad application as
easily as by a command-line tool...

*/
public class JreepadSearcher
{

  JreepadNode root;
  public JreepadSearcher(JreepadNode root)
  {
    this.root = root;
  }


  private JreepadSearchResult[] searchResults;
  private Vector searchResultsVec;
  private Object foundObject;
  public boolean performSearch(String inNodes, String inArticles, TreePath pathToSearchStart,
  							boolean orNotAnd, boolean caseSensitive, int maxResults)
  {
    searchResults = null;
    searchResultsVec = new Vector();
    
    System.out.println("JreepadSearcher.performSearch: pathToSearchStart = " + pathToSearchStart);
    
//    recursiveSearchNode(inNodes, inArticles, root, pathToSearchStart, orNotAnd, caseSensitive, maxResults);
    recursiveSearchNode(inNodes, inArticles, (JreepadNode)pathToSearchStart.getLastPathComponent(), pathToSearchStart, orNotAnd, caseSensitive, maxResults);

	searchResults = new JreepadSearchResult[searchResultsVec.size()];
	for(int i=0; i<searchResults.length; i++)
	{
	  foundObject = searchResultsVec.get(i);
	  searchResults[i] = (JreepadSearchResult)foundObject;
	}
	return true;
  }
  private static final int articleQuoteMaxLen = 40;
  private void recursiveSearchNode(String inNodes, String inArticles, JreepadNode thisNode, TreePath pathSoFar,
  					boolean orNotAnd, boolean caseSensitive, int maxResults)
  {
    if(searchResultsVec.size()>=maxResults) return;
    
    String quoteText;
    
    // These things ensure case-sensitivity behaves
    String casedInNodes = caseSensitive    ? inNodes               : inNodes.toUpperCase();
    String casedInArticles = caseSensitive ? inArticles            : inArticles.toUpperCase();
    String casedNode = caseSensitive       ? thisNode.getTitle()   : thisNode.getTitle().toUpperCase();
    String casedArticle = caseSensitive    ? thisNode.getContent() : thisNode.getContent().toUpperCase();
    
    // Look in current node. If it matches criteria, add "pathSoFar" to the Vector
    boolean itMatches;
    boolean nodeMatches    = inNodes.equals("")    || casedNode.indexOf(casedInNodes)!=-1;
    boolean articleMatches = inArticles.equals("") || casedArticle.indexOf(casedInArticles)!=-1;


    if(inNodes.equals("") && inArticles.equals(""))
      itMatches = false;
    else if(inNodes.equals("")) // Only looking in articles
      itMatches = articleMatches;
    else if(inArticles.equals("")) // Only looking in nodes
      itMatches = nodeMatches;
    else // Looking in both
      if(orNotAnd) // Use OR combinator
        itMatches = nodeMatches || articleMatches;
      else // Use AND combinator
        itMatches = nodeMatches && articleMatches;


    if(itMatches)
    {
      if(!articleMatches)
      {
        if(thisNode.getContent().length()>articleQuoteMaxLen)
          quoteText = thisNode.getContent().substring(0,articleQuoteMaxLen) + "...";
        else
          quoteText = thisNode.getContent();
      }
      else
      {
        quoteText = "";
        int start = casedArticle.indexOf(casedInArticles);
        String substring;
        if(start>0)
          quoteText += "...";
        else
          start = 0;
        substring = thisNode.getContent();
        if(substring.length() > articleQuoteMaxLen)
          quoteText += substring.substring(0,articleQuoteMaxLen) + "...";
        else
          quoteText += thisNode.getContent().substring(start);
      }
      searchResultsVec.add(new JreepadSearchResult(pathSoFar, quoteText, thisNode));
//      System.out.println("Positive match: "+thisNode);
    }
    
    // Whether or not it matches, make the recursive call on the children
    Enumeration getKids = thisNode.children();
    JreepadNode thisKid;
    while(getKids.hasMoreElements())
    {
      thisKid = (JreepadNode)getKids.nextElement();
      recursiveSearchNode(inNodes, inArticles, thisKid, pathSoFar.pathByAddingChild(thisKid), 
                          orNotAnd, caseSensitive, maxResults);
    }
  }
  public JreepadSearchResult[] getSearchResults()
  {
    return searchResults;
  }
  public class JreepadSearchResult
  {
    private TreePath treePath;
    private String articleQuote;
    private JreepadNode node;
    public JreepadSearchResult(TreePath treePath, String articleQuote, JreepadNode node)
    {
      this.treePath = treePath;
      this.articleQuote = articleQuote;
      this.node = node;
    }
    public String getArticleQuote()	{ return articleQuote;	}
    public TreePath getTreePath()	{ return treePath;		}
    public JreepadNode getNode()	{ return node;		}
  }


}