package jreepad.io;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Enumeration;

import jreepad.JreepadNode;

/**
 * Writes the Jreepad tree as a Treepad file.
 *
 * @version $Id$
 */
public class TreepadWriter
    implements JreepadWriter
{

    private String encoding;

    public TreepadWriter(String encoding)
    {
        this.encoding = encoding;
    }

    public void write(OutputStream out, JreepadNode node)
        throws IOException
    {
        Writer writer = new OutputStreamWriter(out, encoding);
        writer.write("<Treepad version 2.7>\n");
        writeNode(writer, node, 0);
        writer.close();
    }

    private void writeNode(Writer writer, JreepadNode node, int depth)
        throws IOException
    {
        writer.write("dt=Text\n<node>\n");
        writer.write(node.getTitle());
        writer.write("\n");
        writer.write(depth + "\n");
        writer.write(node.getContent());
        writer.write("\n");
        writer.write("<end node> 5P9i0s8y19Z\n");

        Enumeration kids = node.children();
        while (kids.hasMoreElements())
            writeNode(writer, (JreepadNode)kids.nextElement(), depth + 1);
    }
}
