package beastfx.app.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Optional;

import org.w3c.dom.Node;

import com.sun.org.apache.xerces.internal.dom.TextImpl;

import beast.base.core.Log;
import beast.pkgmgmt.BEASTVersion;
import beastfx.app.treeannotator.Main;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.scene.control.ButtonBar.ButtonData;

public class Console extends javafx.application.Application {
	static TextArea textView;

	public Console() {
		createDialog();
		initStreams();
	}

	private void createDialog() {
		Platform.runLater(new Runnable() {
	        public void run() {
	        	try {
					Dialog<String> dialog = new Dialog<>();
				    dialog.setTitle("BEAST Console " + BEASTVersion.INSTANCE.getVersion());
				    textView = new TextArea();
				    textView.setPrefColumnCount(200);
				    textView.setPrefRowCount(200);
    
			        DialogPane root = new DialogPane();
			        root.getChildren().add(textView);
			 
			        root.setStyle("-fx-padding: 10;" +
			                "-fx-border-style: solid inside;" +
			                "-fx-border-width: 2;" +
			                "-fx-border-insets: 5;" +
			                "-fx-border-radius: 5;" +
			                "-fx-border-color: blue;");				    

			        dialog.setDialogPane(root);
			
				    ButtonType close = new ButtonType("Close", ButtonData.OK_DONE);
				    dialog.getDialogPane().getButtonTypes().add(close);
				    ButtonType save = new ButtonType("Save", ButtonData.CANCEL_CLOSE);
				    dialog.getDialogPane().getButtonTypes().add(save);
				    
					//Showing the dialog on clicking the button
			        Optional<String> result = dialog.showAndWait();
			        dialog.close();
	        } catch (Exception e) {
	        	e.printStackTrace();
	        }
	    }});		
	}
	private void initStreams() {
		ByteArrayOutputStream baos = null;

		final PrintStream log = System.err;
		baos = new ByteArrayOutputStream() {
			StringBuilder buf = new StringBuilder();
			
			@Override
			public synchronized void write(byte[] b, int off, int len) {
				for (int i = off; i < off + len; i++) {					
					buf.append((char) b[i]);
				    textView.appendText((char) b[i] + "");
				}
				log.write(b, off, len);
			};

			@Override
			public synchronized void write(int b) {
				buf.append((char) b);
				log.write(b);
			    textView.appendText((char) b + "");
			};

			@Override
			public void write(byte[] b) throws java.io.IOException {
				for (int i = 0; i < b.length; i++) {					
					buf.append((char) b[i]);
				    textView.appendText((char) b[i] + "");
				}
				log.write(b);
			};

			@Override
			public void flush() throws java.io.IOException {
				super.flush();
				log.flush();
				// Node text = new TextImpl(null, buf.toString());
				// webView.getEngine().getDocument().appendChild(text);
			    textView.appendText(buf.toString());
				buf.delete(0,buf.length()-1);
			};

			@Override
			public void close() throws IOException {
				super.close();
				log.close();
			}
		};

		PrintStream p = new PrintStream(baos);
		System.setOut(p);
		System.setErr(p);
		Log.err = p;
		Log.warning = p;
		Log.info = p;
		Log.debug = p;
		Log.trace = p;
	}
	
	public static void main(String[] args) {
	    launch();
	        
		
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
	    textView = new TextArea("sss");
	    textView.setPrefColumnCount(200);
	    textView.setPrefRowCount(200);
	    
	    				    
        // Create the VBox
		ScrollPane root = new ScrollPane();
        // Add the WebView to the VBox
        // root.getChildren().add(webView);
        root.setContent(textView);
 
        // Set the Style-properties of the VBox
        root.setStyle("-fx-padding: 10;" +
                "-fx-border-style: solid inside;" +
                "-fx-border-width: 2;" +
                "-fx-border-insets: 5;" +
                "-fx-border-radius: 5;" +
                "-fx-border-color: blue;");				    

        
		Scene scene = new Scene(root);
		// scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		primaryStage.setScene(scene);
		primaryStage.show();

		initStreams();
        createDialog();

        System.err.println("err");
		System.out.println("out");
		try {
			int i = 4/0;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}        

}
