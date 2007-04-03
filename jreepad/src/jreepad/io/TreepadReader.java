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

package jreepad.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.Stack;

import jreepad.JreepadArticle;
import jreepad.JreepadNode;
import jreepad.JreepadPrefs;
import jreepad.JreepadTreeModel;

/**
 * Reads a treepad file into Jreepad.
 *
 * @version $Id$
 */
public class TreepadReader implements JreepadReader
{

    private boolean autoDetectHtmlArticles;

    private String encoding;

    private int fileFormat;

    public TreepadReader(String encoding, boolean autoDetectHtmlArticles)
    {
        this.encoding = encoding;
        this.autoDetectHtmlArticles = autoDetectHtmlArticles;
    }

    public JreepadTreeModel read(InputStream in)
        throws IOException
    {
        LineNumberReader reader = new LineNumberReader(new InputStreamReader(in, encoding));
        reader.readLine(); // skip first line // TODO check for treepadness

        Stack nodeStack = new Stack();
        int depthMarker;
        JreepadNode newNode;
        JreepadNode rootNode = null;
        String dtLine, nodeLine, titleLine, depthLine;
        StringBuffer currentContent;
        String currentLine;
        dtLine = "dt=text";

        while ((fileFormat == 2 || (dtLine = reader.readLine()) != null)
            && (nodeLine = reader.readLine()) != null && (titleLine = reader.readLine()) != null
            && (depthLine = reader.readLine()) != null)
        {
            // Read "dt=text" [or error] - NB THE OLDER FORMAT DOESN'T INCLUDE THIS LINE SO WE SKIP
            // IT
            if (dtLine.equals("") && nodeLine.startsWith("<bmarks>"))
                throw new IOException(
                    "This is not a Treepad-Lite-compatible file!\n\nFiles created in more advanced versions of Treepad\ncontain features that are not available in Jreepad.");

            if (fileFormat != 2)
                if (!(dtLine.toLowerCase().startsWith("dt=text")))
                    throw new IOException("Unrecognised node dt format at line " + reader.getLineNumber() + ": "
                        + dtLine);
            // Read "<node>" [or error]
            if (!(nodeLine.toLowerCase().startsWith("<node>")))
                throw new IOException("Unrecognised node format at line " + (reader.getLineNumber() + 1) + ": "
                    + nodeLine);

            // Read THE CONTENT! [loop until we find "<end node> 5P9i0s8y19Z"]
            currentContent = new StringBuffer();
            while ((currentLine = reader.readLine()) != null
                && !currentLine.equals("<end node> 5P9i0s8y19Z"))
            {
                currentContent.append(currentLine + "\n");
            }

            // Now, having established the content and the title and the depth, we'll create the
            // child
            String content = currentContent.substring(0, Math.max(currentContent.length() - 1, 0));
            newNode = new JreepadNode(titleLine, content);
            // babyNode = new JreepadNode(titleLine, currentContent.substring(0,
            // Math.max(currentContent.length()-2,0)),
            // (JreepadNode)(nodeStack.peek()));

            // Turn it into a HTML-mode node if it matches "<html> ... </html>"
            String compareContent = newNode.getContent().toLowerCase().trim();
            int newArticleMode = (autoDetectHtmlArticles && compareContent.startsWith("<html>") && compareContent
                .endsWith("</html>")) ? JreepadArticle.ARTICLEMODE_HTML : JreepadArticle.ARTICLEMODE_ORDINARY;
            newNode.getArticle().setArticleMode(newArticleMode);

            if (depthLine.equals("0"))
            {
                rootNode = newNode;
            }
            else
            {
                depthMarker = Integer.parseInt(depthLine);
                while (nodeStack.size() > depthMarker)
                    nodeStack.pop();

                ((JreepadNode)(nodeStack.peek())).add(newNode);
            }
            nodeStack.push(newNode);
        }

        JreepadTreeModel document = new JreepadTreeModel(rootNode);
        document.setFileType(JreepadPrefs.FILETYPE_HJT);
        document.setEncoding(encoding);
        return document;
    }

    public boolean isAutoDetectHtmlArticles()
    {
        return autoDetectHtmlArticles;
    }

    public void setAutoDetectHtmlArticles(boolean autoDetectHtmlArticles)
    {
        this.autoDetectHtmlArticles = autoDetectHtmlArticles;
    }

    public String getEncoding()
    {
        return encoding;
    }

    public void setEncoding(String encoding)
    {
        this.encoding = encoding;
    }

    public int getFileFormat()
    {
        return fileFormat;
    }

    public void setFileFormat(int fileFormat)
    {
        this.fileFormat = fileFormat;
    }
}
