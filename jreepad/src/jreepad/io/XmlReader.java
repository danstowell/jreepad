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

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import jreepad.JreepadArticle;
import jreepad.JreepadNode;
import jreepad.JreepadPrefs;
import jreepad.JreepadTreeModel;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Reads XML input into Jreepad.
 *
 * @version $Id$
 */
public class XmlReader implements JreepadReader
{
    public static final String NODE_TAG = "node";
    public static final String TITLE_ATTRIBUTE = "title";
    public static final String TYPE_ATTRIBUTE = "type";

    public JreepadTreeModel read(InputStream in)
        throws IOException
    {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        Handler handler = null;
        try
        {
            SAXParser parser = factory.newSAXParser();
            handler = new Handler();
            parser.parse(in, handler);
        }
        catch (ParserConfigurationException e)
        {
            throw new IOException(e);
        }
        catch (SAXException e)
        {
            throw new IOException(e);
        }

        JreepadTreeModel document = new JreepadTreeModel(handler.getRoot());
        document.setFileType(JreepadPrefs.FILETYPE_XML);
        return document;
    }

    private class Handler extends DefaultHandler
    {
        JreepadNode root = null;
        JreepadNode currentNode = null;
        StringBuffer content = null;
        int articleMode;

        public void characters(char[] ch, int start, int length) throws SAXException
        {
            if (content != null)
                content.append(ch, start, length);
        }

        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
        {
            if (!qName.equals(NODE_TAG))
                throw new SAXException("Unknown tag " + qName);

            // Save node content
            if (currentNode != null && content != null)
                currentNode.getArticle().setContent(content.toString());
            content = new StringBuffer();

            String title = attributes.getValue(TITLE_ATTRIBUTE);
            String type = attributes.getValue(TYPE_ATTRIBUTE);

            JreepadNode newNode = new JreepadNode(title, "");
            if (currentNode != null)
                currentNode.add(newNode);
            currentNode = newNode;
            if (root == null)
                root = currentNode;

            int articleMode = JreepadArticle.ARTICLEMODE_ORDINARY;
            if (type.equals("text/csv"))
                articleMode = JreepadArticle.ARTICLEMODE_CSV;
            else if (type.equals("text/html"))
                articleMode = JreepadArticle.ARTICLEMODE_HTML;
            else if (type.equals("text/textile"))
                articleMode = JreepadArticle.ARTICLEMODE_TEXTILEHTML;
            //else if (type.equals("application/x-jreepad-softlink"))
            //    articleMode = JreepadArticle.ARTICLEMODE_SOFTLINK;
            currentNode.getArticle().setArticleMode(articleMode);
        }

        public void endElement(String uri, String localName, String qName) throws SAXException
        {
            if (content != null)
                currentNode.getArticle().setContent(content.toString());
            currentNode = currentNode.getParentNode();
            content = null;
        }

        public JreepadNode getRoot()
        {
            return root;
        }
    }
}
