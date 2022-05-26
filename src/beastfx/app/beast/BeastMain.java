package beastfx.app.beast;
	

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Optional;

import beast.base.core.Log;
import beast.pkgmgmt.BEASTVersion;

import beastfx.app.util.Utils;
import beastfx.app.beauti.ThemeProvider;
import beastfx.app.treeannotator.TreeAnnotator;
import beastfx.app.util.Console;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.ButtonBar.ButtonData;

public class BeastMain extends Console {
	
	@Override
	protected void createDialog() {
//		
//		PrintStream err = System.err;
//		System.setErr(new PrintStream(new OutputStream() {
//		    public void write(int b) {
//		    }
//		}));
//		// Utils.loadUIManager();
//		System.setErr(err);
		
        System.setProperty("com.apple.macos.useScreenMenuBar", "true");
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("apple.awt.showGrowBox", "true");


        Dialog<String> dialog = new Dialog<>();
	    dialog.setTitle("BEAST " + BEASTVersion.INSTANCE.getVersion());
	    FXMLLoader fl = new FXMLLoader();
	    fl.setLocation(BeastMain.class.getResource("BeastMain.fxml"));
	    DialogPane root = null;
		try {
			root = fl.load();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    dialog.setDialogPane(root);
	    ThemeProvider.loadStyleSheet(root.getScene());        

	    ButtonType run = new ButtonType("Run", ButtonData.OK_DONE);
	    dialog.getDialogPane().getButtonTypes().add(run);
	    ButtonType cancel = new ButtonType("Quit", ButtonData.CANCEL_CLOSE);
	    dialog.getDialogPane().getButtonTypes().add(cancel);
	    
		//Showing the dialog on clicking the button
        Optional<String> result = dialog.showAndWait();
        dialog.close();
        
        Object o = result.get();
        String str = o.toString();
        
	    if (str.equals(run.toString())) {
		    Controller controller = fl.getController();
        	controller.run();
        } else {
        	Log.warning("Quiting BEAST");
        	System.exit(0);
    		return;
        }

 	}
	
	
//	@Override
//	public void start(Stage primaryStage) {
//		try {
//			FXMLLoader loader = new FXMLLoader(Main.class.getResource("BeastMain.fxml"));
//			Parent root = loader.load();
//			Scene scene = new Scene(root,768,1024);
//			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
//			primaryStage.setScene(scene);
//			primaryStage.show();
//			primaryStage.setTitle("BEAST" + new BEASTVersion().getVersionString());
//
//			
//	        // Give the controller access to the main app
//			Controller controller = loader.getController();
//			controller.setStage(primaryStage);
//		} catch(Exception e) {
//			e.printStackTrace();
//		}
//	}
	
	
	public static void main(String[] args) {
		launch(args);
	}
}
