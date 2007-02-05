package jreepad;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;

/**
 * The search dialog.
 *
 * @version $Id: SearchDialog.java,v 1.1 2007-02-05 11:06:44 pewu Exp $
 */
public class SearchDialog extends JDialog
{
    private JTextField nodeSearchField;
    private JComboBox searchWhereSelector;
    private JSpinner searchMaxNumSpinner;
    private JCheckBox searchCaseCheckBox;
    private AbstractTableModel searchResultsTableModel;
    private JTable searchResultsTable;
    private JScrollPane searchResultsTableScrollPane;
    private JLabel searchResultsLabel;
    private JreepadView theJreepad;

    public SearchDialog(Frame owner, JreepadView theJreepad)
    {
        super(owner, JreepadViewer.lang.getString("SEARCH_WINDOWTITLE"), false);
        this.theJreepad = theJreepad;
        setVisible(false);
        Box vBox = Box.createVerticalBox();
        //
        Box hBox = Box.createHorizontalBox();
        nodeSearchField = new JTextField("");
        vBox.add(new JLabel(JreepadViewer.lang.getString("SEARCH_SEARCHFOR")));
        hBox.add(nodeSearchField);
        nodeSearchField.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    doTheSearch();
                }
            });
        nodeSearchField.addCaretListener(new CaretListener()
            {
                public void caretUpdate(CaretEvent e)
                {
                    doTheSearch();
                }
            });
        vBox.add(hBox);
        //
        searchWhereSelector = new JComboBox(new String[] {
                        JreepadViewer.lang.getString("SEARCH_SELECTEDNODE"),
                        JreepadViewer.lang.getString("SEARCH_WHOLETREE") });
        searchWhereSelector.setSelectedIndex(1);
        searchWhereSelector.setEditable(false);
        searchWhereSelector.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    doTheSearch();
                }
            });
        hBox = Box.createHorizontalBox();
        hBox.add(Box.createGlue());
        hBox.add(new JLabel(JreepadViewer.lang.getString("SEARCH_SEARCHWHERE")));
        hBox.add(searchWhereSelector);
        hBox.add(Box.createGlue());
        hBox.add(searchCaseCheckBox = new JCheckBox(JreepadViewer.lang
            .getString("SEARCH_CASESENSITIVE"), false));
        searchCaseCheckBox.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    doTheSearch();
                }
            });
        hBox.add(Box.createGlue());
        vBox.add(hBox);
        //
        // searchMaxNumSpinner = new DSpinner(1,1000,getPrefs().searchMaxNum);
        searchMaxNumSpinner = new JSpinner(new SpinnerNumberModel(getPrefs().searchMaxNum, 1, 1000,
            1));
        /*
         * searchMaxNumSpinner.addCaretListener(new CaretListener(){ public void
         * caretUpdate(CaretEvent e){ doTheSearch();}}); searchMaxNumSpinner.addActionListener(new
         * ActionListener(){ public void actionPerformed(ActionEvent e){ doTheSearch();}});
         */
        searchMaxNumSpinner.getModel().addChangeListener(new ChangeListener()
            {
                public void stateChanged(ChangeEvent e)
                {
                    doTheSearch();
                }
            });
        hBox = Box.createHorizontalBox();
        hBox.add(Box.createGlue());
        hBox.add(new JLabel(JreepadViewer.lang.getString("SEARCH_MAXRESULTS")));
        hBox.add(searchMaxNumSpinner);
        hBox.add(Box.createGlue());
        vBox.add(hBox);
        //
        // NOW FOR THE SEARCH RESULTS TABLE - COULD BE TRICKY!
        searchResultsTableModel = new AbstractTableModel()
            {
                private final String[] columns = new String[] {
                                JreepadViewer.lang.getString("SEARCH_TBL_COL_NODE"),
                                JreepadViewer.lang.getString("SEARCH_TBL_COL_ARTICLETEXT"),
                                JreepadViewer.lang.getString("SEARCH_TBL_COL_FULLPATH") };

                public int getColumnCount()
                {
                    return columns.length;
                }

                public String getColumnName(int index)
                {
                    return columns[index];
                }

                public int getRowCount()
                {
                    JreepadSearcher.JreepadSearchResult[] results = SearchDialog.this.theJreepad
                        .getSearchResults();
                    if (results == null || results.length == 0)
                        return 1;
                    else
                        return results.length;
                }

                public Object getValueAt(int row, int col)
                {
                    JreepadSearcher.JreepadSearchResult[] results = SearchDialog.this.theJreepad
                        .getSearchResults();
                    if (results == null || results.length == 0)
                        switch (col)
                        {
                        case 2:
                            return "";
                        case 1:
                            return (nodeSearchField.getText() == "" ? JreepadViewer.lang
                                .getString("SEARCH_TBL_BEFORERESULTS") : JreepadViewer.lang
                                .getString("SEARCH_TBL_NORESULTS"));
                        default:
                            return "";
                        }
                    else
                        switch (col)
                        {
                        case 2:
                            return results[row].getTreePath();
                        case 1:
                            return results[row].getArticleQuote();
                        default:
                            return results[row].getNode().getTitle();
                        }
                }
            };
        searchResultsTable = new JTable(searchResultsTableModel);
        searchResultsTable.setCellSelectionEnabled(false);
        searchResultsTable.setColumnSelectionAllowed(false);
        searchResultsTable.setRowSelectionAllowed(true);
        searchResultsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        searchResultsTableScrollPane = new JScrollPane(searchResultsTable);
        vBox.add(searchResultsLabel = new JLabel(JreepadViewer.lang.getString("SEARCH_RESULTS")));
        vBox.add(searchResultsTableScrollPane);
        //
        // Add mouse listener
        MouseListener sml = new MouseAdapter()
            {
                public void mouseClicked(MouseEvent e)
                {
                    mouseClickedOnSearchResultsTable(e);
                }
            };
        searchResultsTable.addMouseListener(sml);
        //
        getContentPane().add(vBox);

        // Now we'll add some keyboard shortcuts
        KeyAdapter searchKeyListener = new KeyAdapter()
            {
                public void keyPressed(KeyEvent eek)
                {
                    switch (eek.getKeyCode())
                    {
                    case KeyEvent.VK_ESCAPE:
                        setVisible(false);
                        break;
                    case KeyEvent.VK_W:
                        if (eek.isControlDown() || eek.isMetaDown())
                            setVisible(false);
                        break;
                    }
                }
            };
        addKeyListener(searchKeyListener);
        nodeSearchField.addKeyListener(searchKeyListener);
        searchCaseCheckBox.addKeyListener(searchKeyListener);
        searchWhereSelector.addKeyListener(searchKeyListener);
        searchResultsTable.addKeyListener(searchKeyListener);
        searchMaxNumSpinner.addKeyListener(searchKeyListener);
        searchResultsTable.addKeyListener(new KeyAdapter()
            {
                public void keyPressed(KeyEvent eek)
                {
                    switch (eek.getKeyCode())
                    {
                    case KeyEvent.VK_SPACE:
                    case KeyEvent.VK_ENTER:
                        mouseClickedOnSearchResultsTable(null);
                        break;
                    }
                }
            });

        // Finished establishing the search dialogue box
    }

    private void doTheSearch()
    {
        getPrefs().searchMaxNum = ((Integer)searchMaxNumSpinner.getValue()).intValue();
        // DSpinner version getPrefs().searchMaxNum = searchMaxNumSpinner.getValue();

        performSearch(nodeSearchField.getText(),
            nodeSearchField.getText(), // articleSearchField.getText(),
            searchWhereSelector.getSelectedIndex(),
            true /* searchCombinatorSelector.getSelectedIndex()==0 */, searchCaseCheckBox
                .isSelected(), getPrefs().searchMaxNum);
    }

    private boolean performSearch(String inNodes, String inArticles,
        int searchWhat /* 0=selected, 1=all */, boolean orNotAnd, boolean caseSensitive,
        int maxResults)
    {
        // setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        boolean ret = theJreepad.performSearch(inNodes, inArticles, searchWhat, orNotAnd,
            caseSensitive, maxResults);
        // setCursor(Cursor.getDefaultCursor());
        if (!ret)
        {
            // JOptionPane.showMessageDialog(searchDialog, "Found nothing.", "Search result..." ,
            // JOptionPane.INFORMATION_MESSAGE);
            searchResultsLabel.setText(JreepadViewer.lang.getString("SEARCH_RESULTS"));
        }
        else
            searchResultsLabel.setText(JreepadViewer.lang.getString("SEARCH_RESULTS")
                + theJreepad.getSearchResults().length
                + JreepadViewer.lang.getString("SEARCH_NODES_MATCHED"));
        searchResultsTableModel.fireTableStructureChanged();
        // searchResultsTable.repaint();
        return ret;
    }

    private void mouseClickedOnSearchResultsTable(MouseEvent e)
    {
        JreepadSearcher.JreepadSearchResult[] results = theJreepad.getSearchResults();
        int selectedRow = searchResultsTable.getSelectedRow();
        if (results == null || results.length == 0 || selectedRow == -1)
            return;

        // Select the node in the tree
        theJreepad.getTree().setSelectionPath(results[selectedRow].getTreePath());
        theJreepad.getTree().scrollPathToVisible(results[selectedRow].getTreePath());
    }

    private static JreepadPrefs getPrefs()
    {
        return JreepadView.getPrefs();
    }

    public void setJreepad(JreepadView theJreepad)
    {
        this.theJreepad = theJreepad;
    }

    public void open()
    {
        setVisible(true);
        nodeSearchField.requestFocus();
        nodeSearchField.setSelectionStart(0);
    }
}
