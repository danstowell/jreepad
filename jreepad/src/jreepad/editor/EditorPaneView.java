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

import javax.swing.JComponent;
import javax.swing.JEditorPane;

import jreepad.JreepadArticle;

/**
 * Abstract article view in the form of a JEditorPane.
 *
 * @author <a href="mailto:pewu@losthive.org">Przemek Więch</a>
 * @version $Id: EditorPaneView.java,v 1.1 2007-02-07 21:10:43 pewu Exp $
 */
public abstract class EditorPaneView extends AbstractArticleView
{

	protected JEditorPane editorPane;

	public EditorPaneView(String type, JreepadArticle article)
	{
		super(article);
		editorPane = new JEditorPane(type, "");
		reloadArticle();
	}

	public void reloadArticle()
	{
		editorPane.setText(article.getContent());
	}

	public JComponent getComponent()
	{
		return editorPane;
	}

	public String getText()
	{
		return editorPane.getText();
	}

	public String getSelectedText()
	{
		return editorPane.getSelectedText();
	}

}