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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import jreepad.JreepadArticle;
import jreepad.JreepadNode;
import jreepad.JreepadPrefs;
import jreepad.JreepadTreeModel;

/**
 * Reads XML input into Jreepad.
 *
 * @version $Id$
 */
public class XmlReader implements JreepadReader
{
    private String encoding;

    public XmlReader()
    {
        this(null);
    }

    public XmlReader(String encoding)
    {
        this.encoding = encoding;
    }

    public JreepadTreeModel read(InputStream in)
        throws IOException
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, encoding));

        String currentLine;
        String currentXmlContent = "";
        int nodeTagOffset = 0;

        // Spool until we're at the very first node
        while ((currentLine = reader.readLine()) != null
            && (nodeTagOffset = currentXmlContent.indexOf("<node")) == -1
            && (nodeTagOffset == -1 || currentXmlContent.indexOf('>', nodeTagOffset) == -1))
        {
            currentXmlContent += (currentLine + "\n");
        }
        if (currentLine != null)
            currentXmlContent += (currentLine + "\n");

        // System.out.println("XMLparse: I've spooled to the first node and content is now: " +
        // currentXmlContent);

        // So currentXmlContent now contains all of the opening tag, including its attributes etc
        // Strip off anything BEFORE the node opening
        currentXmlContent = currentXmlContent.substring(nodeTagOffset);

        // System.out.println("XMLparse: I've stripped anything before the first node and content is
        // now: " + currentXmlContent);

        JreepadTreeModel document = new JreepadTreeModel(readNode(reader, currentXmlContent, 0).node);
        document.setFileFormat(JreepadPrefs.FILETYPE_XML);
        document.setEncoding(encoding);
        return document;
    }

    // This function should return any XML string content that remains unprocessed
    // Also returns newly created node
    ReturnValue readNode(BufferedReader reader, String currentXmlContent, int depth)
        throws IOException
    {

        // System.out.println("XMLparse recursive: depth "+depth);

        // String currentXmlContent should BEGIN with the <node> tag. This is assumed, and if not
        // true may cause problems!
        String currentLine;
        int titleOffset, typeOffset, startTagOffset, endTagOffset;
        String title, typeString, content = "";
        JreepadNode node = new JreepadNode();

        // Extract the attributes
        titleOffset = currentXmlContent.indexOf("title=");
        typeOffset = currentXmlContent.indexOf("type=");
        if (titleOffset != -1)
            title = currentXmlContent.substring(titleOffset + 7, currentXmlContent.indexOf('"',
                titleOffset + 7));
        else
            title = "<Untitled node>";
        if (typeOffset != -1)
            typeString = currentXmlContent.substring(typeOffset + 6, currentXmlContent.indexOf('"',
                typeOffset + 6));
        else
            typeString = "text/plain";

        if (typeString.equals("text/csv"))
            node.getArticle().setArticleMode(JreepadArticle.ARTICLEMODE_CSV);
        else if (typeString.equals("text/html"))
            node.getArticle().setArticleMode(JreepadArticle.ARTICLEMODE_HTML);
        else if (typeString.equals("text/textile"))
            node.getArticle().setArticleMode(JreepadArticle.ARTICLEMODE_TEXTILEHTML);
        //else if (typeString.equals("application/x-jreepad-softlink"))
        //    node.setArticleMode(JreepadArticle.ARTICLEMODE_SOFTLINK);
        else
            node.getArticle().setArticleMode(JreepadArticle.ARTICLEMODE_ORDINARY);

        node.setTitle(xmlUnescapeChars(title));

        // OK, so we've interpreted the attributes etc. Now we need to trim the opening tag away
        currentXmlContent = currentXmlContent.substring(currentXmlContent.indexOf('>') + 1);

        // System.out.println("XMLparse: I've stripped off the <node> tag and content is now: " +
        // currentXmlContent);

        boolean readingContent = true; // Once the baby nodes come in, we're not interested in
        // adding any more to the content
        while ((currentLine = reader.readLine()) != null)
        {
            // System.out.println("XMLparserecursive: Here's a line: " + currentLine);
            currentLine += "\n"; // We want to keep the newlines, but the BufferedReader doesn't
            // give us them

            // We're reading CONTENT into the current node.
            currentXmlContent += currentLine;

            // System.out.println("\n\nThe content that I'm currently trying to process
            // is:\n"+currentXmlContent);

            // Look out for <node which tells us we're starting a child
            startTagOffset = currentXmlContent.indexOf("<node");
            // Look out for </node> which tells us we're finishing this node and returning to the
            // parent
            endTagOffset = currentXmlContent.indexOf("</node>");

            while (!(startTagOffset == -1 || endTagOffset == -1))
            {
                if (startTagOffset == -1 || endTagOffset < startTagOffset)
                {
                    // Process the nearest end tag
                    if (readingContent)
                        content += xmlUnescapeChars(currentXmlContent.substring(0, endTagOffset));
                    String returnFromBaby = currentXmlContent.substring(endTagOffset + 7);
                    // System.out.println("\n\nBaby intends to return:"+returnFromBaby);
                    node.getArticle().setContent(content);
                    return new ReturnValue(returnFromBaby, node);
                }
                else
                {
                    if (readingContent)
                    {
                        content += xmlUnescapeChars(currentXmlContent.substring(0, startTagOffset));
                        node.getArticle().setContent(content);
                    }

                    // Having found a child node, we want to STOP adding anything to the current
                    // node's content (e.g. newlines...)
                    readingContent = false;

                    // Process the nearest start tag
                    // System.out.println("\n\nJust before passing to baby: content
                    // is:\n"+currentXmlContent);
                    ReturnValue returnValue = readNode(reader, currentXmlContent
                        .substring(startTagOffset), depth + 1);
                    currentXmlContent = returnValue.xmlContent;
                    // System.out.println("\n\nJust after passing to baby: content
                    // is:\n"+currentXmlContent);
                    node.add(returnValue.node);
                }

                startTagOffset = currentXmlContent.indexOf("<node");
                endTagOffset = currentXmlContent.indexOf("</node>");
            }

        } // End while

        // Just make sure we haven't wasted any content...
        endTagOffset = currentXmlContent.indexOf('<');
        if (readingContent && (endTagOffset != -1))
            content += xmlUnescapeChars(currentXmlContent.substring(0, endTagOffset));
        node.getArticle().setContent(content);
        // System.out.println("THE MAIN WHILE LOOP HAS ENDED. SPARE CONTENT:\n" +
        // currentXmlContent);
        return new ReturnValue("", node);
    }

    private static String xmlUnescapeChars(String in)
    {
        char[] c = in.toCharArray();
        StringBuffer ret = new StringBuffer();
        StringBuffer entity = new StringBuffer();
        String ent;

        int i, j;
        OuterLoop: for (i = 0; i < c.length; i++)
            if (c[i] == '&')
            {
                entity = new StringBuffer();
                for (j = 0; j < 8; j++) // Add things into the entity buffer until we hit a
                // semicolon
                {
                    i++;
                    if (i == c.length)
                    {
                        ret.append('&' + entity.toString());
                        continue OuterLoop;
                    }
                    else if (c[i] != ';')
                        entity.append(c[i]);
                    else
                        break; // Reached end of the entity (or end of the whole string!)
                }
                ent = entity.toString();
                if (ent.equals("lt"))
                    ret.append("<");
                else if (ent.equals("gt"))
                    ret.append(">");
                else if (ent.equals("amp"))
                    ret.append("&");
                else if (ent.equals("quot"))
                    ret.append("\"");
                else
                    ret.append('&' + ent + ';');
            }
            else
                ret.append(c[i]);

        return ret.toString();
    }

    public String getEncoding()
    {
        return encoding;
    }

    public void setEncoding(String encoding)
    {
        this.encoding = encoding;
    }

    /**
     * Container class to make it possible to return two objects from readNode().
     */
    private static class ReturnValue
    {
        public String xmlContent;

        public JreepadNode node;

        public ReturnValue(String xmlContent, JreepadNode node)
        {
            this.xmlContent = xmlContent;
            this.node = node;
        }
    }
}
