package jreepad.editor;

import jreepad.JreepadArticle;

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
