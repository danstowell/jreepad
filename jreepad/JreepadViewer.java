package jreepad;

import javax.swing.*;
import javax.swing.event.*;
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
  private JFileChooser fileChooser;
  
  private JComboBox viewSelector;
  
  private JMenuBar menuBar;
  private JMenu fileMenu;
  private JMenuItem newMenuItem;
  private JMenuItem openMenuItem;
  private JMenuItem saveMenuItem;
  private JMenuItem saveAsMenuItem;
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
              "Jreepad is an open-source Java clone of \n" +
              "a Windows program called \"Treepad Lite\", \n" +
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
        saveLocation = openLocation;
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
    setBounds((int)(wndSize.width*0.2f),(int)(wndSize.width*0.2f),
              (int)(wndSize.width*0.6f),(int)(wndSize.height*0.6f));
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
        saveLocation = openLocation;
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

  private void setTitleBasedOnFilename(String filename)
  {
    if(filename=="")
      setTitle("Jreepad (Java Treepad Editor)");
    else
      setTitle(filename + " - Jreepad");
  }
}