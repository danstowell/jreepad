package jreepad;

import javax.swing.tree.TreePath;

// A simple class to make it easy to serialize a list of TreePaths

public class TreePathCollection implements java.io.Serializable
{
  public TreePath[] paths;
  public TreePathCollection(TreePath[] coll) { paths = coll; }
}
