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

import java.util.*;
import java.io.*;
import javax.swing.tree.*;
import java.text.*;

public class JreepadNode implements Serializable, TreeNode, MutableTreeNode, Comparable
{
  private Vector children;
  private String title;
  private String content;
//  private int childrenCount=0;
  private JreepadNode parentNode;
  private OurSortComparator ourSortComparator;

//  private String lineSeparator = System.getProperty("line.separator");

  public static final int ARTICLEMODE_ORDINARY = 1;
  public static final int ARTICLEMODE_HTML = 2;
  public static final int ARTICLEMODE_CSV = 3;
  private int articleMode = ARTICLEMODE_ORDINARY;

  public JreepadNode()
  {
    this((JreepadNode)null);
  }
  public JreepadNode(JreepadNode parentNode)
  {
    this("", parentNode);
  }
  public JreepadNode(String content, JreepadNode parentNode)
  {
    this("<Untitled node>",content, parentNode);
  }
  public JreepadNode(String title, String content, JreepadNode parentNode)
  {
    this.title = title;
    this.content = content;
    this.parentNode = parentNode;
    ourSortComparator = new OurSortComparator();
    children = new Vector();
  }
  public JreepadNode(InputStreamReader treeInputStream, boolean autoDetectHtmlArticles) throws IOException
  {
    constructFromInputStream(treeInputStream, autoDetectHtmlArticles);
  }
  private void constructFromInputStream(InputStreamReader treeInputStream, boolean autoDetectHtmlArticles) throws IOException
  {
    int lineNum = 2;
    int depthMarker;
    BufferedReader bReader = new BufferedReader(treeInputStream);
    JreepadNode babyNode;
    children = new Vector();

    Stack nodeStack = new Stack();
    nodeStack.push(this);
    
    int hjtFileFormat = -1;

    String dtLine, nodeLine, titleLine, depthLine;
    StringBuffer currentContent;
    String currentLine = bReader.readLine(); // Read the first line, check for treepadness
    if((currentLine.toLowerCase().startsWith("<treepad") && currentLine.endsWith(">")) )
      hjtFileFormat = 1;
    else if((currentLine.toLowerCase().startsWith("<hj-treepad") && currentLine.endsWith(">")) )
      hjtFileFormat = 2;
    else
      throw new IOException("\"<Treepad>\" tag not found at beginning of file!\n(This can be caused by having the wrong character set specified.)");
    
    dtLine = "dt=text";

    while(       (hjtFileFormat == 2 ||  (dtLine = bReader.readLine())!=null)
         && (nodeLine = bReader.readLine())!=null && 
          (titleLine = bReader.readLine())!=null && (depthLine = bReader.readLine())!=null)
    {
      // Read "dt=text"    [or error] - NB THE OLDER FORMAT DOESN'T INCLUDE THIS LINE SO WE SKIP IT
      if(dtLine.equals("") && nodeLine.startsWith("<bmarks>"))
        throw new IOException("This is not a Treepad-Lite-compatible file!\n\nFiles created in more advanced versions of Treepad\ncontain features that are not available in Jreepad.");

      if(hjtFileFormat != 2)
        if(! (dtLine.toLowerCase().startsWith("dt=text")))
          throw new IOException("Unrecognised node dt format at line " + lineNum + ": " + dtLine);
      // Read "<node>"     [or error]
      if(! (nodeLine.toLowerCase().startsWith("<node>")))
        throw new IOException("Unrecognised node format at line " + (lineNum+1) + ": " + nodeLine);

      lineNum += 4;

      // Read THE CONTENT! [loop until we find "<end node> 5P9i0s8y19Z"]
      currentContent = new StringBuffer();
      while((currentLine = bReader.readLine())!=null && !currentLine.equals("<end node> 5P9i0s8y19Z"))
      {
        currentContent.append(currentLine + "\n");
        lineNum++;
      }

      // Now, having established the content and the title and the depth, we'll create the child
      babyNode = new JreepadNode(titleLine, currentContent.substring(0, Math.max(currentContent.length()-1,0)),
                             (JreepadNode)(nodeStack.peek()));
//      babyNode = new JreepadNode(titleLine, currentContent.substring(0, Math.max(currentContent.length()-2,0)),
//                             (JreepadNode)(nodeStack.peek()));

      // Turn it into a HTML-mode node if it matches "<html> ... </html>"
      String compareContent = babyNode.getContent().toLowerCase().trim();
      int newArticleMode = (autoDetectHtmlArticles && compareContent.startsWith("<html>") && compareContent.endsWith("</html>"))
                      ? ARTICLEMODE_HTML
                      : ARTICLEMODE_ORDINARY;

      if(depthLine.equals("0"))
      {
        this.title = titleLine;
        this.content = currentContent.substring(0, Math.max(currentContent.length()-1,0));
        this.articleMode = newArticleMode;
      }
      else
      {
        babyNode.setArticleMode(newArticleMode);
        depthMarker = Integer.parseInt(depthLine);
        while(nodeStack.size()>depthMarker)
          nodeStack.pop();

        ((JreepadNode)(nodeStack.peek())).addChild(babyNode);
        nodeStack.push(babyNode);
      }
    }

  } // End of constructor from FileInputStream

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
    return new String[]{"Ordinary text", "Preformatted text", "HTML markup"};
  }
  public static final String[] getHtmlExportAnchorLinkTypes()
  {
    return new String[]{"node:// links", "WikiLike links"};
  }
  public static final int EXPORT_HTML_NORMAL=0;
  public static final int EXPORT_HTML_PREFORMATTED=1;
  public static final int EXPORT_HTML_HTML=2;
  public static final int EXPORT_HTML_ANCHORS_PATH=0;
  public static final int EXPORT_HTML_ANCHORS_WIKI=1;
  public String exportAsHtml(int exportMode, boolean urlsToLinks, int anchorType)
  {
    return exportAsHtml(exportMode, urlsToLinks, anchorType, false);
  }
  public String exportAsHtml(int exportMode, boolean urlsToLinks, int anchorType, boolean causeToPrint)
  {
    StringBuffer ret = new StringBuffer();
    ret.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\">\n<head>\n<title>");
    ret.append(htmlSpecialChars(getTitle()));
    ret.append("</title>\n<style type=\"text/css\">\n"
   	  + "dl {}\ndl dt { font-weight: bold; margin-top: 10px; font-size: 24pt; }\ndl dd {margin-left: 20px; padding-left: 0px;}\ndl dd dl dt {background: black; color: white; font-size: 12pt; }\ndl dd dl dd dl dt {background: white; color: black; }"
	  + "\n</style>\n</head>\n\n<body"
	  + (causeToPrint? " onload='print();'" : "")
	  + ">\n<!-- Exported from Jreepad -->\n<dl>");
    ret.append(exportAsHtml(exportMode, urlsToLinks, htmlSpecialChars(getTitle()), anchorType));
    ret.append("\n</dl>\n</body>\n</html>");
    return ret.toString();
  }
  public StringBuffer exportAsHtml(int exportMode, boolean urlsToLinks, String anchorName, int anchorType)
  {
    StringBuffer ret = new StringBuffer();
    ret.append("\n<dt><a name=\"");
    if(anchorType==EXPORT_HTML_ANCHORS_WIKI)
      ret.append(getTitle());
    else
      ret.append(anchorName);
    ret.append("\"></a>");
    ret.append(htmlSpecialChars(getTitle()));
    ret.append("</dt>\n<dd>");

    // Write out the node's article content - using normal, preformatted, or HTML modes as appropriate
    ret.append(articleToHtml(exportMode, urlsToLinks, anchorName, anchorType));

    if(children.size()>0)
      ret.append("\n<dl>");
    for(int i=0; i<children.size(); i++)
    {
      JreepadNode thisKid = (JreepadNode)getChildAt(i);
      ret.append(thisKid.exportAsHtml(exportMode, urlsToLinks, anchorName+"/"+htmlSpecialChars(thisKid.getTitle()), anchorType));
    }
    if(children.size()>0)
      ret.append("\n</dl>");
    ret.append("</dd>");

    return ret;
  }

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
    ret.append(articleToHtml(exportMode, urlsToLinks, htmlSpecialChars(getTitle()), anchorType));
    ret.append("\n</dl>\n</body>\n</html>");
    return ret.toString();
  }
  
  public String articleToHtml(int exportMode, boolean urlsToLinks, String anchorName, int anchorType)
  {
    switch(articleMode)
    {
      case ARTICLEMODE_HTML:
        return getContent();
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
		  case EXPORT_HTML_NORMAL:
		  default:
			return (urlsToLinks ? urlsToHtmlLinksAndHtmlSpecialChars(getContent(), anchorType) : htmlSpecialChars(getContent()) );
		}
    }
  }
  
  private String htmlSpecialChars(String in)
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

  public String exportAsSimpleXml()
  {
    return exportAsSimpleXml(true).toString();
  }
  public StringBuffer exportAsSimpleXml(boolean isRoot)
  {
    StringBuffer ret = new StringBuffer();
    if(isRoot)
    {
      ret.append("<?xml version=\"1.0\" standalone=\"yes\" encoding=\"UTF-8\"?>");
    }
    ret.append("\n<node title=\">");
    ret.append(getTitle());
    ret.append("\">");
    ret.append(getContent());
    for(int i=0; i<children.size(); i++)
      ret.append(((JreepadNode)getChildAt(i)).exportAsSimpleXml(false));
    ret.append("</node>");
    return ret;
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


  private void writeObject(ObjectOutputStream out) throws IOException
  {
    out.writeBytes(this.toTreepadString());
  }
//  private void writeObject(OutputStreamWriter out) throws IOException
//  {
//    out.write(this.toTreepadString());
//  }
  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
  {
    constructFromInputStream(new InputStreamReader(in), false);
  }
  public String toTreepadString()
  {
    return "<Treepad version 2.7>\n" + toTreepadString(0);
  }
  public String toTreepadString(int currentDepth)
  {
    StringBuffer ret = new StringBuffer("dt=Text\n<node>\n");
    ret.append(getTitle() + "\n" + (currentDepth++) + "\n" + getContent() + "\n<end node> 5P9i0s8y19Z\n");
    for(int i=0; i<getChildCount(); i++)
      ret.append(((JreepadNode)getChildAt(i)).toTreepadString(currentDepth));
//    System.out.println("\n\n____________________NODE AT DEPTH " + currentDepth + "_________________________\n" + ret);
    return ret.toString();
  }

  public void addChild(JreepadNode child)
  {
    children.add(child);
    child.setParent(this);
  }
  public JreepadNode removeChild(int child) // Can be used to delete, OR to 'get' one for moving
  {
    if(child<0 || child > children.size()) return null;
    
    JreepadNode ret = (JreepadNode)children.remove(child);
    return ret;
  }
  public TreeNode getChildAt(int child)
  {
    if(child<0 || child>= children.size())
      return null;
    else
      return (JreepadNode)children.get(child);
  }
  public int getChildCount()
  {
    return children.size();
  }
  public boolean indent()
  {
    // Get position in parent. If zero or -1 then return.
    int pos = getIndex();
    if(pos<1) return false;
    // Get sibling node just above, and move self to there.
    getParentNode().removeChild(pos);
    JreepadNode newParent = (JreepadNode)getParentNode().getChildAt(pos-1);
    newParent.addChild(this);
    setParent(newParent);
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
    setParent(pp);

    // Also (as in the original treepad) move all the later siblings so they're children of this node


    // NOT DONE YET


    return true;
  }
  public void moveChildUp(int child)
  {
    if(child<1 || child>= children.size())
      return;

    children.add(child-1, children.remove(child));
  }
  public void moveChildDown(int child)
  {
    if(child<0 || child>= children.size()-1)
      return;

    children.add(child+1, children.remove(child));
  }
  public void moveUp()
  {
    if(getParentNode()==null) return;
    int index = getIndex();
    if(index<1) return;
    
    removeFromParent();
    getParentNode().insert(this, index-1);
  }
  public void moveDown()
  {
    if(getParentNode()==null) return;
    int index = getIndex();
    if(index<0 || index >= getParentNode().getChildCount()-1) return;
    
    removeFromParent();
    getParentNode().insert(this, index+1);
  }
  public JreepadNode addChild()
  {
    JreepadNode theChild = new JreepadNode(this);
    children.add(theChild);
    theChild.setParent(this);
    return theChild;
  }
  public JreepadNode addChild(int index)
  {
    JreepadNode theChild = new JreepadNode(this);
    children.add(index, theChild);
    theChild.setParent(this);
    return theChild;
  }
  
  public int getIndex(TreeNode child)
  {
    for(int i=0; i<getChildCount(); i++)
      if(((JreepadNode)child).equals(getChildAt(i)))
        return i;
    return -1;
  }
  public int getIndex()
  {
    if(getParent()==null)
      return -1;
    return getParent().getIndex(this);
  }
  
  public boolean isNodeInSubtree(JreepadNode n)
  {
    for(int i=0; i<getChildCount(); i++)
    {
      JreepadNode aChild = (JreepadNode)getChildAt(i);
      if(aChild.equals(n) || aChild.isNodeInSubtree(n))
        return true;
    }
    return false;
  }
  
  public void sortChildren()
  {
    sort();
  }

  public void sortChildrenRecursive()
  {
    sort();
    for(int i=0; i<getChildCount(); i++)
      ((JreepadNode)getChildAt(i)).sortChildrenRecursive();
  }

  // Function for using Java's built-in mergesort
  private void sort()
  {
    Object[] childrenArray = children.toArray();
    java.util.Arrays.sort(childrenArray, ourSortComparator);
    children = new Vector();
    for(int i=0; i<childrenArray.length; i++)
      children.add((JreepadNode)childrenArray[i]);
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
  public int OLDSIMPLEcompareTo(Object o)
  {
    return getTitle().compareToIgnoreCase(
            ((JreepadNode)o).getTitle());
  }
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

  public boolean getAllowsChildren() { return true; } // Required by TreeNode interface
  public boolean isLeaf()
  {
    return getChildCount()==0; // Is this the correct behaviour?
  }
  
  public Enumeration children()
  {
    return new JreepadNodeEnumeration();
  } // Required by TreeNode interface

  public JreepadNode getParentNode()
  {
    return parentNode;
  }
  public TreeNode getParent()
  {
    return parentNode;
  }

  // MutableTreeNode functions
  public void remove(int child)
  {
    removeChild(child);
  }
  public void remove(MutableTreeNode node)
  {
    removeChild(getIndex((JreepadNode)node));
  }
  public void removeFromParent()
  {
    if(parentNode != null)
      parentNode.remove(this);
  }
  public void setParent(MutableTreeNode parent)
  {
    parentNode = (JreepadNode)parent; // Do we need to do anything more at this point?
  }

  public void setUserObject(Object object)
  {
    // ?
    setContent(object.toString());
  }
  public void insert(MutableTreeNode child, int index)
  {
    children.insertElementAt((JreepadNode)child, index);
  }

  public void addChildFromTextFile(InputStreamReader textFile, String nodeName) throws IOException
  {
    // Load the content as a string
    StringBuffer contentString = new StringBuffer();
    String currentLine;
    BufferedReader bReader = new BufferedReader(textFile);
    while((currentLine = bReader.readLine())!=null)
      contentString.append(currentLine + "\n");
    // Then just create the node
    addChild(new JreepadNode(nodeName, contentString.toString(), this));
  }

  // This getCopy() function is intended to return a copy of the entire subtree, used for Undo
  public JreepadNode getCopy()
  {
    JreepadNode ret = new JreepadNode(getTitle(), getContent(), null);
    for(int i=0; i<getChildCount(); i++)
    {
      ret.addChild(((JreepadNode)getChildAt(i)).getCopy());
    }
    return ret;
  }
  
  public JreepadNode getChildByTitle(String title)
  {
    for(int i=0; i<getChildCount(); i++)
      if(((JreepadNode)getChildAt(i)).getTitle().equals(title))
        return (JreepadNode)getChildAt(i);
    return null;
  }

  public synchronized void wrapContentToCharWidth(int charWidth)
  {
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

  public String getTitle() { return title; }
  public String getContent() { return content; }
  public void setTitle(String title) { this.title = title; }
  public void setContent(String content) { this.content = content; }


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

/*
  public void toggleArticleMode()
  {
  
    switch(getArticleMode())
    {
      case ARTICLEMODE_ORDINARY:
        setArticleMode(ARTICLEMODE_HTML);
        break;
      case ARTICLEMODE_HTML:
        setArticleMode(ARTICLEMODE_ORDINARY);
        break;
      default:
        return;
    }
  }
*/

  static final int CSVPARSE_MODE_INQUOTES = 1;
  static final int CSVPARSE_MODE_EXPECTINGDELIMITER = 2;
  static final int CSVPARSE_MODE_EXPECTINGDATA = 3;
  protected String[][] interpretContentAsCsv()
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

  public class JreepadNodeEnumeration implements Enumeration
  {
    private int i=0;
    public boolean hasMoreElements() { return i<getChildCount(); }
    public Object nextElement()      { return getChildAt(i++);   }
  } // This enumerator class is required by the TreeNode interface
}

