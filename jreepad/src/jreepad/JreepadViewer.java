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

package jreepad;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

public class JreepadViewer extends JFrame
{
  private static JreepadViewer theApp;
  private Box toolBar;
  private JreepadView theJreepad;
  private Container content;
  private static final File prefsFile = new File(System.getProperty("user.home"), ".jreepref");

//  private static final String[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames(null);  
//  private static final String[] fontSizes = new String[] {"8","9","10","11","12","13","14","16","18","20","24","30","36"};

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
  private JComboBox fileEncodingSelector;
//  private Box fontsPrefsBox;
//    private JComboBox treeFontFamilySelector;
//    private JComboBox treeFontSizeSelector;
//    private JComboBox articleFontFamilySelector;
//    private JComboBox articleFontSizeSelector;
  private Box webSearchPrefsBox;
    private JComboBox defaultSearchModeSelector;
    private JTextField webSearchNameField;
    private JTextField webSearchPrefixField;
    private JTextField webSearchPostfixField;
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

  private JDialog nodeUrlDisplayDialog;
  private JTextField nodeUrlDisplayField;
  private JButton nodeUrlDisplayOkButton;
  
  private JMenuBar menuBar;
  private JMenu fileMenu;
  private JMenuItem newMenuItem;
  private JMenuItem openMenuItem;
    private JMenu openRecentMenu;
  private JMenuItem saveMenuItem;
  private JMenuItem saveAsMenuItem;
  private JMenuItem backupToMenuItem;
    private JMenu importMenu;
    private JMenuItem importHjtMenuItem;
    private JMenuItem importTextMenuItem;
    private JMenuItem importTextAsListMenuItem;
    private JMenu exportMenu;
    private JMenuItem exportHjtMenuItem;
    private JMenuItem exportHtmlMenuItem;
    private JMenuItem exportSimpleXmlMenuItem;
    private JMenuItem exportListMenuItem;
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
  private JMenuItem webSearchMenuItem;
  private JMenuItem launchUrlMenuItem;
  private JMenuItem thisNodesUrlMenuItem;
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
  private JMenuItem linksHelpMenuItem;
  private JMenuItem dragDropHelpMenuItem;
  private JMenuItem aboutMenuItem;
  private JMenuItem licenseMenuItem;
  
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
      {
        showLicense(); // A very crude way of showing the license on first visit
        setPrefs(new JreepadPrefs());
      }
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
    searchMenu = new JMenu("Actions");
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
      openRecentMenu = new JMenu("Open recent");
      updateOpenRecentMenu();
      fileMenu.add(openRecentMenu);
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
      importTextAsListMenuItem = new JMenuItem("...text list file, one-child-per-line");
      importTextAsListMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) {importAction(FILE_FORMAT_TEXTASLIST);}});
      importMenu.add(importTextAsListMenuItem);
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
      exportListMenuItem = new JMenuItem("...subtree to text list (node titles only)");
      exportListMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) {exportAction(FILE_FORMAT_TEXTASLIST);}});
      exportMenu.add(exportListMenuItem);
      exportMenu.add(new JSeparator());
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
    addAboveMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) { theJreepad.addNodeAbove(); theJreepad.returnFocusToTree(); setWarnAboutUnsaved(true); updateWindowTitle();}});
    editMenu.add(addAboveMenuItem);
    addBelowMenuItem = new JMenuItem("Add sibling below");
    addBelowMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) { theJreepad.addNodeBelow(); theJreepad.returnFocusToTree(); setWarnAboutUnsaved(true); updateWindowTitle();}});
    editMenu.add(addBelowMenuItem);
    addChildMenuItem = new JMenuItem("Add child");
    addChildMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) { theJreepad.addNode(); theJreepad.returnFocusToTree(); setWarnAboutUnsaved(true);updateWindowTitle(); }});
    editMenu.add(addChildMenuItem);
    editMenu.add(new JSeparator());
    deleteMenuItem = new JMenuItem("Delete node");
    deleteMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) { deleteNodeAction(); }});
    editMenu.add(deleteMenuItem);
    editMenu.add(new JSeparator());
    upMenuItem = new JMenuItem("Move node up");
    upMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) { theJreepad.moveCurrentNodeUp(); theJreepad.returnFocusToTree(); setWarnAboutUnsaved(true);updateWindowTitle(); }});
    editMenu.add(upMenuItem);
    downMenuItem = new JMenuItem("Move node down");
    downMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) { theJreepad.moveCurrentNodeDown(); theJreepad.returnFocusToTree(); setWarnAboutUnsaved(true);updateWindowTitle(); }});
    editMenu.add(downMenuItem);
    editMenu.add(new JSeparator());
    indentMenuItem = new JMenuItem("Indent node (demote)");
    indentMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) { theJreepad.indentCurrentNode(); theJreepad.returnFocusToTree(); setWarnAboutUnsaved(true);updateWindowTitle(); }});
    editMenu.add(indentMenuItem);
    outdentMenuItem = new JMenuItem("Outdent node (promote)");
    outdentMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) { theJreepad.outdentCurrentNode(); theJreepad.returnFocusToTree(); setWarnAboutUnsaved(true);updateWindowTitle(); }});
    editMenu.add(outdentMenuItem);
    editMenu.add(new JSeparator());
    sortMenuItem = new JMenuItem("Sort children (one level)");
    sortMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) { theJreepad.sortChildren(); setWarnAboutUnsaved(true);updateWindowTitle(); }});
    editMenu.add(sortMenuItem);
    sortRecursiveMenuItem = new JMenuItem("Sort children (all levels)");
    sortRecursiveMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) { theJreepad.sortChildrenRecursive(); setWarnAboutUnsaved(true);updateWindowTitle(); }});
    editMenu.add(sortRecursiveMenuItem);
    //
    searchMenuItem = new JMenuItem("Search");
    searchMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) { openSearchDialog(); }});
    searchMenu.add(searchMenuItem);
    launchUrlMenuItem = new JMenuItem("Follow highlighted link");
    launchUrlMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) { theJreepad.openURLSelectedInArticle(); }});
    searchMenu.add(launchUrlMenuItem);
    webSearchMenuItem = new JMenuItem(getPrefs().webSearchName);
    webSearchMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) { theJreepad.webSearchTextSelectedInArticle(); }});
    searchMenu.add(webSearchMenuItem);
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
    viewMenu.add(new JSeparator());
    thisNodesUrlMenuItem = new JMenuItem("\"node://\" address for current node");
    thisNodesUrlMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) { getTreepadNodeUrl(); }});
    viewMenu.add(thisNodesUrlMenuItem);
    //
    autoSaveMenuItem = new JMenuItem("Autosave...");
    autoSaveMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) { showAutoSaveDialog(); }});
    optionsMenu.add(autoSaveMenuItem);
    prefsMenuItem = new JMenuItem("Preferences");
    prefsMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) { 
                                            // updateFontsInPrefsBox(); 
                                            showPrefsDialog(); }});
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
    linksHelpMenuItem = new JMenuItem("Help with links");
    linksHelpMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e)
    		{
              JOptionPane.showMessageDialog(theApp, 
              "\nSelect any piece of text in an article," +
              "\nthen choose \"Actions > Follow link in article\"." +
              "\n" +
              "\nTYPES OF LINK:" +
              "\n" +
              "\n\"Normal\" link - We've tested these types of link:" +
              "\n      Web:   e.g. http://jreepad.sourceforge.net" +
              "\n      Email: e.g. mailto:billg@microsoft.com" +
              "\n      FTP:   e.g. ftp://ftp.compaq.com/pub/" +
              "\n      File:  (can't get these to work, on OSX at least)" +
              "\n" +
              "\nWiki link - If the selected text is a WikiWord (i.e. if " +
              "\n            it LooksLikeThis with no spaces and some capital " + 
              "\n            letters somewhere in the middle) OR begins " +
              "\n            with \"[[\" and ends with \"]]\" then " +
              "\n            Jreepad will search for a node of the same " +
              "\n            title, and jump directly to it. If one " +
              "\n            isn't found then it'll create one " +
              "\n            for you. Try it!" +
              "\n" + 
              "\nTreepad link - Treepad Lite uses links which begin " +
              "\n            with \"node://\", and specify the exact path" +
              "\n            to a different node within the same file."+
              "\n              e.g. \"node://TreePad manual/Using Treepad\"" +
              "\n" +
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
              "Links in Jreepad", 
              JOptionPane.INFORMATION_MESSAGE); 
    		}});
    helpMenu.add(linksHelpMenuItem);
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
              "a Windows program called \"Treepad Lite\",\n" +
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
    helpMenu.add(new JSeparator());
    helpMenu.add(aboutMenuItem);
    licenseMenuItem = new JMenuItem("License");
    licenseMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e)
            {
              showLicense();
            }});
    helpMenu.add(licenseMenuItem);
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
    newMenuItem.setAccelerator(KeyStroke.getKeyStroke('N', Event.META_MASK));
    openMenuItem.setMnemonic('O');
    openMenuItem.setAccelerator(KeyStroke.getKeyStroke('O', Event.META_MASK));
    openRecentMenu.setMnemonic('R');
    saveMenuItem.setMnemonic('S');
    saveMenuItem.setAccelerator(KeyStroke.getKeyStroke('S', Event.META_MASK));
    saveAsMenuItem.setMnemonic('A');
    backupToMenuItem.setMnemonic('B');
    importMenu.setMnemonic('I');
    importHjtMenuItem.setMnemonic('f');
    importTextMenuItem.setMnemonic('t');
    importTextAsListMenuItem.setMnemonic('l');
    exportMenu.setMnemonic('E');
    exportHjtMenuItem.setMnemonic('f');
    exportHtmlMenuItem.setMnemonic('h');
    exportSimpleXmlMenuItem.setMnemonic('x');
    exportTextMenuItem.setMnemonic('t');
    quitMenuItem.setMnemonic('Q');
    quitMenuItem.setAccelerator(KeyStroke.getKeyStroke('Q', Event.META_MASK));
    editMenu.setMnemonic('E');
    undoMenuItem.setMnemonic('u');
    undoMenuItem.setAccelerator(KeyStroke.getKeyStroke('Z', Event.META_MASK));
    addAboveMenuItem.setMnemonic('a');
    addAboveMenuItem.setAccelerator(KeyStroke.getKeyStroke('A', Event.META_MASK));
    addBelowMenuItem.setMnemonic('b');
    addBelowMenuItem.setAccelerator(KeyStroke.getKeyStroke('B', Event.META_MASK));
    addChildMenuItem.setMnemonic('c');
    addChildMenuItem.setAccelerator(KeyStroke.getKeyStroke('\\', Event.META_MASK));
    upMenuItem.setMnemonic('u');
    upMenuItem.setAccelerator(KeyStroke.getKeyStroke('U', Event.META_MASK));
    downMenuItem.setMnemonic('d');
    downMenuItem.setAccelerator(KeyStroke.getKeyStroke('D', Event.META_MASK));
    indentMenuItem.setMnemonic('i');
    indentMenuItem.setAccelerator(KeyStroke.getKeyStroke(']', Event.META_MASK));
    outdentMenuItem.setMnemonic('o');
    outdentMenuItem.setAccelerator(KeyStroke.getKeyStroke('[', Event.META_MASK));
    deleteMenuItem.setMnemonic('k');
    deleteMenuItem.setAccelerator(KeyStroke.getKeyStroke('K', Event.META_MASK));
    searchMenu.setMnemonic('t');
    searchMenuItem.setMnemonic('s');
    searchMenuItem.setAccelerator(KeyStroke.getKeyStroke('F', Event.META_MASK));
    webSearchMenuItem.setMnemonic('g');
    webSearchMenuItem.setAccelerator(KeyStroke.getKeyStroke('G', Event.META_MASK));
    launchUrlMenuItem.setAccelerator(KeyStroke.getKeyStroke('L', Event.META_MASK));
    launchUrlMenuItem.setMnemonic('l');
    thisNodesUrlMenuItem.setMnemonic('n');
    viewMenu.setMnemonic('V');
    viewBothMenuItem.setMnemonic('b');
    viewTreeMenuItem.setMnemonic('t');
    viewArticleMenuItem.setMnemonic('a');
    viewBothMenuItem.setAccelerator(KeyStroke.getKeyStroke('1', Event.META_MASK));
    viewTreeMenuItem.setAccelerator(KeyStroke.getKeyStroke('2', Event.META_MASK));
    viewArticleMenuItem.setAccelerator(KeyStroke.getKeyStroke('3', Event.META_MASK));
    viewToolbarMenuItem.setAccelerator(KeyStroke.getKeyStroke('4', Event.META_MASK));
    viewToolbarMenuItem.setMnemonic('o');
    optionsMenu.setMnemonic('O');
    autoSaveMenuItem.setMnemonic('a');
    prefsMenuItem.setMnemonic('p');
    helpMenu.setMnemonic('H');
    keyboardHelpMenuItem.setMnemonic('k');
    dragDropHelpMenuItem.setMnemonic('d');
    linksHelpMenuItem.setMnemonic('l');
    aboutMenuItem.setMnemonic('a');
    licenseMenuItem.setMnemonic('i');
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
                               public void actionPerformed(ActionEvent e){ theJreepad.moveCurrentNodeUp(); repaint();  theJreepad.returnFocusToTree(); setWarnAboutUnsaved(true);updateWindowTitle();} });
    downButton.addActionListener(new ActionListener(){
                               public void actionPerformed(ActionEvent e){ theJreepad.moveCurrentNodeDown(); repaint();  theJreepad.returnFocusToTree(); setWarnAboutUnsaved(true);updateWindowTitle();} });
    indentButton.addActionListener(new ActionListener(){
                               public void actionPerformed(ActionEvent e){ theJreepad.indentCurrentNode(); repaint();  theJreepad.returnFocusToTree(); setWarnAboutUnsaved(true);updateWindowTitle();} });
    outdentButton.addActionListener(new ActionListener(){
                               public void actionPerformed(ActionEvent e){ theJreepad.outdentCurrentNode(); repaint(); theJreepad.returnFocusToTree(); setWarnAboutUnsaved(true);updateWindowTitle(); } });
    addAboveButton.addActionListener(new ActionListener(){
                               public void actionPerformed(ActionEvent e){ theJreepad.addNodeAbove(); repaint(); /* theJreepad.returnFocusToTree(); */ setWarnAboutUnsaved(true);updateWindowTitle();} });
    addBelowButton.addActionListener(new ActionListener(){
                               public void actionPerformed(ActionEvent e){ theJreepad.addNodeBelow(); repaint(); /* theJreepad.returnFocusToTree(); */ setWarnAboutUnsaved(true);updateWindowTitle();} });
    addButton.addActionListener(new ActionListener(){
                               public void actionPerformed(ActionEvent e){ theJreepad.addNode(); repaint(); /* theJreepad.returnFocusToTree(); */ setWarnAboutUnsaved(true);updateWindowTitle();} });
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
    Box genPrefVBox = Box.createVerticalBox();
    genPrefVBox.add(loadLastFileOnOpenCheckBox = new JCheckBox("When Jreepad starts, automatically load the last-saved file", getPrefs().loadLastFileOnOpen));
    genPrefVBox.add(autoDateNodesCheckBox = new JCheckBox("Autodate nodes: whenever a new node is created, add the date into its article", getPrefs().autoDateInArticles));

    genPrefVBox.add(new JLabel("Character encoding for load/save:"));
    genPrefVBox.add(fileEncodingSelector = new JComboBox(getPrefs().characterEncodings));
    fileEncodingSelector.setSelectedIndex(getPrefs().fileEncoding);

    JPanel genPanel = new JPanel();
    genPanel.add(genPrefVBox);
    genPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "General"));
    vBox.add(genPanel);

    genPanel = new JPanel();
    hBox = Box.createHorizontalBox();
    hBox.add(new JLabel("Default action to take with ordinary words/phrases:"));
    hBox.add(defaultSearchModeSelector = new JComboBox(new String[]{"Web search","Search for node title"}));
    defaultSearchModeSelector.setSelectedIndex(getPrefs().defaultSearchMode);
    genPanel.add(hBox);
    genPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "\"Follow selected link\" action"));
    vBox.add(genPanel);

//    fontsPrefsBox = Box.createHorizontalBox();
//    fontsPrefsBox.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Font (for article)"));
//    Box tempVBox = Box.createHorizontalBox();
//    tempVBox.add(new JLabel("Font for tree:"));
//    tempVBox.add(treeFontFamilySelector = new JComboBox(fonts));
//    fontsPrefsBox.add(tempVBox);
//    tempVBox = Box.createHorizontalBox();
//    fontsPrefsBox.add(new JLabel("Font face:"));
//    fontsPrefsBox.add(articleFontFamilySelector = new JComboBox(fonts));
//    fontsPrefsBox.add(tempVBox);
//    tempVBox = Box.createHorizontalBox();
//    tempVBox.add(new JLabel("Font size:"));
//    fontsPrefsBox.add(articleFontSizeSelector = new JComboBox(fontSizes));
//    fontsPrefsBox.add(new JLabel("pt"));
 //   fontsPrefsBox.add(tempVBox);
 //   vBox.add(fontsPrefsBox);

    webSearchPrefsBox = Box.createVerticalBox();
    hBox = Box.createHorizontalBox();
    hBox.add(new JLabel("Web search is labelled \""));
    hBox.add(webSearchNameField = new JTextField(getPrefs().webSearchName));
    hBox.add(new JLabel("\" and calls the following URL:"));
    webSearchPrefsBox.add(hBox);
    hBox = Box.createHorizontalBox();
    hBox.add(new JLabel("http://"));
    hBox.add(webSearchPrefixField = new JTextField(getPrefs().webSearchPrefix));
    hBox.add(new JLabel("[SELECTED TEXT]"));
    hBox.add(webSearchPostfixField = new JTextField(getPrefs().webSearchPostfix));
    webSearchPrefsBox.add(hBox);
    JPanel webSearchPanel = new JPanel();
    webSearchPanel.add(webSearchPrefsBox);
    webSearchPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Web search"));
    vBox.add(webSearchPanel);
    
    hBox = Box.createHorizontalBox();
    hBox.add(prefsOkButton = new JButton("OK"));
    hBox.add(prefsCancelButton = new JButton("Cancel"));
    prefsOkButton.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e){
									getPrefs().loadLastFileOnOpen = loadLastFileOnOpenCheckBox.isSelected();
									getPrefs().autoDateInArticles = autoDateNodesCheckBox.isSelected();
									webSearchMenuItem.setText(getPrefs().webSearchName = webSearchNameField.getText());
									getPrefs().webSearchPrefix = webSearchPrefixField.getText();
									getPrefs().webSearchPostfix = webSearchPostfixField.getText();
									getPrefs().defaultSearchMode = defaultSearchModeSelector.getSelectedIndex();
									getPrefs().fileEncoding = fileEncodingSelector.getSelectedIndex();
							//		setFontsFromPrefsBox();
									prefsDialog.hide();
                                   }});
    prefsCancelButton.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e){prefsDialog.hide();}});
    vBox.add(hBox);
    prefsDialog.getContentPane().add(vBox);
    // Finished establishing the prefs dialogue box

    // Establish the nodeUrlDisplay dialogue box
    nodeUrlDisplayDialog = new JDialog(theApp, "Node URL", true);
    nodeUrlDisplayDialog.setVisible(false);
    vBox = Box.createVerticalBox();
    vBox.add(new JLabel("Current node's address:"));
    vBox.add(nodeUrlDisplayField = new JTextField("node://its/a/secret"));
    vBox.add(new JLabel("(You can copy-and-paste this into an article)"));
    vBox.add(nodeUrlDisplayOkButton = new JButton("OK"));
    nodeUrlDisplayOkButton.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e){
									nodeUrlDisplayDialog.hide();
                                   }});
    nodeUrlDisplayDialog.getContentPane().add(vBox);
    // Finished: Establish the nodeUrlDisplay dialogue box
    
    content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
    content.add(toolBar);
    content.add(theJreepad);

    // Load the file - if it has been specified, and if it can be found, and if it's a valid HJT file
    File firstTimeFile = null;
    if(fileNameToLoad != "")
      firstTimeFile = new File(fileNameToLoad);
    else if(getPrefs().loadLastFileOnOpen && getPrefs().saveLocation != null)
      firstTimeFile = getPrefs().saveLocation;

    if(firstTimeFile != null && firstTimeFile.isFile())
    {
      try
      {
        getPrefs().openLocation = firstTimeFile;
        content.remove(theJreepad);
        theJreepad = new JreepadView(new JreepadNode(new InputStreamReader(new FileInputStream(getPrefs().openLocation), getPrefs().getEncoding())));
        getPrefs().saveLocation = getPrefs().exportLocation = getPrefs().importLocation = getPrefs().openLocation;
        content.add(theJreepad);
	    getPrefs().saveLocation = getPrefs().openLocation;
        setTitleBasedOnFilename(getPrefs().openLocation.getName());
        setWarnAboutUnsaved(false);
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
    int chosenWidth = (int)(wndSize.getWidth() * 0.6f);
    int chosenHeight = (int)(wndSize.getHeight() * 0.6f);

    // This bit attempts to ensure that the Jreepad view doesn't get too wide 
    //   (e.g. for people with dual-screen systems)
    //   - it limits the width/height proportion to the golden ratio!
    // Can't seem to find anything in the Toolkit which would automatically give us multi-screen info
    if(chosenWidth > (int)(((float)chosenHeight)*1.618034f) )
      chosenWidth = (int)(((float)chosenHeight)*1.618034f);

    setBounds(chosenWidth/3,chosenHeight/3,
              chosenWidth, chosenHeight);
    searchDialog.setBounds(chosenWidth/2,chosenHeight/6,
              (int)(chosenWidth*0.7f),(int)(chosenHeight*0.9f));
    autoSaveDialog.setBounds((int)(wndSize.width*0.5f),chosenHeight/2,
              chosenWidth/2, chosenHeight/4);
    prefsDialog.setBounds(chosenWidth/2,chosenHeight/3,
              chosenWidth, chosenHeight);
    nodeUrlDisplayDialog.setBounds((int)(wndSize.width*0.1f),(int)(chosenHeight*0.7f),
              (int)(chosenWidth*1.3f), chosenHeight/3);
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
    if(warnAboutUnsaved())
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
	setWarnAboutUnsaved(false);
//	theJreepad.clearUndoCache();
  }
  
  private void openAction()
  {
    if(warnAboutUnsaved())
    {
	  int answer = JOptionPane.showConfirmDialog(theApp, "Save current file before opening a new one?", 
	                   "Save?" , JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
      if(answer == JOptionPane.CANCEL_OPTION)
        return;
      else if(answer == JOptionPane.YES_OPTION)
        if(!saveAction())
          return; // This cancels quit if the save action failed or was cancelled
    }

    fileChooser.setCurrentDirectory(getPrefs().openLocation);
    if(fileChooser.showOpenDialog(theApp) == JFileChooser.APPROVE_OPTION)
    {
      openHjtFile(fileChooser.getSelectedFile());
    }
  } // End of: openAction()
  private void openHjtFile(File f)
  {
      try
      {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        getPrefs().openLocation = f;
        content.remove(theJreepad);
        theJreepad = new JreepadView(new JreepadNode(new InputStreamReader(new FileInputStream(getPrefs().openLocation), getPrefs().getEncoding())));
        getPrefs().saveLocation = getPrefs().openLocation;
        content.add(theJreepad);
        addToOpenRecentMenu(getPrefs().openLocation);
        setTitleBasedOnFilename(getPrefs().openLocation.getName());
        validate();
        repaint();
        setWarnAboutUnsaved(false);
        theJreepad.clearUndoCache();
        setCursor(Cursor.getDefaultCursor());
      }
      catch(IOException err)
      {
        setCursor(Cursor.getDefaultCursor());
        JOptionPane.showMessageDialog(theApp, err, "File input error" , JOptionPane.ERROR_MESSAGE);
      }
  } // End of: openHjtFile()
  
  
  private boolean saveAction()
  {
    if(getPrefs().saveLocation==null)
    {
      return saveAsAction();
    }
    try
    {
      setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      String writeMe = theJreepad.getRootJreepadNode().toTreepadString();
      FileOutputStream fO = new FileOutputStream(getPrefs().saveLocation);
      DataOutputStream dO = new DataOutputStream(fO);
      BufferedWriter bO = new BufferedWriter(new OutputStreamWriter(dO, getPrefs().getEncoding()));
      bO.write(writeMe);
      bO.close();
      dO.close();
      fO.close();
      setWarnAboutUnsaved(false);
      updateWindowTitle();
      setCursor(Cursor.getDefaultCursor());
      return true;
    }
    catch(IOException err)
    {
      setCursor(Cursor.getDefaultCursor());
      JOptionPane.showMessageDialog(theApp, err, "File error during Save" , JOptionPane.ERROR_MESSAGE);
    }
    return false;
  }
  private boolean saveAsAction()
  {
    try
    {
      fileChooser.setCurrentDirectory(getPrefs().saveLocation);
      if(fileChooser.showSaveDialog(theApp) == JFileChooser.APPROVE_OPTION && checkOverwrite(fileChooser.getSelectedFile()))
      {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        getPrefs().saveLocation = fileChooser.getSelectedFile();
        String writeMe = theJreepad.getRootJreepadNode().toTreepadString();
        FileOutputStream fO = new FileOutputStream(getPrefs().saveLocation);
        DataOutputStream dO = new DataOutputStream(fO);
        BufferedWriter bO = new BufferedWriter(new OutputStreamWriter(dO, getPrefs().getEncoding()));
        bO.write(writeMe);
        bO.close();
        dO.close();
        fO.close();
        setWarnAboutUnsaved(false);
        setTitleBasedOnFilename(getPrefs().saveLocation.getName());
        setCursor(Cursor.getDefaultCursor());
        return true;
      }
      else
        return false;
    }
    catch(IOException err)
    {
      setCursor(Cursor.getDefaultCursor());
      JOptionPane.showMessageDialog(theApp, err, "File error during Save As" , JOptionPane.ERROR_MESSAGE);
    }
    return false;
  } // End of: saveAsAction()

  private boolean backupToAction()
  {
    try
    {
      fileChooser.setCurrentDirectory(getPrefs().backupLocation);
      if(fileChooser.showSaveDialog(theApp) == JFileChooser.APPROVE_OPTION && checkOverwrite(fileChooser.getSelectedFile()))
      {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        getPrefs().backupLocation = fileChooser.getSelectedFile();
        String writeMe = theJreepad.getRootJreepadNode().toTreepadString();
        FileOutputStream fO = new FileOutputStream(getPrefs().backupLocation);
        DataOutputStream dO = new DataOutputStream(fO);
        dO.writeBytes(writeMe);
        dO.close();
        fO.close();
        setCursor(Cursor.getDefaultCursor());
        return true;
      }
      else
        return false;
    }
    catch(IOException err)
    {
      setCursor(Cursor.getDefaultCursor());
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
    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    boolean ret = theJreepad.performSearch(inNodes, inArticles, searchWhat, orNotAnd, 
                                          caseSensitive, maxResults);
    setCursor(Cursor.getDefaultCursor());
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
    setTitle(windowTitle + (warnAboutUnsaved()?"*":"") + (getPrefs().autoSave?" [Autosave on]":""));
  }
  
  private static final int FILE_FORMAT_HJT=1;
  private static final int FILE_FORMAT_HTML=2;
  private static final int FILE_FORMAT_XML=3;
  private static final int FILE_FORMAT_TEXT=4;
  private static final int FILE_FORMAT_TEXTASLIST=5;
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
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        getPrefs().importLocation = fileChooser.getSelectedFile();

		switch(importFormat)
		{
		  case FILE_FORMAT_HJT:
			theJreepad.addChild(new JreepadNode(new InputStreamReader(new FileInputStream(getPrefs().importLocation), getPrefs().getEncoding())));
			break;
		  case FILE_FORMAT_TEXT:
		    theJreepad.addChildrenFromTextFiles(fileChooser.getSelectedFiles());
			break;
		  case FILE_FORMAT_TEXTASLIST:
		    theJreepad.addChildrenFromListTextFile(getPrefs().importLocation);
			break;
		  default:
            setCursor(Cursor.getDefaultCursor());
			JOptionPane.showMessageDialog(theApp, "Unknown which format to import - coding error! Oops!", "Error" , JOptionPane.ERROR_MESSAGE);
			return;
		}
	    setWarnAboutUnsaved(true);
	    updateWindowTitle();
      }
      fileChooser.setMultiSelectionEnabled(false);
      setCursor(Cursor.getDefaultCursor());
    }
    catch(IOException err)
    {
      setCursor(Cursor.getDefaultCursor());
      JOptionPane.showMessageDialog(theApp, err, "File error during Import" , JOptionPane.ERROR_MESSAGE);
    }
  } // End of: importAction()

  private void exportAction(int exportFormat)
  {
    try
    {
      fileChooser.setCurrentDirectory(getPrefs().exportLocation);
      fileChooser.setSelectedFile(new File(theJreepad.getCurrentNode().getTitle()));
      if(fileChooser.showSaveDialog(theApp) == JFileChooser.APPROVE_OPTION && checkOverwrite(fileChooser.getSelectedFile()))
      {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
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
		  case FILE_FORMAT_TEXTASLIST:
			output = theJreepad.getCurrentNode().exportTitlesAsList();
			break;
		  default:
            setCursor(Cursor.getDefaultCursor());
			JOptionPane.showMessageDialog(theApp, "Unknown which format to export - coding error! Oops!", "Error" , JOptionPane.ERROR_MESSAGE);
			return;
		}

        FileOutputStream fO = new FileOutputStream(getPrefs().exportLocation);
        DataOutputStream dO = new DataOutputStream(fO);
        dO.writeBytes(output);
        dO.close();
        fO.close();
        setCursor(Cursor.getDefaultCursor());
      }
    }
    catch(IOException err)
    {
      setCursor(Cursor.getDefaultCursor());
      JOptionPane.showMessageDialog(theApp, err, "File error during Export" , JOptionPane.ERROR_MESSAGE);
    }
  } // End of: exportAction()


  public void quitAction()
  {
    // We need to check about warning-if-unsaved!
    if(warnAboutUnsaved())
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
    setWarnAboutUnsaved(true);
    updateWindowTitle();
  }

  private void getTreepadNodeUrl()
  {
//    String ret = theJreepad.getTreepadNodeUrl();
    nodeUrlDisplayField.setText(theJreepad.getTreepadNodeUrl());
	nodeUrlDisplayDialog.show();
  }

  private boolean checkOverwrite(File theFile)
  {
    // If file doesn't already exist then fine
    if(!theFile.isFile()) return true;
    // Else we need to confirm
    return (JOptionPane.showConfirmDialog(theApp, "The file "+theFile.getName()+" already exists.\nAre you sure you want to overwrite it?", 
                "Overwrite file?", 
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION); 
  }

  public void showLicense()
  {
              JOptionPane.showMessageDialog(theApp, 
"           Jreepad - personal information manager.\n" +
"           Copyright (C) 2004 Dan Stowell\n" +
"\n" +
"This program is free software; you can redistribute it and/or\n" +
"modify it under the terms of the GNU General Public License\n" +
"as published by the Free Software Foundation; either version 2\n" +
"of the License, or (at your option) any later version.\n" +
"\n" +
"This program is distributed in the hope that it will be useful,\n" +
"but WITHOUT ANY WARRANTY; without even the implied warranty of\n" +
"MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\n" +
"GNU General Public License for more details.\n" +
"\n" +
"The full license can be read online here:\n" +
"\n" +
"           http://www.gnu.org/copyleft/gpl.html\n" +
             "\n"
              ,
              "Licence", 
              JOptionPane.INFORMATION_MESSAGE); 
  }
  
  private boolean warnAboutUnsaved()
  {
    return theJreepad.warnAboutUnsaved();
  }
  private void setWarnAboutUnsaved(boolean yo)
  {
    theJreepad.setWarnAboutUnsaved(yo);
  }


  private void addToOpenRecentMenu(File f)
  {
    // Remove the file from the list if it's already in there...
    ListIterator iter = getPrefs().openRecentList.listIterator();
    File tempFile;
    while(iter.hasNext())
    {
      tempFile = (File)iter.next();
      if(tempFile == null || tempFile.equals(f))
        iter.remove();
    }
    
    getPrefs().openRecentList.insertElementAt(f, 0);
    updateOpenRecentMenu();
  }
  private File[] openRecentTempFileList;
  private void updateOpenRecentMenu()
  {
    if(getPrefs().openRecentList.size() > getPrefs().openRecentListLength)
      getPrefs().openRecentList.setSize(getPrefs().openRecentListLength);

    try
    {
//      openRecentTempFileList = (File[])getPrefs().openRecentList.toArray();
      openRecentTempFileList = new File[getPrefs().openRecentList.size()];
      for(int i=0; i<getPrefs().openRecentList.size(); i++)
      {
        openRecentTempFileList[i] = (File)getPrefs().openRecentList.get(i);
      }
    }
    catch(ClassCastException e)
    {
      System.err.println(e);
      openRecentTempFileList = new File[0];
    }

    openRecentMenu.setEnabled(getPrefs().openRecentList.size()>1);

    openRecentMenu.removeAll();
    
    JMenuItem tempMenuItem;
    File tempFile;
    char theChar;
    for(int i=1; i<openRecentTempFileList.length; i++)
    {
      tempFile = openRecentTempFileList[i];
      tempMenuItem = new JMenuItem(tempFile.getParentFile().getName() + "/" + tempFile.getName());
      if(i<10)
      {
        theChar = ("" + i).charAt(0);
        tempMenuItem.setText("("+theChar+") "+tempMenuItem.getText());
        tempMenuItem.setMnemonic(theChar);
      }
      // ADD THE ACTIONLISTENER HERE
      tempMenuItem.addActionListener(new FileOpeningActionListener(tempFile));
      openRecentMenu.add(tempMenuItem); 
    }
  }

  private class FileOpeningActionListener implements ActionListener
  {
    File f;
    FileOpeningActionListener(File f) 		   {this.f = f;}
    public void actionPerformed(ActionEvent e) {openHjtFile(f);}
  } // End of:   private class FileOpeningActionListener extends ActionListener


//  private void updateFontsInPrefsBox()
//  {
//    String treeFontName = getPrefs().treeFont.getFamily();
//    String articleFontName = getPrefs().articleFont.getFamily();
//    String treeFontSize = "" + getPrefs().treeFont.getSize();
//    String articleFontSize = "" + getPrefs().articleFont.getSize();
    
//    int i;
//    for(i=0; i<fonts.length; i++)
//    {
//      if(treeFontName.equals(fonts[i]))
//        treeFontFamilySelector.setSelectedIndex(i);
//      if(articleFontName.equals(fonts[i]))
//      {
//        articleFontFamilySelector.setSelectedIndex(i);
//        break;
//      }
//    }
//    for(i=0; i<fontSizes.length; i++)
//    {
//      if(treeFontSize.equals(fontSizes[i]))
//        treeFontSizeSelector.setSelectedIndex(i);
//      if(articleFontSize.equals(fontSizes[i]))
//      {
//        articleFontSizeSelector.setSelectedIndex(i);
//        break;
//      }
//    }
//  }
  
//  private void setFontsFromPrefsBox()
//  {
//    getPrefs().treeFont = new Font((String)treeFontFamilySelector.getSelectedItem(), Font.PLAIN, 
//                   getPrefs().treeFont.getSize() );
//    getPrefs().articleFont = new Font((String)articleFontFamilySelector.getSelectedItem(), Font.PLAIN, 
//                   (Integer.valueOf((String)articleFontSizeSelector.getSelectedItem())).intValue()  );
//    theJreepad.setTreeFont(getPrefs().treeFont);
//    theJreepad.setArticleFont(getPrefs().articleFont);
//  }

}