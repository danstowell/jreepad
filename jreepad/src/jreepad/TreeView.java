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

import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.JTree;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;


/**
 * The GUI component that displays the tree.
 *
 * @version $Id$
 */
public class TreeView extends JTree
{
    private JreepadTreeModel treeModel;

    public TreeView(JreepadTreeModel treeModel)
    {
        super(treeModel);
        this.treeModel = treeModel;

        getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        setExpandsSelectedPaths(true);
        setInvokesStopCellEditing(true);
        setEditable(true);

        DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
        renderer.setOpenIcon(null);
        renderer.setClosedIcon(null);
        renderer.setLeafIcon(null);
        setCellRenderer(renderer);


        // Fiddle with the cell editor - to ensure that when editing a new node, you shouldn't be
        // able to leave a blank title
        getCellEditor().addCellEditorListener(new CellEditorListener()
            {
                public void editingCanceled(ChangeEvent e)
                {
                    ensureNodeTitleIsNotEmpty(e);
                }

                public void editingStopped(ChangeEvent e)
                {
                    ensureNodeTitleIsNotEmpty(e);
                }
            });

        // Add mouse listener - this will be used to implement drag-and-drop, context menu (?), etc
        addMouseListener(new TreeViewMouseListener());

    }

    public void cancelEditing()
    {
        super.cancelEditing(); // if we can override this perhaps we can prevent blank nodes...?
        JreepadNode lastEditedNode = (JreepadNode)(getSelectionPath().getLastPathComponent());
        if (lastEditedNode.getTitle().equals(""))
            lastEditedNode.setTitle("<Untitled node>");
    }

    private void ensureNodeTitleIsNotEmpty(ChangeEvent e)
    {
      TreeCellEditor theEditor = (TreeCellEditor)getCellEditor();
      String newTitle = (String)(theEditor.getCellEditorValue());

//      JreepadNode thatNode = (JreepadNode)(tree.getEditingPath().getLastPathComponent());
//      System.out.println("ensureNodeTitleIsNotEmpty(): Event source = " + e.getSource());
//      System.out.println("ensureNodeTitleIsNotEmpty(): thatNode = " + thatNode);
//      System.out.println("getCellEditorValue() = " + newTitle);

      if(newTitle.equals(""))
      {
        theEditor.getTreeCellEditorComponent(this, "<Untitled node>", true, true, false, 1);
//        thatNode.setTitle("<Untitled node>");
      }
    }

    public void moveNode(JreepadNode node, JreepadNode newParent)
    {
        // First we need to make sure that the node is not a parent of the new parent
        // - otherwise things would go really wonky!
        if (node.isNodeInSubtree(newParent))
        {
            return;
        }

        // DEL storeForUndo();

        JreepadNode oldParent = node.getParentNode();

        // Now make a note of the expanded/collapsed state of the subtree of the moving node
        boolean thisOnesExpanded = isExpanded(getSelectionPath());
        Enumeration enumer;
        Vector expanded;
        if (thisOnesExpanded)
        {
            enumer = getExpandedDescendants(getSelectionPath());
            expanded = new Vector();
            while (enumer.hasMoreElements())
            {
                expanded.add((TreePath)enumer.nextElement());
                // System.out.println(expanded.lastElement());
            }
        }

        node.removeFromParent();
        newParent.add(node);

        treeModel.reload(oldParent);
        treeModel.reload(newParent);
        // treeModel.reload((TreeNode)tree.getPathForRow(0).getLastPathComponent());

        // If the destination node didn't previously have any children, then we'll expand it
        //   if(newParent.getChildCount()==1)

        // Reapply the expanded/collapsed states

    }

    public void expandAll(JreepadNode thisNode, TreePath tp)
    {
        // It's at this point that we expand the current element
        expandPath(tp);

        Enumeration getKids = thisNode.children();
        JreepadNode thisKid;
        while (getKids.hasMoreElements())
        {
            thisKid = (JreepadNode)getKids.nextElement();
            expandAll(thisKid, tp.pathByAddingChild(thisKid));
        }
    }

    public void collapseAll(JreepadNode thisNode, TreePath tp)
    {
        Enumeration getKids = thisNode.children();
        JreepadNode thisKid;
        while (getKids.hasMoreElements())
        {
            thisKid = (JreepadNode)getKids.nextElement();
            collapseAll(thisKid, tp.pathByAddingChild(thisKid));
        }
        // It's at this point that we collapse the current element
        collapsePath(tp);
    }

    public TreePath[] getAllExpandedPaths()
    {
        JreepadNode root = (JreepadNode)treeModel.getRoot();
        if (root.getChildCount() == 0)
            return new TreePath[] { new TreePath(root) };

        Enumeration getPaths = getExpandedDescendants(new TreePath(root));
        TreePath thisKid;
        Vector allPaths = new Vector();
        while (getPaths.hasMoreElements())
        {
            thisKid = (TreePath)getPaths.nextElement();
            allPaths.add(thisKid);
        }
        TreePath[] ret = new TreePath[allPaths.size()];
        for (int i = 0; i < ret.length; i++)
            ret[i] = (TreePath)allPaths.get(i);
        return ret;
    }

    // THIS FUNCTION SEEMS TO HAVE NO EFFECT, ON MY MACHINE AT LEAST! WHAT'S GOING ON?
    public void expandPaths(TreePath[] paths)
    {
        for (int i = 0; i < paths.length; i++)
        {
            expandPath(paths[i]);
        }
    }

    /**
     * Mouse control for the Tree View.
     */
    private class TreeViewMouseListener
        extends MouseAdapter
    {
        private JreepadNode currentDragDropNode;

        public void mousePressed(MouseEvent e)
        {
            TreePath selPath = getPathForLocation(e.getX(), e.getY());
            if (selPath != null)
            {
                currentDragDropNode = (JreepadNode)selPath.getLastPathComponent();
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }
        }

        public void mouseReleased(MouseEvent e)
        {
            TreePath selPath = getPathForLocation(e.getX(), e.getY());
            // System.out.println("Mouse released: path = " + selPath);
            if (selPath != null)
            {
                if (currentDragDropNode != null
                    && currentDragDropNode.getParentNode() != null
                    && currentDragDropNode.getParentNode() != (JreepadNode)selPath
                        .getLastPathComponent()
                    && currentDragDropNode != (JreepadNode)selPath.getLastPathComponent())
                {
                    // Then we need to perform a drag-and-drop operation!
                    moveNode(currentDragDropNode, (JreepadNode)selPath.getLastPathComponent());

                    // Ensure that the destination node is open
                    setSelectionPath(selPath.pathByAddingChild(currentDragDropNode));
                }
            }
            setCursor(Cursor.getDefaultCursor());
            currentDragDropNode = null;
        }

        public void mouseClicked(MouseEvent e)
        {
            TreePath selPath = getPathForLocation(e.getX(), e.getY());
            if (selPath != null)
            {
                if (e.isPopupTrigger())
                {
                    // Now we can implement the pop-up content menu
                    System.out.println("Context menu would be launched here!");
                }
            }
        }
    }
}
