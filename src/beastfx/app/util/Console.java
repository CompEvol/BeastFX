package beastfx.app.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import beast.base.core.Log;
import beastfx.app.beauti.ThemeProvider;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class Console extends javafx.application.Application {
	protected static TextArea textView;

	// to be implemented by sub-classes
	protected void createDialog() {				
	}
	
	private void initStreams() {
		ByteArrayOutputStream baos = null;

		final PrintStream log = System.err;
		baos = new ByteArrayOutputStream() {
			
			@Override
			public synchronized void write(byte[] b, int off, int len) {
			    if (Platform.isFxApplicationThread()) {
					for (int i = off; i < off + len; i++) {					
						textView.appendText((char) b[i] + "");
					}
			    } else {
			    	StringBuilder buffer = new StringBuilder();
					for (int i = off; i < off + len; i++) {					
						buffer.append((char) b[i] + "");
					}
			        Platform.runLater(() -> {
			        	synchronized(this) {
			        		textView.appendText(buffer.toString());
			        	}
			        });
			    }
				log.write(b, off, len);
			};

			@Override
			public synchronized void write(int b) {
				log.write(b);
			    if (Platform.isFxApplicationThread()) {
			    	textView.appendText((char) b + "");
			    } else {
			        Platform.runLater(() -> {
				    	textView.appendText((char) b + "");
			        });			    	
			    }
			};

			@Override
			public void write(byte[] b) throws java.io.IOException {
			    if (Platform.isFxApplicationThread()) {
					for (int i = 0; i < b.length; i++) {					
						textView.appendText((char) b[i] + "");
					}
			    } else {
			    	StringBuilder buffer = new StringBuilder();
					for (int i = 0; i < b.length; i++) {					
						buffer.append((char) b[i] + "");
					}
			        Platform.runLater(() -> {
			        	synchronized(this) {
			        		textView.appendText(buffer.toString());
			        	}
			        });
			    }
				log.write(b);
			};

			@Override
			public void flush() throws java.io.IOException {
				super.flush();
				log.flush();
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
	    launch(Console.class, args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
	    textView = new TextArea("   ");
	    textView.setPrefColumnCount(120);
	    textView.setPrefRowCount(80);
	    
	    				    
		//ScrollPane root = new ScrollPane();
        //root.setContent(textView);
 
        // Set the Style-properties of the VBox
	    textView.setStyle(
                "-fx-border-style: solid inside;" +
                "-fx-border-width: 2;" +
                "-fx-border-insets: 5;" +
                "-fx-border-radius: 5;" +
                "-fx-border-color: blue;" +
                "-fx-font: 11pt Menlo;");				    

        
		Scene scene = new Scene(textView);
		ThemeProvider.loadStyleSheet(scene);
		// scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		primaryStage.setScene(scene);
		primaryStage.show();

		primaryStage.setOnCloseRequest((event) -> {
			// RRB: the recommended action is Platform.exit(), but this causes errors:
			// "Java has been detached already, but someone is still trying to use it at"
			// A crude System.exit() does not:
		    System.exit(0);
		});
		
		// prevent textView to exceed display size
	    Rectangle2D screen = Screen.getPrimary().getBounds();
	    double d = scene.getWindow().getHeight() - scene.getHeight();
	    textView.setMaxSize(screen.getHeight() - d, screen.getWidth());

		initStreams();
		
		Platform.runLater(new Runnable() {
	        public void run() {
	        	createDialog();
	        }
		});
	    	
	}       
	
}
