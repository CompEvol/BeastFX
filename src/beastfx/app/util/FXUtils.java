package beastfx.app.util;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import beast.app.util.Utils;
import javafx.stage.FileChooser;

import java.io.*;
import java.util.List;

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
    		if (defaultFileOrDir.isFile()) {
    			fileChooser.setInitialDirectory(defaultFileOrDir.getParentFile());
    			fileChooser.setInitialFileName(defaultFileOrDir.getName());
    		} else {
    			fileChooser.setInitialDirectory(defaultFileOrDir);
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

}
