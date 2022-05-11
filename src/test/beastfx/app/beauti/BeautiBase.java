package test.beastfx.app.beauti;




import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;


import beastfx.app.beauti.Beauti;
import beastfx.app.beauti.BeautiTabPane;
import beastfx.app.inputeditor.BeautiConfig;
import beastfx.app.inputeditor.BeautiDoc;
import beastfx.app.inputeditor.AlignmentListInputEditor.Partition0;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;
import beast.app.util.Utils;
import beast.base.core.BEASTInterface;
import beast.base.core.BEASTObject;
import beast.base.core.Function;
import beast.base.inference.CompoundDistribution;
import beast.base.inference.Distribution;
import beast.base.inference.Logger;
import beast.base.inference.MCMC;
import beast.base.inference.Operator;
import beast.base.inference.State;
import beast.base.inference.StateNode;
import beast.base.inference.distribution.Prior;
import beast.base.inference.parameter.Parameter;
import beast.base.parser.XMLParser;



/**
 * Basic test methods for Beauti  
 * 
 */
@ExtendWith(ApplicationExtension.class)
public class BeautiBase extends Beauti {
	final static String TEMPLATE_DIR = BeautiConfig.TEMPLATE_DIR;
	final static String NEXUS_DIR = "../beast2/examples/nexus/";

//	protected FrameFixture beautiFrame;
//	protected Beauti beauti;
	protected BeautiDoc doc;

//    @Start
//    public void start(Stage stage) {
//    	try {
//    		BeautiTabPane tabPane = BeautiTabPane.main2(new String[] {}, stage);
//    		this.doc = tabPane.doc;
//            stage.show();
//			super.start(stage);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//    }

	String priorsAsString() {
		CompoundDistribution prior = (CompoundDistribution) doc.pluginmap.get("prior");
		List<Distribution> priors = prior.pDistributions.get();
		return "assertPriorsEqual" + pluginListAsString(priors);
	}
	
	String stateAsString() {
		State state = (State) doc.pluginmap.get("state");
		List<StateNode> stateNodes = state.stateNodeInput.get();
		return "assertStateEquals" + pluginListAsString(stateNodes);
	}

	String operatorsAsString() {
		MCMC mcmc = (MCMC) doc.mcmc.get();
		List<Operator> operators = mcmc.operatorsInput.get();
		return "assertOperatorsEqual" + pluginListAsString(operators);
	}
	
	String traceLogAsString() {
		Logger logger = (Logger) doc.pluginmap.get("tracelog");
		List<BEASTObject> logs = logger.loggersInput.get();
		return "assertTraceLogEqual" + pluginListAsString(logs);
	}

	
	private String pluginListAsString(List<?> list) {
		if (list.size() == 0) {
			return "";
		}
		StringBuffer bf = new StringBuffer();
		for (Object o : list) {
			BEASTObject beastObject = (BEASTObject) o;
			bf.append('"');
			bf.append(beastObject.getID());
			bf.append("\", ");
		}
		String str = bf.toString();
		return "(" + str.substring(0, str.length()-2) + ");";
	}

	void assertPriorsEqual(String... ids) {
		System.err.println("assertPriorsEqual");
		CompoundDistribution prior = (CompoundDistribution) doc.pluginmap.get("prior");
		List<Distribution> priors = prior.pDistributions.get();
		for (String id : ids) {
			boolean found = false;
			for (BEASTObject node : priors) {
				if (node.getID().equals(id)) {
					found = true;
				}
			}
			assertThat(found).as("Could not find beastObject with ID " + id).isEqualTo(true);
		}
		List<String> extras = new ArrayList<>();
		for (BEASTObject node : priors) {
			boolean found = false;
			for (String id : ids) {
				if (node.getID().equals(id)) {
					found = true;
				}
			}
			if (!found) {
				extras.add(node.getID());
			}
		}
		if (extras.size() != 0) {
			System.err.println("Extra ids found: " + Arrays.toString(extras.toArray(new String[]{})));
		}
		assertThat(ids.length).as("list of beastObjects do not match").isEqualTo(priors.size());;
	}

	private void asserListsEqual(List<?> list, String[] ids) {
		// check all ids are in list
		for (String id : ids) {
			boolean found = false;
			for (Object o: list) {
				BEASTObject node = (BEASTObject) o;
				if (node.getID().equals(id)) {
					found = true;
					break;
				}
			}
			assertThat(found).as("Could not find beastObject with ID " + id).isEqualTo(true);
		}
		// check all items in list have a unique ie
		Set<String> idsInList = new HashSet<String>();
		Set<String> duplicates = new HashSet<String>();
		for (Object o : list) {
			String id = ((BEASTObject) o).getID();
			if (idsInList.contains(id)) {
				duplicates.add(id);
			} else {
				idsInList.add(id);
			}
		}
		assertThat(duplicates.size()).as("Duplicate ids found: " + Arrays.toString(duplicates.toArray())).isEqualTo(0);
		
		if (list.size() != ids.length) {
			// list.size > ids.length, otherwise it would have been picked up above
			List<String> extraIDs = new ArrayList<String>(); 
			for (Object o : list) {
				String id = ((BEASTObject) o).getID();
				boolean found = false;
				for (String id2 : ids) {
					if (id2.equals(id)) {
						found = true;
						break;
					}
				}
				if (!found) {
					extraIDs.add(id);
				}
			}
			assertThat(ids.length).as("list of beastObjects do not match: found extra items " + Arrays.toString(extraIDs.toArray())).isEqualTo(list.size());
		}
	}

	void assertStateEquals(String... ids) {
		System.err.println("assertStateEquals");
		State state = (State) doc.pluginmap.get("state");
		List<StateNode> stateNodes = state.stateNodeInput.get();
		asserListsEqual(stateNodes, ids);
	}

	void assertOperatorsEqual(String... ids) {
		System.err.println("assertOperatorsEqual");
		MCMC mcmc = (MCMC) doc.mcmc.get();
		List<Operator> operators = mcmc.operatorsInput.get();
		asserListsEqual(operators, ids);
	}

	void assertTraceLogEqual(String... ids) {
		System.err.println("assertTraceLogEqual");
		Logger logger = (Logger) doc.pluginmap.get("tracelog");
		List<BEASTObject> logs = logger.loggersInput.get();
		asserListsEqual(logs, ids);
	}

	void assertArrayEquals(Object [] o, String array) {
		String str = array.substring(1, array.length() - 1);
		String [] strs = str.split(", ");
		for (int i = 0; i < o.length && i < strs.length; i++) {
			assertThat(strs[i]).as("expected array value " + strs[i] + " instead of " + o[i].toString()).isEqualTo(o[i].toString());
		}
		assertThat(o.length).as("arrays do not match: different lengths").isEqualTo(strs.length);
	}
	
	void assertParameterCountInPriorIs(int i) {
		// count nr of parameters in Prior objects in prior
		// including those for prior distributions (Normal, etc)
		// useful to make sure they do (or do not) get linked
		Set<Function> parameters = new LinkedHashSet<>();
		CompoundDistribution prior = (CompoundDistribution) doc.pluginmap.get("prior");
		for (Distribution p : prior.pDistributions.get()) {
			if (p instanceof Prior) {
				Prior p2 = (Prior) p;
				parameters.add(p2.m_x.get());
				for (BEASTInterface o : p2.distInput.get().listActiveBEASTObjects()) {
					if (o instanceof Parameter) {
						parameters.add((Parameter<?>) o);
					}
				}
			}
		}
		System.err.println("Number of parameters in prior = " + parameters.size());
		if (i >= 0) {
			assertThat(parameters.size()).as("Expected " + i + " parameters in prior").isEqualTo(i);
		}
	}
	
	void printBeautiState() throws InterruptedException {
        doc.scrubAll(true, false);
		// f.selectTab("MCMC");
		System.err.println(stateAsString());
		System.err.println(operatorsAsString());
		System.err.println(priorsAsString());
		System.err.println(traceLogAsString());
	}

//	void printTableContents(JTableFixture t) {
//		String [][] contents = t.contents();
//		for (int i = 0; i < contents.length; i++) {
//			System.err.print("\"" + Arrays.toString(contents[i]));
//			if (i < contents.length - 1) {
//				System.err.print("*\" +");
//			} else {
//				System.err.print("\"");
//			}
//			System.err.println();
//		}
//	}
//	
//	void checkTableContents(JTableFixture t, String str) {
//		String [][] contents = t.contents();
//		String [] strs = str.split("\\*");
//		assertThat(contents.length).as("tables do not match: different #rows").isEqualTo(strs.length);
//		for (int i = 0; i < contents.length; i++) {
//			assertArrayEquals(contents[i], strs[i]);
//		}
//	}

	void warning(String str) {
		System.err.println("\n\n=====================================================\n");
		System.err.println(str);
		System.err.println("\n=====================================================\n\n");
	}
	
	void makeSureXMLParses() {
		warning("Make sure that XML that BEAUti produces parses");
		File XMLFile = new File(org.assertj.core.util.Files.temporaryFolder() + "/x.xml");
		if (XMLFile.exists()) {
			XMLFile.delete();
		}
		
		saveFile(""+org.assertj.core.util.Files.temporaryFolder(), "x.xml");

//		JFileChooserFixture fileChooser = findFileChooser().using(robot());
//		fileChooser.setCurrentDirectory(org.assertj.core.util.Files.temporaryFolder());
//		fileChooser.selectFile(new File("x.xml")).approve();
		
		XMLParser parser = new XMLParser();
		XMLFile = new File(org.assertj.core.util.Files.temporaryFolder() + "/x.xml");
		try {
			parser.parseFile(XMLFile);
		} catch (Exception e) {
			e.printStackTrace();
			assertThat(0).as("Parser exception: " + e.getMessage()).isEqualTo(1);
		}
	}

	protected void saveFile(String dir, String file) {
//		if (!Utils.isMac()) {
//			beautiFrame.menuItemWithPath("File", "Save As").click();
//			JFileChooserFixture fileChooser = findFileChooser().using(robot());
//			fileChooser.setCurrentDirectory(new File(dir));
//			fileChooser.selectFile(new File(file)).approve();
//		} else {
			File _file = new File(dir + "/" + file);
			//Platform.runLater(()-> {
		        	try {
		        		doc.save(_file);
		        	} catch (Exception e) {
						e.printStackTrace();
					}
		    //});
			
//		}	
	}

	// for handling file open events on Mac
//	FileDialog fileDlg = null;
//	String _dir;
//	File _file;

	void importAlignment(String dir, File ... files) {
//		if (!Utils.isMac()) {
//			beautiFrame.menuItemWithPath("File", "Import Alignment").click();
//			JFileChooserFixture fileChooser = findFileChooser().using(robot());
//			fileChooser.setCurrentDirectory(new File(dir));
//			fileChooser.selectFiles(files).approve();
//			// close down any popup message
//			robot().pressKey(KeyEvent.VK_ESCAPE);
//		} else {
			String _dir;
			_dir = dir;
			for (File file : files) {
				File _file = new File(dir + "/" + file.getName());
				if (!_file.exists()) {
					throw new IllegalArgumentException("File " + _file.getName() + " does not exist, so cannot be imported");
				}
				Platform.runLater(() -> {
		        	try {
		        		doc.importNexus(_file);
		    			doc.beauti.refreshPanel();
		        	} catch (Exception e) {
						e.printStackTrace();
					}
			    });
			}
//		}			
	}
	
	
	protected String[] tabTitles(BeautiTabPane pane) {
		String [] titles = new String[pane.getTabs().size()];
		int i = 0;
		for (Tab tab : pane.getTabs()) {
			titles[i++] = tab.getText();
		}
		return titles;
	}

	
	protected void screenshot(String filename) {
		Platform.runLater(() -> {
			WritableImage snapshot = doc.beauti.snapshot(new SnapshotParameters(), null);
			try {
				ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png", new File(filename));
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

	protected String[][] tableContents(TableView<Partition0> table) {
		String [][] contents = new String[table.getItems().size()][table.getColumns().size()];
		for (int i = 0; i < contents.length; i++) {
			Partition0 partition = table.getItems().get(i);
			for (int j = 0; j < contents[0].length; j++) {
			    TableColumn col = table.getColumns().get(j);
			    contents[i][j] = col.getCellObservableValue(partition).getValue().toString() +"";
			}
		}
		return contents;
	}



}