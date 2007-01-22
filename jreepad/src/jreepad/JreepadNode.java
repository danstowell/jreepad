/*
           Jreepad - personal information manager.
           Copyright (C) 2004-2006 Dan Stowell

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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Comparator;
import java.util.Enumeration;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.undo.UndoManager;

import org.philwilson.JTextile;

public class JreepadNode extends DefaultMutableTreeNode implements Comparable
{
  private String title;
  private String content;
  private JreepadNode softLinkTarget;
  private static OurSortComparator ourSortComparator = new OurSortComparator();
  //protected transient javax.swing.table.TableColumnModel tblColModel;

//  private String lineSeparator = System.getProperty("line.separator");

  public static final int ARTICLEMODE_ORDINARY = 1;
  public static final int ARTICLEMODE_HTML = 2;
  public static final int ARTICLEMODE_CSV = 3;
  public static final int ARTICLEMODE_SOFTLINK = 4;
  public static final int ARTICLEMODE_TEXTILEHTML = 5;
  private int articleMode = ARTICLEMODE_ORDINARY;

  // Undo features
  protected transient UndoManager undoMgr;
  //protected transient String lastEditStyle = "";

  public JreepadNode()
  {
    this("");
  }
  public JreepadNode(String content)
  {
    this("",content);
  }
  public JreepadNode(String title, String content)
  {
    this.title = title;
    this.content = content;
    undoMgr = new UndoManager();
  }

  public String toString()
  {
    return getTitle();
  }
  public String toFullString()
  {
    String ret = "JreepadNode \""+getTitle()+"\": " + getChildCount() + " direct child nodes in subtree";
    ret += "\nDirect children:";
    for(int i=0; i<children.size(); i++)
      ret += "\n    " + ((JreepadNode)getChildAt(i)).getTitle();
    return ret;
  }

  public static final String[] getHtmlExportArticleTypes()
  {
    return new String[]{JreepadViewer.lang.getString("PREFS_EXPORTTYPE_TEXT"),
                JreepadViewer.lang.getString("PREFS_EXPORTTYPE_PREFORMATTED"),
                        JreepadViewer.lang.getString("PREFS_EXPORTTYPE_HTML"),
                 JreepadViewer.lang.getString("PREFS_EXPORTTYPE_TEXTILEHTML")};
  }
  public static final String[] getHtmlExportAnchorLinkTypes()
  {
    return new String[]{"node:// links", "WikiLike links"};
  }
  public static final int EXPORT_HTML_NORMAL=0;
  public static final int EXPORT_HTML_PREFORMATTED=1;
  public static final int EXPORT_HTML_HTML=2;
  public static final int EXPORT_HTML_TEXTILEHTML=3;
  public static final int EXPORT_HTML_ANCHORS_PATH=0;
  public static final int EXPORT_HTML_ANCHORS_WIKI=1;

  public String exportSingleArticleAsHtml(int exportMode, boolean urlsToLinks, int anchorType, boolean causeToPrint)
  {
    StringBuffer ret = new StringBuffer();
    ret.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\">\n<head>\n<title>");
    ret.append(htmlSpecialChars(getTitle()));
    ret.append("</title>\n<style type=\"text/css\">\n"
   	  + "dl {}\ndl dt { font-weight: bold; margin-top: 10px; font-size: 24pt; }\ndl dd {margin-left: 20px; padding-left: 0px;}\ndl dd dl dt {background: black; color: white; font-size: 12pt; }\ndl dd dl dd dl dt {background: white; color: black; }"
	  + "\n</style>\n</head>\n\n<body"
	  + (causeToPrint? " onload='print();'" : "")
	  + ">\n<!-- Exported from Jreepad -->\n<dl>");
    ret.append(articleToHtml(exportMode, urlsToLinks, anchorType));
    ret.append("\n</dl>\n</body>\n</html>");
    return ret.toString();
  }

  public String articleToHtml(int exportMode, boolean urlsToLinks, int anchorType)
  {
    switch(articleMode)
    {
      case ARTICLEMODE_SOFTLINK:
        return softLinkTarget.getContent();
      case ARTICLEMODE_HTML:
        return getContent();
      case ARTICLEMODE_TEXTILEHTML:
        try{
          return JTextile.textile(getContent());
        }catch(Exception e){
          return getContent();
        }
      case ARTICLEMODE_CSV:
        String[][] csv = interpretContentAsCsv();
        StringBuffer csvHtml = new StringBuffer("\n  <table border='1' cellspacing='0' cellpadding='2'>");
        for(int i=0; i<csv.length; i++)
        {
          csvHtml.append("\n    <tr>");
          for(int j=0; j<csv[0].length; j++)
            csvHtml.append("\n      <td>" + htmlSpecialChars(csv[i][j]) + "</td>");
          csvHtml.append("\n    </tr>");
        }
        csvHtml.append("\n  </table>");
        return csvHtml.toString();
      case ARTICLEMODE_ORDINARY:
      default:
		switch(exportMode)
		{
		  case EXPORT_HTML_PREFORMATTED:
			return "<pre>"
			  + (urlsToLinks ? urlsToHtmlLinksAndHtmlSpecialChars(getContent(), anchorType) : htmlSpecialChars(getContent()) )
			  + "</pre>";
		  case EXPORT_HTML_HTML:
			return getContent();
		  case EXPORT_HTML_TEXTILEHTML:
            try{
              return JTextile.textile(getContent());
            }catch(Exception e){
              return getContent();
            }
		  case EXPORT_HTML_NORMAL:
		  default:
			return (urlsToLinks ? urlsToHtmlLinksAndHtmlSpecialChars(getContent(), anchorType) : htmlSpecialChars(getContent()) );
		}
    }
  }

  public String getWikiAnchor()
  {
      if (getParent() == null)
          return htmlSpecialChars(getTitle());
      return getParentNode().getWikiAnchor() + "/" + htmlSpecialChars(getTitle());
  }

  private static String htmlSpecialChars(String in)
  {
    char[] c = in.toCharArray();
    StringBuffer ret = new StringBuffer();
    for(int i=0; i<c.length; i++)
      if(c[i]=='<')       ret.append("&lt;");
      else if(c[i]=='>')  ret.append("&gt;");
      else if(c[i]=='&')  ret.append("&amp;");
      else if(c[i]=='\n') ret.append(" <br />\n");
      else if(c[i]=='"') ret.append("&quot;");
      else                ret.append(c[i]);
    return ret.toString();
  }

  // Search through the String, replacing URI-like substrings (containing ://) with HTML links
  private String urlsToHtmlLinksAndHtmlSpecialChars(String in, int anchorType)
  {
    StringCharacterIterator iter = new StringCharacterIterator(in);
    StringBuffer out = new StringBuffer("");
    StringBuffer currentWord = new StringBuffer(""); // "space" characters get stuck straight back out, but words need aggregating

    char c = iter.current(), c2;
    while(true)
    {
      if(Character.isWhitespace(c) || c=='"' || c=='\'' || c=='<' || c=='>' || c=='\n' || c==CharacterIterator.DONE)
      {
       // // First check whether currentWord is empty...?
       // if(c!=CharacterIterator.DONE && currentWord.length()==0)
       //   continue;

        // Check if the current word is a URL - do weird stuff to it if so, else just output it
        if(currentWord.toString().indexOf("://")>0)
        {
          // We don't like quotes - let's remove 'em!
          // Ideally, a beginning quote would signify that we need to keep on searching until we find an end quote
          //   but that aspect is NOT IMPLEMENTED YET
          c2 = currentWord.charAt(0);
          if(c2=='"' || c2=='\'')
            currentWord.deleteCharAt(0);
          c2 = currentWord.charAt(currentWord.length()-1);
          if(c2=='"' || c2=='\'')
            currentWord.deleteCharAt(currentWord.length()-1);

          // At this stage, beginning with "node://" should indicate that we want an anchor link not a "real" HTML link
          String currentWordString = currentWord.toString();
          if(currentWordString.startsWith("node://"))
          {
            String anchorLink;
            if(anchorType==EXPORT_HTML_ANCHORS_WIKI)
              anchorLink = currentWordString.substring(currentWordString.lastIndexOf('/')+1);
            else
              anchorLink = currentWordString.substring(7);
            out.append("<a href=\"#" + anchorLink + "\">" + currentWordString + "</a>");
          }
          else
            out.append("<a href=\"" + currentWord + "\">" + currentWordString + "</a>");
        }
        else if(anchorType==EXPORT_HTML_ANCHORS_WIKI && JreepadView.isWikiWord(currentWord.toString()))
        {
          String currentWordString = currentWord.toString();
          if(currentWordString.length()>4 && currentWordString.startsWith("[[") && currentWordString.endsWith("]]"))
            currentWordString = currentWordString.substring(2, currentWordString.length()-2);
          out.append("<a href=\"#" + currentWordString + "\">" + currentWordString + "</a>");
        }
        else
          out.append(currentWord.toString());
 		if(c=='<')       out.append("&lt;");
		else if(c=='>')  out.append("&gt;");
		else if(c=='\n') out.append(" <br />\n");
		else if(c=='"') out.append("&quot;");
		else if(c=='&') out.append("&amp;");
		else if(c==CharacterIterator.DONE);
		else                out.append(c);

        currentWord.setLength(0);

        if(c==CharacterIterator.DONE)
          break;
      }
      else
      {
        currentWord.append(c); // Just aggregate character onto current "Word"
      }
      c = iter.next();
    } // End "while"

    return out.toString();
  }

  public String exportTitlesAsList()
  {
    return exportTitlesAsList(0).toString();
  }
  public StringBuffer exportTitlesAsList(int currentDepth)
  {
    StringBuffer ret = new StringBuffer();
    for(int i=0; i<currentDepth; i++)
      ret.append(" ");
    ret.append(getTitle() + "\n");
    for(int i=0; i<children.size(); i++)
      ret.append(((JreepadNode)getChildAt(i)).exportTitlesAsList(currentDepth+1));

    return ret;
  }

  public String exportArticlesToText(boolean titlesToo)
  {
//    System.out.println("Expooort beginning");
    return exportArticlesToTextRecursive(titlesToo).toString();
  }
  public StringBuffer exportArticlesToTextRecursive(boolean titlesToo)
  {
//    System.out.println("Expooort " + getTitle());
    StringBuffer ret = new StringBuffer();
    if(titlesToo)
      ret.append(getTitle() + "\n\n");
    ret.append(getContent() + "\n\n");
    for(int i=0; i<children.size(); i++)
      ret.append(((JreepadNode)getChildAt(i)).exportArticlesToTextRecursive(titlesToo));

    return ret;
  }

/*
  private void writeObject(ObjectOutputStream out) throws IOException
  {
    out.writeBytes(this.toTreepadString()); // FIXME - What is this used for? Is it right? What about XML mode?
  }
//  private void writeObject(OutputStreamWriter out) throws IOException
//  {
//    out.write(this.toTreepadString());
//  }
  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
  {
    constructFromInputStream(new InputStreamReader(in), false);
  }
  */

  public void add(JreepadNode child)
  {
    if(articleMode==ARTICLEMODE_SOFTLINK)
    {
      softLinkTarget.add(child);
      return;
    }
    super.add(child);
  }
  public JreepadNode removeChild(int child) // Can be used to delete, OR to 'get' one for moving
  {
    if(articleMode==ARTICLEMODE_SOFTLINK)
    {
      return softLinkTarget.removeChild(child);
    }

    JreepadNode ret = (JreepadNode)getChildAt(child);
    remove(child);
    return ret;
  }
  public TreeNode getChildAt(int child)
  {
    if(articleMode==ARTICLEMODE_SOFTLINK)
    {
      return softLinkTarget.getChildAt(child);
    }
    return super.getChildAt(child);
  }
  public int getChildCount()
  {
    if(articleMode==ARTICLEMODE_SOFTLINK)
    {
      return softLinkTarget.getChildCount();
    }
    return super.getChildCount();
  }
  public boolean indent()
  {
    // Get position in parent. If zero or -1 then return.
    int pos = getIndex();
    if(pos<1) return false;
    // Get sibling node just above, and move self to there.
    MutableTreeNode oldParent = (MutableTreeNode)getParent();
    DefaultMutableTreeNode newParent = (DefaultMutableTreeNode)oldParent.getChildAt(pos-1);
    removeFromParent();
    newParent.add(this);
    return true;
  }
  public boolean outdent()
  {
    // Get parent's parent. If null then return.
    JreepadNode p = (JreepadNode)getParent();
    if(p==null) return false;
    JreepadNode pp = (JreepadNode)p.getParent();
    if(pp==null) return false;
    // Get parent's position in its parent. = ppos
    int ppos = p.getIndex();
    // Move self to parent's parent, at (ppos+1)
    p.removeChild(getIndex());
    pp.insert(this, ppos+1);

    // Also (as in the original treepad) move all the later siblings so they're children of this node


    // NOT DONE YET


    return true;
  }
  public void moveChildUp(int child)
  {
    if(articleMode==ARTICLEMODE_SOFTLINK)
    {
      softLinkTarget.moveChildUp(child);
      return;
    }
    if(child<1 || child>= getChildCount())
      return;

    insert(removeChild(child), child-1);
  }
  public void moveChildDown(int child)
  {
    if(articleMode==ARTICLEMODE_SOFTLINK)
    {
      softLinkTarget.moveChildDown(child);
      return;
    }
    if(child<0 || child>= getChildCount()-1)
      return;

    insert(removeChild(child), child+1);
  }
  public void moveUp()
  {
    MutableTreeNode parent = (MutableTreeNode)getParent();
    if(parent==null) return;
    int index = getIndex();
    if(index<1) return;

    removeFromParent();
    parent.insert(this, index-1);
  }
  public void moveDown()
  {
    MutableTreeNode parent = (MutableTreeNode)getParent();
    if(parent==null) return;
    int index = getIndex();
    if(index<0 || index >= parent.getChildCount()-1) return;

    removeFromParent();
    parent.insert(this, index+1);
  }
  public JreepadNode addChild()
  {
    if(articleMode==ARTICLEMODE_SOFTLINK)
    {
      return softLinkTarget.addChild();
    }
    JreepadNode theChild = new JreepadNode();
    add(theChild);
    return theChild;
  }
  public JreepadNode addChild(int index)
  {
    if(articleMode==ARTICLEMODE_SOFTLINK)
    {
      return softLinkTarget.addChild(index);
    }
    JreepadNode theChild = new JreepadNode();
    insert(theChild, index);
    return theChild;
  }

  public int getIndex(TreeNode child)
  {
    if(articleMode==ARTICLEMODE_SOFTLINK)
    {
      return softLinkTarget.getIndex(child);
    }
    return super.getIndex(child);
  }
  public int getIndex()
  {
    if(getParent()==null)
      return -1;
    return getParent().getIndex(this);
  }

  public boolean isNodeInSubtree(JreepadNode n)
  {
    if(articleMode==ARTICLEMODE_SOFTLINK)
    {
      return softLinkTarget.isNodeInSubtree(n);
    }
    return isNodeDescendant(n);
  }

  public void sortChildren()
  {
    if(articleMode==ARTICLEMODE_SOFTLINK)
    {
      softLinkTarget.sortChildren();
      return;
    }
    sort();
  }

  public void sortChildrenRecursive()
  {
    if(articleMode==ARTICLEMODE_SOFTLINK)
    {
      softLinkTarget.sortChildrenRecursive();
      return;
    }
    sort();
    for(int i=0; i<getChildCount(); i++)
      ((JreepadNode)getChildAt(i)).sortChildrenRecursive();
  }

  // Function for using Java's built-in mergesort
  private void sort()
  {
    Object[] childrenArray = children.toArray();
    java.util.Arrays.sort(childrenArray, ourSortComparator);
    removeAllChildren();
    for(int i=0; i<childrenArray.length; i++)
      add((JreepadNode)childrenArray[i]);
  }
  private static class OurSortComparator implements Comparator, Serializable
  {
    public int compare(Object o1, Object o2)
    {
//      return ((JreepadNode)o1).getTitle().compareToIgnoreCase(
//            ((JreepadNode)o2).getTitle());
      return ((JreepadNode)o1).compareTo(o2);
    }
/*
    public boolean equals(Object obj)
    {
      return obj.equals(this); // Lazy!
    }
    public int hashCode() // Apparently this is required by the contract of Object.hashCode()
    {
      return this.hashCode();
    }
*/
  }
  /*
  public int OLDSIMPLEcompareTo(Object o)
  {
    return getTitle().compareToIgnoreCase(
            ((JreepadNode)o).getTitle());
  }*/
  // The following function is a halfway-house on the way to "natural numerical ordering"
  public int compareTo(Object o)
  {
    String a = getTitle();
    String b = ((JreepadNode)o).getTitle();
    if(a.length()!=0 && b.length()!=0 && Character.isDigit(a.charAt(0)) && Character.isDigit(b.charAt(0)))
    {
      // Both strings begin with digits - so implement natural numerical ordering here
      StringBuffer aBuf = new StringBuffer("");
      StringBuffer bBuf = new StringBuffer("");
      int i;
      for(i=0; i<a.length(); i++)
        if(Character.isDigit(a.charAt(i)))
          aBuf.append(a.charAt(i));
        else
          break;
      for(i=0; i<b.length(); i++)
        if(Character.isDigit(b.charAt(i)))
          bBuf.append(b.charAt(i));
        else
          break;
      return (new Integer(aBuf.toString())).compareTo(new Integer(bBuf.toString()));
    }
    return a.compareToIgnoreCase(b);
  }
  // End of: Stuff to use Java's built-in mergesort

  public boolean isLeaf()
  {
    return (getChildCount()==0);
  }

  public Enumeration children()
  {
    if(articleMode==ARTICLEMODE_SOFTLINK)
    {
      return softLinkTarget.children();
    }
    return super.children();
  } // Required by TreeNode interface

  public JreepadNode getParentNode()
  {
    return (JreepadNode)getParent();
  }

  // MutableTreeNode functions
  public void remove(int child)
  {
    if(articleMode==ARTICLEMODE_SOFTLINK)
    {
      softLinkTarget.remove(child);
      return;
    }
    super.remove(child);
  }
  public void remove(MutableTreeNode node)
  {
    if(articleMode==ARTICLEMODE_SOFTLINK)
    {
      softLinkTarget.remove(node);
      return;
    }
    super.remove(node);
  }

  public void insert(MutableTreeNode child, int index)
  {
    if(articleMode==ARTICLEMODE_SOFTLINK)
    {
      softLinkTarget.insert(child, index);
      return;
    }
    super.insert(child, index);
  }

  public void addChildFromTextFile(InputStreamReader textFile, String nodeName) throws IOException
  {
    if(articleMode==ARTICLEMODE_SOFTLINK)
    {
      softLinkTarget.addChildFromTextFile(textFile, nodeName);
      return;
    }
    // Load the content as a string
    StringBuffer contentString = new StringBuffer();
    String currentLine;
    BufferedReader bReader = new BufferedReader(textFile);
    while((currentLine = bReader.readLine())!=null)
      contentString.append(currentLine + "\n");
    // Then just create the node
    add(new JreepadNode(nodeName, contentString.toString()));
  }

  // This getCopy() function is intended to return a copy of the entire subtree, used for Undo
  public JreepadNode getCopy()
  {
    JreepadNode ret = new JreepadNode(getTitle(), getContent());
    for(int i=0; i<getChildCount(); i++)
    {
      ret.add(((JreepadNode)getChildAt(i)).getCopy());
    }
    return ret;
  }

  public JreepadNode getChildByTitle(String title)
  {
    if(articleMode==ARTICLEMODE_SOFTLINK)
    {
      return softLinkTarget.getChildByTitle(title);
    }
    for(int i=0; i<getChildCount(); i++)
      if(((JreepadNode)getChildAt(i)).getTitle().equals(title))
        return (JreepadNode)getChildAt(i);
    return null;
  }

  public synchronized void wrapContentToCharWidth(int charWidth)
  {
    if(articleMode==ARTICLEMODE_SOFTLINK)
    {
      softLinkTarget.wrapContentToCharWidth(charWidth);
      return;
    }
    if(charWidth < 2)
      return;

    StringBuffer ret = new StringBuffer();
    StringCharacterIterator iter = new StringCharacterIterator(content);
    int charsOnThisLine = 0;
    for(char c = iter.first(); c != CharacterIterator.DONE; c = iter.next())
    {
      if(c=='\n')
        charsOnThisLine = 0;
      else if(++charsOnThisLine >= charWidth)
      {
        ret.append('\n');
        charsOnThisLine=0;
      }
      ret.append(c);
    }
    content = ret.toString();
  }

  public synchronized void stripAllTags()
  {
    if(articleMode==ARTICLEMODE_SOFTLINK)
    {
      softLinkTarget.stripAllTags();
      return;
    }
    StringBuffer ret = new StringBuffer();
    StringCharacterIterator iter = new StringCharacterIterator(content);
    boolean on = true;
    for(char c = iter.first(); c != CharacterIterator.DONE; c = iter.next())
    {
      if((!on) && c=='>')
        on = true;
      else if(on && c=='<')
        on = false;
      else if(on)
        ret.append(c);
    }
    content = ret.toString();
  }

  public JreepadNode getSoftLinkTarget()
  {
      return softLinkTarget;
  }

  public String getTitle()
  { return articleMode==ARTICLEMODE_SOFTLINK ? softLinkTarget.getTitle() : title; }
  public String getContent() { return articleMode==ARTICLEMODE_SOFTLINK ? softLinkTarget.getContent() : content; }
  public void setTitle(String title)
  {
    if(articleMode==ARTICLEMODE_SOFTLINK)
      softLinkTarget.setTitle(title);
    else
      this.title = title;
  }
  public void setContent(String content)
  {
    if(articleMode==ARTICLEMODE_SOFTLINK)
      softLinkTarget.setContent(content);
    else
      this.content = content;
  }


  public void setArticleMode(int newMode)
  {
    switch(newMode)
    {
      case ARTICLEMODE_CSV:
        String[][] csv = interpretContentAsCsv();
  //      System.out.println("interpretContentAsCsv() returned csv[" + csv.length + "][" + csv[0].length + "]");
  //      System.out.println();
  //      for(int i=0; i<csv.length; i++)
  //      {
  //        for(int j=0; j<csv[0].length; j++)
  //          System.out.print("\"" + csv[i][j] + "\"  ");
  //        System.out.println();
  //        System.out.println();
  //      }
  //      System.out.println();
      case ARTICLEMODE_ORDINARY:
      case ARTICLEMODE_HTML:
      case ARTICLEMODE_TEXTILEHTML:
      case ARTICLEMODE_SOFTLINK:  // FIXME: Do we need to do anything special for the softlink creation?
        articleMode = newMode;
        break;
      default:
        return;
    }
  }

  public int getArticleMode()
  {
    return articleMode;
  }

  static final int CSVPARSE_MODE_INQUOTES = 1;
  static final int CSVPARSE_MODE_EXPECTINGDELIMITER = 2;
  static final int CSVPARSE_MODE_EXPECTINGDATA = 3;
  public String[][] interpretContentAsCsv()
  {
    return interpretContentAsCsv(getContent());
  }
  protected static String[][] interpretContentAsCsv(String theContent)
  {
    int rows = 1;
    int cols = 1;
    theContent = theContent.trim();
    char c;
    int curCols = 1;
    int parseMode = CSVPARSE_MODE_EXPECTINGDATA;
    StringBuffer curData = new StringBuffer();

    // Go through once to determine the number of rows and columns
    StringCharacterIterator iter = new StringCharacterIterator(theContent);

    c = iter.current();
    while(true)
    {
      if(c==iter.DONE)
      {
   ///     System.out.println("I've just parsed this data item: " + curData + " and I'm in mode " + parseMode);
        cols = (curCols>cols) ? curCols : cols;
        break;
      }

      if(parseMode==CSVPARSE_MODE_INQUOTES)
      {
        if(c=='"')
        {
          parseMode = CSVPARSE_MODE_EXPECTINGDELIMITER;
        }
        else
        {
          curData.append(c);
        }
      }
      else if(parseMode==CSVPARSE_MODE_EXPECTINGDELIMITER || parseMode==CSVPARSE_MODE_EXPECTINGDATA)
      {
        if(c=='"')
        {
          parseMode = CSVPARSE_MODE_INQUOTES;
        }
        else if(c=='\n' || c==Character.LINE_SEPARATOR)
        {
          parseMode = CSVPARSE_MODE_EXPECTINGDATA;
    ///      System.out.println("I've just parsed this data item: " + curData + " and I'm in mode " + parseMode);
          curData = new StringBuffer();
          cols = (curCols>cols) ? curCols : cols;
          curCols = 1;
          rows++;
        }
        else if(c==',')
        {
          parseMode = CSVPARSE_MODE_EXPECTINGDATA;
          curCols++;
     ///     System.out.println("I've just parsed this data item: " + curData + " and I'm in mode " + parseMode);
          curData = new StringBuffer();
        }
        else
        {
          curData.append(c);
        }
      }

      c = iter.next();
    }



    // Now go through and actually assign the content
    String[][] csv = new String[rows][cols];
    for(int i=0; i<csv.length; i++)
      java.util.Arrays.fill(csv[i], "");
    iter = new StringCharacterIterator(theContent);
    parseMode = CSVPARSE_MODE_EXPECTINGDATA;
    curData = new StringBuffer();
    int col=0;
    int row=0;

    c = iter.current();
    while(true)
    {
      if(c==iter.DONE)
      {
        csv[row][col] = curData.toString();
        break;
      }

      if(parseMode==CSVPARSE_MODE_INQUOTES)
      {
        if(c=='"')
        {
          parseMode = CSVPARSE_MODE_EXPECTINGDELIMITER;
        }
        else
        {
          curData.append(c);
        }
      }
      else if(parseMode==CSVPARSE_MODE_EXPECTINGDELIMITER || parseMode==CSVPARSE_MODE_EXPECTINGDATA)
      {
        if(c=='"')
        {
          parseMode = CSVPARSE_MODE_INQUOTES;
        }
        else if(c=='\n' || c==Character.LINE_SEPARATOR)
        {
          csv[row][col] = curData.toString();
          parseMode = CSVPARSE_MODE_EXPECTINGDATA;
          curData = new StringBuffer();
          col=0;
          row++;
        }
        else if(c==',')
        {
          csv[row][col] = curData.toString();
          parseMode = CSVPARSE_MODE_EXPECTINGDATA;
          col++;
          curData = new StringBuffer();
        }
        else
        {
          curData.append(c);
        }
      }

      c = iter.next();
    }
    return csv;
  }

  protected void setContentAsCsv(String[][] in)
  {
    StringBuffer o = new StringBuffer();
    for(int i=0; i<in.length; i++)
    {
	  for(int j=0; j<in[0].length; j++)
	  {
	  o.append("\"" + in[i][j] + "\"");
	  }
	  o.append("\n");
    }
    setContent(o.toString());
  }

  private void makeMeASoftLinkTo(JreepadNode targetNode)
  {
    articleMode = ARTICLEMODE_SOFTLINK;
    softLinkTarget = targetNode;
  }

  protected JreepadNode makeSoftLink()
  {
    JreepadNode link = new JreepadNode();
    getParentNode().add(link);
    link.makeMeASoftLinkTo(this);
    return link;
  }

/*
    // Listens for edits that can be undone.
    protected class JreepadNodeUndoableEditListener
                    implements UndoableEditListener {
        public void undoableEditHappened(UndoableEditEvent e) {

            //System.out.println("Undoable event is " + (e.getEdit().isSignificant()?"":"NOT ") + "significant");
            //System.out.println("Undoable event source: " + e.getEdit());

            //Remember the edit and update the menus.
            undoMgr.addEdit(e.getEdit());
            //undoAction.updateUndoState();
            //redoAction.updateRedoState();
        }
    }
*/
}
