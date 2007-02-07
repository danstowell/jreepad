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

import javax.swing.JComponent;
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
public class TableViewer extends AbstractArticleView
{
	private JTable table;

	public TableViewer(JreepadArticle article)
	{
		super(article);
		table = new JTable(getTableModel(article));
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setGridColor(Color.GRAY);
		table.setShowGrid(true);
		table.setShowVerticalLines(true);
		table.setShowHorizontalLines(true);
	}

	public JComponent getComponent()
	{
		return table;
	}

	public void reloadArticle()
	{
		table.setModel(getTableModel(article));
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
		int w = table.getColumnCount();
		int h = table.getRowCount();
		StringBuffer csv = new StringBuffer();
		String quoteMark = getPrefs().addQuotesToCsvOutput ? "\"" : "";
		for (int i = 0; i < h; i++)
		{
			for (int j = 0; j < (w - 1); j++)
				csv.append(quoteMark + (String) table.getValueAt(i, j) + quoteMark + ",");
			csv.append(quoteMark + (String) table.getValueAt(i, w - 1) + quoteMark + "\n");
		}
		return csv.toString();
	}

	public String getSelectedText()
	{
        int x = table.getSelectedColumn();
        int y = table.getSelectedRow();
        if(x==-1 || y ==-1)
          return "";
        return table.getValueAt(y,x).toString();
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
