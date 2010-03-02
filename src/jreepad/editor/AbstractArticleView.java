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
 * Abstract implementation of ArticleView.
 * Several basic methods are implemented.
 *
 * @author <a href="mailto:pewu@losthive.org">Przemek Więch</a>
 * @version $Id: AbstractArticleView.java,v 1.2 2007-02-07 21:10:43 pewu Exp $
 */
public abstract class AbstractArticleView implements ArticleView
{
	protected JreepadArticle article;

	// The following boolean should be TRUE while we're changing from node to
	// node, and false otherwise
	protected boolean editLocked = false;

	public AbstractArticleView(JreepadArticle article)
	{
		this.article = article;
	}

	public void lockEdits()
	{
		editLocked = true;
	}

	public void unlockEdits()
	{
		editLocked = false;
	}

	public void saveArticle()
	{
		article.setContent(getText());
	}

	public void setArticle(JreepadArticle article)
	{
		this.article = article;
		reloadArticle();
	}
}
