package net.sourceforge.jreepad;

import java.util.*;
import java.io.*;

public class JreepadNode implements Serializable
{
  private Vector children;
  private String title;
  private String content;
//  private int childrenCount=0;
  private JreepadNode parentNode;

  public JreepadNode()
  {
    this("Untitled node","", null);
  }
  public JreepadNode(String title, String content, JreepadNode parentNode)
  {
    this.title = title;
    this.content = content;
    this.parentNode = parentNode;
    children = new Vector();
  }
  public JreepadNode(InputStream treeInputStream) throws IOException
  {
    constructFromInputStream(treeInputStream);
  }
  private void constructFromInputStream(InputStream treeInputStream) throws IOException
  {
    int lineNum = 2;
    int depthMarker;
    BufferedReader bReader = new BufferedReader(new InputStreamReader(treeInputStream));
    JreepadNode babyNode;
    children = new Vector();

    Stack nodeStack = new Stack();
    nodeStack.push(this);

    String dtLine, nodeLine, titleLine, depthLine;
    StringBuffer currentContent;
    String currentLine = bReader.readLine(); // Read the first line, check for treepadness
    if(! (currentLine.toLowerCase().startsWith("<treepad") && currentLine.endsWith(">")) )
    {
      throw new IOException("\"<Treepad>\" tag not found at beginning of file!");
    }

    while((dtLine = bReader.readLine())!=null && (nodeLine = bReader.readLine())!=null && 
          (titleLine = bReader.readLine())!=null && (depthLine = bReader.readLine())!=null)
    {
      // Read "dt=text"    [or error]
      if(! (dtLine.toLowerCase().startsWith("dt=text")))
        throw new IOException("Unrecognised node format at line " + lineNum + ": " + dtLine);
      // Read "<node>"     [or error]
      if(! (nodeLine.toLowerCase().startsWith("<node>")))
        throw new IOException("Unrecognised node format at line " + (lineNum+1) + ": " + nodeLine);

//      System.out.println("Found node (depth " + depthLine + "): " + titleLine);

      lineNum += 4;

      // Read THE CONTENT! [loop until we find "<end node> 5P9i0s8y19Z"]
      currentContent = new StringBuffer();
      while((currentLine = bReader.readLine())!=null && !currentLine.equals("<end node> 5P9i0s8y19Z"))
      {
        currentContent.append(currentLine + "\r\n");
        lineNum++;
      }

      // Now, having established the content and the title and the depth, we'll create the child
      babyNode = new JreepadNode(titleLine, currentContent.substring(0, Math.max(currentContent.length()-2,0)),
                             (JreepadNode)(nodeStack.peek()));
//      babyNode = new JreepadNode(titleLine, currentContent.toString());

      if(depthLine.equals("0"))
      {
        this.title = titleLine;
        this.content = currentContent.toString();
      }
      else
      {
        depthMarker = Integer.parseInt(depthLine);
        while(nodeStack.size()>depthMarker)
          nodeStack.pop();

 //       System.out.println("   Adding it to a ");

        ((JreepadNode)(nodeStack.peek())).addChild(babyNode);
        nodeStack.push(babyNode);

        // Remember THE LEVEL!   [single integer: 0 is root node, 1 is children of root, etc.]
      
        // Now add the correct node to the right point in the tree
      }
    }

  } // End of constructor from FileInputStream

  public String toString()
  {
    return getTitle();
  }
  public String toFullString()
  {
    String ret = "JreepadNode \""+getTitle()+"\": " + getNumberOfChildren() + " direct child nodes in subtree";
    ret += "\r\nDirect children:";
    for(int i=0; i<children.size(); i++)
      ret += "\r\n    " + getChild(i).getTitle();
    return ret;
  }

  private void writeObject(ObjectOutputStream out) throws IOException
  {
    out.writeBytes(this.toTreepadString());
  }
  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
  {
    constructFromInputStream(in);
  }
  public String toTreepadString()
  {
    return "<Treepad version 2.7>\r\n" + toTreepadString(0);
  }
  public String toTreepadString(int currentDepth)
  {
    StringBuffer ret = new StringBuffer("dt=Text\r\n<node>\r\n");
    ret.append(getTitle() + "\r\n" + (currentDepth++) + "\r\n" + getContent() + "\r\n<end node> 5P9i0s8y19Z\r\n");
    for(int i=0; i<getNumberOfChildren(); i++)
      ret.append(getChild(i).toTreepadString(currentDepth));
    return ret.toString();
  }

  public void addChild(JreepadNode child)
  {
    children.add(child);
 //   childrenCount++;
  }
  public JreepadNode removeChild(int child) // Can be used to delete, OR to 'get' one for moving
  {
    JreepadNode ret = (JreepadNode)children.remove(child);
//    childrenCount--;
    return ret;
  }
  public JreepadNode getChild(int child)
  {
    if(child<0 || child>= children.size())
      return null;
    else
      return (JreepadNode)children.get(child);
  }
  public int getNumberOfChildren()
  {
    return children.size();
  }

  public void indentChild(int child)
  {
    if(child<0 || child>= children.size())
      return;
//    else
//      return (JreepadNode)children.get(child);
  }
  public void outdentChild(int child)
  {
    if(child<0 || child>= children.size())
      return;
//    else
//      return (JreepadNode)children.get(child);
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
  public void moveChildUp(JreepadNode thisChild)
  {
    moveChildUp(children.indexOf(thisChild));
  }
  public void moveChildDown(JreepadNode thisChild)
  {
    moveChildDown(children.indexOf(thisChild));
  }
  public void moveUp()
  {
    if(getParentNode()!=null)
      getParentNode().moveChildUp(this);
  }
  public void moveDown()
  {
    if(getParentNode()!=null)
      getParentNode().moveChildDown(this);
  }

  public JreepadNode getParentNode()
  {
    return parentNode;
  }

/*
  public JreepadNode[] search(String searchText, int maxResults, 
                              boolean searchNodes, boolean searchArticles)
  {
  }
*/

  public String getTitle() { return title; }
  public String getContent() { return content; }
  public void setTitle(String title) { this.title = title; }
  public void setContent(String content) { this.content = content; }

}

