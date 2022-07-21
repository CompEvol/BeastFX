package beastfx.app.beauti;

import beastfx.app.util.Utils;
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

    static public boolean isInBeauti() {
    	return ProgramStatus.name.equals("BEAUti");
    }

	
    public static void main(String[] args) {
    	ProgramStatus.name = "BEAUti";

        // check for new packages in the background
        new Thread() {
        	public void run() {
        		try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
        		String statuString = Utils.getBeautiProperty("package.update.status");
        		if (statuString == null) {
        			statuString = PackageManager.UpdateStatus.AUTO_CHECK_AND_ASK.toString(); 
        		}
        		PackageManager.UpdateStatus updateStatus = PackageManager.UpdateStatus.valueOf(statuString);
        		PackageManager.updatePackages(updateStatus, true);
        	};
        }.start();
        
    	launch(Beauti.class, args);
    }

}
