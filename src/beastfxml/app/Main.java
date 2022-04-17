package beastfxml.app;
	

import beast.pkgmgmt.BEASTVersion;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;

public class Main extends Application {
	
	
	@Override
	public void start(Stage primaryStage) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("BeastMain.fxml"));
			Parent root = loader.load();
			Scene scene = new Scene(root,768,1024);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.show();
			primaryStage.setTitle("BEAST" + new BEASTVersion().getVersionString());

			
	        // Give the controller access to the main app
			Controller controller = loader.getController();
			controller.setStage(primaryStage);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public static void main(String[] args) {
		launch(args);
	}
}
