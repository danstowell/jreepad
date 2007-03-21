/*
 Jreepad - personal information manager.
 Copyright (C) 2004-2006 Dan Stowell

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

import java.text.CharacterIterator;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.StringCharacterIterator;
import java.util.Date;

import javax.swing.undo.UndoManager;

import org.philwilson.JTextile;

/**
 * Class representing a single article without its tree context.
 *
 * @version $Id$
 */
public class JreepadArticle
{
    /**
     * The default name for unnamed nodes.
     */
    public static final String UNTITLED_NODE_TEXT = "<Untitled node>";

    public static final int ARTICLEMODE_ORDINARY = 1;
    public static final int ARTICLEMODE_HTML = 2;
    public static final int ARTICLEMODE_CSV = 3;
    public static final int ARTICLEMODE_TEXTILEHTML = 4;

    public static final int CSVPARSE_MODE_INQUOTES = 1;
    public static final int CSVPARSE_MODE_EXPECTINGDELIMITER = 2;
    public static final int CSVPARSE_MODE_EXPECTINGDATA = 3;

    public static final int EXPORT_HTML_NORMAL = 0;
    public static final int EXPORT_HTML_PREFORMATTED = 1;
    public static final int EXPORT_HTML_HTML = 2;
    public static final int EXPORT_HTML_TEXTILEHTML = 3;

    public static final int EXPORT_HTML_ANCHORS_PATH = 0;
    public static final int EXPORT_HTML_ANCHORS_WIKI = 1;

    private String title;

    private String content;

    private int articleMode;

    private UndoManager undoMgr;

    public JreepadArticle()
    {
        this(getNewContent());
    }

    public JreepadArticle(String content)
    {
        this(UNTITLED_NODE_TEXT, content);
    }

    public JreepadArticle(String title, String content)
    {
        this(title, content, ARTICLEMODE_ORDINARY);
    }

    public JreepadArticle(String title, String content, int articleMode)
    {
        if (title == null || title.equals(""))
            this.title = UNTITLED_NODE_TEXT;
        else
            this.title = title;
        this.content = content;
        this.articleMode = articleMode;
        undoMgr = new UndoManager();
    }

    public int getArticleMode()
    {
        return articleMode;
    }

    public void setArticleMode(int articleMode)
    {
        this.articleMode = articleMode;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        if (title == null || title.equals(""))
            this.title = UNTITLED_NODE_TEXT;
        else
            this.title = title;
    }

    public UndoManager getUndoMgr()
    {
        return undoMgr;
    }

    public String toString()
    {
        return title;
    }

    public static final String[] getHtmlExportArticleTypes()
    {
        return new String[] { JreepadViewer.lang.getString("PREFS_EXPORTTYPE_TEXT"),
                        JreepadViewer.lang.getString("PREFS_EXPORTTYPE_PREFORMATTED"),
                        JreepadViewer.lang.getString("PREFS_EXPORTTYPE_HTML"),
                        JreepadViewer.lang.getString("PREFS_EXPORTTYPE_TEXTILEHTML") };
    }

    public static final String[] getHtmlExportAnchorLinkTypes()
    {
        return new String[] { "node:// links", "WikiLike links" };
    }

    public String exportAsHtml(int exportMode, boolean urlsToLinks, int anchorType,
        boolean causeToPrint)
    {
        StringBuffer ret = new StringBuffer();
        ret.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\">\n<head>\n<title>");
        ret.append(htmlSpecialChars(getTitle()));
        ret.append("</title>\n<style type=\"text/css\">\n"
                + "dl {}\ndl dt { font-weight: bold; margin-top: 10px; font-size: 24pt; }\ndl dd {margin-left: 20px; padding-left: 0px;}\ndl dd dl dt {background: black; color: white; font-size: 12pt; }\ndl dd dl dd dl dt {background: white; color: black; }"
                + "\n</style>\n</head>\n\n<body" + (causeToPrint ? " onload='print();'" : "")
                + ">\n<!-- Exported from Jreepad -->\n<dl>");
        ret.append(toHtml(exportMode, urlsToLinks, anchorType));
        ret.append("\n</dl>\n</body>\n</html>");
        return ret.toString();
    }

    public String toHtml(int exportMode, boolean urlsToLinks, int anchorType)
    {
        switch (articleMode)
        {
        case ARTICLEMODE_HTML:
            return getContent();
        case ARTICLEMODE_TEXTILEHTML:
            try
            {
                return JTextile.textile(getContent());
            }
            catch (Exception e)
            {
                return getContent();
            }
        case ARTICLEMODE_CSV:
            String[][] csv = interpretContentAsCsv();
            StringBuffer csvHtml = new StringBuffer(
                "\n  <table border='1' cellspacing='0' cellpadding='2'>");
            for (int i = 0; i < csv.length; i++)
            {
                csvHtml.append("\n    <tr>");
                for (int j = 0; j < csv[0].length; j++)
                    csvHtml.append("\n      <td>" + htmlSpecialChars(csv[i][j]) + "</td>");
                csvHtml.append("\n    </tr>");
            }
            csvHtml.append("\n  </table>");
            return csvHtml.toString();
        case ARTICLEMODE_ORDINARY:
        default:
            switch (exportMode)
            {
            case EXPORT_HTML_PREFORMATTED:
                return "<pre>"
                    + (urlsToLinks ? urlsToHtmlLinksAndHtmlSpecialChars(getContent(), anchorType)
                        : htmlSpecialChars(getContent())) + "</pre>";
            case EXPORT_HTML_HTML:
                return getContent();
            case EXPORT_HTML_TEXTILEHTML:
                try
                {
                    return JTextile.textile(getContent());
                }
                catch (Exception e)
                {
                    return getContent();
                }
            case EXPORT_HTML_NORMAL:
            default:
                return (urlsToLinks ? urlsToHtmlLinksAndHtmlSpecialChars(getContent(), anchorType)
                    : htmlSpecialChars(getContent()));
            }
        }
    }

    private static String htmlSpecialChars(String in)
    {
        char[] c = in.toCharArray();
        StringBuffer ret = new StringBuffer();
        for (int i = 0; i < c.length; i++)
            if (c[i] == '<')
                ret.append("&lt;");
            else if (c[i] == '>')
                ret.append("&gt;");
            else if (c[i] == '&')
                ret.append("&amp;");
            else if (c[i] == '\n')
                ret.append(" <br />\n");
            else if (c[i] == '"')
                ret.append("&quot;");
            else
                ret.append(c[i]);
        return ret.toString();
    }

    // Search through the String, replacing URI-like substrings (containing ://) with HTML links
    private String urlsToHtmlLinksAndHtmlSpecialChars(String in, int anchorType)
    {
        StringCharacterIterator iter = new StringCharacterIterator(in);
        StringBuffer out = new StringBuffer("");
        StringBuffer currentWord = new StringBuffer(""); // "space" characters get stuck straight
                                                            // back out, but words need aggregating

        char c = iter.current(), c2;
        while (true)
        {
            if (Character.isWhitespace(c) || c == '"' || c == '\'' || c == '<' || c == '>'
                || c == '\n' || c == CharacterIterator.DONE)
            {
                // // First check whether currentWord is empty...?
                // if(c!=CharacterIterator.DONE && currentWord.length()==0)
                // continue;

                // Check if the current word is a URL - do weird stuff to it if so, else just output
                // it
                if (currentWord.toString().indexOf("://") > 0)
                {
                    // We don't like quotes - let's remove 'em!
                    // Ideally, a beginning quote would signify that we need to keep on searching
                    // until we find an end quote
                    // but that aspect is NOT IMPLEMENTED YET
                    c2 = currentWord.charAt(0);
                    if (c2 == '"' || c2 == '\'')
                        currentWord.deleteCharAt(0);
                    c2 = currentWord.charAt(currentWord.length() - 1);
                    if (c2 == '"' || c2 == '\'')
                        currentWord.deleteCharAt(currentWord.length() - 1);

                    // At this stage, beginning with "node://" should indicate that we want an
                    // anchor link not a "real" HTML link
                    String currentWordString = currentWord.toString();
                    if (currentWordString.startsWith("node://"))
                    {
                        String anchorLink;
                        if (anchorType == EXPORT_HTML_ANCHORS_WIKI)
                            anchorLink = currentWordString.substring(currentWordString
                                .lastIndexOf('/') + 1);
                        else
                            anchorLink = currentWordString.substring(7);
                        out.append("<a href=\"#" + anchorLink + "\">" + currentWordString + "</a>");
                    }
                    else
                        out.append("<a href=\"" + currentWord + "\">" + currentWordString + "</a>");
                }
                else if (anchorType == EXPORT_HTML_ANCHORS_WIKI
                    && JreepadView.isWikiWord(currentWord.toString()))
                {
                    String currentWordString = currentWord.toString();
                    if (currentWordString.length() > 4 && currentWordString.startsWith("[[")
                        && currentWordString.endsWith("]]"))
                        currentWordString = currentWordString.substring(2, currentWordString
                            .length() - 2);
                    out.append("<a href=\"#" + currentWordString + "\">" + currentWordString
                        + "</a>");
                }
                else
                    out.append(currentWord.toString());
                if (c == '<')
                    out.append("&lt;");
                else if (c == '>')
                    out.append("&gt;");
                else if (c == '\n')
                    out.append(" <br />\n");
                else if (c == '"')
                    out.append("&quot;");
                else if (c == '&')
                    out.append("&amp;");
                else if (c != CharacterIterator.DONE)
                    out.append(c);

                currentWord.setLength(0);

                if (c == CharacterIterator.DONE)
                    break;
            }
            else
            {
                currentWord.append(c); // Just aggregate character onto current "Word"
            }
            c = iter.next();
        } // End "while"

        return out.toString();
    }

    public synchronized void wrapContentToCharWidth(int charWidth)
    {
        if (charWidth < 2)
            return;

        StringBuffer ret = new StringBuffer();
        StringCharacterIterator iter = new StringCharacterIterator(content);
        int charsOnThisLine = 0;
        for (char c = iter.first(); c != CharacterIterator.DONE; c = iter.next())
        {
            if (c == '\n')
                charsOnThisLine = 0;
            else if (++charsOnThisLine >= charWidth)
            {
                ret.append('\n');
                charsOnThisLine = 0;
            }
            ret.append(c);
        }
        content = ret.toString();
    }

    public synchronized void stripAllTags()
    {
        StringBuffer ret = new StringBuffer();
        StringCharacterIterator iter = new StringCharacterIterator(content);
        boolean on = true;
        for (char c = iter.first(); c != CharacterIterator.DONE; c = iter.next())
        {
            if ((!on) && c == '>')
                on = true;
            else if (on && c == '<')
                on = false;
            else if (on)
                ret.append(c);
        }
        content = ret.toString();
    }

    public String[][] interpretContentAsCsv()
    {
        String theContent = getContent();
        int rows = 1;
        int cols = 1;
        theContent = theContent.trim();
        char c;
        int curCols = 1;
        int parseMode = CSVPARSE_MODE_EXPECTINGDATA;
        StringBuffer curData = new StringBuffer();

        // Go through once to determine the number of rows and columns
        StringCharacterIterator iter = new StringCharacterIterator(theContent);

        c = iter.current();
        while (true)
        {
            if (c == CharacterIterator.DONE)
            {
                // / System.out.println("I've just parsed this data item: " + curData + " and I'm in
                // mode " + parseMode);
                cols = (curCols > cols) ? curCols : cols;
                break;
            }

            if (parseMode == CSVPARSE_MODE_INQUOTES)
            {
                if (c == '"')
                {
                    parseMode = CSVPARSE_MODE_EXPECTINGDELIMITER;
                }
                else
                {
                    curData.append(c);
                }
            }
            else if (parseMode == CSVPARSE_MODE_EXPECTINGDELIMITER
                || parseMode == CSVPARSE_MODE_EXPECTINGDATA)
            {
                if (c == '"')
                {
                    parseMode = CSVPARSE_MODE_INQUOTES;
                }
                else if (c == '\n' || c == Character.LINE_SEPARATOR)
                {
                    parseMode = CSVPARSE_MODE_EXPECTINGDATA;
                    // / System.out.println("I've just parsed this data item: " + curData + " and
                    // I'm in mode " + parseMode);
                    curData = new StringBuffer();
                    cols = (curCols > cols) ? curCols : cols;
                    curCols = 1;
                    rows++;
                }
                else if (c == ',')
                {
                    parseMode = CSVPARSE_MODE_EXPECTINGDATA;
                    curCols++;
                    // / System.out.println("I've just parsed this data item: " + curData + " and
                    // I'm in mode " + parseMode);
                    curData = new StringBuffer();
                }
                else
                {
                    curData.append(c);
                }
            }

            c = iter.next();
        }

        // Now go through and actually assign the content
        String[][] csv = new String[rows][cols];
        for (int i = 0; i < csv.length; i++)
            java.util.Arrays.fill(csv[i], "");
        iter = new StringCharacterIterator(theContent);
        parseMode = CSVPARSE_MODE_EXPECTINGDATA;
        curData = new StringBuffer();
        int col = 0;
        int row = 0;

        c = iter.current();
        while (true)
        {
            if (c == CharacterIterator.DONE)
            {
                csv[row][col] = curData.toString();
                break;
            }

            if (parseMode == CSVPARSE_MODE_INQUOTES)
            {
                if (c == '"')
                {
                    parseMode = CSVPARSE_MODE_EXPECTINGDELIMITER;
                }
                else
                {
                    curData.append(c);
                }
            }
            else if (parseMode == CSVPARSE_MODE_EXPECTINGDELIMITER
                || parseMode == CSVPARSE_MODE_EXPECTINGDATA)
            {
                if (c == '"')
                {
                    parseMode = CSVPARSE_MODE_INQUOTES;
                }
                else if (c == '\n' || c == Character.LINE_SEPARATOR)
                {
                    csv[row][col] = curData.toString();
                    parseMode = CSVPARSE_MODE_EXPECTINGDATA;
                    curData = new StringBuffer();
                    col = 0;
                    row++;
                }
                else if (c == ',')
                {
                    csv[row][col] = curData.toString();
                    parseMode = CSVPARSE_MODE_EXPECTINGDATA;
                    col++;
                    curData = new StringBuffer();
                }
                else
                {
                    curData.append(c);
                }
            }

            c = iter.next();
        }
        return csv;
    }

    protected void setContentAsCsv(String[][] in)
    {
        StringBuffer o = new StringBuffer();
        for (int i = 0; i < in.length; i++)
        {
            for (int j = 0; j < in[0].length; j++)
            {
                o.append("\"" + in[i][j] + "\"");
            }
            o.append("\n");
        }
        setContent(o.toString());
    }

    /**
     * Returns the content for a new node. It is either empty or with a timestamp.
     */
    public static String getNewContent()
    {
        if (JreepadView.getPrefs().autoDateInArticles)
            return getCurrentDate();
        return "";
    }

    /**
     * Returns the current time and date. The date is formatted acording to the
     * preferences. If the format is not set in thepreferences, the default
     * format is used.
     */
    public static String getCurrentDate()
    {
        DateFormat dateFormat = null;
        String format = JreepadView.getPrefs().dateFormat;

        if (!format.equals(""))
        {
            try
            {
                dateFormat = new SimpleDateFormat(format);
            }
            catch (IllegalArgumentException e)
            {
                // Default format will be set
                // TODO: Log this
            }
        }
        if (dateFormat == null)
            dateFormat = DateFormat.getDateInstance();

        return dateFormat.format(new Date());
    }
}
