package jreepad;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class JreepadViewer extends JFrame
{
  private static JreepadViewer theApp;
  private Box toolBar;
  private JreepadView theJreepad;
  private Container content;
//  private JreepadPrefs prefs;
  private static final File prefsFile = new File(".jreepref");
  
  private boolean warnAboutUnsaved = false;
  
//  private File openLocation = new File("/Users/dan/javaTestArea/Jreepad/");
//  private File saveLocation;
//  private File importLocation;
//  private File exportLocation;
  private JFileChooser fileChooser;
  
  private String windowTitle;
  
  private JComboBox viewSelector;
  
  private Thread autoSaveThread;

  private JDialog autoSaveDialog;
  private JCheckBox autoSaveCheckBox;
  private JSpinner autoSavePeriodSpinner;
  private JButton autoSaveOkButton;
  private JButton autoSaveCancelButton;
  
  private JDialog prefsDialog;
  private JCheckBox loadLastFileOnOpenCheckBox;
  private JCheckBox autoDateNodesCheckBox;
  private JButton prefsOkButton;
  private JButton prefsCancelButton;
  
  private JDialog searchDialog;
  private JTextField nodeSearchField;
  private JTextField articleSearchField;
  private JComboBox searchCombinatorSelector;
  private JCheckBox searchCaseCheckBox;
  private JComboBox searchWhereSelector;
  private JSpinner searchMaxNumSpinner;
  private JButton searchGoButton;
  private JButton searchCloseButton;
  private JLabel searchResultsLabel;
  private JTable searchResultsTable;
  private JScrollPane searchResultsTableScrollPane;
  private AbstractTableModel searchResultsTableModel;
  
  private JMenuBar menuBar;
  private JMenu fileMenu;
  private JMenuItem newMenuItem;
  private JMenuItem openMenuItem;
  private JMenuItem saveMenuItem;
  private JMenuItem saveAsMenuItem;
  private JMenuItem backupToMenuItem;
    private JMenu importMenu;
    private JMenuItem importHjtMenuItem;
    private JMenuItem importTextMenuItem;
    private JMenu exportMenu;
    private JMenuItem exportHjtMenuItem;
    private JMenuItem exportHtmlMenuItem;
    private JMenuItem exportSimpleXmlMenuItem;
    private JMenuItem exportTextMenuItem;
  private JMenuItem quitMenuItem;
  private JMenu editMenu;
  private JMenuItem undoMenuItem;
  private JMenuItem addAboveMenuItem;
  private JMenuItem addBelowMenuItem;
  private JMenuItem addChildMenuItem;
  private JMenuItem deleteMenuItem;
  private JMenuItem upMenuItem;
  private JMenuItem downMenuItem;
  private JMenuItem indentMenuItem;
  private JMenuItem outdentMenuItem;
  private JMenuItem sortMenuItem;
  private JMenuItem sortRecursiveMenuItem;
  private JMenu searchMenu;
  private JMenuItem searchMenuItem;
  private JMenu viewMenu;
  private JMenuItem viewBothMenuItem;
  private JMenuItem viewTreeMenuItem;
  private JMenuItem viewArticleMenuItem;
  private JCheckBoxMenuItem viewToolbarMenuItem;
  private JMenu optionsMenu;
  private JMenuItem autoSaveMenuItem;
  private JMenuItem prefsMenuItem;
  private JMenu helpMenu;
  private JMenuItem keyboardHelpMenuItem;
  private JMenuItem dragDropHelpMenuItem;
  private JMenuItem aboutMenuItem;
  
  public JreepadViewer()
  {
    this("");
  }
  public JreepadViewer(String fileNameToLoad)
  {
    // Check if a preferences file exists - and if so, load it
    try
    {
      if(prefsFile.exists() && prefsFile.isFile())
      {
        ObjectInputStream prefsLoader = new ObjectInputStream(new FileInputStream(prefsFile));
        setPrefs((JreepadPrefs)prefsLoader.readObject());
        prefsLoader.close();
      }
      else
        setPrefs(new JreepadPrefs());
    }
    catch(Exception err)
    {
      setPrefs(new JreepadPrefs());
    }
    
    fileChooser = new JFileChooser();
    content = getContentPane();

    theJreepad = new JreepadView();
/*
    try
    {
      File inFile = new File("/Users/dan/javaTestArea/Jreepad/__tasks__.hjt");
      theJreepad = new JreepadView(new JreepadNode(new FileInputStream(inFile)));
    }
    catch(IOException e)    {      e.printStackTrace();    }
*/
    
    // Create the menu bar
    menuBar = new JMenuBar();
    //
    fileMenu = new JMenu("File");
    editMenu = new JMenu("Edit");
    searchMenu = new JMenu("Search");
    viewMenu = new JMenu("View");
    optionsMenu = new JMenu("Options");
    helpMenu = new JMenu("Help");
    //
    newMenuItem = new JMenuItem("New");
    newMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) { newAction();}});
    fileMenu.add(newMenuItem);
    openMenuItem = new JMenuItem("Open");
    openMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) {openAction();}});
    fileMenu.add(openMenuItem);
    saveMenuItem = new JMenuItem("Save");
    saveMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) {saveAction();}});
    fileMenu.add(saveMenuItem);
    saveAsMenuItem = new JMenuItem("Save as...");
    saveAsMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) {saveAsAction();}});
    fileMenu.add(saveAsMenuItem);
    backupToMenuItem = new JMenuItem("Backup to...");
    backupToMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) {backupToAction();}});
    fileMenu.add(backupToMenuItem);
    fileMenu.add(new JSeparator());
      importMenu = new JMenu("Import...");
      fileMenu.add(importMenu);
      importHjtMenuItem = new JMenuItem("...Treepad file as subtree");
      importHjtMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) {importAction(FILE_FORMAT_HJT);}});
      importMenu.add(importHjtMenuItem);
      importTextMenuItem = new JMenuItem("...text file(s) as child node(s)");
      importTextMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) {importAction(FILE_FORMAT_TEXT);}});
      importMenu.add(importTextMenuItem);
      //
      exportMenu = new JMenu("Export selected...");
      fileMenu.add(exportMenu);
      exportHjtMenuItem = new JMenuItem("...subtree to Treepad file");
      exportHjtMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) {exportAction(FILE_FORMAT_HJT);}});
      exportMenu.add(exportHjtMenuItem);
      exportHtmlMenuItem = new JMenuItem("...subtree to HTML");
      exportHtmlMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) {exportAction(FILE_FORMAT_HTML);}});
      exportMenu.add(exportHtmlMenuItem);
      exportSimpleXmlMenuItem = new JMenuItem("...subtree to simple XML");
      exportSimpleXmlMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) {exportAction(FILE_FORMAT_XML);}});
      exportMenu.add(exportSimpleXmlMenuItem);
      exportTextMenuItem = new JMenuItem("...article to text file");
      exportTextMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) {exportAction(FILE_FORMAT_TEXT);}});
      exportMenu.add(exportTextMenuItem);
    fileMenu.add(new JSeparator());
    quitMenuItem = new JMenuItem("Quit");
    quitMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) { quitAction(); }});
    fileMenu.add(quitMenuItem);
    //
    undoMenuItem = new JMenuItem("Undo");
    undoMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) { undoAction();}});
    editMenu.add(undoMenuItem);
    editMenu.add(new JSeparator());
    addAboveMenuItem = new JMenuItem("Add sibling above");
    addAboveMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) { theJreepad.addNodeAbove(); theJreepad.returnFocusToTree(); warnAboutUnsaved = true; updateWindowTitle();}});
    editMenu.add(addAboveMenuItem);
    addBelowMenuItem = new JMenuItem("Add sibling below");
    addBelowMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) { theJreepad.addNodeBelow(); theJreepad.returnFocusToTree(); warnAboutUnsaved = true; updateWindowTitle();}});
    editMenu.add(addBelowMenuItem);
    addChildMenuItem = new JMenuItem("Add child");
    addChildMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) { theJreepad.addNode(); theJreepad.returnFocusToTree(); warnAboutUnsaved = true;updateWindowTitle(); }});
    editMenu.add(addChildMenuItem);
    editMenu.add(new JSeparator());
    deleteMenuItem = new JMenuItem("Delete node");
    deleteMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) { deleteNodeAction(); }});
    editMenu.add(deleteMenuItem);
    editMenu.add(new JSeparator());
    upMenuItem = new JMenuItem("Move node up");
    upMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) { theJreepad.moveCurrentNodeUp(); theJreepad.returnFocusToTree(); warnAboutUnsaved = true;updateWindowTitle(); }});
    editMenu.add(upMenuItem);
    downMenuItem = new JMenuItem("Move node down");
    downMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) { theJreepad.moveCurrentNodeDown(); theJreepad.returnFocusToTree(); warnAboutUnsaved = true;updateWindowTitle(); }});
    editMenu.add(downMenuItem);
    editMenu.add(new JSeparator());
    indentMenuItem = new JMenuItem("Indent node (demote)");
    indentMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) { theJreepad.indentCurrentNode(); theJreepad.returnFocusToTree(); warnAboutUnsaved = true;updateWindowTitle(); }});
    editMenu.add(indentMenuItem);
    outdentMenuItem = new JMenuItem("Outdent node (promote)");
    outdentMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) { theJreepad.outdentCurrentNode(); theJreepad.returnFocusToTree(); warnAboutUnsaved = true;updateWindowTitle(); }});
    editMenu.add(outdentMenuItem);
    editMenu.add(new JSeparator());
    sortMenuItem = new JMenuItem("Sort children (one level)");
    sortMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) { theJreepad.sortChildren(); warnAboutUnsaved = true;updateWindowTitle(); }});
    editMenu.add(sortMenuItem);
    sortRecursiveMenuItem = new JMenuItem("Sort children (all levels)");
    sortRecursiveMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) { theJreepad.sortChildrenRecursive(); warnAboutUnsaved = true;updateWindowTitle(); }});
    editMenu.add(sortRecursiveMenuItem);
    //
    searchMenuItem = new JMenuItem("Search");
    searchMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) { openSearchDialog(); }});
    searchMenu.add(searchMenuItem);
    //
    viewBothMenuItem = new JMenuItem("Both tree and article");
    viewBothMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) { setViewMode(JreepadPrefs.VIEW_BOTH); }});
    viewMenu.add(viewBothMenuItem);
    viewTreeMenuItem = new JMenuItem("Tree");
    viewTreeMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) { setViewMode(JreepadPrefs.VIEW_TREE); }});
    viewMenu.add(viewTreeMenuItem);
    viewArticleMenuItem = new JMenuItem("Article");
    viewArticleMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) { setViewMode(JreepadPrefs.VIEW_ARTICLE); }});
    viewMenu.add(viewArticleMenuItem);
    viewMenu.add(new JSeparator());
    viewToolbarMenuItem = new JCheckBoxMenuItem("Toolbar", true);
    viewToolbarMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) { setViewToolbar(viewToolbarMenuItem.isSelected()); }});
    viewMenu.add(viewToolbarMenuItem);
    //
    autoSaveMenuItem = new JMenuItem("Autosave...");
    autoSaveMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) { showAutoSaveDialog(); }});
    optionsMenu.add(autoSaveMenuItem);
    prefsMenuItem = new JMenuItem("Preferences");
    prefsMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) { showPrefsDialog(); }});
    optionsMenu.add(prefsMenuItem);
    //
    keyboardHelpMenuItem = new JMenuItem("Keyboard shortcuts");
    keyboardHelpMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e)
    		{
              JOptionPane.showMessageDialog(theApp, 
              "\nNAVIGATING AROUND THE TREE:" +
              "\nUse the arrow (cursor) keys to navigate around the tree." +
              "\nUp/down will move you up/down the visible nodes." +
              "\nLeft/right will expand/collapse nodes." +
              "\n" +
              "\nADDING/DELETING NODES:" +
              "\n[Alt+A] Add sibling node above current node" +
              "\n[Alt+B] Add sibling node below current node" +
              "\n[Alt+C] Add child node to current node" +
              "\n[Alt+K] Delete current node" +
              "\n" +
              "\nMOVING NODES:" +
              "\n[Alt+U] Move node up" +
              "\n[Alt+D] Move node down" +
              "\n[Alt+I] Indent node" +
              "\n[Alt+O] Outdent node" +
              "\n" +
              "\nCOPYING AND PASTING:" +
              "\n[Ctrl+A] Select all" +
              "\n[Ctrl+X] Cut selected text" +
              "\n[Ctrl+C] Copy selected text" +
              "\n[Ctrl+V] Paste selected text" +
              "\n - The copy/paste functions are included automatically" +
              "\n    by the Mac OSX runtime. I can't guarantee they exist" +
              "\n    for you if you're using a different operating system!" +
/*
              "\n" +
              "\n" +
              "\n" +
              "\n" +
              "\n" +
              "\n" +
              "\n" +
              "\n[] " +
              "\n" +
*/
              ""
              ,
              "Jreepad keyboard shortcuts", 
              JOptionPane.INFORMATION_MESSAGE); 
    		}});
    helpMenu.add(keyboardHelpMenuItem);
    dragDropHelpMenuItem = new JMenuItem("Help with drag-and-drop");
    dragDropHelpMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e)
    		{
              JOptionPane.showMessageDialog(theApp, 
              "\nDRAG-AND-DROP:" +
              "\n" +
              "\nOne of the easiest ways to manage the structure" +
              "\nof your Treepad file is to drag the nodes around" +
              "\nusing the mouse." +
              "\n" +
              "\nClick on a node's title, and, keeping the mouse" +
              "\nbutton held down, move the mouse to where you" +
              "\nwant the node to be moved to. Then release the" +
              "\nmouse button, and the node will be moved to its" +
              "\nnew position in the tree." +
              "\n" +
              "\n" +
              "\n" +
/*
              "\n" +
              "\n" +
              "\n" +
              "\n" +
              "\n" +
              "\n" +
              "\n" +
              "\n[] " +
              "\n" +
*/
              ""
              ,
              "Drag-and-drop", 
              JOptionPane.INFORMATION_MESSAGE); 
    		}});
    helpMenu.add(dragDropHelpMenuItem);
    aboutMenuItem = new JMenuItem("About Jreepad");
    aboutMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e)
            {
/*
              HTMLDocument aboutDoc = new HTMLDocument();
              
              try
              {
                aboutDoc.insertString(0,"<h1>Well</h1><p>hello there</p>", new SimpleAttributeSet());
              }
              catch(BadLocationException ee)
              {
                System.out.println(ee);
              }
              
              JTextPane aboutPane = new JTextPane(aboutDoc);
              aboutPane.setContentType("text/html");
*/
              
              JOptionPane.showMessageDialog(theApp, 
//				aboutPane,
              "Jreepad is an open-source Java program\n" +
              "designed to provide the functionality\n" +
              "(including file interoperability) of\n" +
              "a Windows program called \"Treepad Lite\"\n" +
              "part of the \"Treepad\" suite of software \n" +
              "written by Henk Hagedoorn.\n" +
              "\n" +
              "\nJreepad project website:" +
              "\n  http://jreepad.sourceforge.net" +
              "\n" +
              "\nTreepad website:" +
              "\n  http://www.treepad.com"
              ,
              "About Jreepad", 
              JOptionPane.INFORMATION_MESSAGE); 
            }});
    helpMenu.add(aboutMenuItem);
    //
    menuBar.add(fileMenu);
    menuBar.add(editMenu);
    menuBar.add(searchMenu);
    menuBar.add(viewMenu);
    menuBar.add(optionsMenu);
    menuBar.add(helpMenu);
    setJMenuBar(menuBar);
    //
    // Now the mnemonics...
    fileMenu.setMnemonic('F');
    newMenuItem.setMnemonic('N');
    openMenuItem.setMnemonic('O');
    saveMenuItem.setMnemonic('S');
    saveAsMenuItem.setMnemonic('A');
    backupToMenuItem.setMnemonic('B');
    importMenu.setMnemonic('I');
    importHjtMenuItem.setMnemonic('f');
    importTextMenuItem.setMnemonic('t');
    exportMenu.setMnemonic('E');
    exportHjtMenuItem.setMnemonic('f');
    exportHtmlMenuItem.setMnemonic('h');
    exportSimpleXmlMenuItem.setMnemonic('x');
    exportTextMenuItem.setMnemonic('t');
    quitMenuItem.setMnemonic('Q');
    editMenu.setMnemonic('E');
    undoMenuItem.setMnemonic('u');
    addAboveMenuItem.setMnemonic('a');
    addBelowMenuItem.setMnemonic('b');
    addChildMenuItem.setMnemonic('c');
    upMenuItem.setMnemonic('u');
    downMenuItem.setMnemonic('d');
    indentMenuItem.setMnemonic('i');
    outdentMenuItem.setMnemonic('o');
    deleteMenuItem.setMnemonic('k');
    searchMenu.setMnemonic('S');
    searchMenuItem.setMnemonic('s');
    viewMenu.setMnemonic('V');
    viewBothMenuItem.setMnemonic('b');
    viewTreeMenuItem.setMnemonic('t');
    viewArticleMenuItem.setMnemonic('a');
    viewToolbarMenuItem.setMnemonic('o');
    optionsMenu.setMnemonic('O');
    autoSaveMenuItem.setMnemonic('a');
    prefsMenuItem.setMnemonic('p');
    helpMenu.setMnemonic('H');
    keyboardHelpMenuItem.setMnemonic('k');
    dragDropHelpMenuItem.setMnemonic('d');
    aboutMenuItem.setMnemonic('a');
    // Finished creating the menu bar
    
    // Add the toolbar buttons
    toolBar = Box.createHorizontalBox();
   /* THESE BUTTONS HAVE BEEN REMOVED. But leave the code here, since they may later be replaced with iconic buttons.
    JButton newButton = new JButton("New");
    toolBar.add(newButton);
    JButton openButton = new JButton("Open");
    toolBar.add(openButton);
    JButton saveButton = new JButton("Save");
    toolBar.add(saveButton);
   */
    //
    JButton addAboveButton = new JButton("Add above");
    toolBar.add(addAboveButton);
    JButton addBelowButton = new JButton("Add below");
    toolBar.add(addBelowButton);
    JButton addButton = new JButton("Add child");
    toolBar.add(addButton);
    JButton removeButton = new JButton("Del");
    toolBar.add(removeButton);
    //
    JButton upButton = new JButton("Up");
    toolBar.add(upButton);
    JButton downButton = new JButton("Down");
    toolBar.add(downButton);
    JButton indentButton = new JButton("In");
    toolBar.add(indentButton);
    JButton outdentButton = new JButton("Out");
    toolBar.add(outdentButton);
    //
    // Now the mnemonics...
    addAboveButton.setMnemonic('a');
    addBelowButton.setMnemonic('b');
    addButton.setMnemonic('c');
    upButton.setMnemonic('u');
    downButton.setMnemonic('d');
    indentButton.setMnemonic('i');
    outdentButton.setMnemonic('o');
    removeButton.setMnemonic('k');
    //
    viewSelector = new JComboBox(new String[]{"Tree+Article","Tree","Article"});
    viewSelector.setEditable(false);
    viewSelector.setSelectedIndex(0);
    viewSelector.addActionListener(new ActionListener(){
                               public void actionPerformed(ActionEvent e){ 
                               switch(viewSelector.getSelectedIndex())
                               {
                                 case 1:
                                   setViewMode(JreepadPrefs.VIEW_TREE); break;
                                 case 2:
                                   setViewMode(JreepadPrefs.VIEW_ARTICLE); break;
                                 default:
                                   setViewMode(JreepadPrefs.VIEW_BOTH); break;
                               }
                               } });
    toolBar.add(viewSelector);
    
    // Add the actions to the toolbar buttons
   /* THESE BUTTONS HAVE BEEN REMOVED. But leave the code here, since they may later be replaced with iconic buttons.
    newButton.addActionListener(new ActionListener(){
                               public void actionPerformed(ActionEvent e){ content.remove(theJreepad); theJreepad = new JreepadView(); content.add(theJreepad); repaint(); } });
    openButton.addActionListener(new ActionListener(){
                               public void actionPerformed(ActionEvent e){ openAction(); } });
    saveButton.addActionListener(new ActionListener(){
                               public void actionPerformed(ActionEvent e){ saveAction(); } });
   */
    upButton.addActionListener(new ActionListener(){
                               public void actionPerformed(ActionEvent e){ theJreepad.moveCurrentNodeUp(); repaint();  theJreepad.returnFocusToTree(); warnAboutUnsaved = true;updateWindowTitle();} });
    downButton.addActionListener(new ActionListener(){
                               public void actionPerformed(ActionEvent e){ theJreepad.moveCurrentNodeDown(); repaint();  theJreepad.returnFocusToTree(); warnAboutUnsaved = true;updateWindowTitle();} });
    indentButton.addActionListener(new ActionListener(){
                               public void actionPerformed(ActionEvent e){ theJreepad.indentCurrentNode(); repaint();  theJreepad.returnFocusToTree(); warnAboutUnsaved = true;updateWindowTitle();} });
    outdentButton.addActionListener(new ActionListener(){
                               public void actionPerformed(ActionEvent e){ theJreepad.outdentCurrentNode(); repaint(); theJreepad.returnFocusToTree(); warnAboutUnsaved = true;updateWindowTitle(); } });
    addAboveButton.addActionListener(new ActionListener(){
                               public void actionPerformed(ActionEvent e){ theJreepad.addNodeAbove(); repaint(); /* theJreepad.returnFocusToTree(); */ warnAboutUnsaved = true;updateWindowTitle();} });
    addBelowButton.addActionListener(new ActionListener(){
                               public void actionPerformed(ActionEvent e){ theJreepad.addNodeBelow(); repaint(); /* theJreepad.returnFocusToTree(); */ warnAboutUnsaved = true;updateWindowTitle();} });
    addButton.addActionListener(new ActionListener(){
                               public void actionPerformed(ActionEvent e){ theJreepad.addNode(); repaint(); /* theJreepad.returnFocusToTree(); */ warnAboutUnsaved = true;updateWindowTitle();} });
    removeButton.addActionListener(new ActionListener(){
                               public void actionPerformed(ActionEvent e){ deleteNodeAction(); } });

    // Establish the search dialogue box - so that it can be called whenever wanted
    searchDialog = new JDialog(theApp, "Search Jreepad", false);
    searchDialog.setVisible(false);
    Box vBox = Box.createVerticalBox();
    //
    Box hBox = Box.createHorizontalBox();
    nodeSearchField = new JTextField("");
    vBox.add(new JLabel("Search in node titles for: "));
    hBox.add(nodeSearchField);
    vBox.add(hBox);
    //
    hBox = Box.createHorizontalBox();
    articleSearchField = new JTextField("");
    vBox.add(new JLabel("Search in article text for: "));
    hBox.add(articleSearchField);
    vBox.add(hBox);
    //
    searchCombinatorSelector = new JComboBox(new String[]{"\"OR\" (Either one must be found)", "\"AND\" (Both must be found)"});
    searchCombinatorSelector.setEditable(false);
    hBox = Box.createHorizontalBox();
    hBox.add(new JLabel("Combine searches using: "));
    hBox.add(searchCombinatorSelector);
    vBox.add(hBox);
    //
    vBox.add(searchCaseCheckBox = new JCheckBox("Case sensitive search", false));
    //
    searchWhereSelector = new JComboBox(new String[]{"Selected node and its children", "Entire tree"});
    searchWhereSelector.setEditable(false);
    hBox = Box.createHorizontalBox();
    hBox.add(new JLabel("Search where: "));
    hBox.add(searchWhereSelector);
    vBox.add(hBox);
    //
    searchMaxNumSpinner = new JSpinner(new SpinnerNumberModel(getPrefs().searchMaxNum,1,1000,1));
    hBox = Box.createHorizontalBox();
    hBox.add(new JLabel("Max number of results: "));
    hBox.add(searchMaxNumSpinner);
    vBox.add(hBox);
    //
    hBox = Box.createHorizontalBox();
    hBox.add(searchGoButton = new JButton("Search"));
    searchGoButton.addActionListener(new ActionListener(){
                               public void actionPerformed(ActionEvent e)
                               {
                                 getPrefs().searchMaxNum = (Integer.valueOf(searchMaxNumSpinner.getValue().toString())).intValue();

                                 performSearch(nodeSearchField.getText(), articleSearchField.getText(), 
                                 searchWhereSelector.getSelectedIndex(), searchCombinatorSelector.getSelectedIndex()==0,
                                 searchCaseCheckBox.isSelected(), 
                                 getPrefs().searchMaxNum
                                 );
                               } });
    hBox.add(searchCloseButton = new JButton("Close"));
    searchCloseButton.addActionListener(new ActionListener(){
                               public void actionPerformed(ActionEvent e){ searchDialog.hide(); } });
    vBox.add(hBox);
    //
    // NOW FOR THE SEARCH RESULTS TABLE - COULD BE TRICKY!
    searchResultsTableModel = new AbstractTableModel()
    {
      private final String[] columns = new String[]{"Node","Article text","Full path"};
      public int getColumnCount() { return columns.length; }
      public String getColumnName(int index)
      {
        return columns[index];
      }
      public int getRowCount()
      {
        JreepadView.JreepadSearchResult[] results = theJreepad.getSearchResults();
        if(results==null || results.length==0)
          return 1;
        else
          return results.length;
      }
      public Object getValueAt(int row, int col)
      {
        JreepadView.JreepadSearchResult[] results = theJreepad.getSearchResults();
        if(results==null || results.length==0)
          switch(col)
          {
            case 2:
              return "";
            case 1:
              return "Results will appear here...";
            default:
              return "";
          }
        else
          switch(col)
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
    vBox.add(searchResultsLabel = new JLabel("Search results:"));
    vBox.add(searchResultsTableScrollPane);
    //
    // Add mouse listener
    MouseListener sml = new MouseAdapter()
    {
      public void mouseClicked(MouseEvent e)
      {
        JreepadView.JreepadSearchResult[] results = theJreepad.getSearchResults();
        int selectedRow = searchResultsTable.getSelectedRow();
        if(results==null || results.length==0 || selectedRow==-1)
          return;
        
        if(e.getClickCount()>1)
        {
          // Select the node in the tree, and move focus to the tree
          theJreepad.getTree().setSelectionPath(results[selectedRow].getTreePath());
          theJreepad.getTree().scrollPathToVisible(results[selectedRow].getTreePath());
          theApp.toFront();
          theJreepad.returnFocusToTree();
        }
      }
    };
    searchResultsTable.addMouseListener(sml); 
    //
    searchDialog.getContentPane().add(vBox);
    // Finished establishing the search dialogue box

    
    // Establish the autosave thread
    autoSaveThread = new Thread("Autosave thread")
    					{
    					  public void run()
    					  {
    					    while(getPrefs().autoSave)
    					    {
    					      try
    					      {
    					        // Sleep for a bit...
    					        sleep(60000L * getPrefs().autoSavePeriod);
    					        yield();
    					        // ...then if the saveLocation != null, trigger saveAction()
    					        if(getPrefs().autoSave && getPrefs().saveLocation != null)
    					        {
    					          saveAction();
    					 //         System.out.println("Autosave performed a save action.");
    					        }
    					        else
    					 //         System.out.println("Autosave decided not to save.");
								updateWindowTitle();
    					      }
    					      catch(InterruptedException e)
    					      {
    					      }
    					    }
    					  }
    					};
    autoSaveThread.setPriority(Thread.MIN_PRIORITY);
    if(getPrefs().autoSave)
      autoSaveThread.start();
    // Finished establishing the autosave thread


    // Establish the autosave dialogue box
    autoSaveDialog = new JDialog(theApp, "Autosave", true);
    autoSaveDialog.setVisible(false);
    vBox = Box.createVerticalBox();
    vBox.add(autoSaveCheckBox = new JCheckBox("Autosave", getPrefs().autoSave));
    hBox = Box.createHorizontalBox();
    hBox.add(new JLabel("Frequency (minutes):"));
    hBox.add(autoSavePeriodSpinner = new JSpinner(new SpinnerNumberModel(getPrefs().autoSavePeriod,1,1000,1)));
    vBox.add(hBox);
    hBox = Box.createHorizontalBox();
    hBox.add(autoSaveOkButton = new JButton("OK"));
    hBox.add(autoSaveCancelButton = new JButton("Cancel"));
    autoSaveOkButton.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e){
									getPrefs().autoSavePeriod = ((Integer)(autoSavePeriodSpinner.getValue())).intValue();
									getPrefs().autoSave = autoSaveCheckBox.isSelected();
                                    autoSaveDialog.hide();
									if(getPrefs().autoSave && !(autoSaveThread.isAlive()))
									{
	  JOptionPane.showMessageDialog(theApp, "Autosave turned on. It will stay on until you deactivate it (even if you quit and re-start Jreepad).", "Autosave is ON" , JOptionPane.INFORMATION_MESSAGE);
	  								  autoSaveThread.start();
									}
									updateWindowTitle();
                                   }});
    autoSaveCancelButton.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e){autoSaveDialog.hide();}});
    vBox.add(hBox);
    autoSaveDialog.getContentPane().add(vBox);
    // Finished establishing the autosave dialogue box

    // Establish the prefs dialogue box
    prefsDialog = new JDialog(theApp, "Preferences", true);
    prefsDialog.setVisible(false);
    vBox = Box.createVerticalBox();
    vBox.add(loadLastFileOnOpenCheckBox = new JCheckBox("When Jreepad starts, automatically load the last-saved file", getPrefs().loadLastFileOnOpen));
    vBox.add(autoDateNodesCheckBox = new JCheckBox("Autodate nodes: whenever a new node is created, add the date into its article", getPrefs().autoDateInArticles));
    hBox = Box.createHorizontalBox();
    hBox.add(prefsOkButton = new JButton("OK"));
    hBox.add(prefsCancelButton = new JButton("Cancel"));
    prefsOkButton.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e){
									getPrefs().loadLastFileOnOpen = loadLastFileOnOpenCheckBox.isSelected();
									getPrefs().autoDateInArticles = autoDateNodesCheckBox.isSelected();
									prefsDialog.hide();
                                   }});
    prefsCancelButton.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e){prefsDialog.hide();}});
    vBox.add(hBox);
    prefsDialog.getContentPane().add(vBox);
    // Finished establishing the prefs dialogue box

    
    content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
    content.add(toolBar);
    content.add(theJreepad);

    // Load the file - if it has been specified, and if it can be found, and if it's a valid HJT file
    if(fileNameToLoad != "")
    {
      try
      {
        getPrefs().openLocation = new File(fileNameToLoad);
        content.remove(theJreepad);
        theJreepad = new JreepadView(new JreepadNode(new FileInputStream(getPrefs().openLocation)));
        getPrefs().saveLocation = getPrefs().exportLocation = getPrefs().importLocation = getPrefs().openLocation;
        content.add(theJreepad);
	    getPrefs().saveLocation = getPrefs().openLocation;
        setTitleBasedOnFilename(getPrefs().openLocation.getName());
        warnAboutUnsaved = false;
      }
      catch(IOException err)
      {
        JOptionPane.showMessageDialog(theApp, err, "Sorry - failed to load requested file." , JOptionPane.ERROR_MESSAGE);
        content.remove(theJreepad);
        theJreepad = new JreepadView(new JreepadNode());
        content.add(theJreepad);
        setTitleBasedOnFilename("");
      }
    }
    else
      getPrefs().saveLocation = null;



    // Set close operation
    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    addWindowListener(new WindowAdapter() { public void windowClosing(WindowEvent e){ quitAction(); }});

    // Finally, make the window visible and well-sized
    setTitleBasedOnFilename("");
    Toolkit theToolkit = getToolkit();
    Dimension wndSize = theToolkit.getScreenSize();
    setBounds((int)(wndSize.width*0.2f),(int)(wndSize.height*0.2f),
              (int)(wndSize.width*0.6f),(int)(wndSize.height*0.6f));
    searchDialog.setBounds((int)(wndSize.width*0.3f),(int)(wndSize.height*0.1f),
              (int)(wndSize.width*0.4f),(int)(wndSize.height*0.5f));
    autoSaveDialog.setBounds((int)(wndSize.width*0.5f),(int)(wndSize.height*0.3f),
              (int)(wndSize.width*0.3f),(int)(wndSize.height*0.15f));
    prefsDialog.setBounds((int)(wndSize.width*0.3f),(int)(wndSize.height*0.2f),
              (int)(wndSize.width*0.6f),(int)(wndSize.height*0.2f));
    setVisible(true);
  }
  
  public static void main(String[] args)
  {
    if(args.length==0)
      theApp = new JreepadViewer();
    else if(args.length==1)
      theApp = new JreepadViewer(args[0]);
    else
      System.err.println("Only one (optional) argument can be passed - the name of the HJT file to load.");
  }
  
  private void newAction()
  {
    if(warnAboutUnsaved)
    {
	  int answer = JOptionPane.showConfirmDialog(theApp, "Save current file before starting a new one?", 
	                   "Save?" , JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
      if(answer == JOptionPane.CANCEL_OPTION)
        return;
      else if(answer == JOptionPane.YES_OPTION)
        if(!saveAction())
          return; // This cancels "New" if the save action failed or was cancelled
    }
	content.remove(theJreepad);
	theJreepad = new JreepadView(new JreepadNode());
	getPrefs().saveLocation = null;
	content.add(theJreepad);
	setTitleBasedOnFilename("");
	validate();
	repaint();
	warnAboutUnsaved = false;
//	theJreepad.clearUndoCache();
  }
  
  private void openAction()
  {
    if(warnAboutUnsaved)
    {
	  int answer = JOptionPane.showConfirmDialog(theApp, "Save current file before opening a new one?", 
	                   "Save?" , JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
      if(answer == JOptionPane.CANCEL_OPTION)
        return;
      else if(answer == JOptionPane.YES_OPTION)
        if(!saveAction())
          return; // This cancels quit if the save action failed or was cancelled
    }

    try
    {
      fileChooser.setCurrentDirectory(getPrefs().openLocation);
      if(fileChooser.showOpenDialog(theApp) == JFileChooser.APPROVE_OPTION)
      {
        getPrefs().openLocation = fileChooser.getSelectedFile();
        content.remove(theJreepad);
        theJreepad = new JreepadView(new JreepadNode(new FileInputStream(getPrefs().openLocation)));
        getPrefs().backupLocation = getPrefs().saveLocation = getPrefs().exportLocation = getPrefs().importLocation = getPrefs().openLocation;
        content.add(theJreepad);
        setTitleBasedOnFilename(getPrefs().openLocation.getName());
        validate();
        repaint();
        warnAboutUnsaved = false;
        theJreepad.clearUndoCache();
      }
    }
    catch(IOException err)
    {
      JOptionPane.showMessageDialog(theApp, err, "File input error" , JOptionPane.ERROR_MESSAGE);
    }
  } // End of: openAction()
  
  private boolean saveAction()
  {
    if(getPrefs().saveLocation==null)
    {
      return saveAsAction();
    }
    try
    {
      String writeMe = theJreepad.getRootJreepadNode().toTreepadString();
      FileOutputStream fO = new FileOutputStream(getPrefs().saveLocation);
      DataOutputStream dO = new DataOutputStream(fO);
      dO.writeBytes(writeMe);
      dO.close();
      fO.close();
      warnAboutUnsaved = false;
      updateWindowTitle();
      return true;
    }
    catch(IOException err)
    {
      JOptionPane.showMessageDialog(theApp, err, "File error during Save" , JOptionPane.ERROR_MESSAGE);
    }
    return false;
  }
  private boolean saveAsAction()
  {
    try
    {
      fileChooser.setCurrentDirectory(getPrefs().saveLocation);
      if(fileChooser.showSaveDialog(theApp) == JFileChooser.APPROVE_OPTION)
      {
        getPrefs().saveLocation = fileChooser.getSelectedFile();
        String writeMe = theJreepad.getRootJreepadNode().toTreepadString();
        FileOutputStream fO = new FileOutputStream(getPrefs().saveLocation);
        DataOutputStream dO = new DataOutputStream(fO);
        dO.writeBytes(writeMe);
        dO.close();
        fO.close();
        warnAboutUnsaved = false;
        setTitleBasedOnFilename(getPrefs().saveLocation.getName());
        return true;
      }
      else
        return false;
    }
    catch(IOException err)
    {
      JOptionPane.showMessageDialog(theApp, err, "File error during Save As" , JOptionPane.ERROR_MESSAGE);
    }
    return false;
  } // End of: saveAsAction()

  private boolean backupToAction()
  {
    try
    {
      fileChooser.setCurrentDirectory(getPrefs().backupLocation);
      if(fileChooser.showSaveDialog(theApp) == JFileChooser.APPROVE_OPTION)
      {
        getPrefs().backupLocation = fileChooser.getSelectedFile();
        String writeMe = theJreepad.getRootJreepadNode().toTreepadString();
        FileOutputStream fO = new FileOutputStream(getPrefs().backupLocation);
        DataOutputStream dO = new DataOutputStream(fO);
        dO.writeBytes(writeMe);
        dO.close();
        fO.close();
        return true;
      }
      else
        return false;
    }
    catch(IOException err)
    {
      JOptionPane.showMessageDialog(theApp, err, "File error during Backup" , JOptionPane.ERROR_MESSAGE);
    }
    return false;
  } // End of: backupToAction()

  private void openSearchDialog()
  {
//    searchDialog.setVisible(true);
    searchDialog.show();
  } // End of: openSearchDialog()
  
  private boolean performSearch(String inNodes, String inArticles, int searchWhat /* 0=selected, 1=all */, 
                                boolean orNotAnd, boolean caseSensitive, int maxResults)
  {
    boolean ret = theJreepad.performSearch(inNodes, inArticles, searchWhat, orNotAnd, 
                                          caseSensitive, maxResults);
    if(!ret)
    {
      JOptionPane.showMessageDialog(searchDialog, "Found nothing.", "Search result..." , JOptionPane.INFORMATION_MESSAGE);
      searchResultsLabel.setText("Search results: ");
    }
    else
      searchResultsLabel.setText("Search results: " + theJreepad.getSearchResults().length + " nodes matched");
    searchResultsTableModel.fireTableStructureChanged();
//    searchResultsTable.repaint();
    return ret;
  }

  private void setTitleBasedOnFilename(String filename)
  {
    if(filename=="")
      windowTitle = "Jreepad (Java Treepad Editor)";
    else
      windowTitle = filename + " - Jreepad";
    updateWindowTitle();
  }

  private void updateWindowTitle()
  {
    setTitle(windowTitle + (warnAboutUnsaved?"*":"") + (getPrefs().autoSave?" [Autosave on]":""));
  }
  
  private static final int FILE_FORMAT_HJT=1;
  private static final int FILE_FORMAT_HTML=2;
  private static final int FILE_FORMAT_XML=3;
  private static final int FILE_FORMAT_TEXT=4;
  private void importAction(int importFormat)
  {
    boolean multipleFiles;
    try
    {
      multipleFiles = (importFormat == FILE_FORMAT_TEXT);

      if(multipleFiles)
        fileChooser.setMultiSelectionEnabled(true);
      fileChooser.setCurrentDirectory(getPrefs().importLocation);
      fileChooser.setSelectedFile(new File(theJreepad.getCurrentNode().getTitle()));

      if(fileChooser.showOpenDialog(theApp) == JFileChooser.APPROVE_OPTION)
      {
        getPrefs().importLocation = fileChooser.getSelectedFile();

		switch(importFormat)
		{
		  case FILE_FORMAT_HJT:
			theJreepad.addChild(new JreepadNode(new FileInputStream(getPrefs().importLocation)));
			break;
		  case FILE_FORMAT_TEXT:
		    theJreepad.addChildrenFromTextFiles(fileChooser.getSelectedFiles());
			break;
		  default:
			JOptionPane.showMessageDialog(theApp, "Unknown which format to import - coding error! Oops!", "Error" , JOptionPane.ERROR_MESSAGE);
			return;
		}
	    warnAboutUnsaved = true;
	    updateWindowTitle();
      }
      fileChooser.setMultiSelectionEnabled(false);
    }
    catch(IOException err)
    {
      JOptionPane.showMessageDialog(theApp, err, "File error during Import" , JOptionPane.ERROR_MESSAGE);
    }
  } // End of: importAction()

  private void exportAction(int exportFormat)
  {
    try
    {
      fileChooser.setCurrentDirectory(getPrefs().exportLocation);
      fileChooser.setSelectedFile(new File(theJreepad.getCurrentNode().getTitle()));
      if(fileChooser.showSaveDialog(theApp) == JFileChooser.APPROVE_OPTION)
      {
        getPrefs().exportLocation = fileChooser.getSelectedFile();

		String output;
		switch(exportFormat)
		{
		  case FILE_FORMAT_HJT:
			output = theJreepad.getCurrentNode().toTreepadString();
			break;
		  case FILE_FORMAT_HTML:
			output = theJreepad.getCurrentNode().exportAsHtml();
			break;
		  case FILE_FORMAT_XML:
			output = theJreepad.getCurrentNode().exportAsSimpleXml();
			break;
		  case FILE_FORMAT_TEXT:
			output = theJreepad.getCurrentNode().getContent();
			break;
		  default:
			JOptionPane.showMessageDialog(theApp, "Unknown which format to export - coding error! Oops!", "Error" , JOptionPane.ERROR_MESSAGE);
			return;
		}

        FileOutputStream fO = new FileOutputStream(getPrefs().exportLocation);
        DataOutputStream dO = new DataOutputStream(fO);
        dO.writeBytes(output);
        dO.close();
        fO.close();
      }
    }
    catch(IOException err)
    {
      JOptionPane.showMessageDialog(theApp, err, "File error during Export" , JOptionPane.ERROR_MESSAGE);
    }
  } // End of: exportAction()


  public void quitAction()
  {
    // We need to check about warning-if-unsaved!
    if(warnAboutUnsaved)
    {
	  int answer = JOptionPane.showConfirmDialog(theApp, "Save current file before quitting?", 
	                   "Save before quit?" , JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
      if(answer == JOptionPane.CANCEL_OPTION)
        return;
      else if(answer == JOptionPane.YES_OPTION)
        if(!saveAction())
          return; // This cancels quit if the save action failed or was cancelled
    }
    savePreferencesFile();
    System.exit(0);
  }
  
  private void savePreferencesFile()
  {
    try
    {
      ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(prefsFile));
      out.writeObject(getPrefs());
      out.close();
    }
    catch(IOException err)
    {
    }
  }
  
  private void setViewMode(int mode)
  {
    theJreepad.setViewMode(mode);
    // Update the dropdown menu
    
    // Update the preferences object
    getPrefs().viewWhich = mode;
  }

  private static JreepadPrefs getPrefs() { return JreepadView.getPrefs(); }
  private static void setPrefs(JreepadPrefs thesePrefs) { JreepadView.setPrefs(thesePrefs); }

  private void setViewToolbar(boolean boo)
  {
    toolBar.setVisible(boo);
  }
  
  private void undoAction()
  {
    if(theJreepad.canWeUndo())
      theJreepad.undoAction();
    else
	  JOptionPane.showMessageDialog(theApp, "Nothing to undo!", "No change" , JOptionPane.INFORMATION_MESSAGE);
    updateWindowTitle();
  }
  
  private void showAutoSaveDialog()
  {
    // The autosave simply launches a background thread which periodically triggers saveAction if saveLocation != null
    autoSaveCheckBox.setSelected(getPrefs().autoSave);
    autoSavePeriodSpinner.getModel().setValue(new Integer(getPrefs().autoSavePeriod));
    autoSaveDialog.show();
    autoSaveDialog.toFront();
  }

  private void showPrefsDialog()
  {
    // First make sure the components in the dialogue reflect the true state
    loadLastFileOnOpenCheckBox.setSelected(getPrefs().loadLastFileOnOpen);
    autoDateNodesCheckBox.setSelected(getPrefs().autoDateInArticles);

    prefsDialog.show();
    prefsDialog.toFront();
  }

  private void deleteNodeAction()
  {
    if(JOptionPane.showConfirmDialog(theApp, "Delete node:\n"+theJreepad.getCurrentNode().getTitle(), "Confirm delete", 
               JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return; 
    theJreepad.removeNode();
    theJreepad.returnFocusToTree();
    warnAboutUnsaved = true;
    updateWindowTitle();
  }

}