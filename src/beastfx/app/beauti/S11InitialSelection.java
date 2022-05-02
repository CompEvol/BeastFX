package beastfx.app.beauti;



import javafx.scene.Scene;

//http://www.java2s.com/Code/Java/Swing-Components/JComboBoxaddingautomaticcompletionHandlingtheinitialselection.htm
//Code from: http://www.orbital-computer.de/ComboBox/
/*
Inside ComboBox: adding automatic completion

Author: Thomas Bierhance
        thomas@orbital-computer.de
*/

/*
Handling the initial selection

It is a quiet annoying that the initially selected item is not shown in the combo box. This
can be easily changed in the constructor of the auto completing document.
*/
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;

//import javax.swing.JFrame;
//import javax.swing.text.AttributeSet;
//import javax.swing.text.BadLocationException;
//import javax.swing.text.JTextComponent;
//import javax.swing.text.PlainDocument;

import beastfx.app.util.FXUtils;
import javafx.application.Application;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


public class S11InitialSelection extends Control {
	

	ComboBox<Object> comboBox;
    // ComboBoxModel<Object> model;
    TextField editor;
    
    // flag to indicate if setSelectedItem has been called
    // subsequent calls to remove/insertString should be ignored
    boolean selecting=false;

    public S11InitialSelection(final ComboBox<Object> comboBox) {
    	
    	FXUtils.autoCompleteComboBoxPlus(comboBox, 
    			(typedText, itemToCompare) -> itemToCompare.toString().toLowerCase().contains(typedText.toLowerCase()));

    	
//    	
//        this.comboBox = comboBox;
//        comboBox.setEditable(true);
//        // model = comboBox.getModel();
//        editor = (JTextComponent) comboBox.getEditor().getEditorComponent();
//        editor.setDocument(this);
//        comboBox.setOnAction(e -> {
//                if (!selecting) highlightCompletedText(0);
//            });
//        editor.setOnKeyReleased(e-> {
//                if (comboBox.isDisplayable()) comboBox.setPopupVisible(true);
//        });
        // Handle initially selected object
        Object selected = comboBox.getValue();
        //if (selected!=null) setText(selected.toString());
        //highlightCompletedText(0);
    }

//    @Override
//	public void remove(int offs, int len) throws BadLocationException {
//        // return immediately when selecting an item
//        if (selecting) return;
//        super.remove(offs, len);
//    }

//    @Override
//	public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
//        // return immediately when selecting an item
//        if (selecting) return;
//        // insert the string into the document
//        super.insertString(offs, str, a);
//        // lookup and select a matching item
//        Object item = lookupItem(getText(0, getLength()));
//        if (item != null) {
//            comboBox.getSelectionModel().select(item);
//        } else {
//            // keep old item selected if there is no match
//            item = comboBox.getValue();
//            // imitate no insert (later on offs will be incremented by str.length(): selection won't move forward)
//            offs = offs-str.length();
//            // provide feedback to the user that his input has been received but can not be accepted
//            // comboBox.getToolkit().beep(); // when available use: UIManager.getLookAndFeel().provideErrorFeedback(comboBox);
//            java.awt.Toolkit.getDefaultToolkit().beep();
//        }
//        setText(item.toString());
//        // select the completed part
//        highlightCompletedText(offs+str.length());
//    }
//
//    private void setText(String text) {
//        try {
//            // remove all text and insert the completed string
//            super.remove(0, getLength());
//            super.insertString(0, text, null);
//        } catch (BadLocationException e) {
//            throw new RuntimeException(e.toString());
//        }
//    }
//
//    private void highlightCompletedText(int start) {
//        editor.setCaretPosition(getLength());
//        editor.moveCaretPosition(start);
//    }
//
//    private void setSelectedItem(Object item) {
//        selecting = true;
//        model.setSelectedItem(item);
//        selecting = false;
//    }

    private Object lookupItem(String pattern) {
        Object selectedItem = comboBox.getSelectionModel().getSelectedItem();
        // only search for a different item if the currently selected does not match
        if (selectedItem != null && startsWithIgnoreCase(selectedItem.toString(), pattern)) {
            return selectedItem;
        } else {
            // iterate over all items
            for (int i=0, n=comboBox.getItems().size(); i < n; i++) {
                Object currentItem = comboBox.getItems().get(i);
                // current item starts with the pattern?
                if (startsWithIgnoreCase(currentItem.toString(), pattern)) {
                    return currentItem;
                }
            }
        }
        // no item starts with the pattern => return null
        return null;
    }

    // checks if str1 starts with str2 - ignores case
    private boolean startsWithIgnoreCase(String str1, String str2) {
        return str1.toUpperCase().startsWith(str2.toUpperCase());
    }

    private static Dialog createAndShowGUI() {
        // the combo box (add/modify items if you like to)
        ComboBox<Object> comboBox = new ComboBox<>();
        comboBox.getItems().addAll(new Object[] {"Ester", "Jordi", "Jordina", "Jorge", "Sergi"});
        // has to be editable
        comboBox.setEditable(true);
        // change the editor's document
        new S11InitialSelection(comboBox);

        // create and show a window containing the combo box
        Dialog dlg = new Dialog();
        dlg.getDialogPane().getChildren().add(comboBox);
        return dlg;
        
    }


    @SuppressWarnings("static-access")
	public static void main(String[] args) {
    	new Application() {
    		
			@Override
			public void start(Stage primaryStage) throws Exception {
				VBox root = new VBox();
		        // the combo box (add/modify items if you like to)
		        ComboBox<Object> comboBox = new ComboBox<>();
		        comboBox.getItems().addAll(new Object[] {"Ester", "Jordi", "Jordina", "Jorge", "Sergi"});
		        // has to be editable
		        comboBox.setEditable(true);
		        // change the editor's document
		        new S11InitialSelection(comboBox);
				root.getChildren().add(comboBox);
				Scene scene = new Scene(root);
		        primaryStage.setScene(scene);
			}
			
		}.launch(args);
    }
}
