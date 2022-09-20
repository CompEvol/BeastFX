package beastfx.app.tools;


import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;


import beast.base.core.ProgramStatus;
import beastfx.app.util.FXUtils;
import beastfx.app.util.Utils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class LogCombinerController implements Initializable {

	static Dialog<String> dialog = null;

    private final ObservableList<FileInfo> files = FXCollections.observableArrayList();
    private File outputFile = null;

    @FXML
    public TableView<FileInfo> filesTable = null;
	
	@FXML
	public ComboBox<String> fileTypeCombo;
	
	@FXML
	public CheckBox decimalCheck;
	
	@FXML
	public CheckBox renumberOutput;
	
	@FXML
	public CheckBox resampleCheck;
	
	@FXML
	public TextField resampleText;

    @FXML
    public Button addButton;

    @FXML
    public Button delButton;

    @FXML
    public TextField fileNameText;
    
    @FXML
    public Button browseButton;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		fileTypeCombo.setItems(FXCollections.observableArrayList(new String[] {"Log Files", "Tree Files"}));
		
		browseButton.setOnAction(e -> {
	        File file = FXUtils.getSaveFile("Select output file...", new File(ProgramStatus.g_sDir), "Beast log and tree files", "log", "trees");
	        if (file == null) {
	            // the dialog was cancelled...
	            return;
	        }
	        outputFile = file;
	        String fileName = file.getAbsolutePath();
	        if (fileName.lastIndexOf(File.separator) > 0) {
	        	ProgramStatus.setCurrentDir(fileName.substring(0, fileName.lastIndexOf(File.separator)));
	        }
	        fileNameText.setText(outputFile.getName());
		});
		
		
		resampleText.setDisable(true);
        resampleCheck.setOnAction(e -> {
            resampleText.setDisable(!resampleCheck.isSelected());
        });

		addButton.setOnAction(e-> {
            File[] files = FXUtils.getLoadFiles("Select log file", new File(ProgramStatus.g_sDir), "Trace or tree log files", "log", "trees");
            if (files != null) {
                addFiles(files);
            }			
		});
		
		filesTable.setItems(files);
        filesTable.getSelectionModel().selectionModeProperty().addListener(e->filesTableSelectionChanged());

        delButton.setDisable(true);
        delButton.setOnAction(e-> {
        	List<Integer> selected = filesTable.getSelectionModel().getSelectedIndices();
        	if (selected.size() == 0) {
        		return;
        	}
        	int row = selected.get(0);
            files.remove(row);

            if (row >= files.size()) {
            	row = files.size() - 1;
            }
            if (row >= 0) {
                filesTable.getSelectionModel().select(row);
            }
        });


		
//        new FileDrop(null, scrollPane1, focusBorder, new FileDrop.Listener() {
//            @Override
//			public void filesDropped(java.io.File[] files) {
//                addFiles(files);
//            }   // end filesDropped
//        }); // end FileDrop.Listener

	}
	
    private void filesTableSelectionChanged() {
        if (filesTable.getSelectionModel().getSelectedItems().size() == 0) {
            delButton.setDisable(true);
        } else {
            delButton.setDisable(false);
        }
    }

    private void addFiles(File[] fileArray) {
        int sel1 = files.size();
        for (File file : fileArray) {
            FileInfo fileInfo = new FileInfo(file);

            files.add(fileInfo);

            String fileName = file.getAbsolutePath();
            if (fileName.lastIndexOf(File.separator) > 0) {
            	ProgramStatus.setCurrentDir(fileName.substring(0, fileName.lastIndexOf(File.separator)));
            }
        }

        filesTable.refresh();

        int sel2 = files.size() - 1;
        filesTable.getSelectionModel().selectRange(sel1, sel2);
    }	
    
	private void closeDialog() {
		if (dialog.getDialogPane().getButtonTypes().size() == 0) {
			// need a button to be able to close the dialog
			dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL);
		}
		dialog.close();
	}
	
    public String[] getFileNames() {
        String[] fileArray = new String[files.size()];
        for (int i = 0; i < files.size(); i++) {
            FileInfo fileInfo = files.get(i);
            fileArray[i] = fileInfo.getFile();
        }
        return fileArray;
    }

    public int[] getBurnins() {
        int[] burnins = new int[files.size()];
        for (int i = 0; i < files.size(); i++) {
            FileInfo fileInfo = files.get(i);
            burnins[i] = fileInfo.getBurnin();
        }
        return burnins;
    }

    public boolean isTreeFiles() {
        return fileTypeCombo.getSelectionModel().getSelectedIndex() == 1;
    }

    public boolean convertToDecimal() {
        return decimalCheck.isSelected();
    }

    public boolean renumberOutputStates() {
        return renumberOutput.isSelected();
    }

    public boolean isResampling() {
        return resampleCheck.isSelected();
    }

    public int getResampleFrequency() {
        return Integer.parseInt(resampleText.getText());
    }

    public String getOutputFileName() {
        if (outputFile == null) return null;
        return outputFile.getPath();
    }

}
