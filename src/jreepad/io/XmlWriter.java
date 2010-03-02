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
import java.util.Enumeration;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.OutputKeys;

import jreepad.JreepadArticle;
import jreepad.JreepadNode;
import jreepad.JreepadTreeModel;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Writes the Jreepad tree as XML.
 *
 * @version $Id$
 */
public class XmlWriter
    implements JreepadWriter
{
    public static final String NODE_TAG = "node";
    public static final String TITLE_ATTRIBUTE = "title";
    public static final String TYPE_ATTRIBUTE = "type";
    public static final String NSU = "";
    private AttributesImpl attributes = new AttributesImpl();

    public XmlWriter()
    {
        attributes.addAttribute("", "", TITLE_ATTRIBUTE, "", "");
        attributes.addAttribute("", "", TYPE_ATTRIBUTE, "", "");
    }

    public void write(OutputStream out, JreepadTreeModel document)
        throws IOException
    {
        StreamResult result = new StreamResult(out);
        SAXTransformerFactory factory = (SAXTransformerFactory)SAXTransformerFactory.newInstance();
        TransformerHandler handler;
        try
        {
             handler = factory.newTransformerHandler();
             handler.getTransformer().setOutputProperty(OutputKeys.INDENT,"yes");
        }
        catch (TransformerConfigurationException e)
        {
            throw new IOException(e.toString());
        }
        handler.setResult(result);

        try
        {
            write(handler, document);
        }
        catch (SAXException e)
        {
            throw new IOException(e.toString());
        }
    }

    private void write(ContentHandler handler, JreepadTreeModel document) throws SAXException
    {
        handler.startDocument();
        writeNode(handler, document.getRootNode());
        handler.endDocument();
    }

    private void writeNode(ContentHandler handler, JreepadNode node) throws SAXException
    {
        String type;
        switch (node.getArticle().getArticleMode())
        {
        case JreepadArticle.ARTICLEMODE_HTML:
            type = "text/html";
            break;
        case JreepadArticle.ARTICLEMODE_TEXTILEHTML:
            type = "text/textile";
            break;
        case JreepadArticle.ARTICLEMODE_CSV:
            type = "text/csv";
            break;
        default:
            type = "text/plain";
            break;
        }

        attributes.setValue(0, node.getTitle());
        attributes.setValue(1, type);

        handler.startElement(NSU, NODE_TAG, NODE_TAG, attributes);
        String content = node.getContent();
        handler.characters(content.toCharArray(), 0, content.length());

        Enumeration kids = node.children();
        while (kids.hasMoreElements())
            writeNode(handler, (JreepadNode)kids.nextElement());

        handler.endElement(NSU, NODE_TAG, NODE_TAG);
    }
}
