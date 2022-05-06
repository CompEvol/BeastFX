package beastfx.app.util;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import beast.app.util.Utils;
import beast.pkgmgmt.PackageManager;

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
        box.setSpacing(10);
        box.setPadding(new Insets(5));
        return box;
	}

	public static VBox newVBox() {
		VBox box = new VBox();
        box.setSpacing(3);
        box.setPadding(new Insets(2));
        return box;
	}

	static public boolean loadStyleSheet(Scene scene) {
        String themeFile = Utils.getBeautiProperty("theme");
        return loadStyleSheet(scene, themeFile);
	}
	
	static public boolean loadStyleSheet(Scene scene, String themeFile) {
		String cssFile = themeFile;
		if (cssFile == null) {
			return false;
		}
		if (!new File(themeFile).exists()) {
			cssFile = PackageManager.getBeastDirectories().get(0) + "/" +themeFile;
			if (!new File(cssFile).exists()) {
    			cssFile = System.getProperty("user.dir") + "/../" +themeFile;
				if (!new File(cssFile).exists()) {
					Alert.showMessageDialog(null, "Could not find theme file " + themeFile + ". Choose another theme, or remove 'theme' entry from beauti.properties.file");
					return false;
				}							
			}
		}
		try {
			scene.getStylesheets().clear();
			scene.getStylesheets().add(new URL("file:///" + cssFile).toExternalForm());
		} catch (MalformedURLException e) {
			return false;
		}
		return true;
	}
}
