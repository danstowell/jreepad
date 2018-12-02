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

import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ListIterator;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

import jreepad.io.AutoDetectReader;
import jreepad.io.EncryptedWriter;
import jreepad.io.HtmlWriter;
import jreepad.io.JreepadReader;
import jreepad.io.JreepadWriter;
import jreepad.io.TreepadWriter;
import jreepad.io.XmlWriter;
import jreepad.ui.PasswordDialog;
import jreepad.ui.SaveFileChooser;
import edu.stanford.ejalbert.BrowserLauncher;
import edu.stanford.ejalbert.exception.BrowserLaunchingExecutionException;
import edu.stanford.ejalbert.exception.BrowserLaunchingInitializingException;
import edu.stanford.ejalbert.exception.UnsupportedOperatingSystemException;
import jreepad.ui.FontHelper;

public class JreepadViewer
    extends JFrame { // implements ApplicationListener
  // Jreepad version, to appear in "about" box etc
  public static String version = "1.6 rc1";

  private static Vector theApps = new Vector(1,1);
  private Box toolBar, toolBarIconic;
  private JreepadView theJreepad;
  private JreepadTreeModel document;
  private Container content;
// DEPRECATED  private File prefsFile = new File(System.getProperty("user.home"), ".jreepref");
  protected static ResourceBundle lang = ResourceBundle.getBundle("jreepad.lang.JreepadStrings");

//  private static final String[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames(null);
//  private static final String[] fontSizes = new String[] {"8","9","10","11","12","13","14","16","18","20","24","30","36"};

  private JFileChooser fileChooser;

  private String windowTitle;

  protected Clipboard systemClipboard;

  private JButton addAboveButton;
  private JButton addBelowButton;
  private JButton addButton;
  private JButton removeButton;
  private JButton upButton;
  private JButton downButton;
  private JButton indentButton;
  private JButton outdentButton;
  private JComboBox viewSelector, viewSelectorIconic;

  private JButton newIconButton;
  private JButton openIconButton;
  private JButton addAboveIconButton;
  private JButton addBelowIconButton;
  private JButton addIconButton;
  private JButton removeIconButton;
  private JButton upIconButton;
  private JButton downIconButton;
  private JButton indentIconButton;
  private JButton outdentIconButton;

  private Thread autoSaveThread;

  private JDialog autoSaveDialog;
  private JCheckBox autoSaveCheckBox;
  private JSpinner autoSavePeriodSpinner;
//  private DSpinner autoSavePeriodSpinner;
  private JButton autoSaveOkButton;
  private JButton autoSaveCancelButton;

  private JDialog prefsDialog;
  private SearchDialog searchDialog;

  private JDialog nodeUrlDisplayDialog;
  private JTextField nodeUrlDisplayField;
  private JButton nodeUrlDisplayOkButton;


    private JMenu openRecentMenu;
  private JMenuItem undoMenuItem;
  private JMenuItem redoMenuItem;
  private JMenuItem webSearchMenuItem;
  private JMenuItem characterWrapArticleMenuItem;
    private JCheckBoxMenuItem viewToolbarIconsMenuItem;
    private JCheckBoxMenuItem viewToolbarTextMenuItem;
    private JCheckBoxMenuItem viewToolbarOffMenuItem;

  private ColouredStrip funkyGreenStrip;

  // private boolean htmlExportOkChecker = false; // Just used to check whether OK or Cancel has been pressed in a certain dialogue box

  // Check whether we are on Mac OS X.  This is crucial to loading and using the OSXAdapter class.
  public static boolean MAC_OS_X = (System.getProperty("os.name").toLowerCase().startsWith("mac os x"));

  // Ask AWT which menu modifier we should be using.
  final static int MENU_MASK = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

  /*
  Note - The application code registered with Apple is:
  JREE� (Hex) 4A524545

  In Apple's Java methods they require an integer, so
  presumably we use 0x4A524545
  */
  public static final int appleAppCode = 0x4A524545;

  public JreepadViewer()
  {
    this("");
  }
  public JreepadViewer(String fileNameToLoad)
  {
    this(fileNameToLoad, null);
  }
  public JreepadViewer(String fileNameToLoad, String prefFilename)
  {
// THIS DOESN'T SEEM TO HAVE ANY EFFECT, ON MAC OSX - I'd be interested to know if it does its job on other OSs
// Apparently it does work on Windows. So I'll leave it in place!
    ClassLoader loader = this.getClass().getClassLoader(); // Used for loading icon
    java.net.URL iconUrl = loader.getResource("images/jreepadlogo-01-iconsize.gif");
    if(iconUrl != null){
      setIconImage(new ImageIcon(iconUrl).getImage());
    }else{
      System.out.println("Icon image failed to load: images/jreepadlogo-01-iconsize.gif");
    }

    Toolkit theToolkit = getToolkit();
    Dimension wndSize = theToolkit.getScreenSize();
    systemClipboard = theToolkit.getSystemClipboard();

    // New method of loading prefs - using java's own API rather handling a file
    setPrefs(new JreepadPrefs(wndSize));
    if(!getPrefs().seenLicense) {
      showLicense();
      getPrefs().seenLicense = true;
    }
/*
    // Check if a preferences file exists - and if so, load it
    if(prefFilename!=null)
      prefsFile = new File(prefFilename);
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
        showLicense(); // A crude way of showing the license on first visit
        setPrefs(new JreepadPrefs(wndSize));
      }
    }
    catch(Exception err)
    {
      setPrefs(new JreepadPrefs(wndSize));
    }
*/

    fileChooser = new JFileChooser();
    fileChooser.addChoosableFileFilter(SaveFileChooser.JREEPAD_FILE_FILTER);
    fileChooser.addChoosableFileFilter(SaveFileChooser.JREEPAD_ENCRYPTED_FILE_FILTER);
    fileChooser.addChoosableFileFilter(SaveFileChooser.TREEPAD_FILE_FILTER);

    content = getContentPane();

    document = new JreepadTreeModel();
    theJreepad = new JreepadView(document);

    establishMenus();
    establishToolbar();
    searchDialog = new SearchDialog(this, theJreepad);
    prefsDialog = new PrefsDialog(this);
    establishAutosaveDialogue();
    establishNodeUrlDisplayDialogue();

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
                                if(getPrefs().autoSave && document.getSaveLocation() != null)
                                  new SaveAction().actionPerformed(null);
                                else
                                  updateWindowTitle();
                              }
                              catch(InterruptedException e){}
                            }
                          }
                        };
    autoSaveThread.setPriority(Thread.MIN_PRIORITY);
    if(getPrefs().autoSave)
      autoSaveThread.start();
    // Finished establishing the autosave thread

    content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
    content.add(funkyGreenStrip = new ColouredStrip(new Color(0.09f, 0.4f, 0.12f), wndSize.width, 10) );
    funkyGreenStrip.setVisible(getPrefs().showGreenStrip);
    content.add(toolBar);
    content.add(toolBarIconic);
    setToolbarMode(getPrefs().toolbarMode);
    content.add(theJreepad);

    // Load the file - if it has been specified, and if it can be found, and if it's a valid HJT file
    File firstTimeFile = null;
    if(fileNameToLoad != "")
      firstTimeFile = new File(fileNameToLoad);
    else if(getPrefs().loadLastFileOnOpen && getPrefs().getMostRecentFile() != null)
      firstTimeFile = getPrefs().getMostRecentFile();

    if(firstTimeFile != null && firstTimeFile.isFile())
      openFile(firstTimeFile);

    // Set close operation
    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    addWindowListener(new WindowAdapter() { public void windowClosing(WindowEvent e){ quitAction(); }});

    setTitleBasedOnFilename("");

    // Finally, make the window visible and well-sized
    setBounds(getPrefs().windowLeft,getPrefs().windowTop,
              getPrefs().windowWidth, getPrefs().windowHeight);
    searchDialog.setBounds(getPrefs().windowWidth/2,getPrefs().windowHeight/6,
              (int)(getPrefs().windowWidth*0.7f),(int)(getPrefs().windowHeight*0.9f));
    autoSaveDialog.setBounds((int)(wndSize.width*0.5f),getPrefs().windowHeight/2,
              getPrefs().windowWidth/2, getPrefs().windowHeight/4);
    prefsDialog.setBounds(getPrefs().windowWidth/2,getPrefs().windowHeight/3,
              getPrefs().windowWidth, getPrefs().windowHeight);
    nodeUrlDisplayDialog.setBounds((int)(wndSize.width*0.1f),(int)(getPrefs().windowHeight*0.7f),
              (int)(getPrefs().windowWidth*1.3f), getPrefs().windowHeight/3);

    // pack() actually deprecates some of the functionality of the setBounds() calls just above
    //  - but hopefully gives a better mixture of sizes set programmatically and by the OS
    searchDialog.pack();
    autoSaveDialog.pack();
    prefsDialog.pack();
    nodeUrlDisplayDialog.pack();

    theApps.add(this);
    macOSXRegistration();

    setVisible(true);
    // If loading the last-saved file, expand the nodes we last had open
    if(document.getSaveLocation() != null)
        theJreepad.expandPaths(getPrefs().treePathCollection.paths);

    theJreepad.returnFocusToTree();
  }

  // Used by the constructor
  private void establishMenus()
  {
    // Create the menu bar
    JMenuBar menuBar = new JMenuBar();

    // Add menus
    menuBar.add(createFileMenu());
    menuBar.add(createEditMenu());
    menuBar.add(createActionsMenu());
    menuBar.add(createViewMenu());
    menuBar.add(createOptionsMenu());
    menuBar.add(createHelpMenu());
    setJMenuBar(menuBar);
  }

  /**
   * Creates the File menu.
   */
  private JMenu createFileMenu()
  {
      JMenu fileMenu = new JMenu(lang.getString("MENU_FILE")); //"File");
      fileMenu.setMnemonic(KeyEvent.VK_F);

      JMenuItem newMenuItem = new JMenuItem(lang.getString("MENUITEM_NEW")); //"New");
      newMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) { newAction();}});
      fileMenu.add(newMenuItem);

      JMenuItem openMenuItem = new JMenuItem(lang.getString("MENUITEM_OPEN")); //"Open");
      openMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) {openAction();}});
      fileMenu.add(openMenuItem);

      openRecentMenu = new JMenu(lang.getString("MENUITEM_OPENRECENT")); //"Open recent");
      updateOpenRecentMenu();
      fileMenu.add(openRecentMenu);

      fileMenu.add(new SaveAction()); // Save
      fileMenu.add(new SaveAction(true)); // Save As

      JMenuItem backupToMenuItem = new JMenuItem(lang.getString("MENUITEM_BACKUPTO")); //"Backup to...");
      backupToMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) {backupToAction();}});
      fileMenu.add(backupToMenuItem);

      fileMenu.add(new JSeparator());

      JMenuItem printSubtreeMenuItem = new JMenuItem(lang.getString("MENUITEM_PRINTSUBTREE")); //"Print subtree");
      printSubtreeMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) {subtreeToBrowserForPrintAction();}});
      fileMenu.add(printSubtreeMenuItem);

      JMenuItem printArticleMenuItem = new JMenuItem(lang.getString("MENUITEM_PRINTARTICLE")); //"Print article");
      printArticleMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) {articleToBrowserForPrintAction();}});
      fileMenu.add(printArticleMenuItem);

      fileMenu.add(new JSeparator());
      JMenu importMenu = new JMenu(lang.getString("MENUITEM_IMPORT")); //"Import...");
      fileMenu.add(importMenu);

      JMenuItem importHjtMenuItem = new JMenuItem(lang.getString("MENUITEM_IMPORT_HJT")); //"...Treepad file as subtree");
      importHjtMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) {importAction(FILE_FORMAT_HJT);}});
      importMenu.add(importHjtMenuItem);

      JMenuItem importTextMenuItem = new JMenuItem(lang.getString("MENUITEM_IMPORT_TEXTFILES")); //"...text file(s) as child node(s)");
      importTextMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) {importAction(FILE_FORMAT_TEXT);}});
      importMenu.add(importTextMenuItem);

      JMenuItem importTextAsListMenuItem = new JMenuItem(lang.getString("MENUITEM_IMPORT_TEXTLIST")); //"...text list file, one-child-per-line");
      importTextAsListMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) {importAction(FILE_FORMAT_TEXTASLIST);}});
      importMenu.add(importTextAsListMenuItem);

      fileMenu.add(createExportMenu());

      fileMenu.add(new JSeparator());
      if(!MAC_OS_X)
      {
          JMenuItem quitMenuItem = new JMenuItem(lang.getString("MENUITEM_QUIT")); //"Quit");
          quitMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) { quitAction(); }});
          fileMenu.add(quitMenuItem);
          quitMenuItem.setMnemonic('Q');
          quitMenuItem.setAccelerator(KeyStroke.getKeyStroke('Q', MENU_MASK));
      }

      newMenuItem.setMnemonic('N');
      newMenuItem.setAccelerator(KeyStroke.getKeyStroke('N', MENU_MASK));
      openMenuItem.setMnemonic('O');
      openMenuItem.setAccelerator(KeyStroke.getKeyStroke('O', MENU_MASK));
      openRecentMenu.setMnemonic('R');
      printSubtreeMenuItem.setMnemonic('P');
      printSubtreeMenuItem.setAccelerator(KeyStroke.getKeyStroke('P', MENU_MASK));
      printArticleMenuItem.setAccelerator(KeyStroke.getKeyStroke('P', MENU_MASK | java.awt.Event.SHIFT_MASK));
      backupToMenuItem.setMnemonic('B');
      importMenu.setMnemonic('I');
      importHjtMenuItem.setMnemonic('f');
      importTextMenuItem.setMnemonic('t');
      importTextAsListMenuItem.setMnemonic('l');

      return fileMenu;
  }

  /**
   * Creates the Export submenu.
   */
  private JMenu createExportMenu()
  {
      JMenu exportMenu = new JMenu(lang.getString("MENUITEM_EXPORT")); //"Export selected...");

      JMenuItem exportHjtMenuItem = new JMenuItem(lang.getString("MENUITEM_EXPORT_HJT")); //"...subtree to Treepad HJT file");
      exportHjtMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) {exportAction(FILE_FORMAT_HJT);}});
      exportMenu.add(exportHjtMenuItem);

      JMenuItem exportHtmlMenuItem = new JMenuItem(lang.getString("MENUITEM_EXPORT_HTML")); //"...subtree to HTML");
      exportHtmlMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) {exportAction(FILE_FORMAT_HTML);}});
      exportMenu.add(exportHtmlMenuItem);

      JMenuItem exportSimpleXmlMenuItem = new JMenuItem(lang.getString("MENUITEM_EXPORT_XML")); //"...subtree to XML");
      exportSimpleXmlMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) {exportAction(FILE_FORMAT_XML);}});
      exportMenu.add(exportSimpleXmlMenuItem);

      JMenuItem exportListMenuItem = new JMenuItem(lang.getString("MENUITEM_EXPORT_TEXTLIST")); //"...subtree to text list (node titles only)");
      exportListMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) {exportAction(FILE_FORMAT_TEXTASLIST);}});
      exportMenu.add(exportListMenuItem);

      exportMenu.add(new JSeparator());

      JMenuItem exportTextMenuItem = new JMenuItem(lang.getString("MENUITEM_EXPORT_ARTICLE")); //"...article to text file");
      exportTextMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) {exportAction(FILE_FORMAT_TEXT);}});
      exportMenu.add(exportTextMenuItem);

      JMenuItem exportSubtreeTextMenuItem = new JMenuItem(lang.getString("MENUITEM_EXPORT_ARTICLES")); //"...subtree articles to text file");
      exportSubtreeTextMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) {exportAction(FILE_FORMAT_ARTICLESTOTEXT);}});
      exportMenu.add(exportSubtreeTextMenuItem);

      exportMenu.setMnemonic('E');
      exportHjtMenuItem.setMnemonic('f');
      exportHtmlMenuItem.setMnemonic('h');
      exportSimpleXmlMenuItem.setMnemonic('x');
      exportTextMenuItem.setMnemonic('t');

      return exportMenu;
  }

  /**
   * Creates the Edit menu.
   */
  private JMenu createEditMenu()
  {
      JMenu editMenu = new JMenu(lang.getString("MENU_EDIT")); //"Edit");
      editMenu.setMnemonic(KeyEvent.VK_E);

      undoMenuItem = new JMenuItem(lang.getString("MENUITEM_UNDO")); //"Undo");
      undoMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) {
          undoAction();
          updateUndoRedoMenuState();}});
      editMenu.add(undoMenuItem);

      redoMenuItem = new JMenuItem(lang.getString("MENUITEM_REDO")); //"Redo");
      redoMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) {
          redoAction();
          updateUndoRedoMenuState();}});
      editMenu.add(redoMenuItem);

      editMenu.add(new JSeparator());

      JMenuItem addAboveMenuItem = new JMenuItem(lang.getString("MENUITEM_ADDABOVE")); //"Add sibling above");
      addAboveMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) { theJreepad.addNodeAbove(); updateWindowTitle();}});
      editMenu.add(addAboveMenuItem);

      JMenuItem addBelowMenuItem = new JMenuItem(lang.getString("MENUITEM_ADDBELOW")); //"Add sibling below");
      addBelowMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) { theJreepad.addNodeBelow(); updateWindowTitle();}});
      editMenu.add(addBelowMenuItem);

      JMenuItem addChildMenuItem = new JMenuItem(lang.getString("MENUITEM_ADDCHILD")); //"Add child");
      addChildMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) { theJreepad.addNode(); updateWindowTitle(); }});
      editMenu.add(addChildMenuItem);

      editMenu.add(new JSeparator());

      JMenuItem newFromClipboardMenuItem = new JMenuItem(lang.getString("MENUITEM_NEWFROMCLIPBOARD")); //"New node from clipboard");
      newFromClipboardMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) { systemClipboardToNewNode(); }});
      editMenu.add(newFromClipboardMenuItem);

      editMenu.add(new JSeparator());

      JMenuItem editNodeTitleMenuItem = new JMenuItem(lang.getString("MENUITEM_EDITNODETITLE")); //"Edit node title");
      editNodeTitleMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) { theJreepad.editNodeTitleAction(); }});
      editMenu.add(editNodeTitleMenuItem);

      editMenu.add(new JSeparator());

      JMenuItem deleteMenuItem = new JMenuItem(lang.getString("MENUITEM_DELETENODE")); //"Delete node");
      deleteMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) { deleteNodeAction(); }});
      editMenu.add(deleteMenuItem);

      editMenu.add(new JSeparator());

      JMenuItem upMenuItem = new JMenuItem(lang.getString("MENUITEM_MOVEUP")); //"Move node up");
      upMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) { theJreepad.moveCurrentNodeUp(); theJreepad.returnFocusToTree(); updateWindowTitle(); }});
      editMenu.add(upMenuItem);

      JMenuItem downMenuItem = new JMenuItem(lang.getString("MENUITEM_MOVEDOWN")); //"Move node down");
      downMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) { theJreepad.moveCurrentNodeDown(); theJreepad.returnFocusToTree(); updateWindowTitle(); }});
      editMenu.add(downMenuItem);

      editMenu.add(new JSeparator());

      JMenuItem indentMenuItem = new JMenuItem(lang.getString("MENUITEM_MOVEIN")); //"Indent node (demote)");
      indentMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) { theJreepad.indentCurrentNode(); theJreepad.returnFocusToTree(); updateWindowTitle(); }});
      editMenu.add(indentMenuItem);

      JMenuItem outdentMenuItem = new JMenuItem(lang.getString("MENUITEM_MOVEOUT")); //"Outdent node (promote)");
      outdentMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) { theJreepad.outdentCurrentNode(); theJreepad.returnFocusToTree(); updateWindowTitle(); }});
      editMenu.add(outdentMenuItem);

      editMenu.add(new JSeparator());

      JMenuItem expandAllMenuItem = new JMenuItem(lang.getString("MENUITEM_EXPAND")); //"Expand subtree");
      expandAllMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) { theJreepad.expandAllCurrentNode(); }});
      editMenu.add(expandAllMenuItem);

      JMenuItem collapseAllMenuItem = new JMenuItem(lang.getString("MENUITEM_COLLAPSE")); //"Collapse subtree");
      collapseAllMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) { theJreepad.collapseAllCurrentNode(); }});
      editMenu.add(collapseAllMenuItem);

      undoMenuItem.setMnemonic('u');
      undoMenuItem.setAccelerator(KeyStroke.getKeyStroke('Z', MENU_MASK));
      redoMenuItem.setMnemonic('r');
      redoMenuItem.setAccelerator(KeyStroke.getKeyStroke('Z', MENU_MASK | java.awt.event.InputEvent.SHIFT_MASK));
      addAboveMenuItem.setMnemonic('a');
      addAboveMenuItem.setAccelerator(KeyStroke.getKeyStroke('T', MENU_MASK));
      addBelowMenuItem.setMnemonic('b');
      addBelowMenuItem.setAccelerator(KeyStroke.getKeyStroke('B', MENU_MASK));
      addChildMenuItem.setMnemonic('c');
      addChildMenuItem.setAccelerator(KeyStroke.getKeyStroke('D', MENU_MASK));
      newFromClipboardMenuItem.setAccelerator(KeyStroke.getKeyStroke('M', MENU_MASK));
      editNodeTitleMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));
      upMenuItem.setMnemonic('u');
      upMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_UP, MENU_MASK | java.awt.Event.ALT_MASK));
      downMenuItem.setMnemonic('d');
      downMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, MENU_MASK | java.awt.Event.ALT_MASK));
      indentMenuItem.setMnemonic('i');
      indentMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, MENU_MASK | java.awt.Event.ALT_MASK));
      outdentMenuItem.setMnemonic('o');
      outdentMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, MENU_MASK | java.awt.Event.ALT_MASK));
      expandAllMenuItem.setMnemonic('x');
      expandAllMenuItem.setAccelerator(KeyStroke.getKeyStroke('=', MENU_MASK));
      collapseAllMenuItem.setMnemonic('l');
      collapseAllMenuItem.setAccelerator(KeyStroke.getKeyStroke('-', MENU_MASK));
      deleteMenuItem.setMnemonic('k');
      deleteMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, MENU_MASK));

      undoMenuItem.setMnemonic('u');
      undoMenuItem.setAccelerator(KeyStroke.getKeyStroke('Z', MENU_MASK));
      redoMenuItem.setMnemonic('r');
      redoMenuItem.setAccelerator(KeyStroke.getKeyStroke('Z', MENU_MASK | java.awt.event.InputEvent.SHIFT_MASK));
      addAboveMenuItem.setMnemonic('a');
      addAboveMenuItem.setAccelerator(KeyStroke.getKeyStroke('T', MENU_MASK));
      addBelowMenuItem.setMnemonic('b');
      addBelowMenuItem.setAccelerator(KeyStroke.getKeyStroke('B', MENU_MASK));
      addChildMenuItem.setMnemonic('c');
      addChildMenuItem.setAccelerator(KeyStroke.getKeyStroke('D', MENU_MASK));
      newFromClipboardMenuItem.setAccelerator(KeyStroke.getKeyStroke('M', MENU_MASK));
      upMenuItem.setMnemonic('u');
      upMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_UP, MENU_MASK | java.awt.Event.ALT_MASK));
      downMenuItem.setMnemonic('d');
      downMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, MENU_MASK | java.awt.Event.ALT_MASK));
      indentMenuItem.setMnemonic('i');
      indentMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, MENU_MASK | java.awt.Event.ALT_MASK));
      outdentMenuItem.setMnemonic('o');
      outdentMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, MENU_MASK | java.awt.Event.ALT_MASK));
      expandAllMenuItem.setMnemonic('x');
      expandAllMenuItem.setAccelerator(KeyStroke.getKeyStroke('=', MENU_MASK));
      collapseAllMenuItem.setMnemonic('l');
      collapseAllMenuItem.setAccelerator(KeyStroke.getKeyStroke('-', MENU_MASK));
      deleteMenuItem.setMnemonic('k');
      deleteMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, MENU_MASK));

      return editMenu;
  }

  /**
   * Creates the Actions menu.
   */
  private JMenu createActionsMenu()
  {
    JMenu actionsMenu = new JMenu(lang.getString("MENU_ACTIONS")); // "Actions");
    actionsMenu.setMnemonic(KeyEvent.VK_T);

    JMenuItem searchMenuItem = new JMenuItem(lang.getString("MENUITEM_SEARCH")); //"Search");
    searchMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) { searchDialog.open(); }});
    actionsMenu.add(searchMenuItem);

    JMenuItem launchUrlMenuItem = new JMenuItem(lang.getString("MENUITEM_FOLLOWLINK")); //"Follow highlighted link");
    launchUrlMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) { theJreepad.openURLSelectedInArticle(); }});
    actionsMenu.add(launchUrlMenuItem);

    webSearchMenuItem = new JMenuItem(getPrefs().webSearchName);
    webSearchMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) { theJreepad.webSearchTextSelectedInArticle(); }});
    actionsMenu.add(webSearchMenuItem);

    actionsMenu.add(new JSeparator());

    characterWrapArticleMenuItem = new JMenuItem(lang.getString("MENUITEM_HARDWRAP1") + " " + getPrefs().characterWrapWidth + " " + lang.getString("MENUITEM_HARDWRAP2")); //);
    characterWrapArticleMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) { wrapContentToCharWidth(); }});
    actionsMenu.add(characterWrapArticleMenuItem);

    JMenuItem stripTagsMenuItem = new JMenuItem(lang.getString("MENUITEM_STRIPTAGS")); //"Strip <tags> from article");
    stripTagsMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) { stripAllTags(); }});
    actionsMenu.add(stripTagsMenuItem);

    JMenuItem insertDateMenuItem = new JMenuItem(lang.getString("MENUITEM_INSERTDATE")); //"Insert date");
    insertDateMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) { insertDate(); }});
    actionsMenu.add(insertDateMenuItem);

    actionsMenu.add(new JSeparator());

    JMenuItem sortMenuItem = new JMenuItem(lang.getString("MENUITEM_SORTONELEVEL")); //"Sort children (one level)");
    sortMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) { theJreepad.sortChildren(); updateWindowTitle(); }});
    actionsMenu.add(sortMenuItem);

    JMenuItem sortRecursiveMenuItem = new JMenuItem(lang.getString("MENUITEM_SORTALLLEVELS")); //"Sort children (all levels)");
    sortRecursiveMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) { theJreepad.sortChildrenRecursive(); updateWindowTitle(); }});
    actionsMenu.add(sortRecursiveMenuItem);

    searchMenuItem.setMnemonic('s');
    searchMenuItem.setAccelerator(KeyStroke.getKeyStroke('F', MENU_MASK));
    webSearchMenuItem.setMnemonic('g');
    webSearchMenuItem.setAccelerator(KeyStroke.getKeyStroke('G', MENU_MASK));
    characterWrapArticleMenuItem.setAccelerator(KeyStroke.getKeyStroke('R', MENU_MASK));
    characterWrapArticleMenuItem.setMnemonic('r');
    launchUrlMenuItem.setAccelerator(KeyStroke.getKeyStroke('L', MENU_MASK));
    launchUrlMenuItem.setMnemonic('l');
    stripTagsMenuItem.setAccelerator(KeyStroke.getKeyStroke('T', MENU_MASK));
    stripTagsMenuItem.setMnemonic('t');
    insertDateMenuItem.setAccelerator(KeyStroke.getKeyStroke('E', MENU_MASK));
    insertDateMenuItem.setMnemonic('e');

    return actionsMenu;
  }

  /**
   * Creates the View menu.
   */
  private JMenu createViewMenu()
  {
    JMenu viewMenu = new JMenu(lang.getString("MENU_VIEW")); //"View");
    viewMenu.setMnemonic('V');

    JMenuItem viewBothMenuItem = new JMenuItem(lang.getString("MENUITEM_VIEWBOTH")); //"Both tree and article");
    viewBothMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) { setViewMode(JreepadPrefs.VIEW_BOTH); }});
    viewMenu.add(viewBothMenuItem);
    JMenuItem viewTreeMenuItem = new JMenuItem(lang.getString("MENUITEM_VIEWTREE")); //"Tree");
    viewTreeMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) { setViewMode(JreepadPrefs.VIEW_TREE); }});
    viewMenu.add(viewTreeMenuItem);
    JMenuItem viewArticleMenuItem = new JMenuItem(lang.getString("MENUITEM_VIEWARTICLE")); //"Article");
    viewArticleMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) { setViewMode(JreepadPrefs.VIEW_ARTICLE); }});
    viewMenu.add(viewArticleMenuItem);
    viewMenu.add(new JSeparator());

    JMenu viewToolbarMenu = new JMenu(lang.getString("MENUITEM_TOOLBAR")); //"Toolbar");
    viewMenu.add(viewToolbarMenu);
      viewToolbarIconsMenuItem = new JCheckBoxMenuItem(lang.getString("MENUITEM_TOOLBAR_ICONS")); //"Icons", true);
      viewToolbarIconsMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) {
                                                  setToolbarMode(JreepadPrefs.TOOLBAR_ICON);
                                                  }});
      viewToolbarMenu.add(viewToolbarIconsMenuItem);
      viewToolbarTextMenuItem = new JCheckBoxMenuItem(lang.getString("MENUITEM_TOOLBAR_TEXT")); //"Text", true);
      viewToolbarTextMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) {
                                                  setToolbarMode(JreepadPrefs.TOOLBAR_TEXT);
                                                  }});
      viewToolbarMenu.add(viewToolbarTextMenuItem);
      viewToolbarOffMenuItem = new JCheckBoxMenuItem(lang.getString("MENUITEM_TOOLBAR_OFF")); //"Off", true);
      viewToolbarOffMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) {
                                                  setToolbarMode(JreepadPrefs.TOOLBAR_OFF);
                                                  }});
      viewToolbarMenu.add(viewToolbarOffMenuItem);



    viewMenu.add(new JSeparator());

    JMenuItem articleViewModeMenuItem = new JMenu(lang.getString("MENUITEM_ARTICLEFORMAT")); //"View this article as...");
    JMenuItem articleViewModeTextMenuItem = new JMenuItem(lang.getString("MENUITEM_ARTICLEFORMAT_TEXT")); //"Text");
      articleViewModeTextMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) {
                theJreepad.setArticleMode(JreepadArticle.ARTICLEMODE_ORDINARY);
                updateUndoRedoMenuState();
                       }});
      articleViewModeMenuItem.add(articleViewModeTextMenuItem);
      JMenuItem articleViewModeHtmlMenuItem = new JMenuItem(lang.getString("MENUITEM_ARTICLEFORMAT_HTML")); //"HTML");
      articleViewModeHtmlMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) {
                theJreepad.setArticleMode(JreepadArticle.ARTICLEMODE_HTML);
                updateUndoRedoMenuState();
                       }});
      articleViewModeMenuItem.add(articleViewModeHtmlMenuItem);
      JMenuItem articleViewModeCsvMenuItem = new JMenuItem(lang.getString("MENUITEM_ARTICLEFORMAT_CSV")); //"Table (comma-separated data)");
      articleViewModeCsvMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) {
                theJreepad.setArticleMode(JreepadArticle.ARTICLEMODE_CSV);
                updateUndoRedoMenuState();
                       }});
      articleViewModeMenuItem.add(articleViewModeCsvMenuItem);
      JMenuItem articleViewModeTextileMenuItem = new JMenuItem(lang.getString("MENUITEM_ARTICLEFORMAT_TEXTILE"));
      articleViewModeTextileMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) {
                theJreepad.setArticleMode(JreepadArticle.ARTICLEMODE_TEXTILEHTML);
                updateUndoRedoMenuState();
                       }});
      articleViewModeMenuItem.add(articleViewModeTextileMenuItem);
    viewMenu.add(articleViewModeMenuItem);


    viewMenu.add(new JSeparator());
    JMenuItem thisNodesUrlMenuItem = new JMenuItem(lang.getString("MENUITEM_NODEADDRESS")); //"\"node://\" address for current node");
    thisNodesUrlMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) { getTreepadNodeUrl(); }});
    viewMenu.add(thisNodesUrlMenuItem);

    thisNodesUrlMenuItem.setMnemonic('n');
    viewBothMenuItem.setMnemonic('b');
    viewTreeMenuItem.setMnemonic('t');
    viewArticleMenuItem.setMnemonic('a');
    viewBothMenuItem.setAccelerator(KeyStroke.getKeyStroke('1', MENU_MASK));
    viewTreeMenuItem.setAccelerator(KeyStroke.getKeyStroke('2', MENU_MASK));
    viewArticleMenuItem.setAccelerator(KeyStroke.getKeyStroke('3', MENU_MASK));
    viewToolbarIconsMenuItem.setAccelerator(KeyStroke.getKeyStroke('4', MENU_MASK));
    viewToolbarTextMenuItem.setAccelerator(KeyStroke.getKeyStroke('5', MENU_MASK));
    viewToolbarOffMenuItem.setAccelerator(KeyStroke.getKeyStroke('6', MENU_MASK));
    articleViewModeTextMenuItem.setAccelerator(KeyStroke.getKeyStroke('7', MENU_MASK));
    articleViewModeHtmlMenuItem.setAccelerator(KeyStroke.getKeyStroke('8', MENU_MASK));
    articleViewModeCsvMenuItem.setAccelerator(KeyStroke.getKeyStroke('9', MENU_MASK));
    articleViewModeTextileMenuItem.setAccelerator(KeyStroke.getKeyStroke('0', MENU_MASK));
    viewToolbarMenu.setMnemonic('o');

    viewMenu.add(new JSeparator());
    // section to adjust fonts
    JMenu articleFontMenu = new JMenu(lang.getString("MENUITEM_FONT_ARTICLE"));
    viewMenu.add(articleFontMenu);
    JMenuItem increaseFontMenuItem = new JMenuItem(lang.getString(
        "MENUITEM_FONT_INCREASE"));
    increaseFontMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        theJreepad.changeFont(FontHelper.FONT_DIR_UP, JreepadView.CHANGE_ARTICLE_FONT);
      }
    });
    JMenuItem decreaseFontMenuItem = new JMenuItem(lang.getString(
        "MENUITEM_FONT_DECREASE"));
    decreaseFontMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        theJreepad.changeFont(FontHelper.FONT_DIR_DOWN, JreepadView.CHANGE_ARTICLE_FONT);
      }
    });
    articleFontMenu.add(increaseFontMenuItem);
    articleFontMenu.add(decreaseFontMenuItem);

    JMenu treeFontMenu = new JMenu(lang.getString("MENUITEM_FONT_TREE"));
    viewMenu.add(treeFontMenu);
    JMenuItem increaseTreeFontMenuItem = new JMenuItem(lang.getString(
        "MENUITEM_FONT_INCREASE"));
    increaseTreeFontMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        theJreepad.changeFont(FontHelper.FONT_DIR_UP, JreepadView.CHANGE_TREE_FONT);
      }
    });
    JMenuItem decreaseTreeFontMenuItem = new JMenuItem(lang.getString(
        "MENUITEM_FONT_DECREASE"));
    decreaseTreeFontMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        theJreepad.changeFont(FontHelper.FONT_DIR_DOWN, JreepadView.CHANGE_TREE_FONT);
      }
    });
    treeFontMenu.add(increaseTreeFontMenuItem);
    treeFontMenu.add(decreaseTreeFontMenuItem);

    return viewMenu;
  }

  /**
   * Creates the Options menu.
   */
  private JMenu createOptionsMenu()
  {
      JMenu optionsMenu = new JMenu(lang.getString("MENU_OPTIONS")); //"Options");
      optionsMenu.setMnemonic('O');

      JMenuItem autoSaveMenuItem = new JMenuItem(lang.getString("MENUITEM_AUTOSAVE_PREFS")); //"Autosave...");
      autoSaveMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) { showAutoSaveDialog(); }});
      optionsMenu.add(autoSaveMenuItem);
      JMenuItem prefsMenuItem = new JMenuItem(lang.getString("MENUITEM_PREFS")); //"Preferences");
      prefsMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) {
                                              // updateFontsInPrefsBox();
                                              showPrefsDialog(); }});
      optionsMenu.add(prefsMenuItem);

      autoSaveMenuItem.setMnemonic('a');
      prefsMenuItem.setMnemonic('p');

      return optionsMenu;
  }

  private JMenu createHelpMenu()
  {
      JMenu helpMenu = new JMenu(lang.getString("MENU_HELP")); //"Help");
      helpMenu.setMnemonic('H');

      JMenuItem keyboardHelpMenuItem = new JMenuItem(lang.getString("MENUITEM_KEYBOARDSHORTCUTS")); //"Keyboard shortcuts");
      keyboardHelpMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e)
            { keyboardHelp();
            }});
      helpMenu.add(keyboardHelpMenuItem);
      JMenuItem linksHelpMenuItem = new JMenuItem(lang.getString("MENUITEM_LINKSHELP")); //"Help with links");
      linksHelpMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e)
            { linksHelp();
            }});
      helpMenu.add(linksHelpMenuItem);
      JMenuItem dragDropHelpMenuItem = new JMenuItem(lang.getString("MENUITEM_DRAGDROPHELP")); //"Help with drag-and-drop");
      dragDropHelpMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e)
            { dragDropHelp();
            }});
      helpMenu.add(dragDropHelpMenuItem);
      helpMenu.add(new JSeparator());
      if(!MAC_OS_X)
      {
        JMenuItem aboutMenuItem = new JMenuItem(lang.getString("MENUITEM_ABOUT")); //"About Jreepad");
        aboutMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e)
              {
                aboutAction();
              }});
        aboutMenuItem.setMnemonic('a');
        helpMenu.add(aboutMenuItem);
      }
      JMenuItem licenseMenuItem = new JMenuItem(lang.getString("MENUITEM_LICENSE")); //"License");
      licenseMenuItem.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e)
              {
                showLicense();
              }});
      helpMenu.add(licenseMenuItem);

      keyboardHelpMenuItem.setMnemonic('k');
      dragDropHelpMenuItem.setMnemonic('d');
      linksHelpMenuItem.setMnemonic('l');
      licenseMenuItem.setMnemonic('i');

      return helpMenu;
  }

  // Used by the constructor
  public void establishToolbar()
  {
    // Add the toolbar buttons
    toolBar = Box.createHorizontalBox();
    toolBarIconic = Box.createHorizontalBox();
    //
    addAboveButton = new JButton(lang.getString("TOOLBAR_ADDABOVE"));
    addBelowButton = new JButton(lang.getString("TOOLBAR_ADDBELOW"));
    addButton = new JButton(lang.getString("TOOLBAR_ADDCHILD"));
    removeButton = new JButton(lang.getString("TOOLBAR_DELETE"));
    //
    upButton = new JButton(lang.getString("TOOLBAR_UP"));
    downButton = new JButton(lang.getString("TOOLBAR_DOWN"));
    indentButton = new JButton(lang.getString("TOOLBAR_IN"));
    outdentButton = new JButton(lang.getString("TOOLBAR_OUT"));
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
    viewSelector = new JComboBox(new String[]{lang.getString("TOOLBAR_VIEWBOTH"),lang.getString("TOOLBAR_VIEWTREE"),lang.getString("TOOLBAR_VIEWARTICLE")});
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
    viewSelectorIconic = new JComboBox(new String[]{lang.getString("TOOLBAR_VIEWBOTH"),lang.getString("TOOLBAR_VIEWTREE"),lang.getString("TOOLBAR_VIEWARTICLE")});
    viewSelectorIconic.setEditable(false);
    viewSelectorIconic.setSelectedIndex(0);
    viewSelectorIconic.addActionListener(new ActionListener(){
                               public void actionPerformed(ActionEvent e){
                               switch(viewSelectorIconic.getSelectedIndex())
                               {
                                 case 1:
                                   setViewMode(JreepadPrefs.VIEW_TREE); break;
                                 case 2:
                                   setViewMode(JreepadPrefs.VIEW_ARTICLE); break;
                                 default:
                                   setViewMode(JreepadPrefs.VIEW_BOTH); break;
                               }
                               } });

    // Add the actions to the toolbar buttons
    upButton.addActionListener(new ActionListener(){
                               public void actionPerformed(ActionEvent e){ theJreepad.moveCurrentNodeUp(); repaint();  theJreepad.returnFocusToTree(); updateWindowTitle();} });
    downButton.addActionListener(new ActionListener(){
                               public void actionPerformed(ActionEvent e){ theJreepad.moveCurrentNodeDown(); repaint();  theJreepad.returnFocusToTree(); updateWindowTitle();} });


    indentButton.addActionListener(new ActionListener(){
                               public void actionPerformed(ActionEvent e){ theJreepad.indentCurrentNode(); repaint();  theJreepad.returnFocusToTree(); updateWindowTitle();} });
    outdentButton.addActionListener(new ActionListener(){
                               public void actionPerformed(ActionEvent e){ theJreepad.outdentCurrentNode(); repaint(); theJreepad.returnFocusToTree(); updateWindowTitle(); } });




    addAboveButton.addActionListener(new ActionListener(){
                               public void actionPerformed(ActionEvent e){ theJreepad.addNodeAbove(); repaint(); updateWindowTitle();} });
    addBelowButton.addActionListener(new ActionListener(){
                               public void actionPerformed(ActionEvent e){ theJreepad.addNodeBelow(); repaint(); updateWindowTitle();} });
    addButton.addActionListener(new ActionListener(){
                               public void actionPerformed(ActionEvent e){ theJreepad.addNode(); repaint(); updateWindowTitle();} });
    removeButton.addActionListener(new ActionListener(){
                               public void actionPerformed(ActionEvent e){ deleteNodeAction(); } });


    // Now establish the iconic buttons (code contributed by Coen Schalkwijk)
        // New file
        newIconButton = new JButton();
        newIconButton.setToolTipText(lang.getString("TOOLBAR_NEW"));
        newIconButton.setBorderPainted(false);
        newIconButton.setIcon(this.getIcon("New16.gif"));

        // Open existing
        openIconButton = new JButton();
        openIconButton.setToolTipText(lang.getString("TOOLBAR_OPEN"));
        openIconButton.setBorderPainted(false);
        openIconButton.setIcon(this.getIcon("Open16.gif"));

        // Save current
        JButton saveIconButton = new JButton(new SaveAction());
        saveIconButton.setBorderPainted(false);
        saveIconButton.setText(null); // Ignore action text

        // Insert node before
        addAboveIconButton = new JButton(lang.getString("TOOLBAR_ADDABOVE"));
//	    addAboveIconButton = new JButton();
//	    addAboveIconButton.setToolTipText(lang.getString("TOOLBAR_"));
//	    addAboveIconButton.setBorderPainted(false);
//	    addAboveIconButton.setMnemonic('a');
//	    addAboveIconButton.setIcon(this.getIcon("InsertBefore16.gif"));

        // Insert node after
        addBelowIconButton = new JButton(lang.getString("TOOLBAR_ADDBELOW"));
//	    addBelowIconButton = new JButton();
        addBelowIconButton.setMnemonic('b');
//	    addBelowIconButton.setToolTipText("Add below");
//	    addBelowIconButton.setBorderPainted(false);
//	    addBelowIconButton.setIcon(this.getIcon("InsertAfter16.gif"));

        // Add child node
        addIconButton = new JButton();
        addIconButton.setMnemonic('c');
        addIconButton.setToolTipText(lang.getString("TOOLBAR_ADDCHILD"));
        addIconButton.setBorderPainted(false);
        addIconButton.setIcon(this.getIcon("Add16.gif"));

        // Remove node
        removeIconButton = new JButton();
        removeIconButton.setMnemonic('k');
        removeIconButton.setToolTipText(lang.getString("TOOLBAR_DELETE"));
        removeIconButton.setBorderPainted(false);
        removeIconButton.setIcon(this.getIcon("Remove16.gif"));

        // Move node up
        upIconButton = new JButton();
        upIconButton.setMnemonic('u');
        upIconButton.setToolTipText(lang.getString("TOOLBAR_UP"));
        upIconButton.setBorderPainted(false);
        upIconButton.setIcon(this.getIcon("Up16.gif"));

        // Move node down
        downIconButton = new JButton();
        downIconButton.setMnemonic('d');
        downIconButton.setToolTipText(lang.getString("TOOLBAR_DOWN"));
        downIconButton.setBorderPainted(false);
        downIconButton.setIcon(this.getIcon("Down16.gif"));

        // Move node from current
        outdentIconButton = new JButton();
        outdentIconButton.setMnemonic('i');
        outdentIconButton.setToolTipText(lang.getString("TOOLBAR_IN"));
        outdentIconButton.setBorderPainted(false);
        outdentIconButton.setIcon(this.getIcon("Back16.gif"));

        // Move node to previous
        indentIconButton = new JButton();
        indentIconButton.setMnemonic('o');
        indentIconButton.setToolTipText(lang.getString("TOOLBAR_OUT"));
        indentIconButton.setBorderPainted(false);
        indentIconButton.setIcon(this.getIcon("Forward16.gif"));

    // Add the actions to the toolbar buttons
    newIconButton.addActionListener(new ActionListener(){
                               public void actionPerformed(ActionEvent e){ newAction(); } });
    openIconButton.addActionListener(new ActionListener(){
                               public void actionPerformed(ActionEvent e){ openAction(); } });
    upIconButton.addActionListener(new ActionListener(){
                               public void actionPerformed(ActionEvent e){ theJreepad.moveCurrentNodeUp(); repaint();
          theJreepad.returnFocusToTree();
          updateWindowTitle();} });
    downIconButton.addActionListener(new ActionListener(){
                               public void actionPerformed(ActionEvent e){ theJreepad.moveCurrentNodeDown(); repaint();
          theJreepad.returnFocusToTree();
          updateWindowTitle();} });
    indentIconButton.addActionListener(new ActionListener(){
                               public void actionPerformed(ActionEvent e){ theJreepad.indentCurrentNode(); repaint();
          theJreepad.returnFocusToTree();
          updateWindowTitle();} });
    outdentIconButton.addActionListener(new ActionListener(){
                               public void actionPerformed(ActionEvent e){ theJreepad.outdentCurrentNode(); repaint();
          theJreepad.returnFocusToTree();
          updateWindowTitle(); } });
    addAboveIconButton.addActionListener(new ActionListener(){
                               public void actionPerformed(ActionEvent e){ theJreepad.addNodeAbove(); repaint(); /*
          theJreepad.returnFocusToTree(); */
          updateWindowTitle();} });
    addBelowIconButton.addActionListener(new ActionListener(){
                               public void actionPerformed(ActionEvent e){ theJreepad.addNodeBelow(); repaint(); /*
          theJreepad.returnFocusToTree(); */
          updateWindowTitle();} });
    addIconButton.addActionListener(new ActionListener(){
                               public void actionPerformed(ActionEvent e){ theJreepad.addNode(); repaint(); /*
          theJreepad.returnFocusToTree(); */
          updateWindowTitle();} });
    removeIconButton.addActionListener(new ActionListener(){
                               public void actionPerformed(ActionEvent e){ deleteNodeAction(); } });
    // Finished establishing the iconic buttons




// Add all the buttons to their respective toolbar
    toolBar.add(addAboveButton);
    toolBar.add(addBelowButton);
    toolBar.add(addButton);
    toolBar.add(removeButton);
    toolBar.add(upButton);
    toolBar.add(downButton);
    toolBar.add(indentButton);
    toolBar.add(outdentButton);
    toolBar.add(viewSelector);
    toolBar.add(Box.createGlue());


    toolBarIconic.add(newIconButton);
    toolBarIconic.add(openIconButton);
    toolBarIconic.add(saveIconButton);
//	if(!MAC_OS_X) // The separators look RUBBISH on OSX
//	  toolBarIconic.add(new JSeparator(JSeparator.VERTICAL));
//	  toolBarIconic.add(Box.createHorizontalStrut(16));
    toolBarIconic.add(addAboveIconButton);
    toolBarIconic.add(addBelowIconButton);
    toolBarIconic.add(addIconButton);
    toolBarIconic.add(removeIconButton);
//	if(!MAC_OS_X) // The separators look RUBBISH on OSX
//	  toolBarIconic.add(new JSeparator(JSeparator.VERTICAL));
//	  toolBarIconic.add(Box.createHorizontalStrut(16));
    toolBarIconic.add(upIconButton);
    toolBarIconic.add(downIconButton);
    toolBarIconic.add(outdentIconButton);
    toolBarIconic.add(indentIconButton);
//	if(!MAC_OS_X) // The separators look RUBBISH on OSX
//	  toolBarIconic.add(new JSeparator(JSeparator.VERTICAL));
//	  toolBarIconic.add(Box.createHorizontalStrut(16));
    toolBarIconic.add(viewSelectorIconic);
    toolBarIconic.add(Box.createGlue());
  }


  private ClassLoader loader = this.getClass().getClassLoader();
  private final Icon getIcon(String name){
    try{
       // TODO create single funct for all img loading
       URL iconUrl = loader.getResource("images/"+name);
         return new ImageIcon(iconUrl);
       }catch(Exception e){
         System.err.println("Jreepad: Icon image failed to load: images/"+name);
         // e.printStackTrace();// Ignore, use default icon
       }
     return null;
  }



  protected void setToolbarMode(int newMode)
  {
    switch(newMode)
    {
      case JreepadPrefs.TOOLBAR_TEXT:
        toolBar.setVisible(true);
        toolBarIconic.setVisible(false);
        break;
      case JreepadPrefs.TOOLBAR_ICON:
        toolBar.setVisible(false);
        toolBarIconic.setVisible(true);
        break;
      case JreepadPrefs.TOOLBAR_OFF:
        toolBar.setVisible(false);
        toolBarIconic.setVisible(false);
        break;
      default:
        // Invalid mode passed
        return;
    }
    getPrefs().toolbarMode = newMode;
    viewToolbarIconsMenuItem.setSelected(newMode==JreepadPrefs.TOOLBAR_ICON);
    viewToolbarTextMenuItem.setSelected(newMode==JreepadPrefs.TOOLBAR_TEXT);
    viewToolbarOffMenuItem.setSelected(newMode==JreepadPrefs.TOOLBAR_OFF);
    repaint();
  }

  public void establishAutosaveDialogue()
  {
    Box vBox, hBox;
    // Establish the autosave dialogue box
    autoSaveDialog = new JDialog(this, lang.getString("AUTOSAVE"), true);
    autoSaveDialog.setVisible(false);
    vBox = Box.createVerticalBox();
    vBox.add(Box.createGlue());
    hBox = Box.createHorizontalBox();
    hBox.add(autoSaveCheckBox = new JCheckBox(lang.getString("AUTOSAVE_EVERY"), getPrefs().autoSave));
//DSpinner version:    hBox.add(autoSavePeriodSpinner = new DSpinner(1, 1000, getPrefs().autoSavePeriod));
    hBox.add(autoSavePeriodSpinner = new JSpinner(new SpinnerNumberModel(getPrefs().autoSavePeriod, 1, 1000, 1)));
    hBox.add(new JLabel(lang.getString("AUTOSAVE_MINUTES")));
    vBox.add(hBox);
    vBox.add(Box.createGlue());
    hBox = Box.createHorizontalBox();
    hBox.add(autoSaveOkButton = new JButton(lang.getString("OK")));
    hBox.add(autoSaveCancelButton = new JButton(lang.getString("CANCEL")));
    autoSaveOkButton.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e){
                                    getPrefs().autoSavePeriod = ((Integer)(autoSavePeriodSpinner.getValue())).intValue();
//									getPrefs().autoSavePeriod = autoSavePeriodSpinner.getValue();
                                    getPrefs().autoSave = autoSaveCheckBox.isSelected();
                                    autoSaveDialog.setVisible(false);
                                    if(getPrefs().autoSave && !(autoSaveThread.isAlive()))
                                    {
                                      autoSaveWarningMessage();
                                        autoSaveThread.start();
                                    }
                                    updateWindowTitle();
                                   }});
    autoSaveCancelButton.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e){autoSaveDialog.setVisible(false);}});
    vBox.add(Box.createGlue());
    vBox.add(hBox);
    autoSaveDialog.getContentPane().add(vBox);
    // Finished establishing the autosave dialogue box
  }

  public void autoSaveWarningMessage()
  {
      JOptionPane.showMessageDialog(this, lang.getString("AUTOSAVE_ACTIVE_LONG"), lang.getString("AUTOSAVE_ACTIVE") , JOptionPane.INFORMATION_MESSAGE);
  }

  public void establishNodeUrlDisplayDialogue()
  {
    Box vBox;
    // Establish the nodeUrlDisplay dialogue box
    nodeUrlDisplayDialog = new JDialog(this, "Node URL", true);
    nodeUrlDisplayDialog.setVisible(false);
    vBox = Box.createVerticalBox();
    vBox.add(new JLabel(lang.getString("MSG_NODEURL1")));
    vBox.add(nodeUrlDisplayField = new JTextField("node://its/a/secret"));
    vBox.add(new JLabel(lang.getString("MSG_NODEURL2")));
    vBox.add(nodeUrlDisplayOkButton = new JButton(lang.getString("OK")));
    nodeUrlDisplayOkButton.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e){
                                    nodeUrlDisplayDialog.setVisible(false);
                                   }});
    nodeUrlDisplayDialog.getContentPane().add(vBox);
    // Finished: Establish the nodeUrlDisplay dialogue box
  }

  public static void main(String[] args)
  {

    // System.err.println("" + args.length + " input arguments provided.");
    // for(int i=0; i<args.length; i++){
    //   System.err.println(args[i]);
    // }

    try
    {
      UIManager.setLookAndFeel(
      UIManager.getSystemLookAndFeelClassName());
    } catch (Exception e)
    {}

    String launchFilename="", launchPrefsFilename=null;

    int ARGMODE_FILE = 0;
    int ARGMODE_PREF = 1;
    int argMode = ARGMODE_FILE;

    for(int i=0; i<args.length; i++)
    {
      if(args[i].startsWith("-h") || args[i].startsWith("--h"))
      {
        System.out.println("Jreepad command-line arguments:");
        System.out.println("  -p [prefsfile]    Load/save preferences from named location instead of default");
        System.out.println("  [filename]        Jreepad/treepad file to load on startup");
        System.out.println("  ");
        System.out.println("For example:");
        System.out.println("  java -jar Jreepad.jar -p /Users/jo/Library/jreeprefs.dat /Users/jo/Documents/mynotes.hjt");
        System.exit(1);
      }
      else if(args[i].equals("-p"))
        argMode = ARGMODE_PREF;
      else if(argMode == ARGMODE_PREF && launchPrefsFilename==null)
      {
        launchPrefsFilename=args[i];
        argMode = ARGMODE_FILE;
      }
      else if(argMode == ARGMODE_FILE && launchFilename.equals(""))
      {
        launchFilename=args[i];
      }
    }

    // System.err.println("Launching using prefs file \"" + launchPrefsFilename + "\" and loadfile \"" + launchFilename + "\"\n");

    new JreepadViewer(launchFilename, launchPrefsFilename);

    /*
    The old way of handling the arguments

    if(args.length==0)
      new JreepadViewer();
    else if(args.length==1)
      new JreepadViewer(args[0]);
    else
      System.err.println("Only one (optional) argument can be passed - the name of the HJT file to load.");
    */

  }

  /**
   * Ask the user whether to save the current file if it is unsaved.
   * @param prompt  message to display
   * @return true if the user clicked "Yes" and the save was successful or the user clicked "No" or the file didn't need to be saved;
   *         false if the user clicked "Cancel" or the save failed
   */
  private boolean askAndSave(String prompt)
  {
    if (document.isContentSaved())
      return true;
    int answer = JOptionPane.showConfirmDialog(this, prompt,
                       "Save?" , JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
    if(answer == JOptionPane.CANCEL_OPTION)
      return false;
    if(answer == JOptionPane.YES_OPTION)
    {
      SaveAction saveAction = new SaveAction();
      saveAction.actionPerformed(null);
      if (!saveAction.isSuccessful())
        return false;
    }
    return true;
  }

  private void newAction()
  {
    if (!askAndSave(lang.getString("PROMPT_SAVE_BEFORE_NEW")))
      return;

    content.remove(theJreepad);
    document = new JreepadTreeModel();
    theJreepad = new JreepadView(document);
    content.add(theJreepad);
    searchDialog.setJreepad(theJreepad);

    setTitleBasedOnFilename("");
    validate();
    repaint();
    document.setContentSaved(true);
    updateUndoRedoMenuState();
//	theJreepad.clearUndoCache();
  }

  private void openAction()
  {
    if (!askAndSave(lang.getString("PROMPT_SAVE_BEFORE_OPEN")))
      return;

    fileChooser.setCurrentDirectory(getPrefs().openLocation);
    if(fileChooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION)
        return;

    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    openFile(fileChooser.getSelectedFile());
    setCursor(Cursor.getDefaultCursor());
    updateUndoRedoMenuState();
  } // End of: openAction()

  public boolean openFile(File file)
  {
    getPrefs().openLocation = file; // Remember the open directory
    try
    {
      InputStream in = new FileInputStream(file);
      JreepadReader reader = new AutoDetectReader(getPrefs().getEncoding(), getPrefs().autoDetectHtmlArticles);
      document = reader.read(in);
      document.setSaveLocation(file);
    }
    catch(IOException e)
    {
      JOptionPane.showMessageDialog(this, e, lang.getString("MSG_LOAD_FILE_FAILED") , JOptionPane.ERROR_MESSAGE);
      return false;
    }
    content.remove(theJreepad);
    theJreepad = new JreepadView(document);
    content.add(theJreepad);
    searchDialog.setJreepad(theJreepad);

    getPrefs().exportLocation = getPrefs().importLocation = file;
    setTitleBasedOnFilename(file.getName());
    validate();
    repaint();
    return true;
  }

  private class SaveAction extends AbstractAction
  {
    private boolean askForFilename;

    private boolean successful = false;

    public SaveAction()
    {
        this(false);
    }

    public SaveAction(boolean askForFilename)
    {
        super(askForFilename ? lang.getString("MENUITEM_SAVEAS") : lang.getString("MENUITEM_SAVE"),
                getIcon("Save16.gif"));
        this.askForFilename = askForFilename;

        if (askForFilename)
        {
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_A));
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('A', MENU_MASK));
        }
        else
        {
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_S));
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('S', MENU_MASK));
            putValue(SHORT_DESCRIPTION, lang.getString("TOOLBAR_SAVE"));
        }
    }

    public void actionPerformed(ActionEvent e)
    {
        int fileType = getPrefs().mainFileType;
        if (document.getSaveLocation() != null)
            fileType = document.getFileType();

        File saveLocation = document.getSaveLocation();
        if(askForFilename || saveLocation==null || (saveLocation.isFile() && !saveLocation.canWrite()))
        {
          // Ask for filename
          SaveFileChooser fileChooser = new SaveFileChooser(getPrefs().mainFileType);
          fileChooser.setCurrentDirectory(getPrefs().openLocation);

          fileChooser.setSelectedFile(new File(document.getRootNode().getTitle() +
                       (fileType==JreepadPrefs.FILETYPE_XML?".jree":".hjt")));
          if(fileChooser.showSaveDialog(JreepadViewer.this) != JFileChooser.APPROVE_OPTION)
          {
              successful = false;
              return; // No file chosen
          }
          saveLocation = fileChooser.getSelectedFile();
          fileType = fileChooser.getFileType();
        }
        getPrefs().openLocation = saveLocation; // Remember the file's directory

        // Save the file
        try
        {
          setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

          // Select default application encoding or document encoding
          String encoding;
          if (askForFilename || document.getEncoding() == null)
            encoding = getPrefs().getEncoding();
          else
            encoding = document.getEncoding();

          // Write to either HJT or XML
          JreepadWriter writer;
          if(fileType == JreepadPrefs.FILETYPE_XML)
            writer = new XmlWriter();
          else if (fileType == JreepadPrefs.FILETYPE_XML_ENCRYPTED)
          {
              String password = document.getPassword();
              if (password == null || askForFilename)
                  password = PasswordDialog.showPasswordDialog(true);
              if (password == null)
              {
                  successful = false; // "Cancel" was pressed
                  return;
              }
              EncryptedWriter encryptedWriter = new EncryptedWriter(new XmlWriter());
              encryptedWriter.setPassword(password);
              writer = encryptedWriter;
              document.setPassword(password);
          }
          else
            writer = new TreepadWriter(encoding);

          OutputStream fos = new FileOutputStream(saveLocation);
          writer.write(fos, document);
          fos.close();

	  /* not working in JDK 9+, see JEP 272 for alleged replacement http://openjdk.java.net/jeps/272
          if(MAC_OS_X){
            com.apple.eio.FileManager.setFileTypeAndCreator(saveLocation.toString(),
                    appleAppCode, appleAppCode);
          }
	  */

          // When calling "Save As..." remember the _new_ file settings
          document.setSaveLocation(saveLocation);
          document.setFileType(fileType);
          document.setEncoding(encoding);

          updateWindowTitle();
          addToOpenRecentMenu(saveLocation);
          savePreferencesFile();
          setCursor(Cursor.getDefaultCursor());
          successful = true;
        }
        catch(IOException err)
        {
          setCursor(Cursor.getDefaultCursor());
          JOptionPane.showMessageDialog(JreepadViewer.this, err, lang.getString("TITLE_FILE_ERROR") , JOptionPane.ERROR_MESSAGE);
          successful = false;
        }
    }

    public boolean isSuccessful()
    {
        return successful;
    }
  }

  private boolean backupToAction()
  {
    try
    {
      fileChooser.setCurrentDirectory(getPrefs().backupLocation);
      if(fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION && checkOverwrite(fileChooser.getSelectedFile()))
      {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        getPrefs().backupLocation = fileChooser.getSelectedFile();

        // Write to either HJT
        JreepadWriter writer = new TreepadWriter(getPrefs().getEncoding());
        OutputStream fos = new FileOutputStream(getPrefs().backupLocation);
        writer.write(fos, document);
        fos.close();

        setCursor(Cursor.getDefaultCursor());
        return true;
      }
      else
        return false;
    }
    catch(IOException err)
    {
      setCursor(Cursor.getDefaultCursor());
      JOptionPane.showMessageDialog(this, err, lang.getString("TITLE_FILE_ERROR") , JOptionPane.ERROR_MESSAGE);
    }
    return false;
  } // End of: backupToAction()


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
    setTitle(windowTitle + (document.isContentSaved()?"":"*") + (getPrefs().autoSave?" ["+lang.getString("AUTOSAVE_ACTIVE")+"]":""));
  }

  private static final int FILE_FORMAT_HJT=1;
  private static final int FILE_FORMAT_HTML=2;
  private static final int FILE_FORMAT_XML=3;
  private static final int FILE_FORMAT_TEXT=4;
  private static final int FILE_FORMAT_TEXTASLIST=5;
  private static final int FILE_FORMAT_ARTICLESTOTEXT=6;
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

      if(fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
      {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        getPrefs().importLocation = fileChooser.getSelectedFile();

        switch(importFormat)
        {
          case FILE_FORMAT_HJT:
            InputStream in = new FileInputStream(getPrefs().importLocation);
            JreepadReader reader = new AutoDetectReader(getPrefs().getEncoding(), getPrefs().autoDetectHtmlArticles);
            theJreepad.addChild(reader.read(in).getRootNode());
            break;
          case FILE_FORMAT_TEXT:
            theJreepad.addChildrenFromTextFiles(fileChooser.getSelectedFiles());
            break;
          case FILE_FORMAT_TEXTASLIST:
            theJreepad.addChildrenFromListTextFile(new InputStreamReader(new FileInputStream(getPrefs().importLocation), getPrefs().getEncoding()));
            break;
          default:
            setCursor(Cursor.getDefaultCursor());
            JOptionPane.showMessageDialog(this,  "Unknown which format to import - coding error! Oops!",lang.getString("TITLE_MISC_ERROR") , JOptionPane.ERROR_MESSAGE);
            return;
        }
        updateWindowTitle();
      }
      fileChooser.setMultiSelectionEnabled(false);
      setCursor(Cursor.getDefaultCursor());
    }
    catch(IOException err)
    {
      setCursor(Cursor.getDefaultCursor());
      JOptionPane.showMessageDialog(this, err, lang.getString("TITLE_MISC_ERROR") , JOptionPane.ERROR_MESSAGE);
    }
  } // End of: importAction()

  private void exportAction(int exportFormat)
  {
    try
    {
      fileChooser.setCurrentDirectory(getPrefs().exportLocation);
      String suggestFilename = theJreepad.getCurrentNode().getTitle();
      switch(exportFormat)
      {
        case FILE_FORMAT_HJT:
          suggestFilename += ".hjt";
          break;
        case FILE_FORMAT_HTML:
          suggestFilename += ".html";
          break;
        case FILE_FORMAT_XML:
          suggestFilename += ".xml";
          break;
        case FILE_FORMAT_TEXT:
        case FILE_FORMAT_TEXTASLIST:
        case FILE_FORMAT_ARTICLESTOTEXT:
          suggestFilename += ".txt";
          break;
      }
      fileChooser.setSelectedFile(new File(suggestFilename));
      if(fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION && checkOverwrite(fileChooser.getSelectedFile()))
      {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        getPrefs().exportLocation = fileChooser.getSelectedFile();

        String output = null;
        JreepadWriter writer = null;
        switch(exportFormat)
        {
          case FILE_FORMAT_HTML:
            writer = new HtmlWriter(getPrefs().getEncoding(),
                getPrefs().htmlExportArticleType,
                getPrefs().htmlExportUrlsToLinks,
                getPrefs().htmlExportAnchorLinkType);
            break;
          case FILE_FORMAT_XML:
            writer= new XmlWriter();
            break;
          case FILE_FORMAT_HJT:
            writer= new TreepadWriter(getPrefs().getEncoding());
            break;
          case FILE_FORMAT_TEXT:
            output = theJreepad.getCurrentNode().getContent();
            break;
          case FILE_FORMAT_TEXTASLIST:
            output = theJreepad.getCurrentNode().exportTitlesAsList();
            break;
          case FILE_FORMAT_ARTICLESTOTEXT:
            int answer = JOptionPane.showConfirmDialog(this, lang.getString("PROMPT_INCLUDE_TITLES"),
                       lang.getString("TITLE_INCLUDE_TITLES") , JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            boolean titlesToo = (answer == JOptionPane.YES_OPTION);
            output = theJreepad.getCurrentNode().exportArticlesToText(titlesToo);
            break;
          default:
            setCursor(Cursor.getDefaultCursor());
            JOptionPane.showMessageDialog(this,  "Unknown which format to export - coding error! Oops!",lang.getString("TITLE_MISC_ERROR") , JOptionPane.ERROR_MESSAGE);
            return;
        }

        OutputStream fos = new FileOutputStream(getPrefs().exportLocation);
        if (writer != null)
        {
          writer.write(fos, new JreepadTreeModel(theJreepad.getCurrentNode()));
          fos.close();
        }
        else // assume (output != null)
        {
          OutputStreamWriter osw = new OutputStreamWriter(fos, getPrefs().getEncoding());
          osw.write(output);
          osw.close();
        }
        setCursor(Cursor.getDefaultCursor());
      }
    }
    catch(IOException err)
    {
      setCursor(Cursor.getDefaultCursor());
      JOptionPane.showMessageDialog(this, err, lang.getString("TITLE_FILE_ERROR") , JOptionPane.ERROR_MESSAGE);
    }
  } // End of: exportAction()

  private void subtreeToBrowserForPrintAction()
  {
    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

    HtmlWriter writer = new HtmlWriter(
        getPrefs().getEncoding(),
        getPrefs().htmlExportArticleType,
        getPrefs().htmlExportUrlsToLinks,
        getPrefs().htmlExportAnchorLinkType,
        true);
    String output = writer.write(theJreepad.getCurrentNode());

    toBrowserForPrintAction(output);

    setCursor(Cursor.getDefaultCursor());
  } // End of: subtreeToBrowserForPrintAction()

  private void articleToBrowserForPrintAction()
  {
    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

    String output = theJreepad.getCurrentNode().getArticle().exportAsHtml(
      getPrefs().htmlExportArticleType, getPrefs().htmlExportUrlsToLinks,
      getPrefs().htmlExportAnchorLinkType, true);

    toBrowserForPrintAction(output);
    setCursor(Cursor.getDefaultCursor());
  } // End of: articleToBrowserForPrintAction()

  private void toBrowserForPrintAction(String output)
  {
    String surl = "";
    try
    {
      File systemTempFile = File.createTempFile("TMPjree", ".html");

      FileOutputStream fO = new FileOutputStream(systemTempFile);
      DataOutputStream dO = new DataOutputStream(fO);
      dO.writeBytes(output);
      dO.close();
      fO.close();
      setCursor(Cursor.getDefaultCursor());

      // Now launch the file in a browser...
      surl = systemTempFile.toURL().toString();
      new BrowserLauncher(null).openURLinBrowser(surl);
    }
    catch (BrowserLaunchingInitializingException e)
    {
      displayBrowserLauncherException(e, surl);
    }
    catch (BrowserLaunchingExecutionException e)
    {
      displayBrowserLauncherException(e, surl);
    }
    catch (UnsupportedOperatingSystemException e)
    {
      displayBrowserLauncherException(e, surl);
    }
    catch (IOException err)
    {
      JOptionPane.showMessageDialog(this, err, lang.getString("TITLE_FILE_ERROR"),
        JOptionPane.ERROR_MESSAGE);
    }
  }

  private void displayBrowserLauncherException(Exception e, String url)
  {
    JOptionPane.showMessageDialog(this, "Error while opening URL:\n" + url + "\n"
      + e.getMessage() + "\n\n"
      + "The \"BrowserLauncher\" used to open a URL is an open-source Java library \n"
      + "separate from Jreepad itself - i.e. a separate Sourceforge project. \n"
      + "It may be a good idea to submit a bug report to\n"
      + "http://browserlaunch2.sourceforge.net/\n\n"
      + "If you do, please remember to supply information about the operating system\n"
      + "you are using - which type, and which version.", lang.getString("TITLE_MISC_ERROR"),
      JOptionPane.ERROR_MESSAGE);
  }


  public void quitAction()
  {
    // We need to check about warning-if-unsaved!

    // For a multiple-Jreepad interface, we would need to use:
    //  for(int i=0; i<theApps.length(); i++)
    //  {
    //    currApp = getApp(i);
    //    if(currApp.warnAboutUnsaved())
    //    {
    //

    if (!askAndSave(lang.getString("PROMPT_SAVE_BEFORE_QUIT")))
      return;

    // Save preferences - including window position and size, and open/closed state of the current tree's nodes
    getPrefs().treePathCollection = new TreePathCollection(theJreepad.getAllExpandedPaths());
    getPrefs().windowLeft = getX();
    getPrefs().windowTop = getY();
    getPrefs().windowWidth = getWidth();
    getPrefs().windowHeight = getHeight();
    savePreferencesFile();

    System.exit(0);
  }

  private void savePreferencesFile()
  {
    getPrefs().save();
    /*
    try
    {
      ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(prefsFile));
      out.writeObject(getPrefs());
      out.close();
    }
    catch(IOException err)
    {
    }
    */
  }

  private void setViewMode(int mode)
  {
    theJreepad.setViewMode(mode);
    // Update the dropdown menu
    if(mode==JreepadPrefs.VIEW_ARTICLE){
      viewSelector.setSelectedIndex(2);
    }else if(mode==JreepadPrefs.VIEW_TREE){
      viewSelector.setSelectedIndex(1);
    }else{
      viewSelector.setSelectedIndex(0);
    }

    // Update the preferences object
    getPrefs().viewWhich = mode;

    // Update the undo menus
    updateUndoRedoMenuState();
  }

  private static JreepadPrefs getPrefs() { return JreepadView.getPrefs(); }
  private static void setPrefs(JreepadPrefs thesePrefs) { JreepadView.setPrefs(thesePrefs); }

  private void setViewToolbar(boolean boo)
  {
    toolBar.setVisible(boo);
  }

  private void undoAction()
  {
/*
    if(theJreepad.canWeUndo())
      theJreepad.undoAction();
    else
      JOptionPane.showMessageDialog(this, lang.getString("MSG_NOTHING_TO_UNDO"), "No change" , JOptionPane.INFORMATION_MESSAGE);
*/
    UndoManager undoMgr = theJreepad.getCurrentNode().getArticle().getUndoMgr();
    String undoStyle = undoMgr.getUndoPresentationName();
    try{
      // This "while" should roll multiple adds or deletes into one.
      //System.out.println(undoStyle);
      //System.out.println(undoMgr.getUndoPresentationName());
      while(undoStyle.equals(undoMgr.getUndoPresentationName()))
        undoMgr.undo();
    }catch(CannotUndoException ex){
      //JOptionPane.showMessageDialog(this, lang.getString("MSG_NOTHING_TO_UNDO"), "No change" , JOptionPane.INFORMATION_MESSAGE);
    }
    updateWindowTitle();
    updateUndoRedoMenuState();
  }
  private void redoAction(){
    UndoManager undoMgr = theJreepad.getCurrentNode().getArticle().getUndoMgr();
    String redoStyle = undoMgr.getRedoPresentationName();
    try{
      while(redoStyle.equals(undoMgr.getRedoPresentationName()))
        undoMgr.redo();
    }catch(CannotRedoException ex){
      //JOptionPane.showMessageDialog(this, lang.getString("MSG_NOTHING_TO_UNDO"), "No change" , JOptionPane.INFORMATION_MESSAGE);
    }
    updateWindowTitle();
    updateUndoRedoMenuState();
  }

  public void updateUndoRedoMenuState(){
    undoMenuItem.setEnabled(isArticleUndoPossible());
    redoMenuItem.setEnabled(isArticleRedoPossible());
  }

  public boolean isArticleUndoPossible(){
    if(getPrefs().viewWhich == JreepadPrefs.VIEW_TREE)
      return false;

    if(theJreepad.getCurrentNode().getArticle().getArticleMode() != JreepadArticle.ARTICLEMODE_ORDINARY)
      return false;

    // Deactivated: since this class can't tell if text has been typed or not...
    // if(!theJreepad.getCurrentNode().undoMgr.canUndo())
    //   return false;

    return true;
  }
  public boolean isArticleRedoPossible(){
    if(getPrefs().viewWhich == JreepadPrefs.VIEW_TREE)
      return false;

    if(theJreepad.getCurrentNode().getArticle().getArticleMode() != JreepadArticle.ARTICLEMODE_ORDINARY)
      return false;

    if(!theJreepad.getCurrentNode().getArticle().getUndoMgr().canRedo())
      return false;

    return true;
  }

  private void aboutAction()
  {
              JOptionPane.showMessageDialog(this,
              "Jreepad v " + version + "\n\n" +
              lang.getString("HELP_ABOUT") +
              "\n" +
              "\nJreepad \u00A9 2004-2007 Dan Stowell" +
              "\n" +
              "\nhttp://jreepad.sourceforge.net"
              ,
              "About Jreepad",
              JOptionPane.INFORMATION_MESSAGE);
  }

  private void showAutoSaveDialog()
  {
    // The autosave simply launches a background thread which periodically triggers saveAction if saveLocation != null
    autoSaveCheckBox.setSelected(getPrefs().autoSave);
    autoSavePeriodSpinner.getModel().setValue(new Integer(getPrefs().autoSavePeriod));
 //   autoSavePeriodSpinner.setValue(getPrefs().autoSavePeriod);
    autoSaveDialog.setVisible(true);
    autoSaveDialog.toFront();
  }

  private void showPrefsDialog()
  {
    prefsDialog.setVisible(true);
    prefsDialog.toFront();

    // Apply preferences that immediately affect the GUI
    funkyGreenStrip.setVisible(getPrefs().showGreenStrip);
    webSearchMenuItem.setText(getPrefs().webSearchName);
    characterWrapArticleMenuItem.setText(JreepadViewer.lang.getString("MENUITEM_HARDWRAP1")
        + getPrefs().characterWrapWidth + JreepadViewer.lang.getString("MENUITEM_HARDWRAP2"));
  }

  private void deleteNodeAction()
  {
    if(JOptionPane.showConfirmDialog(this, lang.getString("PROMPT_CONFIRM_DELETE")+":\n"+theJreepad.getCurrentNode().getTitle(), lang.getString("TITLE_CONFIRM_DELETE"),
               JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
    theJreepad.removeNode();
    theJreepad.returnFocusToTree();
    updateWindowTitle();
  }

  private void getTreepadNodeUrl()
  {
//    String ret = theJreepad.getTreepadNodeUrl();
    nodeUrlDisplayField.setText(theJreepad.getTreepadNodeUrl());
    nodeUrlDisplayDialog.setVisible(true);
  }

  private boolean checkOverwrite(File theFile)
  {
    // If file doesn't already exist then fine
    if(theFile == null || !theFile.exists())
        return true;

    // Else we need to confirm
    return (JOptionPane.showConfirmDialog(this, lang.getString("PROMPT_CONFIRM_OVERWRITE1")+" "+theFile.getName()+" "+lang.getString("PROMPT_CONFIRM_OVERWRITE2"),
                lang.getString("TITLE_CONFIRM_OVERWRITE"),
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION);
  }

  public void showLicense()
  {
              JOptionPane.showMessageDialog(this,
lang.getString("HELP_LICENSE") + "\n\n           http://www.gnu.org/copyleft/gpl.html\n" +
             "\n"
              ,
              lang.getString("MENUITEM_LICENSE"),
              JOptionPane.INFORMATION_MESSAGE);
  }

  public void wrapContentToCharWidth()
  {
    theJreepad.wrapContentToCharWidth(getPrefs().characterWrapWidth);
  }
  public void stripAllTags()
  {
    if(JOptionPane.showConfirmDialog(this, lang.getString("PROMPT_CONFIRM_STRIPTAGS"),
                lang.getString("TITLE_CONFIRM_STRIPTAGS"),
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
      theJreepad.stripAllTags();
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

  private void insertDate()
  {
    theJreepad.insertDate();
  }

  private class FileOpeningActionListener implements ActionListener
  {
    File f;
    FileOpeningActionListener(File f) 		   {this.f = f;}
    public void actionPerformed(ActionEvent e) {openFile(f);}
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


// Generic registration with the Mac OS X application menu.  Checks the platform, then attempts
  // to register with the Apple EAWT.
  // This method calls OSXAdapter.registerMacOSXApplication() and OSXAdapter.enablePrefs().
  // See OSXAdapter.java for the signatures of these methods.
  public void macOSXRegistration() {
    if (MAC_OS_X) {
      /* not working in JDK 9+, see JEP 272 for alleged replacement http://openjdk.java.net/jeps/272
      try {
        Class osxAdapter = Class.forName("jreepad.OSXAdapter");

        Class[] defArgs = {JreepadViewer.class};
        Method registerMethod = osxAdapter.getDeclaredMethod("registerMacOSXApplication", defArgs);
        if (registerMethod != null) {
          Object[] args = { this };
          registerMethod.invoke(osxAdapter, args);
        }
      } catch (NoClassDefFoundError e) {
        // This will be thrown first if the OSXAdapter is loaded on a system without the EAWT
        // because OSXAdapter extends ApplicationAdapter in its def
        System.err.println("This version of Mac OS X does not support the Apple EAWT.  Application Menu handling has been disabled (" + e + ")");
      } catch (ClassNotFoundException e) {
        // This shouldn't be reached; if there's a problem with the OSXAdapter we should get the
        // above NoClassDefFoundError first.
        System.err.println("This version of Mac OS X does not support the Apple EAWT.  Application Menu handling has been disabled (" + e + ")");
      } catch (Exception e) {
        System.err.println("Exception while loading the OSXAdapter:");
        e.printStackTrace();
      }
      */
    }
  }


// General info dialog.  The OSXAdapter calls this method when "About OSXAdapter"
  // is selected from the application menu.
  public void about() {
    aboutAction();
  }

  // General preferences dialog.  The OSXAdapter calls this method when "Preferences..."
  // is selected from the application menu.
  public void preferences() {
//    prefs.setSize(320, 240);
//    prefs.setLocation((int)this.getLocation().getX() + 22, (int)this.getLocation().getY() + 22);
//    prefs.setResizable(false);
//    prefs.setVisible(true);
  }

  // General info dialog.  The OSXAdapter calls this method when "Quit OSXAdapter"
  // is selected from the application menu, Cmd-Q is pressed, or "Quit" is selected from the Dock.
  public void quit() {
      quitAction();
  }


  protected void systemClipboardToNewNode()
  {
    Transferable cont = systemClipboard.getContents(this);

/*
    DataFlavor[] flavs = cont.getTransferDataFlavors();
    System.out.println("Data flavors supported by contents:\n");
    for(int i=0; i<flavs.length; i++)
      System.out.println("  " + flavs[i].getHumanPresentableName() + "\n"
                              + flavs[i].getMimeType() + "\n");
*/

    if(cont == null)
    {
      JOptionPane.showMessageDialog(this, lang.getString("MSG_CLIPBOARD_EMPTY"), lang.getString("TITLE_CLIPBOARD_EMPTY") , JOptionPane.ERROR_MESSAGE);
      return;
    }
    try
    {
//      Reader readIt = DataFlavor.getTextPlainUnicodeFlavor().getReaderForText(cont);
      Reader readIt = DataFlavor.stringFlavor.getReaderForText(cont);
      char[] theStuff = new char[64];
      int numRead;
      StringBuffer theStuffBuf = new StringBuffer();
      while((numRead = readIt.read(theStuff)) != -1)
      {
        theStuffBuf.append(theStuff, 0, numRead);
      }

      String contStr = theStuffBuf.toString();
      String titStr;
      int newlinePos = contStr.indexOf("\n");
      if(newlinePos == -1)
      {
        titStr = contStr;
        contStr = "";
      }
      else
        titStr = contStr.substring(0, newlinePos-1);
      theJreepad.addChild(new JreepadNode(titStr, contStr));
    }
    catch(Exception err)
    {
      JOptionPane.showMessageDialog(this, lang.getString("MSG_CLIPBOARD_TEXTERROR"), lang.getString("TITLE_CLIPBOARD_ERROR") , JOptionPane.ERROR_MESSAGE);
      return;
    }
  }

  public void keyboardHelp()
  {
    String menuText;
    if(MAC_OS_X)
      menuText = "Apple";
    else
      switch(MENU_MASK)
      {
        case Event.ALT_MASK:
          menuText = "Alt";
          break;
        case Event.META_MASK:
          menuText = "Meta";
          break;
        case Event.CTRL_MASK:
        default:
          menuText = "Ctrl";
          break;
      }
              JOptionPane.showMessageDialog(this,
              "\nNAVIGATING AROUND THE TREE:" +
              "\nUse the arrow (cursor) keys to navigate around the tree." +
              "\nUp/down will move you up/down the visible nodes." +
              "\nLeft/right will expand/collapse nodes." +
              "\n" +
              "\nADDING/DELETING NODES:" +
              "\n["+menuText+"+T] Add sibling node above current node" +
              "\n["+menuText+"+B] Add sibling node below current node" +
              "\n["+menuText+"+D] Add child node to current node" +
              "\n["+menuText+"+K] Delete current node" +
              "\n" +
              "\nMOVING NODES:" +
              "\n["+menuText+"+up arrow] Move node up" +
              "\n["+menuText+"+down arrow] Move node down" +
              "\n["+menuText+"+right arrow] Indent node" +
              "\n["+menuText+"+left arrow] Outdent node" +
              "\n" +
              "\nCOPYING AND PASTING:" +
              "\n["+menuText+"+X] Cut selected text" +
              "\n["+menuText+"+C] Copy selected text" +
              "\n["+menuText+"+V] Paste selected text" +
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
              lang.getString("HELP_KEYBOARD_TITLE"),
              JOptionPane.INFORMATION_MESSAGE);
  }

  public void linksHelp()
  {
              JOptionPane.showMessageDialog(this,
              lang.getString("HELP_LINKS")
/*
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
              "\n" +
              "\n" +
              "\n" +
              "\n" +
              "\n" +
              "\n" +
              "\n" +
              "\n[] " +
              "\n" +
              ""
*/
              ,
              lang.getString("HELP_LINKS_TITLE"),
              JOptionPane.INFORMATION_MESSAGE);
  }

  public void dragDropHelp()
  {
              JOptionPane.showMessageDialog(this,
              lang.getString("HELP_DRAGDROP")
/*
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
              "\n" +
              "\n" +
              "\n" +
              "\n" +
              "\n" +
              "\n" +
              "\n" +
              "\n[] " +
              "\n" +
              ""
*/
              ,
              lang.getString("HELP_DRAGDROP_TITLE"),
              JOptionPane.INFORMATION_MESSAGE);
  }


/*
  // Replacement for the "JSpinner" which is not available in Java 1.3 or 1.2
  static class DSpinner extends Box
  {
    private int min, max, val;
    private JTextField textField;
    private JButton upBut, downBut;
    private ActionListener al;

    DSpinner(int min, int max, int myVal)
    {
      super(BoxLayout.X_AXIS);
      this.min=this.val=min;
      this.max=max;
      this.add(Box.createGlue());
      this.add(textField = new JTextField(val));
      this.add(downBut = new JButton("-"));
      this.add(upBut = new JButton("+"));
      this.add(Box.createGlue());

      downBut.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) {
                              setValue(val-1);
                              if(al!=null)
                                al.actionPerformed(null);
                              }});
      upBut.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) {
                              setValue(val+1);
                              if(al!=null)
                                al.actionPerformed(null);
                              }});
      setValue(myVal);
    }

    int getValue()
    {
      try
      {
        val = Integer.parseInt(textField.getText());
      }
      catch(NumberFormatException e)
      {
      }
      return val;
    }
    void setValue(int newVal)
    {
      if(newVal>=min && newVal<=max)
        val=newVal;
        textField.setText("" + val);
    }
    void addActionListener(ActionListener al)
    {
      this.al = al;
      textField.addActionListener(al);
    }
    void addCaretListener(CaretListener cl)
    {
      textField.addCaretListener(cl);
    }

  } // End of class DSpinner
*/

  static class ColouredStrip extends JPanel
  {
    Color col;
    ColouredStrip(Color col, int maxWidth, int maxHeight)
    {
      super(false);
      setMaximumSize(new Dimension(maxWidth, maxHeight));
      setBackground(col);
//      this.col = col;
    }

//    public void paint(Graphics gggg)
//    {
//      Graphics2D g = (Graphics2D)gggg;
//      g.setPaint(col);
//      g.fillRect(0,0,getWidth(),getHeight());
//    }
  }




/*
  // Methods required by Apple's "ApplicationListener" interface
  public void handleOpenFile(ApplicationEvent ae)
  {
     System.err.println("Jreepad.handleOpenFile() - ApplicationEvent is " + ae);
     openHjtFile(new java.io.File(ae.getFilename()));
     ae.setHandled(true);
  }
  public void handleOpenApplication(ApplicationEvent ae)
  {
     System.err.println("Jreepad.handleOpenApplication() - ApplicationEvent is " + ae);
  }
  public void handleReOpenApplication(ApplicationEvent ae)
  {
     System.err.println("Jreepad.handleReOpenApplication() - ApplicationEvent is " + ae);
  }
  public void handlePrintFile(ApplicationEvent ae)
  {
     System.err.println("Jreepad.handlePrintFile() - ApplicationEvent is " + ae);
     ae.setHandled(true);
     toBrowserForPrintAction();
  }
    public void handleAbout(ApplicationEvent ae) {
        ae.setHandled(true);
        about();
    }
    public void handlePreferences(ApplicationEvent ae) {
        preferences();
        ae.setHandled(true);
    }
    public void handleQuit(ApplicationEvent ae) {
        //
        //	You MUST setHandled(false) if you want to delay or cancel the quit.
        //	This is important for cross-platform development -- have a universal quit
        //	routine that chooses whether or not to quit, so the functionality is identical
        //	on all platforms.  This example simply cancels the AppleEvent-based quit and
        //	defers to that universal method.
        //
        ae.setHandled(false);
        quit();
    }
*/
}
