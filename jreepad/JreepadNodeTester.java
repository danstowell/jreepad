package jreepad;

import java.io.*;

public class JreepadNodeTester
{

  public static void main(String[] args)
  {
    System.out.println("Starting JreepadNode...");

    try
    {
      File inFile = new File("/Users/dan/Stuff imported from the PC/TreePad/thingstodo.hjt");
      JreepadNode test = new JreepadNode(new FileInputStream(inFile));
      System.out.println(test.toTreepadString());
    }
    catch(IOException e)
    {
      e.printStackTrace();
    }

    System.out.println("...finished JreepadNode");
  }

}