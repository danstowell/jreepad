package jreepad;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

/*

A class to hold Jreepad's preferences - and hopefully to store them on disk in a nice 
permanent way which can be carried across from different versions

*/

public class JreepadPrefs implements Serializable
{
  File openLocation, saveLocation, importLocation, exportLocation, lastOpenFile;
  
  int autoSavePeriod;
  boolean autoSave;
  
  public static final int VIEW_TREE = 0;
  public static final int VIEW_ARTICLE = 1;
  public static final int VIEW_BOTH = 2;
  int viewWhich;
  
  boolean viewToolbar;
  
  int searchMaxNum;
  
  JreepadPrefs()
  {
    openLocation = new File("/");
    
    autoSavePeriod = 10;
    autoSave = false;
    
    viewWhich = 2;
    
    viewToolbar = true;
    
    searchMaxNum = 200;
  }
  
  // We override the serialization routines so that different versions of our class can read 
  // each other's serialized states.
  private void writeObject(java.io.ObjectOutputStream out)
     throws IOException
  {
    out.writeObject(openLocation);
    out.writeObject(saveLocation);
    out.writeObject(importLocation);
    out.writeObject(exportLocation);
    out.writeObject(lastOpenFile);
    
    out.writeInt(autoSavePeriod);
    out.writeBoolean(autoSave);
    
    out.writeInt(viewWhich);
    
    out.writeBoolean(viewToolbar);
    
    out.writeInt(searchMaxNum);
  }
  private void readObject(java.io.ObjectInputStream in)
     throws IOException, ClassNotFoundException
  {
    openLocation = (File)in.readObject();
    saveLocation = (File)in.readObject();
    importLocation = (File)in.readObject();
    exportLocation = (File)in.readObject();
    lastOpenFile = (File)in.readObject();
    
    autoSavePeriod = in.readInt();
    autoSave = in.readBoolean();
    
    viewWhich = in.readInt();
    
    viewToolbar = in.readBoolean();
    
    searchMaxNum = in.readInt();
  } 
}