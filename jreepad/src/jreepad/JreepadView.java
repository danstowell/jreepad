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
import javax.swing.tree.*;
import javax.swing.event.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Enumeration;
import java.util.Vector;
import java.io.*;
import java.awt.print.*;

public class JreepadView extends Box
{

  // Code to ensure that the article word-wraps follows
  //   - contributed by Michael Labhard based on code found on the web...
  class JPEditorKit extends StyledEditorKit
  {
	public ViewFactory getViewFactory()
	{
      return new JPRTFViewFactory();
	}
  }

  class JPRTFViewFactory implements ViewFactory
  {
	public View create(Element elem)
	{
      String kind = elem.getName();
	  if(kind != null)
		if (kind.equals(AbstractDocument.ContentElementName)) {
			return new LabelView(elem);
		} else if (kind.equals(AbstractDocument.ParagraphElementName)) {
			return new JPParagraphView(elem);
		} else if (kind.equals(AbstractDocument.SectionElementName)) {
			return new BoxView(elem, View.Y_AXIS);
		} else if (kind.equals(StyleConstants.ComponentElementName)) {
			return new ComponentView(elem);
		} else if (kind.equals(StyleConstants.IconElementName)) {
			return new IconView(elem);
		}
		// default to text display
		return new LabelView(elem);
	}
  }

  private short paraRightMargin = 0; 
  class JPParagraphView extends javax.swing.text.ParagraphView
  {
	public JPParagraphView(Element e)
	{
	  super(e);
	  this.setInsets((short)0, (short)0, (short)0, paraRightMargin);
	}
  }
  // Code to ensure that the article word-wraps ends here
  //   - contributed by Michael Labhard

  private static JreepadPrefs prefs;
  private JreepadNode root;
  private JreepadNode currentNode;
  private JreepadNode currentDragDropNode;
  private TreeNode topNode;
  private JreepadTreeModel treeModel;
  private JTree tree;
  private JScrollPane treeView;
  private JScrollPane articleView;
  private JEditorPane editorPane;
  private JSplitPane splitPane;

  // The following boolean should be FALSE while we're changing from node to node, and true otherwise
  private boolean copyEditorPaneContentToNodeContent = true;

  private boolean warnAboutUnsaved = false;

  // Things concerned with the "undo" function
  private JreepadNode oldRootForUndo, oldCurrentNodeForUndo;
  private TreePath[] oldExpandedPaths;
  private TreePath oldSelectedPath;

  public JreepadView()
  {
    this(new JreepadNode());
  }
  
  public JreepadView(JreepadNode root)
  {
    super(BoxLayout.X_AXIS);
    treeView = new JScrollPane();
    splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    splitPane.setResizeWeight(0.5);

    this.root = root;

    treeModel = new JreepadTreeModel(root);
    treeModel.addTreeModelListener(new JreepadTreeModelListener());

    tree = new JTree(treeModel);
    tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    tree.setExpandsSelectedPaths(true);
    tree.setInvokesStopCellEditing(true);
    tree.setEditable(true);
    
    tree.setModel(treeModel);

    DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
    renderer.setOpenIcon(null);
    renderer.setClosedIcon(null);
    renderer.setLeafIcon(null);
    tree.setCellRenderer(renderer);

    //Listen for when the selection changes.
    tree.addTreeSelectionListener(new TreeSelectionListener()
                   {
                     public void valueChanged(TreeSelectionEvent e)
                     {
                        JreepadNode node = (JreepadNode)
                           tree.getLastSelectedPathComponent();
                        if (node == null) return;

                      //  JreepadNode nodeInfo = (JreepadNode)(node.getUserObject());
                        setCurrentNode(node);
                      }
                   }); 

    // Add mouse listener - this will be used to implement drag-and-drop, context menu (?), etc
    MouseListener ml = new MouseAdapter()
    {
      public void mousePressed(MouseEvent e)
      {
        TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
//        System.out.println("Mouse pressed: path = " + selPath);
        if(selPath != null)
        {
          currentDragDropNode = (JreepadNode)selPath.getLastPathComponent();
          setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
          // if(e.getClickCount() == 1) {mySingleClick(selPath);}
//            System.out.println("Setting dragdrop node to " + currentDragDropNode);
        }
      }
      public void mouseReleased(MouseEvent e)
      {
        TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
//        System.out.println("Mouse released: path = " + selPath);
        if(selPath != null)
        {
          if(currentDragDropNode != null && 
             currentDragDropNode.getParentNode() != null && 
             currentDragDropNode.getParentNode() != (JreepadNode)selPath.getLastPathComponent() && 
             currentDragDropNode != (JreepadNode)selPath.getLastPathComponent())
          {
            // Then we need to perform a drag-and-drop operation!
//            System.out.println("Drag-and-drop event occurred!");
            moveNode(currentDragDropNode, (JreepadNode)selPath.getLastPathComponent());
            
            // Ensure that the destination node is open
            tree.setSelectionPath(selPath.pathByAddingChild(currentDragDropNode));
          }
        }
        setCursor(Cursor.getDefaultCursor());
        currentDragDropNode = null;
      }
      public void mouseClicked(MouseEvent e)
      {
        TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
 //       System.out.println("Mouse clicked: path = " + selPath);
        if(selPath != null)
        {
          if(e.isPopupTrigger())
          {
            // Now we can implement the pop-up content menu
      //      System.out.println("Context menu would be launched here!");
          }
        }
      }
    };
    tree.addMouseListener(ml); 
 
 
    treeView.setViewportView(tree);


    editorPane = new JEditorPane("text/plain", root.getContent());
    editorPane.setEditable(true);
    setEditorPaneKit();
    // Add a listener to make sure the editorpane's content is always stored when it changes
    editorPane.addCaretListener(new CaretListener() {
    				public void caretUpdate(CaretEvent e)
    				{
    				  if(!copyEditorPaneContentToNodeContent)
    				    return; // i.e. we are deactivated while changing from node to node
    				
    				  if(!editorPane.getText().equals(currentNode.getContent()))
    				  {
    				    // System.out.println("UPDATE - I'd now overwrite node content with editorpane content");
    				    currentNode.setContent(editorPane.getText());
    				    setWarnAboutUnsaved(true);
    				  }
    				  else
    				  {
    				    // System.out.println("  No need to update content.");
    				  }
    				}});
    articleView = new JScrollPane(editorPane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    articleView.addComponentListener(new ComponentListener()
    					{
    					  public void componentResized(ComponentEvent e)
    					  {
    					    editorPane.setMaximumSize(new Dimension(articleView.getViewport().getWidth(), Integer.MAX_VALUE));
    					    editorPane.setSize(articleView.getViewport().getViewSize());
    					  }
    					  public void componentMoved(ComponentEvent e){}
    					  public void componentHidden(ComponentEvent e){}
    					  public void componentShown(ComponentEvent e){}
    					}
    					);
    articleView.getViewport().addChangeListener(new ChangeListener()
    					{
    					  public void stateChanged(ChangeEvent e)
    					  {
 			     editorPane.setPreferredSize(articleView.getViewport().getExtentSize());
    					    editorPane.setMaximumSize(new Dimension(articleView.getViewport().getWidth(), Integer.MAX_VALUE));
    					    editorPane.setSize(articleView.getViewport().getViewSize());
    					  }
    					}
    					);

    setViewBoth();
    setCurrentNode(root);
    tree.setSelectionRow(0);
  }

  public void setEditorPaneKit()
  {
    if(getPrefs().wrapToWindow)
      editorPane.setEditorKit(new JPEditorKit());
 //   else
 //     editorPane.setEditorKit(new javax.swing.text.StyledEditorKit());
  }

  public void setViewMode(int mode)
  {
//      System.out.println("-------------------------------------------------------------");
//      System.out.println("editorPane size: " + editorPane.getSize());
//      System.out.println("articleView size: " + articleView.getSize());
//      System.out.println("articleView viewport size: " + articleView.getViewport().getSize());
//      System.out.println();
      
    switch(mode)
    {
      case JreepadPrefs.VIEW_BOTH:
        setViewBoth();
        break;
      case JreepadPrefs.VIEW_TREE:
        setViewTreeOnly();
        break;
      case JreepadPrefs.VIEW_ARTICLE:
        setViewArticleOnly();
        break;
      default:
        System.err.println("Invalid argument to JreepadView.setViewMode()!");
        return;
    }
      setSize(getSize());
      editorPane.setPreferredSize(articleView.getViewport().getExtentSize());
      editorPane.setSize(articleView.getViewport().getExtentSize());
      validate(); 
      repaint();
//      System.out.println("editorPane size: " + editorPane.getSize());
//      System.out.println("articleView size: " + articleView.getSize());
//      System.out.println("articleView viewport size: " + articleView.getViewport().getSize());
//      System.out.println();
//      System.out.println();
  }

  private void setViewBoth()
  {   
      splitPane.setLeftComponent(treeView);
      splitPane.setRightComponent(articleView);
      this.add(splitPane);
//      editorPane.setSize(articleView.getSize());
//      editorPane.setSize(articleView.getViewport().getViewSize());
  }
  private void setViewTreeOnly()
  {   this.remove(splitPane);
      this.remove(articleView);
      this.add(treeView);
      treeView.setSize(getSize());
  }
  private void setViewArticleOnly()
  {
       this.remove(splitPane);
       this.remove(treeView);
       this.add(articleView); 
       articleView.setSize(getSize());   
  }
  /*
  private void setCurrentNode(DefaultMutableTreeNode node)
  {
    setCurrentNode((JreepadNode)(node.getUserObject()));
  }
  */
  private void setCurrentNode(JreepadNode n)
  {
    copyEditorPaneContentToNodeContent = false; // Deactivate the caret-listener, effectively
    if(currentNode != null)
    {
      currentNode.setContent(editorPane.getText());
    }
    currentNode = n;
    editorPane.setText(n.getContent());
    copyEditorPaneContentToNodeContent = true; // Reactivate the caret listener
  }

  public JTree getTree()
  {
    return tree;
  }
  public JreepadNode getRootJreepadNode()
  {
    setCurrentNode(getCurrentNode()); // Ensures any edits have been committed
    return root;
  }
  public JreepadNode getCurrentNode()
  {
    return currentNode;
  }

  public String getTreepadNodeUrl()
  {
    StringBuffer ret = new StringBuffer("\"node:/");
    Object[] p = tree.getLeadSelectionPath().getPath();
    for(int i=0; i<p.length; i++)
      ret.append("/" + ((JreepadNode)p[i]).getTitle());
    return ret.toString() + "\"";
  }

  public void moveNode(JreepadNode node, JreepadNode newParent)
  {
    // First we need to make sure that the node is not a parent of the new parent
    // - otherwise things would go really wonky!
    if(node.isNodeInSubtree(newParent))
    {
      return;
    }
    
    storeForUndo();
    
    JreepadNode oldParent = node.getParentNode();

    // Now make a note of the expanded/collapsed state of the subtree of the moving node
    boolean thisOnesExpanded = tree.isExpanded(tree.getSelectionPath());
    Enumeration enum;
    Vector expanded;
    if(thisOnesExpanded)
    {
      enum = tree.getExpandedDescendants(tree.getSelectionPath());
      expanded = new Vector();
      while(enum.hasMoreElements())
      {
        expanded.add((TreePath)enum.nextElement());
//        System.out.println(expanded.lastElement());
      }
    }

    node.removeFromParent();
    newParent.addChild(node);

    treeModel.reload(oldParent);
    treeModel.reload(newParent);
  //  treeModel.reload((TreeNode)tree.getPathForRow(0).getLastPathComponent());
    
    // If the destination node didn't previously have any children, then we'll expand it
 //   if(newParent.getChildCount()==1)
      
    
    // Reapply the expanded/collapsed states
    
  }

  public void indentCurrentNode()
  {
    int nodeRow = tree.getLeadSelectionRow();
    TreePath parentPath = tree.getSelectionPath().getParentPath();
    int pos = currentNode.getIndex();
    if(pos<1) return;
    
    storeForUndo();
    
    JreepadNode newParent = ((JreepadNode)currentNode.getParent().getChildAt(pos-1));
    
    if(currentNode.indent())
    {
      treeModel.reload(currentNode.getParent().getParent());
      parentPath = parentPath.pathByAddingChild(newParent);
      TreePath myPath = parentPath.pathByAddingChild(currentNode);
      // Now use scrollPathToVisible() or scrollRowToVisible() to make sure it's visible
      tree.scrollPathToVisible(myPath);
      tree.setSelectionPath(myPath);
    }
  }
  public void outdentCurrentNode()
  {
    TreePath parentPath = tree.getSelectionPath().getParentPath();
    if(parentPath==null) return;
    TreePath parentParentPath = parentPath.getParentPath();
    if(parentParentPath==null) return;

    storeForUndo();

    if(currentNode.outdent())
    {
      TreePath myPath = parentParentPath.pathByAddingChild(currentNode);
      treeModel.reload(currentNode.getParent());
      // Now use scrollPathToVisible() or scrollRowToVisible() to make sure it's visible
      tree.scrollPathToVisible(myPath);
      tree.setSelectionPath(myPath);
    }
  }

  public void moveCurrentNodeUp()
  {
    TreePath nodePath = tree.getSelectionPath();
    storeForUndo();
    currentNode.moveUp();
    treeModel.reload(currentNode.getParent());
    tree.setSelectionPath(nodePath);
  }
  public void moveCurrentNodeDown()
  {
    TreePath nodePath = tree.getSelectionPath();
    storeForUndo();
    currentNode.moveDown();
    treeModel.reload(currentNode.getParent());
    tree.setSelectionPath(nodePath);
  }
  
  private String getContentForNewNode()
  {
    if(prefs.autoDateInArticles)
      return getCurrentDate(); // java.text.DateFormat.getDateInstance().format(new java.util.Date());
    else
      return "";
  }
  
  private java.text.DateFormat dateFormat = java.text.DateFormat.getDateInstance();
  private String getCurrentDate()
  {
    return dateFormat.format(new java.util.Date());
  }
  
  public void insertDate()
  {
    storeForUndo();
    String theDate = getCurrentDate();
    Document doc = editorPane.getDocument();
    int here = editorPane.getCaretPosition();
    try
    {
      editorPane.setText(doc.getText(0, here) + theDate + 
                              doc.getText(here, doc.getLength() - here)); 
      editorPane.setCaretPosition(here + theDate.length()); 
    }
    catch(BadLocationException e)
    {
      // Simply ignore this
    }
  }
  
  public JreepadNode addNodeAbove()
  {
    int index = currentNode.getIndex();
    if(index==-1)
      return null;
    storeForUndo();
    TreePath parentPath = tree.getSelectionPath().getParentPath();
    JreepadNode parent = currentNode.getParentNode();
    JreepadNode ret = parent.addChild(index);
    ret.setContent(getContentForNewNode());
    treeModel.nodesWereInserted(parent, new int[]{index});
    TreePath newPath = (parentPath.pathByAddingChild(ret));
    if(newPath!=null)
      tree.startEditingAtPath(newPath);
    return ret;
  }
  public JreepadNode addNodeBelow()
  {
    int index = currentNode.getIndex();
    if(index==-1)
      return null;
    storeForUndo();
    TreePath parentPath = tree.getSelectionPath().getParentPath();
    JreepadNode parent = currentNode.getParentNode();
    JreepadNode ret = parent.addChild(index+1);
    ret.setContent(getContentForNewNode());
    treeModel.nodesWereInserted(parent, new int[]{index+1});
    tree.startEditingAtPath(parentPath.pathByAddingChild(ret));
    return ret;
  }
  public JreepadNode addNode()
  {
    storeForUndo();
    JreepadNode ret = currentNode.addChild();
    ret.setContent(getContentForNewNode());
    TreePath nodePath = tree.getSelectionPath();
    treeModel.nodesWereInserted(currentNode, new int[]{currentNode.getIndex(ret)});
    tree.startEditingAtPath(nodePath.pathByAddingChild(ret));
    return ret;
  }
  public JreepadNode removeNode()
  {
    JreepadNode parent = (JreepadNode)currentNode.getParent();
    TreePath parentPath = tree.getSelectionPath().getParentPath();
    if(parent != null)
    {
      storeForUndo();
      int index = parent.getIndex(currentNode);
      JreepadNode ret = parent.removeChild(index);
      setCurrentNode(parent);

      tree.setSelectionPath(parentPath);
      treeModel.nodesWereRemoved(parent, new int[]{index}, new Object[]{ret});

      repaint();
      return ret;
    }
    else
      return null;
  }

  public void sortChildren()
  {
    storeForUndo();
    currentNode.sortChildren();
    treeModel.reload(currentNode);
    // System.out.println(currentNode.toFullString());
  }
  public void sortChildrenRecursive()
  {
    storeForUndo();
    currentNode.sortChildrenRecursive();
    treeModel.reload(currentNode);
    // System.out.println(currentNode.toFullString());
  }
  
  public void returnFocusToTree()
  {
    tree.requestFocus();
  }
  
  public void expandAllCurrentNode()
  {
    expandAll(currentNode, tree.getLeadSelectionPath());
  }
  public void expandAll(JreepadNode thisNode, TreePath tp)
  {
    // It's at this point that we expand the current element
    tree.expandPath(tp);
    
    Enumeration getKids = thisNode.children();
    JreepadNode thisKid;
    while(getKids.hasMoreElements())
    {
      thisKid = (JreepadNode)getKids.nextElement();
      expandAll(thisKid, tp.pathByAddingChild(thisKid));
    }
  }
  public void collapseAllCurrentNode()
  {
    collapseAll(currentNode, tree.getLeadSelectionPath());
  }
  public void collapseAll(JreepadNode thisNode, TreePath tp)
  {
    Enumeration getKids = thisNode.children();
    JreepadNode thisKid;
    while(getKids.hasMoreElements())
    {
      thisKid = (JreepadNode)getKids.nextElement();
      collapseAll(thisKid, tp.pathByAddingChild(thisKid));
    }
    // It's at this point that we collapse the current element
    tree.collapsePath(tp);
  }


  // Functions and inner class for searching nodes
  private JreepadSearchResult[] searchResults;
  private Vector searchResultsVec;
  private Object foundObject;
  public boolean performSearch(String inNodes, String inArticles, int searchWhat /* 0=selected, 1=all */,
  							boolean orNotAnd, boolean caseSensitive, int maxResults)
  {
    searchResults = null;
    searchResultsVec = new Vector();
    
    // Now look through the nodes, adding things to searchResultsVec if found.
    switch(searchWhat)
    {
      case 0: // search selected node
        recursiveSearchNode(inNodes, inArticles, currentNode, tree.getSelectionPath(), orNotAnd, caseSensitive, maxResults);
        break;
      default: // case 1==search whole tree
        recursiveSearchNode(inNodes, inArticles, root, new TreePath(root), orNotAnd, caseSensitive, maxResults);
        break;
    }

    if(searchResultsVec.size()>0)
    {
      searchResults = new JreepadSearchResult[searchResultsVec.size()];
      for(int i=0; i<searchResults.length; i++)
      {
        foundObject = searchResultsVec.get(i);
        searchResults[i] = (JreepadSearchResult)foundObject;
      }
      return true;
    }
    return false;
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
  // End of: functions and inner class for searching nodes

  public void addChildrenFromTextFiles(File[] inFiles) throws IOException
  {
    storeForUndo();
	for(int i=0; i<inFiles.length; i++)
      getCurrentNode().addChildFromTextFile(new InputStreamReader(new FileInputStream(inFiles[i]), getPrefs().getEncoding())
                         , inFiles[i].getName());
    treeModel.reload(currentNode);
    tree.expandPath(tree.getSelectionPath());
  }
  
  public void addChild(JreepadNode newKid)
  {
    storeForUndo();
	getCurrentNode().addChild(newKid);
    treeModel.reload(currentNode);
    tree.expandPath(tree.getSelectionPath());
  }

  public void addChildrenFromListTextFile(InputStreamReader inFile) throws IOException
  {
    storeForUndo();

    BufferedReader bReader = new BufferedReader(inFile);

    String curLine;
    while((curLine = bReader.readLine())!=null)
      if(curLine.trim().length() > 0)
        getCurrentNode().addChild(new JreepadNode(curLine.trim(), "", getCurrentNode()));

    treeModel.reload(currentNode);
    tree.expandPath(tree.getSelectionPath());
  }

  public String getSelectedTextInArticle()
  {
    return editorPane.getSelectedText();
  }

  // Stuff concerned with printing
  public void printCurrentArticle()
  {
    PrinterJob job = PrinterJob.getPrinterJob();
    if(job.printDialog())
    {
      PageFormat pageFormat = job.validatePage(job.defaultPage());
      job.setJobName(currentNode.getTitle());
      try
      {
        job.print();
      }
      catch(PrinterException err)
      {
        System.out.println(err);
      }
    }
  }
  // End of: stuff concerned with printing
  
  public static JreepadPrefs getPrefs()
  {
    return prefs;
  }
  public static void setPrefs(JreepadPrefs thesePrefs)
  {
    prefs = thesePrefs;
  }
  
  // Stuff concerned with undo
  public void undoAction()
  {
    if(!canWeUndo())
      return;
  
    // Swap the old root / selectionpath / expandedpaths for the current ones
    JreepadNode tempRoot = root;
    root = oldRootForUndo;
    oldRootForUndo = tempRoot;

    // Fire a tree-structure-changed event for the entire tree
    treeModel.setRoot(root);
    treeModel.reload(root);
    // Set the correct selection and expanded paths
    tree.setSelectionPath(oldSelectedPath); // I hope this ends up firing the setCurrentNode() function...

    editorPane.setText(currentNode.getContent());
    repaint();
  }
  public boolean canWeUndo()
  {
    return oldRootForUndo != null;
  }
  private void storeForUndo()
  {
    // Use JreepadNode.getCopy() on the root node to get a copy of the whole tree
    oldRootForUndo = root.getCopy();
    // Also get the tree's entire set of open TreePaths and selected TreePaths
    oldSelectedPath = tree.getSelectionPath();
  }
  void clearUndoCache()
  {
    oldRootForUndo = null;
  }
  // End of: stuff concerned with undo

  // Stuff concerned with linking
  public void webSearchTextSelectedInArticle()
  {
    // JComponent treeOrArticle;
    String url = editorPane.getSelectedText();

    if(url==null || url.length()==0)
      url = currentNode.getTitle();
  
    if(url==null)
    {
      try
      {
      String text = editorPane.getText();
      int startpos = editorPane.getCaretPosition();
      int endpos = startpos;
      if(text.length()>0)
      {
        // Select the character before/after the current position, and grow it until we hit whitespace...
        while(startpos>0 && !Character.isWhitespace(editorPane.getText(startpos-1,1).charAt(0)))
          startpos--;
        while(endpos<(text.length()) && !Character.isWhitespace(editorPane.getText(endpos,1).charAt(0)))
          endpos++;
        if(endpos>startpos)
        {
          editorPane.setSelectionStart(startpos);
          editorPane.setSelectionEnd(endpos);
          url = editorPane.getSelectedText();
        }
      }
      }
      catch(BadLocationException err)
      {
System.out.println(err);
      }
    }

    if(url==null || !(url.length()>0))
      url = currentNode.getTitle();

    webSearchText(url);
  }
  public void webSearchText(String text)
  {
    openURL("http://" + getPrefs().webSearchPrefix + text + getPrefs().webSearchPostfix);
  }
  public void openURLSelectedInArticle()
  {
    String url = editorPane.getSelectedText();
    if(url == null)
    {
      try
      {
      String text = editorPane.getText();
      int startpos = editorPane.getCaretPosition();
      int endpos = startpos;
      if(text != null)
      {
        // Select the character before/after the current position, and grow it until we hit whitespace...
        while(startpos>0 && !Character.isWhitespace(editorPane.getText(startpos-1,1).charAt(0)))
          startpos--;
        while(endpos<(text.length()) && !Character.isWhitespace(editorPane.getText(endpos,1).charAt(0)))
          endpos++;
        if(endpos>startpos)
        {
          editorPane.setSelectionStart(startpos);
          editorPane.setSelectionEnd(endpos);
          url = editorPane.getSelectedText();
        }
      }
      }
      catch(BadLocationException err)
      {
      }
    }
    openURL(url);
  }
  public static boolean isPureWord(String in)
  {
    char[] c = in.toCharArray();
    for(int i=0; i<c.length; i++)
      if(c[i]==':' || c[i]=='/' || c[i]=='[' || c[i]==']')
        return false;
    return true;
  }
  public static boolean isWikiWord(String in)
  {
    if(in.length()>4 && in.startsWith("[[") && in.endsWith("]]"))
      return true;

    char[] c = in.toCharArray();
    int uppers = 0;
    boolean currentlyUpper = false;
    for(int i=0; i<c.length; i++)
      if(!Character.isLetter(c[i]))
        return false;
      else if(i==0 && !Character.isUpperCase(c[i]))
        return false;
      else
        if(currentlyUpper && Character.isLowerCase(c[i]))
        {
          currentlyUpper = false;
          uppers++;
        }
        else if(!currentlyUpper && Character.isUpperCase(c[i]))
        {
          currentlyUpper = true;
        }
    return uppers>1;
  }
  public void openURL(String url)
  {
    if(url==null || url=="")
      return;
    url = url.trim();

    // Wiki-like links
    if(isWikiWord(url))
    {
      followWikiLink(url, prefs.wikiBehaviourActive);
      return;
    }
//    if(url.length()>4 && url.startsWith("[[") && url.endsWith("]]"))
//    {
//      followWikiLink(url.substring(2, url.length()-2));
//      return;
//    }
    if(isPureWord(url))
    {
      if(prefs.defaultSearchMode == 0)
        webSearchText(url);
      else
        followWikiLink(url, false);
      return;
    }
    
    // Strip quotes off
    if(url.length()>2 && url.startsWith("\"") && url.endsWith("\""))
      url = url.substring(1, url.length()-1);

    // Treepad node:// links
    if(url.startsWith("node://"))
    {
      if(!followTreepadInternalLink(url))
	    JOptionPane.showMessageDialog(this, "No node found in the current file\nto match that path.", "Not found" , JOptionPane.ERROR_MESSAGE);
      return;
    }
    
    // It's probably a web-link, so let's do something to it and then try and launch it
    char[] curl = url.toCharArray();
    StringBuffer surl = new StringBuffer();
//    if(url.indexOf(":") == -1)
//      surl.append("http://");
    for(int i=0; i<curl.length; i++)
      if(curl[i]==' ')
        surl.append("%20");
      else
        surl.append(curl[i]);
    try
    {
      BrowserLauncher.openURL(surl.toString());
    }
    catch(IOException err)
    {
	  JOptionPane.showMessageDialog(this, "I/O error while opening URL:\n"+surl+"\n\nThe \"BrowserLauncher\" used to open a URL is an open-source Java library \nseparate from Jreepad itself - i.e. a separate Sourceforge project. \nIt may be a good idea to submit a bug report to\nhttp://sourceforge.net/projects/browserlauncher\n\nIf you do, please remember to supply information about the operating system\nyou are using - which type, and which version.", "Error" , JOptionPane.ERROR_MESSAGE);
    }
  }

  public boolean followTreepadInternalLink(String url)
  {
      url = url.substring(7);
      // Split it at slashes, and then add each one to the new TreePath object as we go
      Vector pathNames = new Vector();
      StringBuffer buf = new StringBuffer();
      char[] curl = url.toCharArray();
      for(int i=0; i<curl.length; i++)
        if(curl[i]=='/')
        {
          pathNames.add(buf.toString());
          buf = new StringBuffer();
        }
        else
          buf.append(curl[i]);
      if(buf.length()>0)
        pathNames.add(buf.toString());

//      System.out.println(pathNames);

      // OK, so we've got the names into an array. Now how do we actually follow the path?
      if(pathNames.size()<1 || !((String)pathNames.get(0)).equals(root.getTitle()))
        return false;
      TreePath goTo = new TreePath(root);
      JreepadNode nextNode = root;
      for(int i=1; i<pathNames.size(); i++)
      {
        nextNode = nextNode.getChildByTitle((String)pathNames.get(i));
        if(nextNode == null)
          return false;
        goTo = goTo.pathByAddingChild(nextNode);
      }
      tree.setSelectionPath(goTo);
	  tree.scrollPathToVisible(goTo);
	  return true;
  }
  // End of: stuff concerned with linking

  // Searching (for wikilike action)
  public void followWikiLink(String text, boolean noNeedToConfirm)
  {
    if(text.length()>4 && text.startsWith("[[") && text.endsWith("]]"))
      text = text.substring(2, text.length()-2);

    TreePath tp = findNearestNodeTitled(text);
    if(tp == null)
    {
	  if(noNeedToConfirm || JOptionPane.showConfirmDialog(this, "No node named \n\"" + text + "\"\nwas found. Create it?", "Not found" , JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE)
	             == JOptionPane.YES_OPTION)
	  {
        JreepadNode newNode;
        TreePath newPath;
	    newNode = new JreepadNode(text, "", currentNode);
	    addChild(newNode);
	    TreePath leadPath = tree.getLeadSelectionPath();
	    if(leadPath != null)
	      newPath = leadPath.pathByAddingChild(newNode);
	    else
	      newPath = new TreePath(newNode);
	    
	    // Now we need to select it... how do we do that?
	    tree.setSelectionPath(newPath);
	    tree.scrollPathToVisible(newPath);
	  }
    }
    else
      tree.setSelectionPath(tp);
  }
  public TreePath findNearestNodeTitled(String text)
  {
    TreePath curPath = tree.getLeadSelectionPath();
    TreePath tp;
    while(curPath != null && curPath.getPathCount()>0)
    {
      tp = findChildTitled(text, curPath);
      if(tp!=null)
        return tp;
      // Else try again but using the parent...
      curPath = curPath.getParentPath();
    }
    return null;
  }
  public TreePath findChildTitled(String text)
  {
    return findChildTitled(text, tree.getLeadSelectionPath());
  }
  public TreePath findChildTitled(String text, TreePath pathToNode)
  {
    JreepadNode myNode = (JreepadNode)pathToNode.getLastPathComponent();
    JreepadNode myChild;
    TreePath childPath;
    for(int i=0; i<myNode.getChildCount(); i++)
    {
      myChild = (JreepadNode)myNode.getChildAt(i);
      childPath = pathToNode.pathByAddingChild(myChild);
      if(myChild.getTitle().equals(text))
        return childPath;
      else
      { // Ask the child to search its descendents
        childPath = findChildTitled(text, childPath);
        if(childPath!=null)
          return childPath;
      }
    }
    return null;
    
  }
  // End of: Searching (for wikilike action)

  public boolean warnAboutUnsaved()
  {
    return warnAboutUnsaved;
  }
  void setWarnAboutUnsaved(boolean yo)
  {
    warnAboutUnsaved = yo;
  }
  
//  public void setTreeFont(Font f)
//  {
//    ((DefaultTreeCellRenderer)tree.getCellRenderer()).setFont(f);
//  }
//  public void setArticleFont(Font f)
//  {
//    editorPane.setFont(f);
//  }

  public void wrapContentToCharWidth(int charWidth)
  {
    storeForUndo();
    currentNode.wrapContentToCharWidth(charWidth);
    editorPane.setText(currentNode.getContent());
    setWarnAboutUnsaved(true);
  }
  public void stripAllTags()
  {
    storeForUndo();
    currentNode.stripAllTags();
    editorPane.setText(currentNode.getContent());
    setWarnAboutUnsaved(true);
  }


  class JreepadTreeModelListener implements TreeModelListener
  {
    public void treeNodesChanged(TreeModelEvent e)
    {
      Object[] parentPath = e.getPath(); // Parent of the changed node(s)
      int[] children = e.getChildIndices(); // Indices of the changed node(s)
      JreepadNode parent = (JreepadNode)(parentPath[parentPath.length-1]);
      tree.repaint();
    }
    public void treeNodesInserted(TreeModelEvent e)
    {
      Object[] parentPath = e.getPath(); // Parent of the new node(s)
      int[] children = e.getChildIndices(); // Indices of the new node(s)
      JreepadNode parent = (JreepadNode)(parentPath[parentPath.length-1]);
      tree.expandPath(e.getTreePath());
      tree.scrollPathToVisible(e.getTreePath());
      tree.repaint();
    }
    public void treeNodesRemoved(TreeModelEvent e)
    {
      Object[] parentPath = e.getPath(); // Parent of the removed node(s)
      int[] children = e.getChildIndices(); // Indices the node(s) had before they were removed
      JreepadNode parent = (JreepadNode)(parentPath[parentPath.length-1]);
      tree.repaint();
    }
    public void treeStructureChanged(TreeModelEvent e)
    {
      Object[] parentPath = e.getPath(); // Parent of the changed node(s)
      JreepadNode parent = (JreepadNode)(parentPath[parentPath.length-1]);
      tree.repaint();
    }
  } // End of: class JreepadTreeModelListener

}