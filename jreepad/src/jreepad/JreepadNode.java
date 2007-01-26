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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Enumeration;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

/**
 * The tree node. Contains an article or is a soft link to another node.
 *
 * @version $Id: JreepadNode.java,v 1.25 2007-01-26 21:47:55 pewu Exp $
 */
public class JreepadNode
    extends DefaultMutableTreeNode
    implements Comparable
{
    private JreepadNode softLinkTarget = null;

    public JreepadNode()
    {
        this(new JreepadArticle());
    }

    public JreepadNode(String content)
    {
        this(new JreepadArticle(content));
    }

    public JreepadNode(String title, String content)
    {
        this(new JreepadArticle(title, content));
    }

    public JreepadNode(JreepadArticle article)
    {
        setUserObject(article);
    }

    public JreepadArticle getArticle()
    {
        if (isSoftLink())
            return softLinkTarget.getArticle();
        return (JreepadArticle)getUserObject();
    }

    public JreepadNode(JreepadNode softLinkTarget)
    {
        this.softLinkTarget = softLinkTarget;
    }

    public String toString()
    {
        return getTitle();
    }

    public boolean isSoftLink()
    {
        return softLinkTarget != null;
    }

    public String toFullString()
    {
        String ret = "JreepadNode \"" + getTitle() + "\": " + getChildCount()
            + " direct child nodes in subtree";
        ret += "\nDirect children:";
        for (int i = 0; i < children.size(); i++)
            ret += "\n    " + ((JreepadNode)getChildAt(i)).getTitle();
        return ret;
    }

    public String getWikiAnchor()
    {
        if (getParent() == null)
            return htmlSpecialChars(getTitle());
        return getParentNode().getWikiAnchor() + "/" + htmlSpecialChars(getTitle());
    }

    private static String htmlSpecialChars(String in)
    {
        char[] c = in.toCharArray();
        StringBuffer ret = new StringBuffer();
        for (int i = 0; i < c.length; i++)
            if (c[i] == '<')
                ret.append("&lt;");
            else if (c[i] == '>')
                ret.append("&gt;");
            else if (c[i] == '&')
                ret.append("&amp;");
            else if (c[i] == '\n')
                ret.append(" <br />\n");
            else if (c[i] == '"')
                ret.append("&quot;");
            else
                ret.append(c[i]);
        return ret.toString();
    }

    public String exportTitlesAsList()
    {
        return exportTitlesAsList(0).toString();
    }

    private StringBuffer exportTitlesAsList(int currentDepth)
    {
        StringBuffer ret = new StringBuffer();
        for (int i = 0; i < currentDepth; i++)
            ret.append(" ");
        ret.append(getTitle() + "\n");
        for (int i = 0; i < children.size(); i++)
            ret.append(((JreepadNode)getChildAt(i)).exportTitlesAsList(currentDepth + 1));

        return ret;
    }

    public String exportArticlesToText(boolean titlesToo)
    {
        // System.out.println("Expooort beginning");
        return exportArticlesToTextRecursive(titlesToo).toString();
    }

    public StringBuffer exportArticlesToTextRecursive(boolean titlesToo)
    {
        // System.out.println("Expooort " + getTitle());
        StringBuffer ret = new StringBuffer();
        if (titlesToo)
            ret.append(getTitle() + "\n\n");
        ret.append(getArticle().getContent() + "\n\n");
        for (int i = 0; i < children.size(); i++)
            ret.append(((JreepadNode)getChildAt(i)).exportArticlesToTextRecursive(titlesToo));

        return ret;
    }

    public void add(JreepadNode child)
    {
        if (isSoftLink())
            softLinkTarget.add(child);
        else
            super.add(child);
    }

    public JreepadNode removeChild(int child) // Can be used to delete, OR to 'get' one for moving
    {
        if (isSoftLink())
            return softLinkTarget.removeChild(child);

        JreepadNode ret = (JreepadNode)getChildAt(child);
        remove(child);
        return ret;
    }

    public TreeNode getChildAt(int child)
    {
        if (isSoftLink())
            return softLinkTarget.getChildAt(child);
        return super.getChildAt(child);
    }

    public int getChildCount()
    {
        if (isSoftLink())
            return softLinkTarget.getChildCount();
        return super.getChildCount();
    }

    public boolean indent()
    {
        // Get position in parent. If zero or -1 then return.
        int pos = getIndex();
        if (pos < 1)
            return false;
        // Get sibling node just above, and move self to there.
        MutableTreeNode oldParent = (MutableTreeNode)getParent();
        DefaultMutableTreeNode newParent = (DefaultMutableTreeNode)oldParent.getChildAt(pos - 1);
        removeFromParent();
        newParent.add(this);
        return true;
    }

    public boolean outdent()
    {
        // Get parent's parent. If null then return.
        JreepadNode p = (JreepadNode)getParent();
        if (p == null)
            return false;
        JreepadNode pp = (JreepadNode)p.getParent();
        if (pp == null)
            return false;
        // Get parent's position in its parent. = ppos
        int ppos = p.getIndex();
        // Move self to parent's parent, at (ppos+1)
        p.removeChild(getIndex());
        pp.insert(this, ppos + 1);

        // Also (as in the original treepad) move all the later siblings so they're children of this
        // node

        // NOT DONE YET

        return true;
    }

    public void moveChildUp(int child)
    {
        if (isSoftLink())
        {
            softLinkTarget.moveChildUp(child);
            return;
        }
        if (child < 1 || child >= getChildCount())
            return;

        insert(removeChild(child), child - 1);
    }

    public void moveChildDown(int child)
    {
        if (isSoftLink())
        {
            softLinkTarget.moveChildDown(child);
            return;
        }
        if (child < 0 || child >= getChildCount() - 1)
            return;

        insert(removeChild(child), child + 1);
    }

    public void moveUp()
    {
        MutableTreeNode parent = (MutableTreeNode)getParent();
        if (parent == null)
            return;
        int index = getIndex();
        if (index < 1)
            return;

        removeFromParent();
        parent.insert(this, index - 1);
    }

    public void moveDown()
    {
        MutableTreeNode parent = (MutableTreeNode)getParent();
        if (parent == null)
            return;
        int index = getIndex();
        if (index < 0 || index >= parent.getChildCount() - 1)
            return;

        removeFromParent();
        parent.insert(this, index + 1);
    }

    public JreepadNode addChild()
    {
        if (isSoftLink())
            return softLinkTarget.addChild();

        JreepadNode theChild = new JreepadNode();
        add(theChild);
        return theChild;
    }

    public JreepadNode addChild(int index)
    {
        if (isSoftLink())
            return softLinkTarget.addChild(index);

        JreepadNode theChild = new JreepadNode();
        insert(theChild, index);
        return theChild;
    }

    public int getIndex(TreeNode child)
    {
        if (isSoftLink())
            return softLinkTarget.getIndex(child);
        return super.getIndex(child);
    }

    public int getIndex()
    {
        if (getParent() == null)
            return -1;
        return getParent().getIndex(this);
    }

    public boolean isNodeInSubtree(JreepadNode n)
    {
        if (isSoftLink())
            return softLinkTarget.isNodeInSubtree(n);
        return isNodeDescendant(n);
    }

    public void sortChildren()
    {
        if (isSoftLink())
            softLinkTarget.sortChildren();
        else
            sort();
    }

    public void sortChildrenRecursive()
    {
        if (isSoftLink())
        {
            softLinkTarget.sortChildrenRecursive();
            return;
        }
        sort();
        for (int i = 0; i < getChildCount(); i++)
            ((JreepadNode)getChildAt(i)).sortChildrenRecursive();
    }

    // Function for using Java's built-in mergesort
    private void sort()
    {
        Object[] childrenArray = children.toArray();
        java.util.Arrays.sort(childrenArray);
        removeAllChildren();
        for (int i = 0; i < childrenArray.length; i++)
            add((JreepadNode)childrenArray[i]);
    }

    // The following function is a halfway-house on the way to "natural numerical ordering"
    public int compareTo(Object o)
    {
        String a = getTitle();
        String b = ((JreepadNode)o).getTitle();
        if (a.length() != 0 && b.length() != 0 && Character.isDigit(a.charAt(0))
            && Character.isDigit(b.charAt(0)))
        {
            // Both strings begin with digits - so implement natural numerical ordering here
            StringBuffer aBuf = new StringBuffer("");
            StringBuffer bBuf = new StringBuffer("");
            int i;
            for (i = 0; i < a.length(); i++)
                if (Character.isDigit(a.charAt(i)))
                    aBuf.append(a.charAt(i));
                else
                    break;
            for (i = 0; i < b.length(); i++)
                if (Character.isDigit(b.charAt(i)))
                    bBuf.append(b.charAt(i));
                else
                    break;
            return (new Integer(aBuf.toString())).compareTo(new Integer(bBuf.toString()));
        }
        return a.compareToIgnoreCase(b);
    }

    public boolean isLeaf()
    {
        return (getChildCount() == 0);
    }

    public Enumeration children()
    {
        if (isSoftLink())
            return softLinkTarget.children();
        return super.children();
    }

    public JreepadNode getParentNode()
    {
        return (JreepadNode)getParent();
    }

    // MutableTreeNode functions
    public void remove(int child)
    {
        if (isSoftLink())
            softLinkTarget.remove(child);
        else
            super.remove(child);
    }

    public void remove(MutableTreeNode node)
    {
        if (isSoftLink())
            softLinkTarget.remove(node);
        else
            super.remove(node);
    }

    public void insert(MutableTreeNode child, int index)
    {
        if (isSoftLink())
            softLinkTarget.insert(child, index);
        else
            super.insert(child, index);
    }

    public void addChildFromTextFile(InputStreamReader textFile, String nodeName)
        throws IOException
    {
        if (isSoftLink())
        {
            softLinkTarget.addChildFromTextFile(textFile, nodeName);
            return;
        }
        // Load the content as a string
        StringBuffer contentString = new StringBuffer();
        String currentLine;
        BufferedReader bReader = new BufferedReader(textFile);
        while ((currentLine = bReader.readLine()) != null)
            contentString.append(currentLine + "\n");
        // Then just create the node
        add(new JreepadNode(new JreepadArticle(nodeName, contentString.toString())));
    }

    // This getCopy() function is intended to return a copy of the entire subtree, used for Undo
    public JreepadNode getCopy()
    {
        JreepadNode ret = new JreepadNode(getArticle());
        for (int i = 0; i < getChildCount(); i++)
        {
            ret.add(((JreepadNode)getChildAt(i)).getCopy());
        }
        return ret;
    }

    public JreepadNode getChildByTitle(String title)
    {
        if (isSoftLink())
            return softLinkTarget.getChildByTitle(title);

        for (int i = 0; i < getChildCount(); i++)
            if (((JreepadNode)getChildAt(i)).getTitle().equals(title))
                return (JreepadNode)getChildAt(i);
        return null;
    }

    public JreepadNode getSoftLinkTarget()
    {
        return softLinkTarget;
    }

    public String getTitle()
    {
        return getArticle().getTitle();
    }

    public void setTitle(String title)
    {
        getArticle().setTitle(title);
    }

    public String getContent()
    {
        return getArticle().getContent();
    }

    /*
     // Listens for edits that can be undone.
     protected class JreepadNodeUndoableEditListener
     implements UndoableEditListener {
     public void undoableEditHappened(UndoableEditEvent e) {

     //System.out.println("Undoable event is " + (e.getEdit().isSignificant()?"":"NOT ") + "significant");
     //System.out.println("Undoable event source: " + e.getEdit());

     //Remember the edit and update the menus.
     undoMgr.addEdit(e.getEdit());
     //undoAction.updateUndoState();
     //redoAction.updateRedoState();
     }
     }
     */
}
