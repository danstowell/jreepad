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

  public void moveCurrentNodeUp()
  {
    currentNode.moveUp();
    treeModel.reload(currentNode.getParent());
  }
  public void moveCurrentNodeDown()
  {
    currentNode.moveDown();
    treeModel.reload(currentNode.getParent());
  }
  
  public JreepadNode addNode()
  {
    JreepadNode ret = currentNode.addChild();
    treeModel.nodesWereInserted(currentNode, new int[]{currentNode.getIndex(ret)});
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

/*
DEPRECATED - HOPEFULLY! Should simply use JreepadNodes instead of mirroring them in DMTNs

  private void createNodes(DefaultMutableTreeNode parentNode, JreepadNode parent)
  {
    DefaultMutableTreeNode temp;
    for(int i=0; i<parent.getChildCount(); i++)
    {
      temp = new DefaultMutableTreeNode(parent.getChildAt(i)); // Create a "leaf"
      createNodes(temp, (JreepadNode)parent.getChildAt(i)); // Create any children the leaf requires
      parentNode.add(temp); // Add the leaf on to the tree
    }
  }
*/

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