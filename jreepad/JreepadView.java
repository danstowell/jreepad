package jreepad;

import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import java.awt.event.*;

/*

The original free Windows version is 380Kb

Todo:
- Drag-and-drop of nodes
- Menus and the actions they entail
- Toolbar actions
- Search for text
- The article needs to resize properly, EVERY time its container (its scrollpane) is resized

*/

public class JreepadView extends Box
{
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

/* DEPRECATED - HOPEFULLY!
    topNode = new DefaultMutableTreeNode(root);

    // Now set up all the JTree, DefaultTreeNode, stuff
    createNodes(topNode, root);

    treeModel = new DefaultTreeModel(topNode);
*/
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
          }
        }
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
            System.out.println("Context menu would be launched here!");
          }
        }
      }
    };
    tree.addMouseListener(ml); 
 
 
    treeView.setViewportView(tree);


    editorPane = new JEditorPane("text/plain", root.getContent());
    editorPane.setEditable(true);
    articleView = new JScrollPane(editorPane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    articleView.addComponentListener(new ComponentListener()
    					{
    					  public void componentResized(ComponentEvent e)
    					  {
    					    editorPane.setSize(articleView.getViewport().getViewSize());
    					  }
    					  public void componentMoved(ComponentEvent e){}
    					  public void componentHidden(ComponentEvent e){}
    					  public void componentShown(ComponentEvent e){}
    					}
    					);

    setViewBoth();
    setCurrentNode(root);
    tree.setSelectionRow(0);
  }

  public void setViewBoth()
  {   
      splitPane.setLeftComponent(treeView);    splitPane.setRightComponent(articleView);
      this.add(splitPane);
      setSize(getSize()); 
      editorPane.setSize(articleView.getViewport().getViewSize());
      validate(); 
      repaint();
  }
  public void setViewTreeOnly()
  {   this.remove(splitPane);
      this.remove(articleView);
      this.add(treeView);
      setSize(getSize());  treeView.setSize(getSize());
      validate(); repaint();
  }
  public void setViewArticleOnly()
  {    this.remove(splitPane);
       this.remove(treeView);
       this.add(articleView); 
       setSize(getSize());  articleView.setSize(getSize()); validate(); repaint();
  }
  /*
  private void setCurrentNode(DefaultMutableTreeNode node)
  {
    setCurrentNode((JreepadNode)(node.getUserObject()));
  }
  */
  private void setCurrentNode(JreepadNode n)
  {
    if(currentNode != null)
    {
      currentNode.setContent(editorPane.getText());
    }
    currentNode = n;
    editorPane.setText(n.getContent());
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

  public void moveNode(JreepadNode node, JreepadNode newParent)
  {
    // First we need to make sure that the node is not a parent of the new parent
    // - otherwise things would go really wonky!
    if(node.isNodeInSubtree(newParent))
    {
//      System.out.println("New parent is in subtree - therefore moving not possible!");
      return;
    }
    JreepadNode oldParent = node.getParentNode();
    node.removeFromParent();
    newParent.addChild(node);
//    treeModel.reload(oldParent);
    treeModel.reload((TreeNode)tree.getPathForRow(0).getLastPathComponent());
  }

  public void indentCurrentNode()
  {
    int nodeRow = tree.getLeadSelectionRow();
    TreePath parentPath = tree.getSelectionPath().getParentPath();
    int pos = currentNode.getIndex();
    if(pos<1) return;
    
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

    if(currentNode.outdent())
    {
      TreePath myPath = parentParentPath.pathByAddingChild(currentNode);
      treeModel.reload(currentNode.getParent());
      // Now use scrollPathToVisible() or scrollRowToVisible() to make sure it's visible
      tree.scrollPathToVisible(myPath);
      tree.setSelectionPath(myPath);
      System.out.println("New path: " + myPath);
    }
  }

  public void moveCurrentNodeUp()
  {
    TreePath nodePath = tree.getSelectionPath();
    currentNode.moveUp();
    treeModel.reload(currentNode.getParent());
    tree.setSelectionPath(nodePath);
  }
  public void moveCurrentNodeDown()
  {
    TreePath nodePath = tree.getSelectionPath();
    currentNode.moveDown();
    treeModel.reload(currentNode.getParent());
    tree.setSelectionPath(nodePath);
  }
  
  public JreepadNode addNodeAbove()
  {
    int index = currentNode.getIndex();
    if(index==-1)
      return null;
    TreePath parentPath = tree.getSelectionPath().getParentPath();
    JreepadNode parent = currentNode.getParentNode();
    JreepadNode ret = parent.addChild(index);
    treeModel.nodesWereInserted(parent, new int[]{index});
    tree.startEditingAtPath(parentPath.pathByAddingChild(ret));
    return ret;
  }
  public JreepadNode addNodeBelow()
  {
    int index = currentNode.getIndex();
    if(index==-1)
      return null;
    TreePath parentPath = tree.getSelectionPath().getParentPath();
    JreepadNode parent = currentNode.getParentNode();
    JreepadNode ret = parent.addChild(index+1);
    treeModel.nodesWereInserted(parent, new int[]{index+1});
    tree.startEditingAtPath(parentPath.pathByAddingChild(ret));
    return ret;
  }
  public JreepadNode addNode()
  {
    JreepadNode ret = currentNode.addChild();
    TreePath nodePath = tree.getSelectionPath();
    treeModel.nodesWereInserted(currentNode, new int[]{currentNode.getIndex(ret)});
    tree.startEditingAtPath(nodePath.pathByAddingChild(ret));
    return ret;
  }
  public JreepadNode removeNode()
  {
    JreepadNode parent = (JreepadNode)currentNode.getParent();
    if(parent != null)
    {
      int index = parent.getIndex(currentNode);
      JreepadNode ret = parent.removeChild(index);
      currentNode = parent;

      treeModel.nodesWereRemoved(parent, new int[]{index}, new Object[]{ret});

      repaint();
      return ret;
    }
    else
      return null;
  }

  public void sortChildren()
  {
    currentNode.sortChildren();
    treeModel.reload(currentNode);
    // System.out.println(currentNode.toFullString());
  }
  public void sortChildrenRecursive()
  {
    currentNode.sortChildrenRecursive();
    treeModel.reload(currentNode);
    // System.out.println(currentNode.toFullString());
  }
  
  public void returnFocusToTree()
  {
    tree.requestFocus();
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