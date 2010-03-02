package jreepad.editor;

/**
 *
 * @author not attributable
 * @version 1.0
 */
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

import javax.swing.JComponent;
import javax.swing.TransferHandler;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;

/**
 * An implementation of TransferHandler that adds support for the
 * import and export of text using drag and drop and cut/copy/paste.
 */
public class TextTransferHandler extends TransferHandler {
    //Start and end position in the source text.
    //We need this information when performing a MOVE
    //in order to remove the dragged text from the source.
    private Position p0 = null;
    private Position p1 = null;

    public boolean importData(JComponent comp,
                              Transferable t) {
        boolean insertDone = false;
        if(comp instanceof JTextComponent) {
            if (!t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                return false;
            }
            String data;
            try {
                data = (String) t.getTransferData(DataFlavor.stringFlavor);
            }
            catch (UnsupportedFlavorException e) {
                return false;
            }
            catch (java.io.IOException e) {
                return false;
            }
            JTextComponent tc = (JTextComponent) comp;
            tc.replaceSelection(data);
            insertDone = true;
        }
        return insertDone;
    }

    /**
     * Bundle up the data for export.
     */
    protected Transferable createTransferable(JComponent comp) {
        Transferable transferData = null;
        if(comp instanceof JTextComponent) {
            JTextComponent source = (JTextComponent) comp;
            int start = source.getSelectionStart();
            int end = source.getSelectionEnd();
            if (start == end) {
                return null;
            }
            Document doc = source.getDocument();
            try {
                p0 = doc.createPosition(start);
                p1 = doc.createPosition(end);
            }
            catch (BadLocationException e) {
                // unable to do a drag
                // should log this
                p0 = p1 = null;
            }
            String data = source.getSelectedText();
            transferData = new StringSelection(data);
        }
        return transferData;
    }

    /**
     * These text fields handle both copy and move actions.
     */
    public int getSourceActions(JComponent c) {
        return COPY_OR_MOVE;
    }

    /**
     * When the export is complete, remove the old text if the action
     * was a move.
     */
    protected void exportDone(JComponent c, Transferable data, int action) {
        if (action != MOVE) {
            return;
        }

        if ((p0 != null) && (p1 != null) &&
            (p0.getOffset() != p1.getOffset())) {
            try {
                JTextComponent tc = (JTextComponent)c;
                tc.getDocument().remove(p0.getOffset(),
                        p1.getOffset() - p0.getOffset());
            } catch (BadLocationException e) {
                System.out.println("Can't remove text from source.");
            }
        }
    }

    public boolean canImport(JComponent comp,
                             DataFlavor[] transferFlavors) {
        boolean supported = false;
        for(int idx = 0; idx < transferFlavors.length && !supported; idx++) {
            supported = DataFlavor.stringFlavor.equals(transferFlavors[idx]);
        }
        return supported;
    }
}
