package jreepad;

import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import java.awt.event.*;

/*

(((((Changing just for the sake of it)))))

The original free Windows version is 380Kb

Todo:
- Drag-and-drop of nodes
- Menus and the actions they entail
- Toolbar actions
- Search for text
- Make certain that extra line breaks aren't being introduced/lost during the Save/Load processes
- The article needs to resize when its container (its scrollpane) is resized

*/

public class JreepadView extends Box
{
  private JreepadNode root;
  private JreepadNode currentNode;
  private DefaultMutableTreeNode topNode;
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

    topNode = new DefaultMutableTreeNode(root);

    // Now set up all the JTree, DefaultTreeNode, stuff
    createNodes(topNode, root);

    tree = new JTree(topNode)/* {
                      protected void fireValueChanged(TreeSelectionEvent e)
                      {
                        System.out.println(( (JreepadNode)(((DefaultMutableTreeNode)(e.getPath().getLastPathComponent())).getUserObject())
                        ).getTitle());
                        super.fireValueChanged(e);
                      }
                             } */;
    tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    tree.setEditable(true);

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
                        DefaultMutableTreeNode node = (DefaultMutableTreeNode)
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
  private void setCurrentNode(DefaultMutableTreeNode node)
  {
    setCurrentNode((JreepadNode)(node.getUserObject()));
  }
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
  }
  public void moveCurrentNodeDown()
  {
    currentNode.moveDown();
  }

  private void createNodes(DefaultMutableTreeNode parentNode, JreepadNode parent)
  {
    DefaultMutableTreeNode temp;
    for(int i=0; i<parent.getNumberOfChildren(); i++)
    {
      temp = new DefaultMutableTreeNode(parent.getChild(i)); // Create a "leaf"
      createNodes(temp, parent.getChild(i)); // Create any children the leaf requires
      parentNode.add(temp); // Add the leaf on to the tree
    }
  }

}