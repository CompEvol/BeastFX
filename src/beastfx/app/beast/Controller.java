package beastfx.app.beast;


import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import beagle.BeagleFlag;
import beagle.BeagleInfo;
import beast.base.core.Log;
import beast.base.util.Randomizer;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class Controller implements Initializable {

	@FXML
	private TextField inputFile;

	@FXML
	private TextField seed;


	@FXML
	private Button runButton;
	
	@FXML
	private ChoiceBox<Integer> threads;
	@FXML
	private ChoiceBox<Integer> instances;

	@FXML
	private ChoiceBox<String> logFileMode;
	@FXML
	private ChoiceBox<Log.Level> logLevel;
	@FXML
	private ChoiceBox<String> precision;
	@FXML
	private ChoiceBox<String> scaling;
	@FXML
	private ChoiceBox<String> beagle;

	private final String[] logFileModes = new String[] { "default: only write new log files", "overwrite: overwrite log files",
			"resume: appends log to existing files (if any)" };
	private final String[] precisions = new String[] {"Single", "Double"};
	private final String[] scalings = new String[] {"Default", "None", "Dynamic", "Always"};
	private final String[] beagles = new String[] {"java", "CPU", "SSE", "GPU"};

	private Stage stage;

	private Thread beastThread = null;

	void setStage(Stage stage) {
		this.stage = stage;
	}

	@FXML
	void chooseFile() {
		FileChooser fc = new FileChooser();
		fc.setTitle("Choose BEAST input file");

		fc.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("BEAST input files", "*.xml", "*.json"),
				new FileChooser.ExtensionFilter("All files", "*.*"));
		File file = fc.showOpenDialog(stage);
		if (file != null) {
			inputFile.setText(file.getPath());
		}
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.seed.setText(Randomizer.nextLong() + "");
		//this.useBeagle.setSelected(true);
		this.inputFile.setText("/tmp/x.xml");
		this.logFileMode.setItems(FXCollections.observableArrayList(logFileModes));
		logFileMode.setValue(logFileModes[0]);

		logLevel.setItems(FXCollections.observableArrayList(Log.Level.values()));
		logLevel.setValue(Log.Level.info);

		precision.setItems(FXCollections.observableArrayList(precisions));
		precision.setValue(precisions[0]);
		
		scaling.setItems(FXCollections.observableArrayList(scalings));
		scaling.setValue(scalings[0]);

		beagle.setItems(FXCollections.observableArrayList(beagles));
		beagle.setValue(beagles[0]);
		
		BeastMain.printTitle();

		
		Set<Integer> set = IntStream.range(1, 21).boxed().collect(Collectors.toSet());
		threads.setItems(FXCollections.observableArrayList(set));
		threads.setValue(1);
		
		instances.setItems(FXCollections.observableArrayList(set));
		instances.setValue(1);
		
		new Thread() {
			public void run() {
				try {
					sleep(2000);
					// clear backlog if any
					logToView(null, null);
				} catch (InterruptedException e) {
				}
			};
		}.start();
	}

	@FXML
	void run() {
		if (runButton.getText().equals("Quit")) {
			System.exit(0);
		}
		
		if (beastThread == null) {
			logToView("trying to run " + inputFile.getText(), "color:green");

			final List<String> MCMCargs = getMCMCargs();
			final BeastMCMC beastMCMC = new BeastMCMC();

			beastThread = new Thread() {
				public void run() {
					try {
						// set all the settings...
						beastMCMC.parseArgs(MCMCargs.toArray(new String[0]));
						beastMCMC.run();
					} catch (Exception e) {
						e.printStackTrace();
					}
					Platform.runLater(new Runnable() {
						public void run() { /* your code here */

							beastThread = null;
							runButton.setText("Quit");
						}
					});
				};
			};
			beastThread.start();
			runButton.setText("Stop");
		} else {
			beastThread.stop();
			beastThread = null;
			runButton.setText("Quit");
		}
	}

	private List<String> getMCMCargs() {
		List<String> MCMCArgs = new ArrayList<>();
		MCMCArgs.add("-seed");
		MCMCArgs.add(seed.getText());
		MCMCArgs.add("-threads");
		MCMCArgs.add(threads.getSelectionModel().getSelectedItem() + "");
        System.setProperty("beast.instance.count", instances.getSelectionModel().getSelectedItem() +"");

		String beagle = this.beagle.getSelectionModel().getSelectedItem();
		if (beagle.equals(beagles[0])) {
            System.setProperty("java.only", "true");
		} else {
			long beagleFlags = 0;
			if (beagle.equals(beagles[1])) {
	            beagleFlags |= BeagleFlag.PROCESSOR_CPU.getMask();
			} else if (beagle.equals(beagles[2])) {
	            beagleFlags |= BeagleFlag.VECTOR_SSE.getMask();
			} else {
	            beagleFlags |= BeagleFlag.PROCESSOR_GPU.getMask();
				if (precision.getSelectionModel().getSelectedItem().equals(precisions[0])) {
		            beagleFlags |= BeagleFlag.PRECISION_SINGLE.getMask();
				} else {
		            beagleFlags |= BeagleFlag.PRECISION_DOUBLE.getMask();
				}
			}
			
			
			switch (scaling.getSelectionModel().getSelectedItem()) {
			case "Default":
	            beagleFlags |= BeagleFlag.SCALING_AUTO.getMask();
				break;
			case "None":
	            beagleFlags |= BeagleFlag.SCALING_MANUAL.getMask();
				break;
			case "Dynamic":
	            beagleFlags |= BeagleFlag.SCALING_DYNAMIC.getMask();
				break;
			case "Always":
	            beagleFlags |= BeagleFlag.SCALING_ALWAYS.getMask();
				break;
			}
            System.setProperty("beagle.preferred.flags", Long.toString(beagleFlags));
        }

		
		switch (this.logFileMode.getSelectionModel().getSelectedIndex()) {
		case 0:
			break;
		case 1:
			MCMCArgs.add("-overwrite");
			break;
		case 2:
			MCMCArgs.add("-resume");
			break;
		}

		Log.setLevel(logLevel.getSelectionModel().getSelectedItem());
		
		MCMCArgs.add(inputFile.getText());
		return MCMCArgs;
	}

	class Message {
		public Message(String data, String style) {
			this.data = data;
			this.style = style;
		}
		
		String data; 
		String style;
	};
	List<Message> backLog = new ArrayList<>();
	
	void logToView(String _data, String _style) {
		Log.info(_data);
		System.out.println(_data);
	}

	
	@FXML
	void showBeagleInfo() {
		BeagleInfo.printResourceList();
		runButton.setText("Quit");
	}

}
