package beastfxml.app;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import beagle.BeagleFlag;
import beagle.BeagleInfo;
import beast.base.core.Log;
import beast.base.util.Randomizer;
import beastfx.app.beast.BeastMCMC;
import beastfx.app.beast.BeastMain;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
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

	@FXML
	private WebView output;

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
		
		WebEngine engine = output.getEngine();

		StringBuilder script = new StringBuilder().append("<html>");
		script.append("<head>");
		script.append("   <script language=\"javascript\" type=\"text/javascript\">");
		script.append("       function toBottom(){");
		script.append("           window.scrollTo(0,document.body.scrollHeight);");
		script.append("       }");
		script.append("   </script>");
		script.append("</head>");
		script.append("<body onload='toBottom()'>");
		script.append("<div id='content' style='color:#0000D0;padding:0;border:0;margin:0;'> "
				+ "<pre id='pre'></pre></div></body></html>");

		engine.loadContent(script.toString());

		PrintStream p1 = new PrintStream(new BOAS("color:blue"));
		PrintStream p2 = new PrintStream(new BOAS("color:red"));
		PrintStream p3 = new PrintStream(new BOAS("color:green"));
		System.setOut(p1);
		System.setErr(p2);
		Log.err = p2;
		Log.warning = p2;
		Log.info = p1;
		Log.debug = p3;
		Log.trace = p3;

		isPre = true;
		BeastMain.printTitle();
		isPre = false;

		
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

	private boolean isPre;

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
		// new Runnable() {
		// public void run() {
		Platform.runLater(new Runnable() {
			public void run() { /* your code here */
				Document doc = output.getEngine().getDocument();
				if (_style != null) {
					backLog.add(new Message(_data, _style));
				}
				if (doc == null) {
					return;
				}
				for (Message msg : backLog) {
					String data = msg.data;
					String style = msg.style;
					if (isPre) {
						Element el = doc.getElementById("pre");
						String text = el.getTextContent();
						text += "\n" + data;
						el.setTextContent(text);
					} else {
						Element newLine = doc.createElement("DIV");
						newLine.setAttribute("style", "padding: 0 0 0 0;" + style);
						newLine.appendChild(doc.createTextNode(data));
						Element el = doc.getElementById("pre");
						el.appendChild(newLine);
						// el.appendChild(doc.createElement("BR"));
					}
				}
				output.getEngine().executeScript("toBottom()");
				backLog.clear();
			}
		});
		// }
		// };

	}

	/** logging with colour **/
	class BOAS extends ByteArrayOutputStream {
		String style;
		StringBuilder buf = new StringBuilder();

		BOAS(String style) {
			this.style = style;
		}

		@Override
		public synchronized void write(byte[] b, int off, int len) {
			super.write(b, off, len);
			log(b, off, len);
		};

		@Override
		public synchronized void write(int b) {
			super.write(b);
			log(b);
		};

		@Override
		public void write(byte[] b) throws java.io.IOException {
			super.write(b);
			log(b);
		};

		private void log(byte[] b, int off, int len) {
			for (int i = off; i < len; i++) {
				log(b[i]);
			}
		}

		private void log(int b) {
			if (b == '\n') {
				logToView(buf.toString(), style);
				buf = new StringBuilder();
			} else {
				buf.append((char) b);
			}

		}

		private void log(byte[] b) {
			for (byte i : b) {
				if (i == 0) {
					return;
				}
				log(i);
			}
		}

		@Override
		public void flush() throws java.io.IOException {
			super.flush();
		};

		@Override
		public void close() throws IOException {
			super.close();
		}
	};
	
	@FXML
	void showBeagleInfo() {
		BeagleInfo.printResourceList();
		runButton.setText("Quit");
	}

}
