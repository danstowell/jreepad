package jreepad.editor;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;

/**
 *
 * @author not attributable
 * @version 1.1
 */
public class EditPopupHandler
        extends MouseAdapter {
    private JPopupMenu popupMenu = new JPopupMenu();
    public static final String ITEM_CUT = "cut";
    public static final String ITEM_COPY = "copy";
    public static final String ITEM_PASTE = "paste";
	public static final String ITEM_SELECTALL = "selectall";
    private final Map itemMap = new HashMap();

    public EditPopupHandler() {
        super();
        itemMap.put(ITEM_COPY, null);
        itemMap.put(ITEM_CUT, null);
        itemMap.put(ITEM_PASTE, null);
		itemMap.put(ITEM_SELECTALL, null);
    }

    public void addActionItem(TextAction action,
                              String menuLabel,
                              String type) {
        JMenuItem item = new JMenuItem(action);
        item.setText(menuLabel);
        popupMenu.add(item);
        if (itemMap.containsKey(type)) {
            itemMap.put(type, item);
        }
    }

    private void enableMenuItems(MouseEvent e) {
        JTextComponent textComp = (JTextComponent) e.getComponent();
        String selectedText = textComp.getSelectedText();
        boolean textSelected = selectedText != null &&
                               selectedText.length() > 0;
        boolean isEditable = textComp.isEditable();
        JMenuItem item = (JMenuItem) itemMap.get(ITEM_COPY);
        if (item != null) {
            item.setEnabled(textSelected);
        }
        item = (JMenuItem) itemMap.get(ITEM_CUT);
        if (item != null) {
            item.setEnabled(textSelected && isEditable);
        }
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable contents = clipboard.getContents(null);
        boolean hasTransferableText =
                (contents != null) &&
                contents.isDataFlavorSupported(DataFlavor.stringFlavor);
        item = (JMenuItem) itemMap.get(ITEM_PASTE);
        if (item != null) {
            item.setEnabled(hasTransferableText && isEditable);
        }
		item = (JMenuItem) itemMap.get(ITEM_SELECTALL);
		if (item != null) {
            item.setEnabled(true && isEditable);
        }
    }

    /* -------------- from MouseListener -------------- */

    public void mouseClicked(MouseEvent e) {
        if (e.isPopupTrigger() || e.getButton() == MouseEvent.BUTTON3) {
            enableMenuItems(e);
            popupMenu.show(e.getComponent(),
                           e.getX(),
                           e.getY());
        }
    }
}
