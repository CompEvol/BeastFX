package beastfx.app.util;

import javafx.geometry.Insets;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.awt.Desktop;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import beast.base.core.BEASTInterface;
import beast.base.core.Input;

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
            return files.toArray(new File[]{});
    	} else {
    		File file = fileChooser.showSaveDialog(null);
    		return new File[] {file};
    	}
    }

	public static ImageView getIcon(String string) {
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
	
	
	
	public static void helpMeChoose(BEASTInterface o, Input<?> input)  {
		String id = o.getID();
		if (id.lastIndexOf('.') > 0) {
			id = id.substring(0, id.lastIndexOf('.'));
		}
		String HMC_BASE = "file://" + System.getProperty("user.dir") + "/hmc";
		String url = HMC_BASE + "/" + id + "/" + input.getName() + ".html";
		openInBrowser(url);
	}
	
	public static void helpMeChoose(String templateName, String tabName) {
		String HMC_BASE = "file://" + System.getProperty("user.dir") + "/hmc";
		String url = HMC_BASE + "/" + templateName + "/" + tabName + ".html";
		url = url.replaceAll(" ", "_");
		openInBrowser(url);
	}

	public static void openInBrowser(String url)  {
		Desktop desktop;
        if (Desktop.isDesktopSupported()) {
            desktop = Desktop.getDesktop();
            // Now enable buttons for actions that are supported.
            if (desktop.isSupported(Desktop.Action.BROWSE)) {
                URI uri = null;
                try {
                    uri = new URI(url);
                    desktop.browse(uri);
                } catch (IOException e) {
                	Alert.showMessageDialog(null, "Could not find help: " + e.getMessage());
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        }
	}

}
