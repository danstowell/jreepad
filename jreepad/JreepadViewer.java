package uk.co.mcld.jreepad;

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
    
    // Add the toolbar buttons
    toolBar = Box.createHorizontalBox();
    JButton newButton = new JButton("N");
    toolBar.add(newButton);
    JButton openButton = new JButton("O");
    toolBar.add(openButton);
    JButton saveButton = new JButton("S");
    toolBar.add(saveButton);
    //
    JButton upButton = new JButton("U");
    toolBar.add(upButton);
    JButton downButton = new JButton("D");
    toolBar.add(downButton);
    //
    JButton viewBothButton = new JButton("T+A");
    toolBar.add(viewBothButton);
    JButton viewTreeButton = new JButton("T");
    toolBar.add(viewTreeButton);
    JButton viewArticleButton = new JButton("A");
    toolBar.add(viewArticleButton);
    
    // Add the actions to the toolbar buttons
    newButton.addActionListener(new ActionListener(){
                               public void actionPerformed(ActionEvent e){ content.remove(theJreepad); theJreepad = new JreepadView(); content.add(theJreepad); } });
    openButton.addActionListener(new ActionListener(){
                               public void actionPerformed(ActionEvent e){
                               
                               try
                               {
                                 fileChooser.setCurrentDirectory(openLocation);
                                 if(fileChooser.showOpenDialog(theApp) == JFileChooser.APPROVE_OPTION)
                                 {
                                   openLocation = fileChooser.getSelectedFile();
                                   content.remove(theJreepad);
                                   theJreepad = new JreepadView(new JreepadNode(new FileInputStream(openLocation)));
                                   content.add(theJreepad);
                                 }
                               }
                               catch(IOException err)
                               {
                                 JOptionPane.showMessageDialog(theApp, err, "File input error" , JOptionPane.ERROR_MESSAGE);
                               }
                               
                               
                               } });
    saveButton.addActionListener(new ActionListener(){
                               public void actionPerformed(ActionEvent e){
                               
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
                                 }
                               }
                               catch(IOException err)
                               {
                                 JOptionPane.showMessageDialog(theApp, err, "File input error" , JOptionPane.ERROR_MESSAGE);
                               }
                               
                               
                               } });
    upButton.addActionListener(new ActionListener(){
                               public void actionPerformed(ActionEvent e){ theJreepad.moveCurrentNodeUp();} });
    downButton.addActionListener(new ActionListener(){
                               public void actionPerformed(ActionEvent e){ theJreepad.moveCurrentNodeDown();} });

    viewBothButton.addActionListener(new ActionListener(){
                               public void actionPerformed(ActionEvent e){ theJreepad.setViewBoth();} });
    viewTreeButton.addActionListener(new ActionListener(){
                               public void actionPerformed(ActionEvent e){ theJreepad.setViewTreeOnly();} });
    viewArticleButton.addActionListener(new ActionListener(){
                               public void actionPerformed(ActionEvent e){ theJreepad.setViewArticleOnly();} });
    
    content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
    content.add(toolBar);
    content.add(theJreepad);

    // Finally, make the window visible and well-sized
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    setTitle("JreepadViewer - Java Treepad Viewer");
    Toolkit theToolkit = getToolkit();
    Dimension wndSize = theToolkit.getScreenSize();
    setBounds(0,0,(int)(wndSize.width*0.6f),(int)(wndSize.height*0.6f));
    setVisible(true);
  }
  
  public static void main(String[] args)
  {
    theApp = new JreepadViewer();
  }
}