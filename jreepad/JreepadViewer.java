package jreepad;

import javax.swing.*;
import javax.swing.event.*;
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
  private File saveLocation = new File("/Users/dan/javaTestArea/Jreepad/");
  private JFileChooser fileChooser;
  
  private JComboBox viewSelector;
  
  private JMenuBar menuBar;
  private JMenu fileMenu;
  private JMenuItem newMenuItem;
  private JMenuItem openMenuItem;
  private JMenuItem saveMenuItem;
  private JMenuItem quitMenuItem;
  private JMenu editMenu;
  private JMenuItem addAboveMenuItem;
  private JMenuItem addBelowMenuItem;
  private JMenuItem addChildMenuItem;
  private JMenuItem deleteMenuItem;
  private JMenuItem upMenuItem;
  private JMenuItem downMenuItem;
  private JMenu viewMenu;
  private JMenuItem viewBothMenuItem;
  private JMenuItem viewTreeMenuItem;
  private JMenuItem viewArticleMenuItem;
  
  public JreepadViewer()
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
    quitMenuItem = new JMenuItem("Quit");
    quitMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) { System.exit(0); }});
    fileMenu.add(quitMenuItem);
    //
    addAboveMenuItem = new JMenuItem("Add sibling above");
    addAboveMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) { theJreepad.addNodeAbove(); }});
    editMenu.add(addAboveMenuItem);
    addBelowMenuItem = new JMenuItem("Add sibling below");
    addBelowMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) { theJreepad.addNodeBelow(); }});
    editMenu.add(addBelowMenuItem);
    addChildMenuItem = new JMenuItem("Add child");
    addChildMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) { theJreepad.addNode(); }});
    editMenu.add(addChildMenuItem);
    deleteMenuItem = new JMenuItem("Delete node");
    deleteMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) { theJreepad.removeNode();  }});
    editMenu.add(deleteMenuItem);
    upMenuItem = new JMenuItem("Move node up");
    upMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) { theJreepad.moveCurrentNodeUp(); }});
    editMenu.add(upMenuItem);
    downMenuItem = new JMenuItem("Move node down");
    downMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) { theJreepad.moveCurrentNodeDown(); }});
    editMenu.add(downMenuItem);
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
    menuBar.add(fileMenu);
    menuBar.add(editMenu);
    menuBar.add(viewMenu);
    setJMenuBar(menuBar);
    //
    // Now the mnemonics...
    fileMenu.setMnemonic('F');
    newMenuItem.setMnemonic('N');
    openMenuItem.setMnemonic('O');
    saveMenuItem.setMnemonic('S');
    quitMenuItem.setMnemonic('Q');
    editMenu.setMnemonic('E');
    addAboveMenuItem.setMnemonic('a');
    addBelowMenuItem.setMnemonic('b');
    addChildMenuItem.setMnemonic('c');
    upMenuItem.setMnemonic('u');
    downMenuItem.setMnemonic('d');
    deleteMenuItem.setMnemonic('k');
    viewMenu.setMnemonic('V');
    viewBothMenuItem.setMnemonic('b');
    viewTreeMenuItem.setMnemonic('t');
    viewArticleMenuItem.setMnemonic('a');
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
    JButton removeButton = new JButton("Delete node");
    toolBar.add(removeButton);
    //
    JButton upButton = new JButton("Up");
    toolBar.add(upButton);
    JButton downButton = new JButton("Down");
    toolBar.add(downButton);
    //
    // Now the mnemonics...
    addAboveButton.setMnemonic('a');
    addBelowButton.setMnemonic('b');
    addButton.setMnemonic('c');
    upButton.setMnemonic('u');
    downButton.setMnemonic('d');
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
                               public void actionPerformed(ActionEvent e){ theJreepad.moveCurrentNodeUp(); repaint(); } });
    downButton.addActionListener(new ActionListener(){
                               public void actionPerformed(ActionEvent e){ theJreepad.moveCurrentNodeDown(); repaint(); } });
    addAboveButton.addActionListener(new ActionListener(){
                               public void actionPerformed(ActionEvent e){ theJreepad.addNodeAbove(); repaint();} });
    addBelowButton.addActionListener(new ActionListener(){
                               public void actionPerformed(ActionEvent e){ theJreepad.addNodeBelow(); repaint();} });
    addButton.addActionListener(new ActionListener(){
                               public void actionPerformed(ActionEvent e){ theJreepad.addNode(); repaint();} });
    removeButton.addActionListener(new ActionListener(){
                               public void actionPerformed(ActionEvent e){ theJreepad.removeNode(); repaint(); } });

/* To be removed.
    viewBothButton.addActionListener(new ActionListener(){
                               public void actionPerformed(ActionEvent e){ theJreepad.setViewBoth();} });
    viewTreeButton.addActionListener(new ActionListener(){
                               public void actionPerformed(ActionEvent e){ theJreepad.setViewTreeOnly();} });
    viewArticleButton.addActionListener(new ActionListener(){
                               public void actionPerformed(ActionEvent e){ theJreepad.setViewArticleOnly();} });
*/
    
    content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
    content.add(toolBar);
    content.add(theJreepad);

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
    theApp = new JreepadViewer();
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
      JOptionPane.showMessageDialog(theApp, err, "File input error" , JOptionPane.ERROR_MESSAGE);
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