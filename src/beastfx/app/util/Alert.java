
package beastfx.app.util;

import java.awt.Component;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

import beast.base.core.BEASTInterface;
import beastfx.app.inputeditor.BeautiAlignmentProvider;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

// javafx replacement of joptionpane
public class Alert {
    //
    // Option types
    //

    /**
     * Type meaning Look and Feel should not supply any options -- only
     * use the options from the <code>JOptionPane</code>.
     */
    public static final ButtonType         [] DEFAULT_OPTION = new ButtonType[] {ButtonType.OK};
    /** Type used for <code>showConfirmDialog</code>. */
    public static final ButtonType         [] YES_NO_OPTION = new ButtonType[] {ButtonType.YES, ButtonType.NO};
    /** Type used for <code>showConfirmDialog</code>. */
    public static final ButtonType         [] YES_NO_CANCEL_OPTION = new ButtonType[] {ButtonType.YES, ButtonType.NO, ButtonType.CANCEL};;
    /** Type used for <code>showConfirmDialog</code>. */
    public static final ButtonType         [] OK_CANCEL_OPTION = new ButtonType[] {ButtonType.OK, ButtonType.CANCEL};;;

    //
    // Return values.
    //
    /** Return value from class method if YES is chosen. */
    public static final ButtonType         YES_OPTION = ButtonType.YES;
    /** Return value from class method if NO is chosen. */
    public static final ButtonType         NO_OPTION = ButtonType.NO;
    /** Return value from class method if CANCEL is chosen. */
    public static final ButtonType         CANCEL_OPTION = ButtonType.CANCEL;
    /** Return value form class method if OK is chosen. */
    public static final ButtonType         OK_OPTION = ButtonType.OK;
    /** Return value from class method if user closes window without selecting
     * anything, more than likely this should be treated as either a
     * <code>CANCEL_OPTION</code> or <code>NO_OPTION</code>. */
    public static final ButtonType         CLOSED_OPTION = ButtonType.CLOSE;

    //
    // Message types. Used by the UI to determine what icon to display,
    // and possibly what behavior to give based on the type.
    //
    /** Used for error messages. */
    public static final AlertType  ERROR_MESSAGE = AlertType.ERROR;
    /** Used for information messages. */
    public static final AlertType  INFORMATION_MESSAGE = AlertType.INFORMATION;
    /** Used for warning messages. */
    public static final AlertType  WARNING_MESSAGE = AlertType.WARNING;
    /** Used for questions. */
    public static final AlertType  QUESTION_MESSAGE = AlertType.CONFIRMATION;
    /** No icon is used. */
    public static final AlertType   PLAIN_MESSAGE = AlertType.NONE;
    
	public static void showMessageDialog(Parent parent, String message) {
		javafx.scene.control.Alert alert = new javafx.scene.control.Alert(AlertType.INFORMATION,
				message, 
				ButtonType.OK);
		alert.setResizable(true);
		if (parent != null) {
			Scene node = parent.getScene();
			alert.setX(node.getX() + node.getWidth()/2);
			alert.setY(node.getY() + node.getHeight()/2);
		}
		alert.showAndWait();
	}

	public static ButtonType showConfirmDialog(Parent parent, String message, String header, ButtonType ... yesNoCancelOption) {
		javafx.scene.control.Alert alert = new javafx.scene.control.Alert(AlertType.CONFIRMATION,
				message, 
				yesNoCancelOption);
		alert.setHeaderText(header);
		if (parent != null) {
			Scene node = parent.getScene();
			alert.setX(node.getX() + node.getWidth()/2);
			alert.setY(node.getY() + node.getHeight()/2);
		}
		Optional<ButtonType> option = alert.showAndWait();
		return option.get();
	}

	public static void showMessageDialog(Pane frame, Pane scroller) {
		// TODO Auto-generated method stub
		
	}

	public static void showMessageDialog(Parent parent, String message, String header, AlertType informationMessage) {
		javafx.scene.control.Alert alert = new javafx.scene.control.Alert(informationMessage,
				message, 
				ButtonType.OK);
		alert.setHeaderText(header);
		if (parent != null) {
			Scene node = parent.getScene();
			alert.setX(node.getX() + node.getWidth()/2);
			alert.setY(node.getY() + node.getHeight()/2);
		}
		alert.showAndWait();
	}

	public static void showMessageDialog(Parent parent, String [] messages, String header, AlertType informationMessage, ImageIcon icon) {
		StringBuilder message = new StringBuilder();
		for (String s : messages) {
			message.append(s);
			message.append("\n");
		}
		AnchorPane pane = new AnchorPane();
		TextArea text = new TextArea(message.toString());
		if (icon != null) {
			ImageView img = new ImageView(jswingIconToImage(icon));
			pane.getChildren().add(img);
		}
		pane.getChildren().add(text);
		showMessageDialog(parent, pane, header, informationMessage);
	}
	
	public static Image jswingIconToImage(javax.swing.Icon jswingIcon) {
		  BufferedImage bufferedImage = new BufferedImage(jswingIcon.getIconWidth(),
		    jswingIcon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
		  jswingIcon.paintIcon(null, bufferedImage.getGraphics(), 0, 0);
		  return SwingFXUtils.toFXImage(bufferedImage, null);
	}

	public static Image jswingIconToImage(java.awt.Image awtIcon) {
		int w = awtIcon.getWidth(null);
		int h = awtIcon.getHeight(null);
		  BufferedImage bufferedImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		  bufferedImage.getGraphics().drawImage(awtIcon, 0, 0, 0, 0, w, h, w, h, null);
		  return SwingFXUtils.toFXImage(bufferedImage, null);
	}

	public static void showMessageDialog(Parent parent, Node message, String header, AlertType informationMessage) {
		Dialog<Node> alert = new javafx.scene.control.Dialog<>();
		DialogPane pane = new DialogPane();
		pane.getChildren().add(message);
		alert.setDialogPane(pane);
		alert.setHeaderText(header);
		if (parent != null) {
			Scene node = parent.getScene();
			alert.setX(node.getX() + node.getWidth()/2);
			alert.setY(node.getY() + node.getHeight()/2);
		}
		alert.getDialogPane().getButtonTypes().addAll(CLOSED_OPTION, OK_OPTION);
		pane.setPrefHeight(400);
		pane.setPrefWidth(400);
		alert.setResizable(true);
		alert.showAndWait();
	}

    public static Object showInputDialog(Parent parent,
            Object message, String title, AlertType messageType, Icon icon,
            Object[] selectionValues, Object initialSelectionValue) {
    	String [] values = new String[selectionValues.length];
    	for (int i = 0; i < values.length; i++) {
    		values[i] = valueOf(selectionValues[i]);
    	}
    	ChoiceDialog<?> dlg = new ChoiceDialog<>(valueOf(initialSelectionValue), 
    			values);
    	dlg.setHeaderText(title);
    	Optional<?> option = dlg.showAndWait();
		if (parent != null) {
			Scene node = parent.getScene();
			dlg.setX(node.getX() + node.getWidth()/2);
			dlg.setY(node.getY() + node.getHeight()/2);
		}
		String value = (String) option.get();
    	for (int i = 0; i < values.length; i++) {
    		if (value.equals(values[i])) {
    			return  selectionValues[i];
    		}
    	}
    	return null;
    }

    private static String valueOf(Object o) {
		return o instanceof BEASTInterface ?
				((BEASTInterface) o).getID() :
				o.toString();
	}

	public static Object showInputDialog(Parent parent,
            Object message, String title, AlertType messageType, String initialSelectionValue) {    	
    	TextInputDialog dlg = new TextInputDialog(initialSelectionValue);
    	dlg.setHeaderText(title);
    	Optional<?> option = dlg.showAndWait();
		if (parent != null) {
			Scene node = parent.getScene();
			dlg.setX(node.getX() + node.getWidth()/2);
			dlg.setY(node.getY() + node.getHeight()/2);
		}
		return option.get();
    }
}
