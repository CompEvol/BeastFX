package beastfx.app.beauti;

import beast.app.util.Utils;
import beast.base.core.ProgramStatus;
import beast.pkgmgmt.PackageManager;
import javafx.application.Application;
import javafx.stage.Stage;

public class Beauti extends Application {

	@Override
	public void start(Stage primaryStage) throws Exception {
		String [] args = getParameters().getRaw().toArray(new String[] {}); 
        BeautiTabPane.main2(args, primaryStage);
        primaryStage.show();
	}

	
    public static void main(String[] args) {
    	ProgramStatus.name = "BEAUti";
    	launch(args);

        // check for new packages in the background
        new Thread() {
        	public void run() {
        		String statuString = Utils.getBeautiProperty("package.update.status");
        		if (statuString == null) {
        			statuString = PackageManager.UpdateStatus.AUTO_CHECK_AND_ASK.toString(); 
        		}
        		PackageManager.UpdateStatus updateStatus = PackageManager.UpdateStatus.valueOf(statuString);
        		PackageManager.updatePackages(updateStatus, true);
        	};
        }.start();
    }

}
