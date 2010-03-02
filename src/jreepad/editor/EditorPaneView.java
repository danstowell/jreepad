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

import java.awt.Font;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;

import jreepad.JreepadArticle;
import jreepad.ui.FontHelper;
/**
 * Abstract article view in the form of a JEditorPane.
 *
 * @author <a href="mailto:pewu@losthive.org">Przemek Więch</a>
 * @version $Id: EditorPaneView.java,v 1.2 2008-09-21 11:06:50 danstowell Exp $
 */
public abstract class EditorPaneView
    extends AbstractArticleView {

	protected JEditorPane editorPane;

	public EditorPaneView(String type, JreepadArticle article) {
		super(article);
		editorPane = new JEditorPane(type, "");
		reloadArticle();
	}

	public void reloadArticle()
	{
		editorPane.setText(article.getContent());
	}

  public void updateFont(int direction) {
    StyledEditorKit kit = (StyledEditorKit) editorPane.getEditorKit();
    MutableAttributeSet set = kit.getInputAttributes();
    StyledDocument doc = (StyledDocument) editorPane.getDocument();
    Font currentFont = doc.getFont(set);
    int currentFontSize = currentFont.getSize();
    switch (direction) {
      case FontHelper.FONT_DIR_UP:
        currentFontSize++;
        break;
      case FontHelper.FONT_DIR_DOWN:
        currentFontSize--;
        break;
    }
    StyleConstants.setFontSize(set, currentFontSize);
    doc.setCharacterAttributes(0, doc.getLength(), set, false);
  }

  public JComponent getComponent() {
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