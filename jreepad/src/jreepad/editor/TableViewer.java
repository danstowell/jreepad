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

import java.awt.Color;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import jreepad.JreepadArticle;
import jreepad.JreepadPrefs;
import jreepad.JreepadView;

/**
 * The table view pane.
 * Converts CSV content to table view.
 */
public class TableViewer extends JTable
{
	private JreepadArticle article;

	public TableViewer(JreepadArticle article)
	{
		super(getTableModel(article));
		this.article = article;
		setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		setGridColor(Color.GRAY);
		setShowGrid(true);
		setShowVerticalLines(true);
		setShowHorizontalLines(true);
	}

	public void setArticle(JreepadArticle article)
	{
		this.article = article;
		reloadArticle();
	}
	public void reloadArticle()
	{
		setModel(getTableModel(article));
	}

	private static TableModel getTableModel(JreepadArticle a)
	{
		String[][] rowData = a.interpretContentAsCsv();
		String[] columnNames = new String[rowData[0].length];
		for (int i = 0; i < columnNames.length; i++)
			columnNames[i] = " ";
		return new ArticleTableModel(rowData, columnNames);
	}

	public String getText()
	{
		int w = getColumnCount();
		int h = getRowCount();
		StringBuffer csv = new StringBuffer();
		String quoteMark = getPrefs().addQuotesToCsvOutput ? "\"" : "";
		for (int i = 0; i < h; i++)
		{
			for (int j = 0; j < (w - 1); j++)
				csv.append(quoteMark + (String) getValueAt(i, j) + quoteMark
						+ ",");
			csv.append(quoteMark + (String) getValueAt(i, w - 1) + quoteMark
					+ "\n");
		}
		return csv.toString();
	}

	public String getSelectedText()
	{
        int x = getSelectedColumn();
        int y = getSelectedRow();
        if(x==-1 || y ==-1)
          return "";
        return getValueAt(y,x).toString();
	}

	public static JreepadPrefs getPrefs()
	{
		return JreepadView.getPrefs();
	}

	private static class ArticleTableModel extends DefaultTableModel
	{
		public ArticleTableModel(Object[][] data, Object[] columnNames)
		{
			super(data, columnNames);
		}

		public ArticleTableModel()
		{
			super();
		}

		public boolean isCellEditable(int row, int col)
		{
			return false;
		}
	} // End of: class ArticleTableModel
}
