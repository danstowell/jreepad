package jreepad;

import java.io.File;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

/**
 * The tree model.
 *
 * @version $Id: JreepadTreeModel.java,v 1.6 2007-04-03 12:41:30 pewu Exp $
 */
public class JreepadTreeModel extends DefaultTreeModel
{
    /**
     * The location, where the document was last saved.
     */
    private File saveLocation = null;

    /**
     * Format of the loaded file.
     */
    private int fileType = -1;

    /**
     * Encoding of the loaded file.
     */
    private String encoding = null;

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

    public void setFileType(int fileType)
    {
        this.fileType = fileType;
    }

    public File getSaveLocation()
    {
        return saveLocation;
    }

    public int getFileType()
    {
        return fileType;
    }

    public String getEncoding()
    {
        return encoding;
    }

    public void setEncoding(String encoding)
    {
        this.encoding = encoding;
    }
}
