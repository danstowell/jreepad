package jreepad.ui;

import java.io.File;
import java.util.ResourceBundle;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import jreepad.JreepadPrefs;

public class SaveFileChooser extends JFileChooser
{
    protected static final ResourceBundle LANG = ResourceBundle.getBundle("jreepad.lang.JreepadStrings");

    /**
     * File filter for Jreepad XML .jree files.
     */
    public static final FileFilter JREEPAD_FILE_FILTER = new ExtensionFileFilter("Jreepad XML file (*.jree)", "jree");

    /**
     * File filter for Treepad .hjt files.
     */
    public static final FileFilter TREEPAD_FILE_FILTER = new ExtensionFileFilter("Treepad file (*.hjt)", "hjt");

    public SaveFileChooser(int defaultFileFormat)
    {
        addChoosableFileFilter(JREEPAD_FILE_FILTER);
        addChoosableFileFilter(TREEPAD_FILE_FILTER);
        if (defaultFileFormat == JreepadPrefs.FILETYPE_XML)
            setFileFilter(JREEPAD_FILE_FILTER);
        else
            setFileFilter(TREEPAD_FILE_FILTER);
    }

    public void approveSelection()
    {
        if (!checkOverwrite(getSelectedFile()))
            return; // User doesn't want to overwrite the file
        if (getSelectedFile().isFile() && !getSelectedFile().canWrite())
        {
            JOptionPane.showMessageDialog(this, LANG.getString("MSG_FILE_NOT_WRITEABLE"),
                    LANG.getString("TITLE_FILE_ERROR"), JOptionPane.ERROR_MESSAGE);
            return; // Can't write selected file
        }
        super.approveSelection();
    }

    private boolean checkOverwrite(File theFile)
    {
        // If file doesn't already exist then fine
        if (theFile == null || !theFile.exists())
            return true;

        // Else we need to confirm
        return (JOptionPane.showConfirmDialog(this, LANG.getString("PROMPT_CONFIRM_OVERWRITE1")
                + " " + theFile.getName() + " "
                + LANG.getString("PROMPT_CONFIRM_OVERWRITE2"),
                LANG.getString("TITLE_CONFIRM_OVERWRITE"), JOptionPane.YES_NO_OPTION)
                == JOptionPane.YES_OPTION);
    }
}
