package jreepad.io;

import java.io.IOException;
import java.io.Reader;

import jreepad.JreepadNode;

/**
 * Interface for classes that read Jreepad trees.
 *
 * @author <a href="mailto:pewu@losthive.org">Przemek Więch</a>
 * @version $Id$
 */
public interface JreepadReader
{
    public JreepadNode read(Reader in) throws IOException;
}
