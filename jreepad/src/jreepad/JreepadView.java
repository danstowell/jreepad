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
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.tree.TreePath;

import jreepad.editor.ContentChangeListener;
import jreepad.editor.HtmlViewer;
import jreepad.editor.PlainTextEditor;
import jreepad.editor.TableViewer;
import jreepad.editor.TextileViewer;
import edu.stanford.ejalbert.BrowserLauncher;
import edu.stanford.ejalbert.exception.BrowserLaunchingExecutionException;
import edu.stanford.ejalbert.exception.BrowserLaunchingInitializingException;
import edu.stanford.ejalbert.exception.UnsupportedOperatingSystemException;

public class JreepadView extends Box
{

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
  private PlainTextEditor editorPanePlainText;
  private HtmlViewer editorPaneHtml;
  private TextileViewer editorPaneTextile;
  private TableViewer editorPaneCsv;

  private JComponent currentArticleView;


  // Undo features
//  protected UndoManager undoMgr;

  private JSplitPane splitPane;

  private JreepadSearcher searcher;

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


    editorPanePlainText = new PlainTextEditor(root.getArticle());
    editorPaneHtml = new HtmlViewer(root.getArticle());
    editorPaneTextile = new TextileViewer(root.getArticle());
    editorPaneCsv = new TableViewer(root.getArticle());

    articleView = new JScrollPane(getEditorPaneComponent(), JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

    /* XXX Is this really needed?
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
    */

    editorPanePlainText.setContentChangeListener(new ContentChangeListener() {
    	public void contentChanged()
    	{
    		setWarnAboutUnsaved(true);
    	}
    });

    setCurrentNode(root);

    setViewBoth();
    tree.setSelectionRow(0);
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
      currentArticleView.setPreferredSize(articleView.getViewport().getExtentSize());
      currentArticleView.setSize(articleView.getViewport().getExtentSize());
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

  private void setCurrentNode(JreepadNode n)
  {
    //    This should stop the caret jumping to the end of the text when we press Save.
    if (currentNode == n)
      return;

    editorPanePlainText.lockEdits(); // Deactivate the caret-listener, effectively - ALSO DEACTIVATES UNDO-STORAGE

    currentNode = n;
    setEditorPaneText(n.getArticle());
    ensureCorrectArticleRenderMode();

    editorPanePlainText.unlockEdits(); // Reactivate the caret listener - ALSO REACTIVATES UNDO-STORAGE
  }

  public JTree getTree()
  {
    return tree;
  }

  public JreepadNode getRootJreepadNode()
  {
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
    if(currentNode.getArticle().getArticleMode() != JreepadArticle.ARTICLEMODE_ORDINARY)
      return; // May want to fix this later - allow other modes to have the date inserted...

    //DEL storeForUndo();
    String theDate = getCurrentDate();
    editorPanePlainText.insertText(theDate);
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
    ret.getArticle().setContent(getContentForNewNode());
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
    ret.getArticle().setContent(getContentForNewNode());
    treeModel.nodesWereInserted(parent, new int[]{index+1});
    tree.startEditingAtPath(parentPath.pathByAddingChild(ret));
    return ret;
  }

  public JreepadNode addNode()
  {
    //DEL storeForUndo();
    JreepadNode ret = currentNode.addChild();
    ret.getArticle().setContent(getContentForNewNode());
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
    switch(currentNode.getArticle().getArticleMode())
    {
      case JreepadArticle.ARTICLEMODE_CSV:
        return editorPaneCsv.getSelectedText();
      case JreepadArticle.ARTICLEMODE_HTML:
        return editorPaneHtml.getSelectedText();
      case JreepadArticle.ARTICLEMODE_TEXTILEHTML:
    	  return editorPaneTextile.getSelectedText();
      case JreepadArticle.ARTICLEMODE_ORDINARY:
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

  // Stuff concerned with linking
  public void webSearchTextSelectedInArticle()
  {
    // JComponent treeOrArticle;
    String url = getSelectedTextInArticle();

    if(url==null || url.length()==0)
      url = currentNode.getTitle();

    if((url == null) && (currentNode.getArticle().getArticleMode()==JreepadArticle.ARTICLEMODE_ORDINARY))
      url = editorPanePlainText.selectWordUnderCursor();

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
    if((url == null) && (currentNode.getArticle().getArticleMode()==JreepadArticle.ARTICLEMODE_ORDINARY))
      url = editorPanePlainText.selectWordUnderCursor();
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
    currentNode.getArticle().wrapContentToCharWidth(charWidth);
    editorPanePlainText.reloadArticle();
    editorPaneHtml.reloadArticle();
    editorPaneTextile.reloadArticle();
    editorPaneCsv.reloadArticle();
    setWarnAboutUnsaved(true);
  }

  public void stripAllTags()
  {
    //DEL storeForUndo();
    currentNode.getArticle().stripAllTags();
    editorPanePlainText.reloadArticle();
    editorPaneHtml.reloadArticle();
    editorPaneTextile.reloadArticle();
    editorPaneCsv.reloadArticle();
    setWarnAboutUnsaved(true);
  }


  public void setArticleMode(int newMode)
  {
    editorPanePlainText.lockEdits(); // Disables store-for-undo

    switch(newMode)
    {
      case JreepadArticle.ARTICLEMODE_ORDINARY:
        editorPanePlainText.reloadArticle();
        break;
      case JreepadArticle.ARTICLEMODE_HTML:
        editorPaneHtml.reloadArticle();
        break;
      case JreepadArticle.ARTICLEMODE_TEXTILEHTML:
        editorPaneTextile.reloadArticle();
        break;
      case JreepadArticle.ARTICLEMODE_CSV:
    	editorPaneCsv.reloadArticle();
        break;
      default:
        return;
    }
    currentNode.getArticle().setArticleMode(newMode);
    ensureCorrectArticleRenderMode();
    getEditorPaneComponent().repaint();
    editorPanePlainText.unlockEdits(); // Re-enables store-for-undo
  }

  public void ensureCorrectArticleRenderMode()
  {
    articleView.setViewportView(getEditorPaneComponent());
  }

  // The following functions allow us to use either a JEditorPane or a JTable to display article data
  JComponent getEditorPaneComponent()
  {
    if(currentNode==null)
    {
      currentArticleView = editorPanePlainText;
      return currentArticleView; // This is a bit of a hack - it shouldn't really even be called to act on null
    }

    switch(currentNode.getArticle().getArticleMode())
    {
      case JreepadArticle.ARTICLEMODE_ORDINARY:
    	  currentArticleView = editorPanePlainText;
    	  break;
      case JreepadArticle.ARTICLEMODE_HTML:
    	  currentArticleView = editorPaneHtml;
    	  break;
      case JreepadArticle.ARTICLEMODE_TEXTILEHTML:
    	  currentArticleView = editorPaneTextile;
    	  break;
      case JreepadArticle.ARTICLEMODE_CSV:
    	  currentArticleView = editorPaneCsv;
    	  break;
      default:
        System.err.println("getEditorPaneComponent() says: JreepadNode.getArticleMode() returned an unrecognised value");
        return null;
    }
    return currentArticleView;
  }

  String getEditorPaneText()
  {
    switch(currentNode.getArticle().getArticleMode())
    {
      case JreepadArticle.ARTICLEMODE_ORDINARY:
        return editorPanePlainText.getText();
      case JreepadArticle.ARTICLEMODE_HTML:
        return editorPaneHtml.getText();
      case JreepadArticle.ARTICLEMODE_TEXTILEHTML:
        return editorPaneTextile.getText();
      case JreepadArticle.ARTICLEMODE_CSV:
		return editorPaneCsv.getText();
      default:
        System.err.println("getEditorPaneText() says: JreepadNode.getArticleMode() returned an unrecognised value");
        return null;
    }
  }

  void setEditorPaneText(JreepadArticle a)
  {
    editorPanePlainText.setArticle(a);
    editorPaneHtml.setArticle(a);
    editorPaneTextile.setArticle(a);
	editorPaneCsv.setArticle(a);
  }
  // End of: functions which should allow us to switch between JEditorPane and JTable


  public void editNodeTitleAction()
  {
    tree.startEditingAtPath(tree.getSelectionPath());
  }

  class JreepadTreeModelListener implements TreeModelListener
  {
    public void treeNodesChanged(TreeModelEvent e)
    {
      warnAboutUnsaved = true;
      tree.repaint();
    }

    public void treeNodesInserted(TreeModelEvent e)
    {
      warnAboutUnsaved = true;
      tree.expandPath(e.getTreePath());
      tree.scrollPathToVisible(e.getTreePath());
      tree.repaint();
    }

    public void treeNodesRemoved(TreeModelEvent e)
    {
      warnAboutUnsaved = true;
      tree.repaint();
    }

    public void treeStructureChanged(TreeModelEvent e)
    {
      warnAboutUnsaved = true;
      tree.repaint();
    }
  } // End of: class JreepadTreeModelListener
}
