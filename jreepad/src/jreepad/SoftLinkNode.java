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

import java.util.Enumeration;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;


/**
 * Soft link tree node. Points to another node in the tree.
 *
 * @version $Id$
 */
public class SoftLinkNode extends JreepadNode
{
    private JreepadNode target;

    public SoftLinkNode(SoftLinkNode target)
    {
        super((JreepadArticle)null);
        this.target = target;
    }

    public void add(MutableTreeNode child)
    {
        target.add(child);
    }

    public JreepadNode removeChild(int child) // Can be used to delete, OR to 'get' one for moving
    {
        return target.removeChild(child);
    }

    public TreeNode getChildAt(int child)
    {
        return target.getChildAt(child);
    }

    public int getChildCount()
    {
        return target.getChildCount();
    }

    public JreepadNode addChild()
    {
        return target.addChild();
    }

    public JreepadNode addChild(int index)
    {
        return target.addChild(index);
    }

    public int getIndex(TreeNode child)
    {
        return target.getIndex(child);
    }

    public boolean isNodeDescendant(DefaultMutableTreeNode n)
    {
        return target.isNodeDescendant(n);
    }

    public void sortChildren()
    {
        target.sortChildren();
    }

    public void sortChildrenRecursive()
    {
        target.sortChildrenRecursive();
    }

    public Enumeration children()
    {
        return target.children();
    }

    // MutableTreeNode functions
    public void remove(int child)
    {
        target.remove(child);
    }

    public void remove(MutableTreeNode node)
    {
        target.remove(node);
    }

    public void insert(MutableTreeNode child, int index)
    {
        target.insert(child, index);
    }

    public JreepadNode getSoftLinkTarget()
    {
        return target;
    }
}
