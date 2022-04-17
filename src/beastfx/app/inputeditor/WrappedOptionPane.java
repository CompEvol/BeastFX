package beastfx.app.inputeditor;

import javax.swing.UIManager;

import beastfx.app.util.Alert;
import javafx.scene.Parent;
import javafx.scene.control.Dialog;
import javafx.scene.text.Font;

/**
 * Alert but with text wrapping.
 *
 * @author Tim Vaughan <tgvaughan@gmail.com>
 */
public class WrappedOptionPane extends Dialog {

    @Override
    public int getMaxCharactersPerLineCount() {
        return 70;
    }

    /**
     * Display a message dialog with long lines wrapped at word breaks so
     * that the text width is limited to 70 chars.
     *
     * @param message message to display
     */
    static public void showWrappedMessageDialog(Object message) {
        showWrappedMessageDialog(null, message, null);
    }

    /**
     * Display a message dialog with long lines wrapped at word breaks so
     * that the text width is limited to 70 chars.
     *
     * @param parentComponent parent component
     * @param message message to display
     */
    static public void showWrappedMessageDialog(Parent parentComponent, Object message) {
        showWrappedMessageDialog(parentComponent, message, null);
    }

    /**
     * Display a message dialog with long lines wrapped at word breaks so
     * that the text width is limited to 70 chars.
     *
     * @param parentComponent parent component
     * @param message      message to display
     * @param fontName     name of font used to display message
     */
    static public void showWrappedMessageDialog(Parent parentComponent, Object message, String fontName) {
        Object oldFont = null;
        if (fontName != null) {
            oldFont = UIManager.get("OptionPane.messageFont");

            int oldFontSize = 12;
            if (oldFont instanceof Font)
                oldFontSize = ((Font) oldFont).getSize();
            UIManager.put("OptionPane.messageFont", new Font(fontName, Font.PLAIN, oldFontSize));
        }

        WrappedOptionPane pane = new WrappedOptionPane();
        pane.setContentText(message);
        pane.setMessageType(Alert.INFORMATION_MESSAGE);

//        JDialog dialog = pane.createDialog(parentComponent, "Message");
//        dialog.setModal(true);
//        dialog.setVisible(true);
        pane.show();

        if (oldFont != null)
            UIManager.put("OptionPane.messageFont", oldFont);
    }
}
