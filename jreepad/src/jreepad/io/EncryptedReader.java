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
import java.security.GeneralSecurityException;
import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.spec.SecretKeySpec;

import jreepad.JreepadPrefs;
import jreepad.JreepadTreeModel;

/**
 * Reads encrypted input into Jreepad. This reader is constructed with
 * any other reader as the actual file format which will used when decrypted.
 *
 * @version $Id: EncryptedReader.java,v 1.1 2007-09-28 14:29:22 pewu Exp $
 */
public class EncryptedReader implements JreepadReader
{
	private JreepadReader reader;

	private String password = "";

    public EncryptedReader(JreepadReader reader)
    {
        this.reader = reader;
    }

    public JreepadTreeModel read(InputStream in)
        throws IOException
    {
    	// Read header
    	while (in.read() != '\n');

    	Cipher cipher = null;
		try
		{
			cipher = Cipher.getInstance(EncryptedWriter.ALGORITHM);
			Key key = new SecretKeySpec(password.getBytes(), EncryptedWriter.ALGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, key);
		}
		catch (GeneralSecurityException e)
		{
			throw new IOException(e.toString());
		}
    	InputStream in2 = new CipherInputStream(in, cipher);

        JreepadTreeModel document;
        try
        {
            document = reader.read(in2);
        }
        catch (IOException e)
        {
            throw new IOException("Password incorrect or read problem occurred", e);
        }
        document.setFileType(JreepadPrefs.FILETYPE_XML_ENCRYPTED);
        document.setPassword(password);
        return document;
    }

	public void setPassword(String password)
	{
		this.password = password;
	}
}
