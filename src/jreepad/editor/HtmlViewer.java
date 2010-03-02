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

import jreepad.JreepadArticle;

/**
 * The plain HTML viewer pane.
 */
public class HtmlViewer extends EditorPaneView
{
	public HtmlViewer(JreepadArticle article)
	{
		super("text/html", article);
		editorPane.setEditable(false);
	}
}
