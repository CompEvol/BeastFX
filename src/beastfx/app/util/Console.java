package beastfx.app.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import beast.base.core.Log;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

public class Console extends javafx.application.Application {
	static TextArea textView;

	// to be implemented by sub-classes
	protected void createDialog() {				
	}
	
	private void initStreams() {
		ByteArrayOutputStream baos = null;

		final PrintStream log = System.err;
		baos = new ByteArrayOutputStream() {
			
			@Override
			public synchronized void write(byte[] b, int off, int len) {
				for (int i = off; i < off + len; i++) {					
					textView.appendText((char) b[i] + "");
				}
				log.write(b, off, len);
			};

			@Override
			public synchronized void write(int b) {
				log.write(b);
				textView.appendText((char) b + "");					
			};

			@Override
			public void write(byte[] b) throws java.io.IOException {
				for (int i = 0; i < b.length; i++) {					
					textView.appendText((char) b[i] + "");
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
	
	public static void main(String[] args) throws IOException {
	    launch();
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
	    textView = new TextArea("   ");
	    textView.setPrefColumnCount(80);
	    textView.setPrefRowCount(80);
	    
	    				    
		ScrollPane root = new ScrollPane();
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

		primaryStage.setOnCloseRequest((event) -> {
			// RRB: the recommended action is Platform.exit(), but this causes errors:
			// "Java has been detached already, but someone is still trying to use it at"
			// A crude System.exit() does not:
		    System.exit(0);
		});
		
		initStreams();
        createDialog();
	}       
	
}
