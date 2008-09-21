package jreepad.ui;

import java.awt.Font;
import javax.swing.JComponent;

/**
 *
 * @author not attributable
 * @version 1.0
 */
public class FontHelper {
  public static final int FONT_DIR_UP = 1;
  public static final int FONT_DIR_DOWN = 2;

  private FontHelper() {
    super();
  }

  public static void updateFont(JComponent comp,
                                int direction) {
    Font font = comp.getFont();
    float size = font.getSize2D();
    Font newFont = null;
    switch (direction) {
      case FONT_DIR_UP:
        newFont = font.deriveFont(++size);
        break;
      case FONT_DIR_DOWN:
        newFont = font.deriveFont(--size);
        break;
    }
    if (newFont != null) {
      comp.setFont(newFont);
    }
  }
}
