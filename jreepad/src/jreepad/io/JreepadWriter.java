package jreepad.io;


import java.io.IOException;
import java.io.OutputStream;

import jreepad.JreepadNode;

/**
 * Interface for classes that export Jreepad trees.
 *
 * @author <a href="mailto:pewu@losthive.org">Przemek Więch</a>
 * @version $Id$
 */
public interface JreepadWriter
{
    public void write(OutputStream out, JreepadNode node) throws IOException;
}
