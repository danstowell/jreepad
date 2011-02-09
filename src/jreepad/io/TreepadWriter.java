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
import java.io.Writer;
import java.util.Enumeration;

import jreepad.JreepadNode;
import jreepad.JreepadTreeModel;

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

    public void write(OutputStream out, JreepadTreeModel document)
        throws IOException
    {
        Writer writer = new OutputStreamWriter(out, encoding);
        writer.write("<Treepad version 2.7>\n");
        writeNode(writer, document.getRootNode(), 0);
        writer.flush();
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
