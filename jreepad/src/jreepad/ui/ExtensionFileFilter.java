package jreepad.ui;

import java.io.File;

import javax.swing.filechooser.FileFilter;

/**
 * File filter, which accepts only files with the given extension.
 */
public class ExtensionFileFilter extends FileFilter
{
    /**
     * Description of this filter.
     */
    private String description;

    /**
     * Allowed extension.
     */
    private String extension;

    /**
     * Lowercase version of the extension.
     */
    private String lowercaseExtension;

    public ExtensionFileFilter(String description, String extension)
    {
        this.description = description;
        this.extension = extension;
        this.lowercaseExtension = extension.toLowerCase();
    }

    /**
     * Tests whether the given file has the appropriate extension.
     */
    public boolean accept(File f)
    {
        if (f == null)
            return false;
        if (f.isDirectory())
            return true;
        String fileName = f.getName();
        int i = fileName.lastIndexOf('.');
        if (i <= 0 || i >= fileName.length() - 1)
            return false;
        String fileExtension = fileName.substring(i + 1).toLowerCase();
        if (fileExtension.equals(lowercaseExtension))
            return true;
        return false;
    }

    /**
     * Returns the description of this filter.
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * Returns the filtered file extension.
     */
    public String getExtension()
    {
        return extension;
    }
}