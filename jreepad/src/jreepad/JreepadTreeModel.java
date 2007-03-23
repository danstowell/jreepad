package jreepad;

import java.io.File;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

/**
 * The tree model.
 *
 * @version $Id: JreepadTreeModel.java,v 1.4 2007-03-23 09:37:36 pewu Exp $
 */
public class JreepadTreeModel extends DefaultTreeModel
{
    /**
     * The location, where the document was last saved.
     */
    private File saveLocation = null;

    /**
     * True if the current document content has been saved.
     */
    private boolean contentSaved = false;

    public JreepadTreeModel()
    {
        this(new JreepadNode());
    }

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
        JreepadNode node = (JreepadNode) path.getLastPathComponent();
        node.setTitle((String) newValue);
        nodeChanged(node);
    }

    /**
     * Returns the root tree node.
     */
    public JreepadNode getRootNode()
    {
        return (JreepadNode) getRoot();
    }

    /**
     * Returns true if the current document content has been saved.
     */
    public boolean isContentSaved()
    {
        return contentSaved;
    }

    /**
     * Sets the information whether the current document content has been saved
     * or not.
     *
     * @param contentSaved
     */
    public void setContentSaved(boolean contentSaved)
    {
        this.contentSaved = contentSaved;
    }

    public void setSaveLocation(File saveLocation)
    {
        this.saveLocation = saveLocation;
        setContentSaved(true);
    }

    public File getSaveLocation()
    {
        return saveLocation;
    }
}
