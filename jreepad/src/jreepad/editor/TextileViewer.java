/*
           Jreepad - personal information manager.
           Copyright (C) 2004 Dan Stowell

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

package jreepad.editor;

import org.philwilson.JTextile;

import jreepad.JreepadArticle;

/**
 * The plain text editor pane.
 */
public class TextileViewer extends HtmlViewer
{
	public TextileViewer(JreepadArticle article)
	{
		super(article);
	}

    public void reloadArticle()
    {
    	try
    	{
    		setText(JTextile.textile(article.getContent()));
    	}
    	catch (Exception e)
    	{
    		// Fallback to HTML if Textile failed
    		e.printStackTrace();
    		super.reloadArticle();
    	}
    }
}
