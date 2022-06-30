package beastfx.app.util;



import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;

import java.io.*;
import java.util.HashMap;
import java.util.List;


import beast.base.core.BEASTInterface;
import beast.base.core.Input;
import beastfx.app.inputeditor.BEASTObjectDialog;
import beastfx.app.inputeditor.BeautiPanelConfig;

public class FXUtils {


    public static File getLoadFile(String message) {
        return getLoadFile(message, null, null, (String[]) null);
    }

    public static File getSaveFile(String message) {
        return getSaveFile(message, null, null, (String[]) null);
    }

    public static File getLoadFile(String message, File defaultFileOrDir, String description, final String... extensions) {
        File[] files = getFile(message, true, defaultFileOrDir, false, description, extensions);
        if (files == null) {
            return null;
        } else {
            return files[0];
        }
    }

    public static File getSaveFile(String message, File defaultFileOrDir, String description, final String... extensions) {
        File[] files = getFile(message, false, defaultFileOrDir, false, description, extensions);
        if (files == null) {
            return null;
        } else {
            return files[0];
        }
    }

    public static File[] getLoadFiles(String message, File defaultFileOrDir, String description, final String... extensions) {
        return getFile(message, true, defaultFileOrDir, true, description, extensions);
    }

    public static File[] getSaveFiles(String message, File defaultFileOrDir, String description, final String... extensions) {
        return getFile(message, false, defaultFileOrDir, true, description, extensions);
    }

    public static File[] getFile(String message, boolean isLoadNotSave, File defaultFileOrDir, boolean allowMultipleSelection, String description, final String... extensions) {

    	FileChooser fileChooser = new FileChooser();
    	
    	fileChooser.setInitialDirectory(defaultFileOrDir);
    	
    	if (defaultFileOrDir != null) {
    		if (defaultFileOrDir.isDirectory()) {
    			fileChooser.setInitialDirectory(defaultFileOrDir);
    		} else if (defaultFileOrDir.getParentFile().isDirectory()){
    			fileChooser.setInitialDirectory(defaultFileOrDir.getParentFile());
    			fileChooser.setInitialFileName(defaultFileOrDir.getName());
    		}
    	}
    	
    	for (String extension : extensions) {
    		if (extension.equals("")) {
    			extension = "*.*";
    		}
    		fileChooser.getExtensionFilters().add(
    				new FileChooser.ExtensionFilter(extension, "*."+extension)
    		);
    	}
    	fileChooser.setTitle(message);
    	
    	if (isLoadNotSave) {
    		List<File> files = fileChooser.showOpenMultipleDialog(null);
    		if (files == null) {
    			return new File[]{};
    		}
            return files.toArray(new File[]{});
    	} else {
    		File file = fileChooser.showSaveDialog(null);
    		return new File[] {file};
    	}
    }

	public static ImageView getIcon(String string) {
		string = FXUtils.class.getResource(string).toExternalForm();
		ImageView img = new ImageView(string);
		return img;
	}

	
	

	public static HBox newHBox() {
		HBox box = new HBox();
        box.setSpacing(5);
        box.setPadding(new Insets(5));
        return box;
	}

	public static VBox newVBox() {
		VBox box = new VBox();
        box.setSpacing(3);
        box.setPadding(new Insets(2));
        return box;
	}
	
	
	
	public static void createHMCButton(Pane pane, BEASTInterface o, Input<?> input)  {
        if (o instanceof BeautiPanelConfig) {
        	BeautiPanelConfig cfg = (BeautiPanelConfig) o;
        	FXUtils.createHMCButton(pane, cfg.parentBEASTObjects.get(0), cfg.parentInputs.get(0));
        	return;
        }
        
        String id = o.getID();
		if (id.lastIndexOf('.') > 0) {
			id = id.substring(0, id.lastIndexOf('.'));
		}
		
		if (!hmcPages.containsKey(id + "/" + input.getName() + "/")) {
			return;
		}
		System.out.print(id + "/" + input.getName() + "/ => ");
		id = hmcPages.get(id + "/" + input.getName() + "/");		
		System.out.println(id);
		
		String HMC_BASE = getHMCBase();
		String url = HMC_BASE + "/" + id + "/";//.html";
		Button hmc = createHMCButton(url);
        if (input instanceof BeautiPanelConfig.FlexibleInput) {
        	BeautiPanelConfig.FlexibleInput flexInput = (BeautiPanelConfig.FlexibleInput) input;
        	hmc.setTooltip(new Tooltip(o.getDescription() + "\n" + o.getID() + "\n" + url));
        } else {
        	hmc.setTooltip(new Tooltip(input.getTipText() + "\n" + o.getID() + "\n" + url));
        }
        pane.getChildren().add(hmc);
	}
	
	// maps `help me choose` page ID
	private static java.util.Map<String, String> hmcPages = new HashMap<>();

    public static void processHMCPages(String hmcPages) {
		if (hmcPages == null) {
			// nothing to do
			return;
		}
		String [] strs = hmcPages.split(",");
		for (String str : strs) {
			if (str.indexOf('=') > -1) {
				String [] strs2 = str.split("=");
				String from = strs2[0].trim();
				String to = strs2[1].trim();
				FXUtils.hmcPages.put(from, to);
			} else {
				String from = str.trim();
				FXUtils.hmcPages.put(from, from);
			}
		}		
	}

	private static String getHMCBase() {
		//return "file://" + System.getProperty("user.dir") + "/hmc";
		//return "http://127.0.0.1:4000/hmc/";
		return "https://beast2-dev.github.io/hmc/hmc/";
	}

	
	public static Button createHMCButton(String templateName, String tabName) {
		String HMC_BASE = getHMCBase();
		String base = (templateName + "/" + tabName + "/").replaceAll(" ", "_");
		if (!hmcPages.containsKey(base)) {
			Button button = createHMCButton(HMC_BASE);
			button.setDisable(true);
			return button;
		}

		String url = HMC_BASE + "/" + hmcPages.get(base) + "index.html";
		url = url.replaceAll(" ", "_");
		return createHMCButton(url);
	}
	
	public static Button createHMCButton(final String url) {
		
	   Button hmcButton = new Button();
    	hmcButton.setTooltip(new Tooltip("Click to 'help me choose'"));
    	hmcButton.setGraphic(FXUtils.getIcon(BEASTObjectDialog.ICONPATH + "help16.png"));
    	hmcButton.setOnAction(e->openInBrowser(url, hmcButton));
    	hmcButton.setStyle(
    	        "-fx-background-radius: 5em; " +
				"-fx-min-width: 10px; " +
				"-fx-min-height: 3px; " +
				"-fx-max-width: 10px; " +
				"-fx-max-height: 3px; " +
                "-fx-background-color: -fx-body-color;" +
                "-fx-background-insets: 2px; " +
                "-fx-padding: 2px;"
    	        );
    	return hmcButton;
    }
	   
	public static void openInBrowser(String url, Button hmc)  {
		
		
		WebView webView = new WebView();
		webView.getEngine().load(url);
		Dialog dlg = new Dialog();
		dlg.getDialogPane().setContent(webView);
		dlg.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
		dlg.setResizable(true);
		dlg.showAndWait();

//        //Platform.runLater(() -> {
//			Desktop desktop;
//	        if (Desktop.isDesktopSupported()) {
//	            desktop = Desktop.getDesktop();
//	            // Now enable buttons for actions that are supported.
//	            if (desktop.isSupported(Desktop.Action.BROWSE)) {
//                    
////                  String s = "/Users/remco/workspace/hmc/_pages" + url.substring(("file://" + System.getProperty("user.dir")).length());
////                	creatDirs(s);
////            		PrintStream out = new PrintStream(new File(s));
////            		out.println(hmc.getTooltip().getText());
////            		out.close();
//                    
//                    try {
//                        URI uri = new URI(url);
//                        desktop.browse(uri);                    		
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    	//Alert.showMessageDialog(null, "Could not find help: " + e.getMessage());
//                    } catch (URISyntaxException e) {
//                        e.printStackTrace();
//                    }
//	            }
//	        }
//        //});
	}

	private static void creatDirs(String file) {
		if (!file.endsWith("/")) {
			file = file.substring(0, file.lastIndexOf('/'));
		}
		new File(file).mkdirs();
	}

}
