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

import jreepad.JreepadArticle;

/**
 * Interface for an article viewer or editor.
 *
 * @author <a href="mailto:pewu@losthive.org">Przemek Więch</a>
 * @version $Id: ArticleView.java,v 1.2 2007-02-07 21:10:43 pewu Exp $
 */
public interface ArticleView
{
	/**
	 * Returns the GUI viewer/editor component.
	 */
	public JComponent getComponent();

	/**
	 * Returns selected text in view.
	 */
	public String getSelectedText();

	/**
	 * Reloads the article.
	 * Transfers data from article to view.
	 */
	public void reloadArticle();

	/**
	 * Saves the current article.
	 * Transfers data from view to article.
	 */
	public void saveArticle();

	/**
	 * Returns the content text.
	 */
	public String getText();

	/**
	 * Sets new article content.
	 */
	public void setArticle(JreepadArticle article);

	/**
	 * Lock editor - no edits will be allowed.
	 */
	public void lockEdits();

	/**
	 * Unlock editor - edits will now be allowed.
	 */
	public void unlockEdits();
}
