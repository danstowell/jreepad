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
import java.security.GeneralSecurityException;
import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.SecretKeySpec;

import jreepad.JreepadTreeModel;

/**
 * Writes an encrypted file. This writer is constructed with
 * any other writer as the actual file format which will be encrypted.
 *
 * @version $Id: EncryptedWriter.java,v 1.1 2007-09-28 14:29:22 pewu Exp $
 */
public class EncryptedWriter
    implements JreepadWriter
{
	static final String ALGORITHM = "Blowfish";

	static final String HEADER = "JreepadEncrypted";

	private JreepadWriter writer;

	private String password = "";

	public EncryptedWriter(JreepadWriter writer)
    {
        this.writer = writer;
    }

    public void write(OutputStream out, JreepadTreeModel document)
        throws IOException
    {
    	out.write(HEADER.getBytes());
        out.write("\n".getBytes());

    	Cipher cipher = null;
		try
		{
			cipher = Cipher.getInstance(ALGORITHM);
			Key key = new SecretKeySpec(password.getBytes(), ALGORITHM);
			cipher.init(Cipher.ENCRYPT_MODE, key);
		}
		catch (GeneralSecurityException e)
		{
			throw new IOException(e.toString());
		}
    	OutputStream out2 = new CipherOutputStream(out, cipher);
    	writer.write(out2, document);
    }

    public void setPassword(String password)
	{
		this.password = password;
	}
}
