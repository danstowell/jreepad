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
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Enumeration;

import jreepad.JreepadArticle;
import jreepad.JreepadNode;
import jreepad.JreepadTreeModel;

/**
 * Writes the Jreepad tree as HTML.
 *
 * @version $Id$
 */
public class HtmlWriter
    implements JreepadWriter
{
    private String encoding;
    private int exportMode;
    private boolean urlsToLinks;
    private int anchorType;
    private boolean causeToPrint;

    /**
     * Constructs the writer.
     */
    public HtmlWriter(String encoding, int exportMode, boolean urlsToLinks, int anchorType)
    {
        this(encoding, exportMode, urlsToLinks, anchorType, false);
    }

    /**
     * Constructs the writer.
     */
    public HtmlWriter(String encoding, int exportMode, boolean urlsToLinks, int anchorType, boolean causeToPrint)
    {
        this.encoding = encoding;
        this.exportMode = exportMode;
        this.urlsToLinks = urlsToLinks;
        this.anchorType = anchorType;
        this.causeToPrint = causeToPrint;
    }

    /**
     * Writes the tree to the output stream starting from selected node.
     * @param out  output stream
     * @param document  document to export
     */
    public void write(OutputStream out, JreepadTreeModel document)
        throws IOException
    {
        Writer writer = new OutputStreamWriter(out, encoding);
        write(writer, document.getRootNode());
        out.flush();
        out.close();
    }

    /**
     * Writes the tree to a string starting from selected node.
     * @param node  root node
     */
    public String write(JreepadNode node)
    {
        Writer writer = new StringWriter();
        try
        {
            write(writer, node);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return "";
        }
        return writer.toString();
    }

    /**
     * Writes the tree to the given writer starting from selected node.
     * @param writer  output writer
     * @param node  root node
     */
    public void write(Writer writer, JreepadNode node) throws IOException
    {
        writer.write("<?xml version=\"1.0\" encoding=\"" + encoding + "\"?>\n<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\">\n<head>\n<title>");
        writer.write(htmlSpecialChars(node.getTitle()));
        writer.write("</title>\n<style type=\"text/css\">\n"
                + "dl {}\ndl dt { font-weight: bold; margin-top: 10px; font-size: 24pt; }\ndl dd {margin-left: 20px; padding-left: 0px;}\ndl dd dl dt {background: black; color: white; font-size: 12pt; }\ndl dd dl dd dl dt {background: white; color: black; }"
                + "\n</style>\n</head>\n\n<body" + (causeToPrint ? " onload='print();'" : "")
                + ">\n<!-- Exported from Jreepad -->\n<dl>");
        writeNode(writer, node);
        writer.write("\n</dl>\n</body>\n</html>");
    }

    /**
     * Writes the node and its children to the writer.
     * @param writer  output writer
     * @param node  root node
     */
    public void writeNode(Writer writer, JreepadNode node) throws IOException
    {
        writer.write("\n<dt><a name=\"");
        if (anchorType == JreepadArticle.EXPORT_HTML_ANCHORS_WIKI)
            writer.write(node.getTitle());
        else
            writer.write(node.getWikiAnchor());
        writer.write("\"></a>");
        writer.write(htmlSpecialChars(node.getTitle()));
        writer.write("</dt>\n<dd>");

        // Write out the node's article content - using normal, preformatted, or HTML modes as
        // appropriate
        writer.write(node.getArticle().toHtml(exportMode, urlsToLinks, anchorType));

        if (node.getChildCount() > 0)
            writer.write("\n<dl>");
        Enumeration kids = node.children();
        while (kids.hasMoreElements())
            writeNode(writer, (JreepadNode)kids.nextElement());
        if (node.getChildCount() > 0)
            writer.write("\n</dl>");
        writer.write("</dd>");
    }

    /**
     * Replaces special characters to HTML entities.
     */
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

    public int getAnchorType()
    {
        return anchorType;
    }

    public void setAnchorType(int anchorType)
    {
        this.anchorType = anchorType;
    }

    public boolean isCauseToPrint()
    {
        return causeToPrint;
    }

    public void setCauseToPrint(boolean causeToPrint)
    {
        this.causeToPrint = causeToPrint;
    }

    public String getEncoding()
    {
        return encoding;
    }

    public void setEncoding(String encoding)
    {
        this.encoding = encoding;
    }

    public int getExportMode()
    {
        return exportMode;
    }

    public void setExportMode(int exportMode)
    {
        this.exportMode = exportMode;
    }

    public boolean isUrlsToLinks()
    {
        return urlsToLinks;
    }

    public void setUrlsToLinks(boolean urlsToLinks)
    {
        this.urlsToLinks = urlsToLinks;
    }

}
