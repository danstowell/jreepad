package jreepad;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

/**
 * The tree model.
 *
 * @version $Id: JreepadTreeModel.java,v 1.2 2007-01-15 14:44:45 pewu Exp $
 */
public class JreepadTreeModel extends DefaultTreeModel
{
    /**
     * Creates the model.
     *
     * @param root  root node of the tree
     */
    public JreepadTreeModel(JreepadNode root)
    {
        super(root);
    }

    /**
     * Sets the title of the given node to the given value.
     */
    public void valueForPathChanged(TreePath path, Object newValue)
    {
        JreepadNode node = (JreepadNode)path.getLastPathComponent();
        node.setTitle((String)newValue);
        nodeChanged(node);
    }
}
