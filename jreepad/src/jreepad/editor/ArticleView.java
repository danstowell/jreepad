package jreepad.editor;

import javax.swing.JComponent;

import jreepad.JreepadArticle;

/**
 * Interface for an article viewer or editor.
 *
 * @author <a href="mailto:pewu@losthive.org">Przemek Więch</a>
 * @version $Id: ArticleView.java,v 1.1 2007-02-07 20:16:20 pewu Exp $
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
