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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.BoxView;
import javax.swing.text.ComponentView;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.IconView;
import javax.swing.text.LabelView;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.tree.TreePath;

import org.philwilson.JTextile;

import edu.stanford.ejalbert.BrowserLauncher;
import edu.stanford.ejalbert.exception.BrowserLaunchingExecutionException;
import edu.stanford.ejalbert.exception.BrowserLaunchingInitializingException;
import edu.stanford.ejalbert.exception.UnsupportedOperatingSystemException;

public class JreepadView extends Box implements TableModelListener
{

  // Code to ensure that the article word-wraps follows
  //   - contributed by Michael Labhard based on code found on the web...
  static class JPEditorKit extends StyledEditorKit
  {
	public ViewFactory getViewFactory()
	{
      return new JPRTFViewFactory();
	}
  }

  static class JPRTFViewFactory implements ViewFactory
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

  private static short paraRightMargin = 0;
  static class JPParagraphView extends javax.swing.text.ParagraphView
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
  private JreepadTreeModel treeModel;
  private TreeView tree;
  private JScrollPane treeView;
  private JScrollPane articleView;

  // editorPane is supposed to represent the pane currently displayed/edited - so it's the one
  //    to refer to when you're doing GUI-related stuff
  // It will be equal to one of the content-type-specific panes. Need to set the content of BOTH of these...
//  private JEditorPane editorPane;
  private JEditorPane editorPanePlainText;
  private JEditorPane editorPaneHtml;
  private JTable editorPaneCsv;


  // Undo features
//  protected UndoManager undoMgr;

  private JSplitPane splitPane;

  private JreepadSearcher searcher;

  // The following boolean should be FALSE while we're changing from node to node, and true otherwise
  private boolean copyEditorPaneContentToNodeContent = true;

  private boolean warnAboutUnsaved = false;

  // Things concerned with the "undo" function
 //OLD ATTEMPT private JreepadNode oldRootForUndo, oldCurrentNodeForUndo;
 //OLD ATTEMPT private TreePath[] oldExpandedPaths;
 //OLD ATTEMPT private TreePath oldSelectedPath;

  public JreepadView()
  {
    this(new JreepadNode("<Untitled node>", ""));
  }

  public JreepadView(JreepadNode root)
  {
    super(BoxLayout.X_AXIS);
    treeView = new JScrollPane();
    splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    splitPane.setResizeWeight(0.5);
    if(getPrefs().dividerLocation > 0)
    {
      splitPane.setDividerLocation(getPrefs().dividerLocation);
    }
    splitPane.addPropertyChangeListener("lastDividerLocation", new java.beans.PropertyChangeListener()
					 {
					   public void propertyChange(java.beans.PropertyChangeEvent evt)
					   {
						 // System.out.println(evt.getPropertyName());
						 getPrefs().dividerLocation = splitPane.getDividerLocation();
						 // System.out.println("New divider location = " + getPrefs().dividerLocation);
					   }
					 }
					 );

    this.root = root;

    treeModel = new JreepadTreeModel(root);
    treeModel.addTreeModelListener(new JreepadTreeModelListener());

    tree = new TreeView(treeModel);

    searcher = new JreepadSearcher(root);

//    undoMgr = new UndoManager();


    //Listen for when the selection changes.
    tree.addTreeSelectionListener(new TreeSelectionListener()
                   {
                     public void valueChanged(TreeSelectionEvent e)
                     {
                        JreepadNode node = (JreepadNode)
                           tree.getLastSelectedPathComponent();
                        if (node == null) return;

// UNDO DEVELOPMENT:
//                        System.out.println("TreeSelectionListener:valueChanged");
//                        undoMgr.discardAllEdits();

                        setCurrentNode(node);
                      }
                   });


    tree.addKeyListener(new KeyAdapter(){public void keyPressed(KeyEvent kee) {
     int key = kee.getKeyCode();
     switch(key)
     {
       case KeyEvent.VK_ENTER:
         addNodeBelow();
         break;
       case KeyEvent.VK_F2:
         editNodeTitleAction();
         break;
     }
     // System.out.println("Tree detected a keypress: " + kee.getKeyText(kee.getKeyCode()) + " (key code "+ kee.getKeyCode()+")");
     }});

    treeView.setViewportView(tree);


    editorPanePlainText = new JEditorPanePlus("text/plain", root.getContent());
    editorPanePlainText.setEditable(true);
    editorPaneHtml = new JEditorPanePlus("text/html", root.getContent());
    editorPaneHtml.setEditable(false);
    editorPaneCsv = new JTable(new ArticleTableModel());
    editorPaneCsv.getModel().addTableModelListener(this);

    setEditorPaneKit();

    // Add a listener to make sure the editorpane's content is always stored when it changes
    editorPanePlainText.addCaretListener(new CaretListener() {
    				public void caretUpdate(CaretEvent e)
    				{
    				  if(!copyEditorPaneContentToNodeContent)
    				    return; // i.e. we are deactivated while changing from node to node
    				  if(currentNode.getArticleMode() != JreepadNode.ARTICLEMODE_ORDINARY)
    				    return; // i.e. we are only relevant when in plain-text mode

    				  if(!editorPanePlainText.getText().equals(currentNode.getContent()))
    				  {
    				    // System.out.println("UPDATE - I'd now overwrite node content with editorpane content");
    				    currentNode.setContent(editorPanePlainText.getText());
    				    setWarnAboutUnsaved(true);
    				  }
    				  else
    				  {
    				    // System.out.println("  No need to update content.");
    				  }
    				}});
    articleView = new JScrollPane(getEditorPaneComponent(), JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    articleView.addComponentListener(new ComponentListener()
    					{
    					  public void componentResized(ComponentEvent e)
    					  {
    					    editorPanePlainText.setMaximumSize(new Dimension(articleView.getViewport().getWidth(), Integer.MAX_VALUE));
    					    editorPanePlainText.setSize(articleView.getViewport().getViewSize());
    					    editorPaneHtml.setMaximumSize(new Dimension(articleView.getViewport().getWidth(), Integer.MAX_VALUE));
    					    editorPaneHtml.setSize(articleView.getViewport().getViewSize());
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
 			     			editorPanePlainText.setPreferredSize(articleView.getViewport().getExtentSize());
    					    editorPanePlainText.setMaximumSize(new Dimension(articleView.getViewport().getWidth(), Integer.MAX_VALUE));
    					    editorPanePlainText.setSize(articleView.getViewport().getViewSize());
 			     			editorPaneHtml.setPreferredSize(articleView.getViewport().getExtentSize());
    					    editorPaneHtml.setMaximumSize(new Dimension(articleView.getViewport().getWidth(), Integer.MAX_VALUE));
    					    editorPaneHtml.setSize(articleView.getViewport().getViewSize());
    					  }
    					}
    					);

    setCurrentNode(root);

    setViewBoth();
    tree.setSelectionRow(0);

    editorPanePlainText.getDocument().addUndoableEditListener(new JreepadUndoableEditListener());

  }

  public void setEditorPaneKit()
  {
    if(getPrefs().wrapToWindow)
      editorPanePlainText.setEditorKit(new JPEditorKit());
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
      editorPanePlainText.setPreferredSize(articleView.getViewport().getExtentSize());
      editorPanePlainText.setSize(articleView.getViewport().getExtentSize());
      editorPaneHtml.setPreferredSize(articleView.getViewport().getExtentSize());
      editorPaneHtml.setSize(articleView.getViewport().getExtentSize());
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
    ensureCorrectArticleRenderMode();
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
     ensureCorrectArticleRenderMode();
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
    boolean isSame = currentNode!=null && n.equals(currentNode);
         //System.out.println("setCurrentNode() activated: sameness test = "+isSame);
    //
    //    This "isSame" test should stop the caret jumping to the end of the text when we press Save.
    //
    if(isSame)
    {
      // Only update the node's stored content if it's a plaintext node
      if(currentNode.getArticleMode() == JreepadNode.ARTICLEMODE_ORDINARY)
        currentNode.setContent(getEditorPaneText());
      return;
    }

    copyEditorPaneContentToNodeContent = false; // Deactivate the caret-listener, effectively - ALSO DEACTIVATES UNDO-STORAGE
    if(currentNode != null)
    {
      // Only update the node's stored content if it's a plaintext node
      if(currentNode.getArticleMode() == JreepadNode.ARTICLEMODE_ORDINARY)
        currentNode.setContent(getEditorPaneText());
    }
    currentNode = n;
    setEditorPaneText(n.getContent());
//    editorPanePlainText.setText(n.getContent());
//    editorPaneHtml.setText(n.getContent());
    ensureCorrectArticleRenderMode();
    copyEditorPaneContentToNodeContent = true; // Reactivate the caret listener - ALSO REACTIVATES UNDO-STORAGE
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



  public void indentCurrentNode()
  {
    if(currentNode.equals(root))
    {
      notForRootNode();
      return;
    }

    TreePath parentPath = tree.getSelectionPath().getParentPath();
    int pos = currentNode.getIndex();
    if(pos<1) return;

    //DEL storeForUndo();

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
    if(currentNode.equals(root))
    {
      notForRootNode();
      return;
    }

    TreePath parentPath = tree.getSelectionPath().getParentPath();
    if(parentPath==null) return;
    TreePath parentParentPath = parentPath.getParentPath();
    if(parentParentPath==null) return;

    //DEL storeForUndo();

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

    if(currentNode.equals(root))
    {
      notForRootNode();
      return;
    }

    //DEL storeForUndo();
    currentNode.moveUp();
    treeModel.reload(currentNode.getParent());
    tree.setSelectionPath(nodePath);
  }
  public void moveCurrentNodeDown()
  {
    TreePath nodePath = tree.getSelectionPath();

    if(currentNode.equals(root))
    {
      notForRootNode();
      return;
    }

    //DEL storeForUndo();
    currentNode.moveDown();
    treeModel.reload(currentNode.getParent());
    tree.setSelectionPath(nodePath);
  }

  private void notForRootNode()
  {
    // FIXME: If there are no child nodes, assume the user needs some advice about adding nodes
    if(root.isLeaf())
      JOptionPane.showMessageDialog(this,
       JreepadViewer.lang.getString("MSG_ONLY_ON_CHILDNODES"), JreepadViewer.lang.getString("TITLE_ONLY_ON_CHILDNODES") ,
         JOptionPane.INFORMATION_MESSAGE);
    else
      return;
//      JOptionPane.showMessageDialog(this,
//       "The root node is currently selected - you can only perform this operation on child nodes.", "Root node is selected" ,
//         JOptionPane.INFORMATION_MESSAGE);
  }

  protected String getContentForNewNode()
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
    if(currentNode.getArticleMode() != JreepadNode.ARTICLEMODE_ORDINARY)
      return; // May want to fix this later - allow other modes to have the date inserted...

    //DEL storeForUndo();
    String theDate = getCurrentDate();
    Document doc = editorPanePlainText.getDocument();
    int here = editorPanePlainText.getCaretPosition();
    try
    {
      editorPanePlainText.setText(doc.getText(0, here) + theDate +
                              doc.getText(here, doc.getLength() - here));
      editorPaneHtml.setText(doc.getText(0, here) + theDate +
                              doc.getText(here, doc.getLength() - here));
      editorPanePlainText.setCaretPosition(here + theDate.length());
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
    {
      notForRootNode();
      return null;
    }

    if(tree.getSelectionPath()==null)
      return null;
    //DEL storeForUndo();
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
    {
      notForRootNode();
      return null;
    }

    if(tree.getSelectionPath()==null)
      return null;
    //DEL storeForUndo();
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
    //DEL storeForUndo();
    JreepadNode ret = currentNode.addChild();
    ret.setContent(getContentForNewNode());
    TreePath nodePath = tree.getSelectionPath();
    treeModel.nodesWereInserted(currentNode, new int[]{currentNode.getIndex(ret)});

//    tree.setSelectionPath(nodePath.pathByAddingChild(ret));
    tree.scrollPathToVisible(nodePath.pathByAddingChild(ret));
    tree.startEditingAtPath(nodePath.pathByAddingChild(ret));
    return ret;
  }
  public JreepadNode removeNode()
  {
    JreepadNode parent = (JreepadNode)currentNode.getParent();
    TreePath parentPath = tree.getSelectionPath().getParentPath();
    if(parent != null)
    {
      //DEL storeForUndo();
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
    //DEL storeForUndo();
    currentNode.sortChildren();
    treeModel.reload(currentNode);
    // System.out.println(currentNode.toFullString());
  }
  public void sortChildrenRecursive()
  {
    //DEL storeForUndo();
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
    tree.expandAll(currentNode, tree.getLeadSelectionPath());
  }
  public void collapseAllCurrentNode()
  {
    tree.collapseAll(currentNode, tree.getLeadSelectionPath());
  }

  public TreePath[] getAllExpandedPaths()
  {
      return tree.getAllExpandedPaths();
  }

  // THIS FUNCTION SEEMS TO HAVE NO EFFECT, ON MY MACHINE AT LEAST! WHAT'S GOING ON?
  public void expandPaths(TreePath[] paths)
  {
      tree.expandPaths(paths);
  }


  // Functions and inner class for searching nodes
  public boolean performSearch(String inNodes, String inArticles, int searchWhat // 0=selected, 1=all
  							, boolean orNotAnd, boolean caseSensitive, int maxResults)
  {
    switch(searchWhat)
    {
      case 0: // search selected node
        searcher.performSearch(inNodes, inArticles, tree.getSelectionPath(), orNotAnd, caseSensitive, maxResults);
        break;
      default: // case 1==search whole tree
        searcher.performSearch(inNodes, inArticles, new TreePath(root), orNotAnd, caseSensitive, maxResults);
        break;
    }
	return true;
  }
  public JreepadSearcher.JreepadSearchResult[] getSearchResults()
  {
    return searcher.getSearchResults();
  }

  public void addChildrenFromTextFiles(File[] inFiles) throws IOException
  {
    //DEL storeForUndo();
	for(int i=0; i<inFiles.length; i++)
      getCurrentNode().addChildFromTextFile(new InputStreamReader(new FileInputStream(inFiles[i]), getPrefs().getEncoding())
                         , inFiles[i].getName());
    treeModel.reload(currentNode);
    tree.expandPath(tree.getSelectionPath());
  }

  public void addChild(JreepadNode newKid)
  {
    //DEL storeForUndo();
	getCurrentNode().add(newKid);
    treeModel.reload(currentNode);
    tree.expandPath(tree.getSelectionPath());
  }

  public void addChildrenFromListTextFile(InputStreamReader inFile) throws IOException
  {
    //DEL storeForUndo();

    BufferedReader bReader = new BufferedReader(inFile);

    String curLine;
    while((curLine = bReader.readLine())!=null)
      if(curLine.trim().length() > 0)
        getCurrentNode().add(new JreepadNode(curLine.trim(), ""));

    treeModel.reload(currentNode);
    tree.expandPath(tree.getSelectionPath());
  }

  public String getSelectedTextInArticle()
  {
    switch(currentNode.getArticleMode())
    {
      case JreepadNode.ARTICLEMODE_CSV:
        int x = editorPaneCsv.getSelectedColumn();
        int y = editorPaneCsv.getSelectedRow();
        if(x==-1 || y ==-1)
          return "";
        return editorPaneCsv.getValueAt(y,x).toString();
      case JreepadNode.ARTICLEMODE_HTML:
        return editorPaneHtml.getSelectedText();
      case JreepadNode.ARTICLEMODE_ORDINARY:
      default:
        return editorPanePlainText.getSelectedText();
    }
  }

  public static JreepadPrefs getPrefs()
  {
    return prefs;
  }
  public static void setPrefs(JreepadPrefs thesePrefs)
  {
    prefs = thesePrefs;
    prefs.save();
  }


/*

    DEPRECATED - I wrote this before discovering Java's UndoManager.



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

    editorPanePlainText.setText(currentNode.getContent());
    editorPaneHtml.setText(currentNode.getContent());
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

*/



  // Stuff concerned with linking
  public void webSearchTextSelectedInArticle()
  {
    // JComponent treeOrArticle;
    String url = getSelectedTextInArticle();

    if(url==null || url.length()==0)
      url = currentNode.getTitle();

    if((url == null) && (currentNode.getArticleMode()==JreepadNode.ARTICLEMODE_ORDINARY))
    {
      try
      {

      String text = getEditorPaneText();
      int startpos = editorPanePlainText.getCaretPosition();
      int endpos = startpos;
      if(text.length()>0)
      {
        // Select the character before/after the current position, and grow it until we hit whitespace...
        while(startpos>0 && !Character.isWhitespace(editorPanePlainText.getText(startpos-1,1).charAt(0)))
          startpos--;
        while(endpos<(text.length()) && !Character.isWhitespace(editorPanePlainText.getText(endpos,1).charAt(0)))
          endpos++;
        if(endpos>startpos)
        {
          editorPanePlainText.setSelectionStart(startpos);
          editorPanePlainText.setSelectionEnd(endpos);
          url = editorPanePlainText.getSelectedText();
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
    String url = getSelectedTextInArticle();
    if((url == null) && (currentNode.getArticleMode()==JreepadNode.ARTICLEMODE_ORDINARY))
    {
      try
      {

      String text = getEditorPaneText();
      int startpos = editorPanePlainText.getCaretPosition();
      int endpos = startpos;
      if(text != null)
      {
        // Select the character before/after the current position, and grow it until we hit whitespace...
        while(startpos>0 && !Character.isWhitespace(editorPanePlainText.getText(startpos-1,1).charAt(0)))
          startpos--;
        while(endpos<(text.length()) && !Character.isWhitespace(editorPanePlainText.getText(endpos,1).charAt(0)))
          endpos++;
        if(endpos>startpos)
        {
          editorPanePlainText.setSelectionStart(startpos);
          editorPanePlainText.setSelectionEnd(endpos);
          url = editorPanePlainText.getSelectedText();
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
	    JOptionPane.showMessageDialog(this,
	            JreepadViewer.lang.getString("MSG_NODE_NOT_FOUND"),
	            JreepadViewer.lang.getString("TITLE_NODE_NOT_FOUND"),
	            JOptionPane.ERROR_MESSAGE);
      return;
    }

    // It's probably a web-link, so let's do something to it and then try and launch it

/*

//  NOTE:
//  I haven't been able to get this file:// method to work, on Windows 2000 or on Mac OSX.
//  So I'm disactivating it for now.

    // Firstly we use Kami's method for attempting to open file:// links
    if(url.startsWith("file://"))
    {
      url =  getPrefs().openLocation.getParentFile().getPath() + System.getProperty("file.separator")+  url.substring(7);
      try
      {
        BrowserLauncher.openURL(url.toString());
      }
      catch(IOException err)
      {
        JOptionPane.showMessageDialog(this, "I/O error while opening URL:\n"+url+"\n\nThe \"BrowserLauncher\" used to open a URL is an open-source Java library \nseparate from Jreepad itself - i.e. a separate Sourceforge project. \nIt may be a good idea to submit a bug report to\nhttp://sourceforge.net/projects/browserlauncher\n\nIf you do, please remember to supply information about the operating system\nyou are using - which type, and which version.", "Error" , JOptionPane.ERROR_MESSAGE);
      }
    }
    else
    {
*/
		char[] curl = url.toCharArray();
		StringBuffer surl = new StringBuffer();
		for(int i=0; i<curl.length; i++)
		  if(curl[i]==' ')
			surl.append("%20");
		  else
			surl.append(curl[i]);
        try
        {
          new BrowserLauncher(null).openURLinBrowser(surl.toString());
        }
        catch (BrowserLaunchingInitializingException e)
        {
          displayBrowserLauncherException(e, surl.toString());
        }
        catch (BrowserLaunchingExecutionException e)
        {
          displayBrowserLauncherException(e, surl.toString());
        }
        catch (UnsupportedOperatingSystemException e)
        {
          displayBrowserLauncherException(e, surl.toString());
        }
//    }
  }

  private void displayBrowserLauncherException(Exception e, String url)
  {
    JOptionPane.showMessageDialog(this, "Error while opening URL:\n" + url + "\n"
      + e.getMessage() + "\n\n"
      + "The \"BrowserLauncher\" used to open a URL is an open-source Java library \n"
      + "separate from Jreepad itself - i.e. a separate Sourceforge project. \n"
      + "It may be a good idea to submit a bug report to\n"
      + "http://browserlaunch2.sourceforge.net/\n\n"
      + "If you do, please remember to supply information about the operating system\n"
      + "you are using - which type, and which version.", "Error",
      JOptionPane.ERROR_MESSAGE);
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
	  if(noNeedToConfirm || JOptionPane.showConfirmDialog(this, JreepadViewer.lang.getString("TITLE_NODE_NOT_FOUND_PROMPT_CREATE"), JreepadViewer.lang.getString("MSG_NODE_NOT_FOUND") , JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE)
	             == JOptionPane.YES_OPTION)
	  {
        JreepadNode newNode;
        TreePath newPath;
	    newNode = new JreepadNode(text, "");
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
    //DEL storeForUndo();
    currentNode.wrapContentToCharWidth(charWidth);
    editorPanePlainText.setText(currentNode.getContent());
    editorPaneHtml.setText(currentNode.getContent());
    setWarnAboutUnsaved(true);
  }
  public void stripAllTags()
  {
    //DEL storeForUndo();
    currentNode.stripAllTags();
    editorPanePlainText.setText(currentNode.getContent());
    editorPaneHtml.setText(currentNode.getContent());
    setWarnAboutUnsaved(true);
  }


  public void setArticleMode(int newMode)
  {
//    System.out.println("\n\n\nnode content : " + currentNode.getContent()
//          + "\neditorPanePlainText contains: " + editorPanePlainText.getText());

    copyEditorPaneContentToNodeContent = false; // Disables store-for-undo

    currentNode.setContent(editorPanePlainText.getText());
/*
    switch(currentNode.getArticleMode())
    {
      case JreepadNode.ARTICLEMODE_ORDINARY:
        currentNode.setContent(editorPanePlainText.getText());
        break;
      case JreepadNode.ARTICLEMODE_HTML:
        currentNode.setContent(editorPaneHtml.getText());
        break;
      case JreepadNode.ARTICLEMODE_CSV:
        currentNode.setContent(jTableContentToCsv());
        break;
      default:
        return;
    }
*/
    switch(newMode)
    {
      case JreepadNode.ARTICLEMODE_ORDINARY:
        // DELETEME - PLAINTEXT SHOULD NOT BE AFFECTED BY OTHERS
        editorPanePlainText.setText(currentNode.getContent());
        break;
      case JreepadNode.ARTICLEMODE_HTML:
        editorPaneHtml.setText(currentNode.getContent());
        break;
      case JreepadNode.ARTICLEMODE_TEXTILEHTML:
        try{
          editorPaneHtml.setText(JTextile.textile(currentNode.getContent()));
        }catch(Exception e){
          editorPaneHtml.setText(currentNode.getContent());
        }
        break;
      case JreepadNode.ARTICLEMODE_CSV:
        articleToJTable(currentNode.getContent());
        break;
      default:
        return;
    }
    currentNode.setArticleMode(newMode);
    ensureCorrectArticleRenderMode();
    getEditorPaneComponent().repaint();
    copyEditorPaneContentToNodeContent = true; // Re-enables store-for-undo
  }

  public void ensureCorrectArticleRenderMode()
  {
    articleView.setViewportView(getEditorPaneComponent());
  }

  public void articleToJTable()
  {
    String[][] rowData = currentNode.interpretContentAsCsv();
    String[] columnNames = null;

//    System.out.println("articleToJTable(): rows=" + rowData.length + ", cols="+rowData[0].length);
    initJTable(rowData, columnNames);
  }
  public void articleToJTable(String s)
  {
    String[][] rowData = JreepadNode.interpretContentAsCsv(s);
    String[] columnNames = new String[rowData[0].length];
    for(int i=0; i<columnNames.length; i++)
      columnNames[i] = " ";

//    System.out.println("articleToJTable(s): rows=" + rowData.length + ", cols="+rowData[0].length);
    initJTable(rowData, columnNames);
  }

  private void initJTable(String[][] rowData, String[] columnNames)
  {
    editorPaneCsv = new JTable(new ArticleTableModel(rowData, columnNames));
//    editorPaneCsv = new JTable(new ArticleTableModel(rowData, columnNames),
//             (getCurrentNode().tblColModel==null ? new ArticleTableColumnModel(): getCurrentNode().tblColModel));
//    editorPaneCsv = new JTable(rowData, columnNames);
//    editorPaneCsv.setModel(new ArticleTableModel());
    editorPaneCsv.getModel().addTableModelListener(this);
    editorPaneCsv.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    editorPaneCsv.setGridColor(Color.GRAY);
    editorPaneCsv.setShowGrid(true);
    editorPaneCsv.setShowVerticalLines(true);
    editorPaneCsv.setShowHorizontalLines(true);
  }

  // The following functions allow us to use either a JEditorPane or a JTable to display article data
  JComponent getEditorPaneComponent()
  {
    if(currentNode==null)
      return editorPanePlainText; // This is a bit of a hack - it shouldn't really even be called to act on null

    switch(currentNode.getArticleMode())
    {
      case JreepadNode.ARTICLEMODE_ORDINARY:
        return editorPanePlainText;
      case JreepadNode.ARTICLEMODE_HTML:
      case JreepadNode.ARTICLEMODE_TEXTILEHTML:
        return editorPaneHtml;
      case JreepadNode.ARTICLEMODE_CSV:
        return editorPaneCsv;
      default:
        System.err.println("getEditorPaneComponent() says: JreepadNode.getArticleMode() returned an unrecognised value");
        return null;
    }
  }
  String getEditorPaneText()
  {
    switch(currentNode.getArticleMode())
    {
      case JreepadNode.ARTICLEMODE_ORDINARY:
        return editorPanePlainText.getText();
      case JreepadNode.ARTICLEMODE_HTML:
        return editorPaneHtml.getText();
      case JreepadNode.ARTICLEMODE_TEXTILEHTML:
        return editorPaneHtml.getText();
      case JreepadNode.ARTICLEMODE_CSV:
		return jTableContentToCsv();
      default:
        System.err.println("getEditorPaneText() says: JreepadNode.getArticleMode() returned an unrecognised value");
        return null;
    }
  }
  void setEditorPaneText(String s)
  {
    try{
      editorPanePlainText.setText(s);
    }catch(Exception ex){
      // This shouldn't cause a problem. So this try-catch is only for debugging really.
      System.err.println("setEditorPaneText(): Exception during editorPanePlainText.setText(s)");
      System.err.println("String: " + s);
      ex.printStackTrace();
    }
    switch(currentNode.getArticleMode())
    {
      case JreepadNode.ARTICLEMODE_ORDINARY:
        break;
      case JreepadNode.ARTICLEMODE_HTML:
	    editorPaneHtml.setText(s);
        break;
      case JreepadNode.ARTICLEMODE_TEXTILEHTML:
        try{
          editorPaneHtml.setText(JTextile.textile(s));
        }catch(Exception e){
          editorPaneHtml.setText(s);
        }
        break;
      case JreepadNode.ARTICLEMODE_CSV:
        articleToJTable(s);
        break;
      default:
        System.err.println("setEditorPaneText() says: JreepadNode.getArticleMode() returned an unrecognised value");
        return;
    }
 /*
    switch(currentNode.getArticleMode())
    {
      case JreepadNode.ARTICLEMODE_ORDINARY:
	    editorPanePlainText.setText(s);
        break;
      case JreepadNode.ARTICLEMODE_HTML:
	    editorPaneHtml.setText(s);
        break;
      case JreepadNode.ARTICLEMODE_CSV:
        articleToJTable(s);
        break;
      default:
        System.err.println("setEditorPaneText() says: JreepadNode.getArticleMode() returned an unrecognised value");
        return;
    }
*/
  }
  // End of: functions which should allow us to switch between JEditorPane and JTable



  public void editNodeTitleAction()
  {
    tree.startEditingAtPath(tree.getSelectionPath());
  }

  public String jTableContentToCsv()
  {
	int w = editorPaneCsv.getColumnCount();
	int h = editorPaneCsv.getRowCount();
	StringBuffer csv = new StringBuffer();
	String quoteMark = getPrefs().addQuotesToCsvOutput ? "\"" : "";
	for(int i=0; i<h; i++)
	{
	  for(int j=0; j<(w-1); j++)
		csv.append(quoteMark + (String)editorPaneCsv.getValueAt(i,j) + quoteMark + ",");
	  csv.append(quoteMark + (String)editorPaneCsv.getValueAt(i,w-1) + quoteMark + "\n");
	}
	return csv.toString();
  }

  // Called by the TableModelListener interface
  public void tableChanged(TableModelEvent e)
  {
    // System.out.println(" -- tableChanged() -- ");
    if(currentNode.getArticleMode() == JreepadNode.ARTICLEMODE_CSV)
      currentNode.setContent(jTableContentToCsv());
  }

  class JreepadTreeModelListener implements TreeModelListener
  {
    public void treeNodesChanged(TreeModelEvent e)
    {
      warnAboutUnsaved = true;
//      Object[] parentPath = e.getPath(); // Parent of the changed node(s)
//      int[] children = e.getChildIndices(); // Indices of the changed node(s)
//      JreepadNode parent = (JreepadNode)(parentPath[parentPath.length-1]);
      tree.repaint();
    }
    public void treeNodesInserted(TreeModelEvent e)
    {
      warnAboutUnsaved = true;
      Object[] parentPath = e.getPath(); // Parent of the new node(s)
      int[] children = e.getChildIndices(); // Indices of the new node(s)
      JreepadNode parent = (JreepadNode)(parentPath[parentPath.length-1]);
      tree.expandPath(e.getTreePath());
      tree.scrollPathToVisible(e.getTreePath());
      tree.repaint();
    }
    public void treeNodesRemoved(TreeModelEvent e)
    {
      warnAboutUnsaved = true;
      Object[] parentPath = e.getPath(); // Parent of the removed node(s)
      int[] children = e.getChildIndices(); // Indices the node(s) had before they were removed
      JreepadNode parent = (JreepadNode)(parentPath[parentPath.length-1]);
      tree.repaint();
    }
    public void treeStructureChanged(TreeModelEvent e)
    {
      warnAboutUnsaved = true;
      Object[] parentPath = e.getPath(); // Parent of the changed node(s)
      JreepadNode parent = (JreepadNode)(parentPath[parentPath.length-1]);
      tree.repaint();
    }
  } // End of: class JreepadTreeModelListener


  class ArticleTableModel extends DefaultTableModel {
   public ArticleTableModel(Object[][] data,  Object[] columnNames){
     super(data, columnNames);
   }
   public ArticleTableModel(){
     super();
   }


   public boolean isCellEditable(int row, int col) {
     return false;
   }
  } // End of: class ArticleTableModel


  class JEditorPanePlus extends JEditorPane implements DocumentListener {
    JEditorPanePlus(String type, String text) {
      super(type, text);
    }
    public void changedUpdate(DocumentEvent e){
      setWarnAboutUnsaved(true);
    }
    public void insertUpdate(DocumentEvent e){
      setWarnAboutUnsaved(true);
    }
    public void removeUpdate(DocumentEvent e){
      setWarnAboutUnsaved(true);
    }
  } // End of class JEditorPanePlus


    //This one listens for edits that can be undone.
    protected class JreepadUndoableEditListener
                    implements UndoableEditListener {
        public void undoableEditHappened(UndoableEditEvent e) {

            //System.out.println("Undoable event is " + (e.getEdit().isSignificant()?"":"NOT ") + "significant");
            //System.out.println("Undoable event source: " + e.getEdit());

            //Remember the edit and update the menus.

            if(copyEditorPaneContentToNodeContent){
              //System.out.println("Storing undoable event for node " + getCurrentNode().getTitle());
              //System.out.println("   Event is " + e.getEdit().getPresentationName() );
              //if(getCurrentNode().lastEditStyle != e.getEdit().getPresentationName()){
              //  System.out.println("   This is a SIGNIFICANT change.");
              //}
              //System.out.println("   Content: " + getCurrentNode().getContent());
              //System.out.println("   Node undoMgr: " + getCurrentNode().undoMgr);
              //Thread.currentThread().dumpStack();
              getCurrentNode().undoMgr.addEdit(e.getEdit());
            }



            //undoAction.updateUndoState();
            //redoAction.updateRedoState();
        }
    }









/*
  class ArticleTableColumnModel extends DefaultTableColumnModel {
   public ArticleTableColumnModel(){
     super();
     initColListener();
   }

   private void initColListener(){
     addColumnModelListener(new TableColumnModelListener()
            {
              public void columnAdded(TableColumnModelEvent e){ storeColumnModel();           }
              public void columnMarginChanged(ChangeEvent e){ storeColumnModel();             }
              public void columnMoved(TableColumnModelEvent e){ storeColumnModel();           }
              public void columnRemoved(TableColumnModelEvent e){ storeColumnModel();         }
              public void columnSelectionChanged(ListSelectionEvent e){ storeColumnModel();}
            });
   }

   private void storeColumnModel()
   {
     // Simply store the TableColumnModel in the node, for future reference
     getCurrentNode().tblColModel = this;
   }

  } // End of: class ArticleTableColumnModel
*/

}