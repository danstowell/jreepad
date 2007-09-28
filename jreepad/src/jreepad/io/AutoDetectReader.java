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

import jreepad.JreepadTreeModel;
import jreepad.ui.PasswordDialog;

/**
 * Reads a Jreepad file automatically detecting file type (XML or HJT).
 *
 * @version $Id$
 */
public class AutoDetectReader
    implements JreepadReader
{
    XmlReader xmlReader;

    TreepadReader treepadReader;

    EncryptedReader encryptedReader;

    public AutoDetectReader(String encoding, boolean autoDetectHtmlArticles)
    {
        xmlReader = new XmlReader();
        treepadReader = new TreepadReader(encoding, autoDetectHtmlArticles);
        encryptedReader = new EncryptedReader(this); // Use this reader as the underlying reader
    }

    public JreepadTreeModel read(InputStream in)
        throws IOException
    {
        in = new RewindableInputStream(in);

        // Read first line
        String currentLine = ((RewindableInputStream)in).readLine();
        in.reset(); // reset stream, so the specific readers read from the beginning

        if (currentLine.startsWith("<?xml"))
        {
            return xmlReader.read(in);
        }
        else if ((currentLine.toLowerCase().startsWith("<treepad") && currentLine.endsWith(">")))
        {
            treepadReader.setFileFormat(1);
            return treepadReader.read(in);
        }
        else if ((currentLine.toLowerCase().startsWith("<hj-treepad") && currentLine.endsWith(">")))
        {
            treepadReader.setFileFormat(1);
            return treepadReader.read(in);
        }
        else if (currentLine.startsWith(EncryptedWriter.HEADER))
        {
            String password = PasswordDialog.showPasswordDialog("This file is encrypted. Please enter password:");
            if (password == null)
                throw new IOException("Could not decrypt. No password entered.");
            encryptedReader.setPassword(password);
            return encryptedReader.read(in);
        }
        else
        {
            System.out.println("First line of file does not indicate a recognised format:\n"
                + currentLine + "\n");
            throw new IOException("First line of file does not indicate a recognised format:\n\n"
                + currentLine);
        }
    }

    public boolean isAutoDetectHtmlArticles()
    {
        return treepadReader.isAutoDetectHtmlArticles();
    }

    public void setAutoDetectHtmlArticles(boolean autoDetectHtmlArticles)
    {
        treepadReader.setAutoDetectHtmlArticles(autoDetectHtmlArticles);
    }

    public String getEncoding()
    {
        return treepadReader.getEncoding();
    }

    public void setEncoding(String encoding)
    {
        treepadReader.setEncoding(encoding);
    }

    /**
     * This class wraps the byte inputstreams we're presented with. We need it because
     * java.io.InputStreams don't provide functionality to reread processed bytes, and they have a
     * habit of reading more than one character when you call their read() methods. This means that,
     * once we discover the true (declared) encoding of a document, we can neither backtrack to read
     * the whole doc again nor start reading where we are with a new reader. This class allows
     * rewinding an inputStream by allowing a mark to be set, and the stream reset to that position.
     * <strong>The class assumes that it needs to read one character per invocation when it's read()
     * method is inovked, but uses the underlying InputStream's read(char[], offset length)
     * method--it won't buffer data read this way!</strong>
     *
     * @xerces.internal
     * @author Neil Graham, IBM
     * @author Glenn Marcy, IBM
     */
    protected static class RewindableInputStream
        extends InputStream
    {
        private static int BUFFER_SIZE = 2048;

        private InputStream fInputStream;

        private byte[] fData;

        private int fStartOffset;

        private int fEndOffset;

        private int fOffset;

        private int fLength;

        private int fMark;

        public RewindableInputStream(InputStream is)
        {
            fData = new byte[BUFFER_SIZE];
            fInputStream = is;
            fStartOffset = 0;
            fEndOffset = -1;
            fOffset = 0;
            fLength = 0;
            fMark = 0;
        }

        public void setStartOffset(int offset)
        {
            fStartOffset = offset;
        }

        public void rewind()
        {
            fOffset = fStartOffset;
            System.out.println("Rewinding " + fOffset + "/" + fLength + " -> " + fStartOffset
                + "(end=" + fEndOffset + ")");
        }

        public int read()
            throws IOException
        {
            int b = 0;
            if (fOffset < fLength)
            {
                return fData[fOffset++] & 0xff;
            }
            if (fOffset == fEndOffset)
            {
                return -1;
            }
            if (fOffset == fData.length)
            {
                byte[] newData = new byte[fOffset << 1];
                System.arraycopy(fData, 0, newData, 0, fOffset);
                fData = newData;
            }
            b = fInputStream.read();
            if (b == -1)
            {
                fEndOffset = fOffset;
                return -1;
            }
            fData[fLength++] = (byte)b;
            fOffset++;
            return b & 0xff;
        }

        public int read(byte[] b, int off, int len)
            throws IOException
        {
            int bytesLeft = fLength - fOffset;
            if (bytesLeft == 0)
            {
                if (fOffset == fEndOffset)
                {
                    return -1;
                }
                return fInputStream.read(b, off, len);
            }
            if (len < bytesLeft)
            {
                if (len <= 0)
                {
                    return 0;
                }
            }
            else
            {
                len = bytesLeft;
            }
            if (b != null)
            {
                System.arraycopy(fData, fOffset, b, off, len);
            }
            fOffset += len;
            return len;
        }

        public long skip(long n)
            throws IOException
        {
            int bytesLeft;
            if (n <= 0)
            {
                return 0;
            }
            bytesLeft = fLength - fOffset;
            if (bytesLeft == 0)
            {
                if (fOffset == fEndOffset)
                {
                    return 0;
                }
                return fInputStream.skip(n);
            }
            if (n <= bytesLeft)
            {
                fOffset += n;
                return n;
            }
            fOffset += bytesLeft;
            if (fOffset == fEndOffset)
            {
                return bytesLeft;
            }
            n -= bytesLeft;
            /*
             * In a manner of speaking, when this class isn't permitting more than one byte at a
             * time to be read, it is "blocking". The available() method should indicate how much
             * can be read without blocking, so while we're in this mode, it should only indicate
             * that bytes in its buffer are available; otherwise, the result of available() on the
             * underlying InputStream is appropriate.
             */
            return fInputStream.skip(n) + bytesLeft;
        }

        public int available()
            throws IOException
        {
            int bytesLeft = fLength - fOffset;
            if (bytesLeft == 0)
            {
                if (fOffset == fEndOffset)
                {
                    return -1;
                }
                return fInputStream.available();
            }
            return bytesLeft;
        }

        public void mark(int howMuch)
        {
            fMark = fOffset;
        }

        public void reset()
        {
            fOffset = fMark;
        }

        public boolean markSupported()
        {
            return true;
        }

        public void close()
            throws IOException
        {
            if (fInputStream != null)
            {
                fInputStream.close();
                fInputStream = null;
            }
        }

        public String readLine()
            throws IOException
        {
            byte[] bytes = new byte[BUFFER_SIZE];
            int len = 0;
            while (len < BUFFER_SIZE)
            {
                int ret = read();
                if (ret == -1 || ret == 0x0a || ret == 0x0d)
                    break;
                bytes[len] = (byte)(ret & 0xff);
                len++;
            }
            return new String(bytes, 0, len);
        }

    } // end of RewindableInputStream class
}
