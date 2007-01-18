package jreepad.io;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Enumeration;

import jreepad.JreepadNode;

/**
 * Writes the Jreepad tree as XML.
 *
 * @version $Id$
 */
public class XmlWriter
    implements JreepadWriter
{
    private String encoding;

    public XmlWriter(String encoding)
    {
        this.encoding = encoding;
    }

    public void write(OutputStream out, JreepadNode node)
        throws IOException
    {
        Writer writer = new OutputStreamWriter(out, encoding);
        writer.write("<?xml version=\"1.0\" encoding=\"" + encoding + "\"?>\n");
        writeNode(writer, node, 0, true);
        out.close();
    }

    private void writeNode(Writer writer, JreepadNode node, int depth, boolean includeChildren) throws IOException
    {
        writer.write("<node ");
        if (depth == 0)
            writer.write("xmlns=\"http://jreepad.sourceforge.net/formats\" ");
        writer.write("title=\"" + xmlEscapeChars(node.getTitle()) + "\" type=\"");

        switch (node.getArticleMode())
        {
        case JreepadNode.ARTICLEMODE_HTML:
            writer.write("text/html");
            break;
        case JreepadNode.ARTICLEMODE_TEXTILEHTML:
            writer.write("text/textile");
            break;
        case JreepadNode.ARTICLEMODE_CSV:
            writer.write("text/csv");
            break;
        default:
            writer.write("text/plain");
            break;
        }
        writer.write("\">");
        writer.write(xmlEscapeChars(node.getContent()));
        if (includeChildren)
        {
            Enumeration kids = node.children();
            while (kids.hasMoreElements())
                writeNode(writer, (JreepadNode)kids.nextElement(), depth + 1,
                    includeChildren);
        }
        writer.write("</node>\n");
    }

    public String getEncoding()
    {
        return encoding;
    }

    public void setEncoding(String encoding)
    {
        this.encoding = encoding;
    }

    private static String xmlEscapeChars(String in)
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
            else if (c[i] == '"')
                ret.append("&quot;");
            else
                ret.append(c[i]);
        return ret.toString();
    }
}