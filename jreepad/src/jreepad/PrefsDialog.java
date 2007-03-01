package jreepad;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

/**
 * The preferences dialog.
 *
 * @version $Id$
 */
public class PrefsDialog extends JDialog
{
    private JCheckBox loadLastFileOnOpenCheckBox;
    private JCheckBox autoDateNodesCheckBox;
    private JCheckBox autoDetectHtmlCheckBox;
    private JComboBox fileEncodingSelector;
    private JComboBox fileFormatSelector;
    private JCheckBox showGreenStripCheckBox;
    private JTextField dateFormatField;
    private JComboBox defaultSearchModeSelector;
    private JSpinner wrapWidthSpinner;
    private Box webSearchPrefsBox;
    private JTextField webSearchNameField;
    private JTextField webSearchPrefixField;
    private JTextField webSearchPostfixField;
    private JComboBox htmlExportModeSelector;
    private JCheckBox urlsToLinksCheckBox;
    private JComboBox htmlExportAnchorTypeSelector;
    private JButton prefsOkButton;
    private JButton prefsCancelButton;

    public PrefsDialog(Frame owner)
    {
        super(owner, JreepadViewer.lang.getString("PREFS_WINDOWTITLE"), true);
        setVisible(false);

        Box vBox = Box.createVerticalBox();
        vBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        Box genPrefVBox = Box.createVerticalBox();
        vBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        genPrefVBox.add(loadLastFileOnOpenCheckBox = new JCheckBox(JreepadViewer.lang
            .getString("PREFS_LOADLASTFILEONOPEN"), getPrefs().loadLastFileOnOpen));
        loadLastFileOnOpenCheckBox.setHorizontalAlignment(SwingConstants.LEFT);
        loadLastFileOnOpenCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        genPrefVBox.add(autoDateNodesCheckBox = new JCheckBox(JreepadViewer.lang
            .getString("PREFS_AUTODATE_NODES"), getPrefs().autoDateInArticles));
        autoDateNodesCheckBox.setHorizontalAlignment(SwingConstants.LEFT);
        autoDateNodesCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        genPrefVBox.add(autoDetectHtmlCheckBox = new JCheckBox(JreepadViewer.lang
            .getString("PREFS_AUTODETECT_HTML"), getPrefs().autoDetectHtmlArticles));
        autoDetectHtmlCheckBox.setHorizontalAlignment(SwingConstants.LEFT);
        autoDetectHtmlCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        Box hBox = Box.createHorizontalBox();
        hBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        // hBox.add(Box.createGlue());
        hBox.add(new JLabel(JreepadViewer.lang.getString("PREFS_CHAR_ENCODING"), SwingConstants.LEFT));
        hBox.add(fileEncodingSelector = new JComboBox(JreepadPrefs.characterEncodings));
        fileEncodingSelector.setAlignmentX(Component.LEFT_ALIGNMENT);
        hBox.add(Box.createGlue());
        genPrefVBox.add(hBox);
        fileEncodingSelector.setSelectedIndex(getPrefs().fileEncoding);

        hBox = Box.createHorizontalBox();
        hBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        // hBox.add(Box.createGlue());
        hBox.add(new JLabel(JreepadViewer.lang.getString("PREFS_SAVE_FILETYPE"), SwingConstants.LEFT));
        hBox.add(fileFormatSelector = new JComboBox(JreepadPrefs.mainFileTypes));
        fileFormatSelector.setAlignmentX(Component.LEFT_ALIGNMENT);
        hBox.add(Box.createGlue());
        genPrefVBox.add(hBox);
        fileFormatSelector.setSelectedIndex(getPrefs().mainFileType);

        // genPrefVBox.add(quoteCsvCheckBox = new JCheckBox(lang.getString("PREFS_QUOTE_CSV"),
        // getPrefs().addQuotesToCsvOutput));
        // quoteCsvCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        // quoteCsvCheckBox.setHorizontalAlignment(SwingConstants.LEFT);
        genPrefVBox.add(showGreenStripCheckBox = new JCheckBox(JreepadViewer.lang.getString("PREFS_GREEN_STRIP"),
            getPrefs().showGreenStrip));
        showGreenStripCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        showGreenStripCheckBox.setHorizontalAlignment(SwingConstants.LEFT);

        hBox = Box.createHorizontalBox();
        hBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        hBox.add(new JLabel(JreepadViewer.lang.getString("PREFS_DATEFORMAT_LABEL"), SwingConstants.LEFT));
        dateFormatField = new JTextField(getPrefs().dateFormat, 30);
        hBox.add(dateFormatField);
        hBox.add(new JLabel("(" + JreepadViewer.lang.getString("PREFS_DATEFORMAT_LABEL2") + ")", SwingConstants.LEFT));
        genPrefVBox.add(hBox);

        JPanel genPanel = new JPanel();
        genPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        genPanel.add(genPrefVBox);
        genPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
            JreepadViewer.lang.getString("PREFS_GENERAL")));
        vBox.add(genPanel);

        genPanel = new JPanel();
        genPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        hBox = Box.createHorizontalBox();
        hBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        hBox.add(new JLabel(JreepadViewer.lang.getString("PREFS_DEFAULT_LINK_ACTION"), SwingConstants.LEFT));
        hBox.add(defaultSearchModeSelector = new JComboBox(new String[] {
                        JreepadViewer.lang.getString("PREFS_DEFAULT_LINK_ACTION_WEBSEARCH"),
                        JreepadViewer.lang.getString("PREFS_DEFAULT_LINK_ACTION_NODESEARCH") }));
        hBox.add(Box.createGlue());
        defaultSearchModeSelector.setSelectedIndex(getPrefs().defaultSearchMode);
        defaultSearchModeSelector.setAlignmentX(Component.LEFT_ALIGNMENT);
        genPanel.add(hBox);
        genPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
            JreepadViewer.lang.getString("PREFS_LINK_ACTION_NAME")));
        vBox.add(genPanel);

        genPanel = new JPanel();
        genPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        genPrefVBox = Box.createVerticalBox();
        // hBox = Box.createHorizontalBox();
        // hBox.add(wrapToWindowCheckBox = new JCheckBox("Wrap article to window width",
        // getPrefs().wrapToWindow));
        // hBox.add(new JLabel("(won't take effect until you restart Jreepad)"));
        // genPrefVBox.add(hBox);
        hBox = Box.createHorizontalBox();
        hBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        hBox.add(new JLabel(JreepadViewer.lang.getString("PREFS_HARDWRAP_WIDTH"), SwingConstants.LEFT));
        hBox.add(wrapWidthSpinner = new JSpinner(new SpinnerNumberModel(
            getPrefs().characterWrapWidth, 1, 1000, 1)));
        hBox.add(Box.createGlue());
        wrapWidthSpinner.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrapWidthSpinner.getEditor().setAlignmentX(Component.LEFT_ALIGNMENT);
        // hBox.add(wrapWidthSpinner = new DSpinner(1,1000,getPrefs().characterWrapWidth));
        hBox.add(Box.createGlue());
        genPrefVBox.add(hBox);
        genPanel.add(genPrefVBox);
        genPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
            JreepadViewer.lang.getString("PREFS_HARDWRAP_NAME")));
        vBox.add(genPanel);

        // fontsPrefsBox = Box.createHorizontalBox();
        // fontsPrefsBox.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
        // "Font (for article)"));
        // Box tempVBox = Box.createHorizontalBox();
        // tempVBox.add(new JLabel("Font for tree:"));
        // tempVBox.add(treeFontFamilySelector = new JComboBox(fonts));
        // fontsPrefsBox.add(tempVBox);
        // tempVBox = Box.createHorizontalBox();
        // fontsPrefsBox.add(new JLabel("Font face:"));
        // fontsPrefsBox.add(articleFontFamilySelector = new JComboBox(fonts));
        // fontsPrefsBox.add(tempVBox);
        // tempVBox = Box.createHorizontalBox();
        // tempVBox.add(new JLabel("Font size:"));
        // fontsPrefsBox.add(articleFontSizeSelector = new JComboBox(fontSizes));
        // fontsPrefsBox.add(new JLabel("pt"));
        // fontsPrefsBox.add(tempVBox);
        // vBox.add(fontsPrefsBox);

        webSearchPrefsBox = Box.createVerticalBox();
        webSearchPrefsBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        hBox = Box.createHorizontalBox();
        hBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        hBox.add(new JLabel(JreepadViewer.lang.getString("PREFS_WEBSEARCH_LABEL1"), SwingConstants.LEFT));
        hBox.add(webSearchNameField = new JTextField(getPrefs().webSearchName));
        webSearchNameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        hBox.add(new JLabel(JreepadViewer.lang.getString("PREFS_WEBSEARCH_LABEL2"), SwingConstants.LEFT));
        hBox.add(Box.createGlue());
        webSearchPrefsBox.add(hBox);
        hBox = Box.createHorizontalBox();
        hBox.add(new JLabel("http://", SwingConstants.LEFT));
        hBox.add(webSearchPrefixField = new JTextField(getPrefs().webSearchPrefix));
        webSearchPrefixField.setAlignmentX(Component.LEFT_ALIGNMENT);
        hBox.add(new JLabel(JreepadViewer.lang.getString("PREFS_SELECTEDTEXT_PLACEHOLDER"), SwingConstants.LEFT));
        hBox.add(webSearchPostfixField = new JTextField(getPrefs().webSearchPostfix));
        webSearchPostfixField.setAlignmentX(Component.LEFT_ALIGNMENT);
        webSearchPrefsBox.add(hBox);
        JPanel webSearchPanel = new JPanel();
        webSearchPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        webSearchPanel.add(webSearchPrefsBox);
        webSearchPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory
            .createEtchedBorder(), JreepadViewer.lang.getString("PREFS_WEBSEARCH_NAME")));
        vBox.add(webSearchPanel);

        // Now the HTML export options
        genPanel = new JPanel();
        genPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        genPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
            JreepadViewer.lang.getString("PREFS_HTML_NAME")));
        Box htmlVBox = Box.createVerticalBox();
        htmlVBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        hBox = Box.createHorizontalBox();
        hBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        // hBox.add(Box.createGlue());
        hBox.add(new JLabel(JreepadViewer.lang.getString("PREFS_HTML_TREATTEXTAS"), SwingConstants.LEFT));
        htmlExportModeSelector = new JComboBox(JreepadArticle.getHtmlExportArticleTypes());
        htmlExportModeSelector.setAlignmentX(Component.LEFT_ALIGNMENT);
        htmlExportModeSelector.setSelectedIndex(getPrefs().htmlExportArticleType);
        htmlExportModeSelector.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    if (htmlExportModeSelector.getSelectedIndex() == 2)
                    {
                        urlsToLinksCheckBox.setEnabled(false);
                        urlsToLinksCheckBox.setSelected(false);
                    }
                    else
                    {
                        urlsToLinksCheckBox.setEnabled(true);
                        urlsToLinksCheckBox.setSelected(getPrefs().htmlExportUrlsToLinks);
                    }
                }
            });
        hBox.add(htmlExportModeSelector);
        hBox.add(Box.createGlue());
        htmlVBox.add(hBox);
        htmlVBox.add(urlsToLinksCheckBox = new JCheckBox(JreepadViewer.lang.getString("PREFS_HTML_AUTOLINK"),
            getPrefs().htmlExportUrlsToLinks));
        urlsToLinksCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        urlsToLinksCheckBox.setHorizontalAlignment(SwingConstants.LEFT);
        hBox = Box.createHorizontalBox();
        hBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        // hBox.add(Box.createGlue());
        hBox.add(new JLabel(JreepadViewer.lang.getString("PREFS_HTML_INTERNALLINKS"), SwingConstants.LEFT));
        htmlExportAnchorTypeSelector = new JComboBox(JreepadArticle.getHtmlExportAnchorLinkTypes());
        htmlExportAnchorTypeSelector.setSelectedIndex(getPrefs().htmlExportAnchorLinkType);
        htmlExportAnchorTypeSelector.setAlignmentX(Component.LEFT_ALIGNMENT);
        hBox.add(htmlExportAnchorTypeSelector);
        hBox.add(Box.createGlue());
        htmlVBox.add(hBox);
        genPanel.add(htmlVBox);
        vBox.add(genPanel);

        genPanel = new JPanel();
        genPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        hBox = Box.createHorizontalBox();
        hBox.setAlignmentX(Component.CENTER_ALIGNMENT);
        hBox.add(prefsOkButton = new JButton(JreepadViewer.lang.getString("OK")));
        hBox.add(prefsCancelButton = new JButton(JreepadViewer.lang.getString("CANCEL")));
        prefsOkButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    getPrefs().loadLastFileOnOpen = loadLastFileOnOpenCheckBox.isSelected();
                    getPrefs().autoDateInArticles = autoDateNodesCheckBox.isSelected();
                    getPrefs().autoDetectHtmlArticles = autoDetectHtmlCheckBox.isSelected();
                    getPrefs().webSearchName = webSearchNameField.getText();
                    getPrefs().webSearchPrefix = webSearchPrefixField.getText();
                    getPrefs().webSearchPostfix = webSearchPostfixField.getText();
                    getPrefs().defaultSearchMode = defaultSearchModeSelector.getSelectedIndex();
                    getPrefs().fileEncoding = fileEncodingSelector.getSelectedIndex();
                    getPrefs().mainFileType = fileFormatSelector.getSelectedIndex();
                    getPrefs().characterWrapWidth = ((Integer)(wrapWidthSpinner.getValue()))
                        .intValue();
                    // getPrefs().characterWrapWidth = wrapWidthSpinner.getValue();
                    // setFontsFromPrefsBox();
                    // getPrefs().wrapToWindow = wrapToWindowCheckBox.isSelected();
                    // theJreepad.setEditorPaneKit();
                    getPrefs().htmlExportArticleType = htmlExportModeSelector.getSelectedIndex();
                    getPrefs().htmlExportAnchorLinkType = htmlExportAnchorTypeSelector
                        .getSelectedIndex();
                    // getPrefs().addQuotesToCsvOutput = quoteCsvCheckBox.isSelected();
                    getPrefs().showGreenStrip = showGreenStripCheckBox.isSelected();

                    String dateFormat = dateFormatField.getText();
                    try
                    {
                    	new SimpleDateFormat(dateFormat);
                    }
                    catch (IllegalArgumentException ex)
                    {
                    	JOptionPane.showMessageDialog(PrefsDialog.this,
                            JreepadViewer.lang.getString("MSG_INVALID_DATEFORMAT"),
                            JreepadViewer.lang.getString("TITLE_INVALID_DATEFORMAT") ,
                            JOptionPane.ERROR_MESSAGE);
                    	return;
                    }
                    getPrefs().dateFormat = dateFormat;

                    // If exporting as HTML then we ignore this checkbox
                    if (htmlExportModeSelector.getSelectedIndex() != 2)
                        getPrefs().htmlExportUrlsToLinks = urlsToLinksCheckBox.isSelected();
                    getPrefs().save();
                    setVisible(false);
                }
            });
        prefsCancelButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    setVisible(false);
                }
            });
        genPanel.add(hBox);
        vBox.add(genPanel);
        getContentPane().add(vBox);
        // Finished establishing the prefs dialogue box
    }

    private static JreepadPrefs getPrefs()
    {
        return JreepadView.getPrefs();
    }
}
