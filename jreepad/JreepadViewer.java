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
  
  private File openLocation = new File("/Users/dan/javaTestArea/Jreepad/");
  private File saveLocation;
  private File importLocation;
  private File exportLocation;
  private JFileChooser fileChooser;
  
  private JComboBox viewSelector;
  
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
  private JMenu helpMenu;
  private JMenuItem keyboardHelpMenuItem;
  private JMenuItem aboutMenuItem;
  
  public JreepadViewer()
  {
    this("");
  }
  public JreepadViewer(String fileNameToLoad)
  {
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
    helpMenu = new JMenu("Help");
    //
    newMenuItem = new JMenuItem("New");
    newMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) {content.remove(theJreepad); theJreepad = new JreepadView(); content.add(theJreepad); repaint();}});
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
    quitMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) { System.exit(0); }});
    fileMenu.add(quitMenuItem);
    //
    addAboveMenuItem = new JMenuItem("Add sibling above");
    addAboveMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) { theJreepad.addNodeAbove(); theJreepad.returnFocusToTree(); }});
    editMenu.add(addAboveMenuItem);
    addBelowMenuItem = new JMenuItem("Add sibling below");
    addBelowMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) { theJreepad.addNodeBelow(); theJreepad.returnFocusToTree(); }});
    editMenu.add(addBelowMenuItem);
    addChildMenuItem = new JMenuItem("Add child");
    addChildMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) { theJreepad.addNode(); theJreepad.returnFocusToTree(); }});
    editMenu.add(addChildMenuItem);
    editMenu.add(new JSeparator());
    deleteMenuItem = new JMenuItem("Delete node");
    deleteMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) { theJreepad.removeNode(); theJreepad.returnFocusToTree();  }});
    editMenu.add(deleteMenuItem);
    editMenu.add(new JSeparator());
    upMenuItem = new JMenuItem("Move node up");
    upMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) { theJreepad.moveCurrentNodeUp(); theJreepad.returnFocusToTree(); }});
    editMenu.add(upMenuItem);
    downMenuItem = new JMenuItem("Move node down");
    downMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) { theJreepad.moveCurrentNodeDown(); theJreepad.returnFocusToTree(); }});
    editMenu.add(downMenuItem);
    editMenu.add(new JSeparator());
    indentMenuItem = new JMenuItem("Indent node (demote)");
    indentMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) { theJreepad.indentCurrentNode(); theJreepad.returnFocusToTree(); }});
    editMenu.add(indentMenuItem);
    outdentMenuItem = new JMenuItem("Outdent node (promote)");
    outdentMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) { theJreepad.outdentCurrentNode(); theJreepad.returnFocusToTree(); }});
    editMenu.add(outdentMenuItem);
    editMenu.add(new JSeparator());
    sortMenuItem = new JMenuItem("Sort children (one level)");
    sortMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) { theJreepad.sortChildren(); }});
    editMenu.add(sortMenuItem);
    sortRecursiveMenuItem = new JMenuItem("Sort children (all levels)");
    sortRecursiveMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) { theJreepad.sortChildrenRecursive(); }});
    editMenu.add(sortRecursiveMenuItem);
    //
    searchMenuItem = new JMenuItem("Search");
    searchMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) { openSearchDialog(); }});
    searchMenu.add(searchMenuItem);
    //
    viewBothMenuItem = new JMenuItem("Both tree and article");
    viewBothMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) { theJreepad.setViewBoth(); }});
    viewMenu.add(viewBothMenuItem);
    viewTreeMenuItem = new JMenuItem("Tree");
    viewTreeMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) { theJreepad.setViewTreeOnly(); }});
    viewMenu.add(viewTreeMenuItem);
    viewArticleMenuItem = new JMenuItem("Article");
    viewArticleMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) { theJreepad.setViewArticleOnly(); }});
    viewMenu.add(viewArticleMenuItem);
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
    menuBar.add(helpMenu);
    setJMenuBar(menuBar);
    //
    // Now the mnemonics...
    fileMenu.setMnemonic('F');
    newMenuItem.setMnemonic('N');
    openMenuItem.setMnemonic('O');
    saveMenuItem.setMnemonic('S');
    saveAsMenuItem.setMnemonic('A');
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
    helpMenu.setMnemonic('H');
    keyboardHelpMenuItem.setMnemonic('k');
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
/* To be removed
    JButton viewBothButton = new JButton("T+A");
    toolBar.add(viewBothButton);
    JButton viewTreeButton = new JButton("T");
    toolBar.add(viewTreeButton);
    JButton viewArticleButton = new JButton("A");
    toolBar.add(viewArticleButton);
*/
    viewSelector = new JComboBox(new String[]{"Tree+Article","Tree","Article"});
    viewSelector.setEditable(false);
    viewSelector.setSelectedIndex(0);
    viewSelector.addActionListener(new ActionListener(){
                               public void actionPerformed(ActionEvent e){ 
                               switch(viewSelector.getSelectedIndex())
                               {
                                 case 1:
                                   theJreepad.setViewTreeOnly(); break;
                                 case 2:
                                   theJreepad.setViewArticleOnly(); break;
                                 default:
                                   theJreepad.setViewBoth(); break;
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
                               public void actionPerformed(ActionEvent e){ theJreepad.moveCurrentNodeUp(); repaint();  theJreepad.returnFocusToTree();} });
    downButton.addActionListener(new ActionListener(){
                               public void actionPerformed(ActionEvent e){ theJreepad.moveCurrentNodeDown(); repaint();  theJreepad.returnFocusToTree();} });
    indentButton.addActionListener(new ActionListener(){
                               public void actionPerformed(ActionEvent e){ theJreepad.indentCurrentNode(); repaint();  theJreepad.returnFocusToTree();} });
    outdentButton.addActionListener(new ActionListener(){
                               public void actionPerformed(ActionEvent e){ theJreepad.outdentCurrentNode(); repaint(); theJreepad.returnFocusToTree(); } });
    addAboveButton.addActionListener(new ActionListener(){
                               public void actionPerformed(ActionEvent e){ theJreepad.addNodeAbove(); repaint(); /* theJreepad.returnFocusToTree(); */} });
    addBelowButton.addActionListener(new ActionListener(){
                               public void actionPerformed(ActionEvent e){ theJreepad.addNodeBelow(); repaint(); /* theJreepad.returnFocusToTree(); */} });
    addButton.addActionListener(new ActionListener(){
                               public void actionPerformed(ActionEvent e){ theJreepad.addNode(); repaint(); /* theJreepad.returnFocusToTree(); */} });
    removeButton.addActionListener(new ActionListener(){
                               public void actionPerformed(ActionEvent e){ theJreepad.removeNode(); repaint(); theJreepad.returnFocusToTree(); } });

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
    searchMaxNumSpinner = new JSpinner(new SpinnerNumberModel(200,1,1000,1));
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
                                 performSearch(nodeSearchField.getText(), articleSearchField.getText(), 
                                 searchWhereSelector.getSelectedIndex(), searchCombinatorSelector.getSelectedIndex()==0,
                                 searchCaseCheckBox.isSelected(), 
                                 (Integer.valueOf(searchMaxNumSpinner.getValue().toString())).intValue()
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

    
    content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
    content.add(toolBar);
    content.add(theJreepad);

    // Load the file - if it has been specified, and if it can be found, and if it's a valid HJT file
    if(fileNameToLoad != "")
    {
      try
      {
        openLocation = new File(fileNameToLoad);
        content.remove(theJreepad);
        theJreepad = new JreepadView(new JreepadNode(new FileInputStream(openLocation)));
        saveLocation = exportLocation = importLocation = openLocation;
        content.add(theJreepad);
        setTitleBasedOnFilename(openLocation.getName());
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

    // Finally, make the window visible and well-sized
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    setTitleBasedOnFilename("");
    Toolkit theToolkit = getToolkit();
    Dimension wndSize = theToolkit.getScreenSize();
    setBounds((int)(wndSize.width*0.2f),(int)(wndSize.height*0.2f),
              (int)(wndSize.width*0.6f),(int)(wndSize.height*0.6f));
    searchDialog.setBounds((int)(wndSize.width*0.3f),(int)(wndSize.height*0.1f),
              (int)(wndSize.width*0.4f),(int)(wndSize.height*0.5f));
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
  
  private void openAction()
  {
    try
    {
      fileChooser.setCurrentDirectory(openLocation);
      if(fileChooser.showOpenDialog(theApp) == JFileChooser.APPROVE_OPTION)
      {
        openLocation = fileChooser.getSelectedFile();
        content.remove(theJreepad);
        theJreepad = new JreepadView(new JreepadNode(new FileInputStream(openLocation)));
        saveLocation = exportLocation = importLocation = openLocation;
        content.add(theJreepad);
        setTitleBasedOnFilename(openLocation.getName());
        validate();
        repaint();
      }
    }
    catch(IOException err)
    {
      JOptionPane.showMessageDialog(theApp, err, "File input error" , JOptionPane.ERROR_MESSAGE);
    }
  } // End of: openAction()
  
  private void saveAction()
  {
    if(saveLocation==null)
    {
      saveAsAction();
      return;
    }
    try
    {
      String writeMe = theJreepad.getRootJreepadNode().toTreepadString();
      FileOutputStream fO = new FileOutputStream(saveLocation);
      DataOutputStream dO = new DataOutputStream(fO);
      dO.writeBytes(writeMe);
      dO.close();
      fO.close();
    }
    catch(IOException err)
    {
      JOptionPane.showMessageDialog(theApp, err, "File error during Save" , JOptionPane.ERROR_MESSAGE);
    }
  }
  private void saveAsAction()
  {
    try
    {
      fileChooser.setCurrentDirectory(saveLocation);
      if(fileChooser.showSaveDialog(theApp) == JFileChooser.APPROVE_OPTION)
      {
        saveLocation = fileChooser.getSelectedFile();
        String writeMe = theJreepad.getRootJreepadNode().toTreepadString();
        FileOutputStream fO = new FileOutputStream(saveLocation);
        DataOutputStream dO = new DataOutputStream(fO);
        dO.writeBytes(writeMe);
        dO.close();
        fO.close();
        setTitleBasedOnFilename(saveLocation.getName());
      }
    }
    catch(IOException err)
    {
      JOptionPane.showMessageDialog(theApp, err, "File error during Save As" , JOptionPane.ERROR_MESSAGE);
    }
  } // End of: saveAction()

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
      setTitle("Jreepad (Java Treepad Editor)");
    else
      setTitle(filename + " - Jreepad");
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
      fileChooser.setCurrentDirectory(importLocation);
      fileChooser.setSelectedFile(new File(theJreepad.getCurrentNode().getTitle()));

      if(fileChooser.showOpenDialog(theApp) == JFileChooser.APPROVE_OPTION)
      {
        importLocation = fileChooser.getSelectedFile();

		switch(importFormat)
		{
		  case FILE_FORMAT_HJT:
			theJreepad.addChild(new JreepadNode(new FileInputStream(importLocation)));
			break;
		  case FILE_FORMAT_TEXT:
		    theJreepad.addChildrenFromTextFiles(fileChooser.getSelectedFiles());
			break;
		  default:
			JOptionPane.showMessageDialog(theApp, "Unknown which format to import - coding error! Oops!", "Error" , JOptionPane.ERROR_MESSAGE);
			return;
		}

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
      fileChooser.setCurrentDirectory(exportLocation);
      fileChooser.setSelectedFile(new File(theJreepad.getCurrentNode().getTitle()));
      if(fileChooser.showSaveDialog(theApp) == JFileChooser.APPROVE_OPTION)
      {
        exportLocation = fileChooser.getSelectedFile();

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

        FileOutputStream fO = new FileOutputStream(exportLocation);
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


}