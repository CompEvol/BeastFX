package test.beastfx.app.beauti;




import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import beastfx.app.beauti.BeautiTabPane;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.io.File;

@ExtendWith(ApplicationExtension.class)
public class BeautiStarBeastTest extends BeautiBase {

	@Start
    public void start(Stage stage) {
    	try {
    		BeautiTabPane tabPane = BeautiTabPane.main2(new String[] {}, stage);
    		this.doc = tabPane.doc;
            stage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	final static String PREFIX = "doc/tutorials/STARBEAST/figures/BEAUti_";

	@Test
	public void simpleStarBeastTest(FxRobot robot) throws Exception {
//		ScreenshotTaker screenshotTaker = new ScreenshotTaker();
//		beauti.frame.setSize(1024, 640);

		String BASE_DIR = PREFIX.substring(0, PREFIX.lastIndexOf('/'));
		for (File file : new File(BASE_DIR).listFiles()) {
			if (file.getAbsolutePath().contains(PREFIX) && file.getName().endsWith(".png")) {
				file.delete();
			}
		}
		
		// create screen-shot showing template menu item
		warning("Select StarBeast template");
		robot.clickOn("#File").clickOn("Template");
		//beautiFrame.menuItemWithPath("File", "Template").click();
		screenshot( PREFIX + "selectTemplate.png");
		robot.clickOn("#File").clickOn("Template").clickOn("StarBeast");
		//JMenuItemFixture templateMenu = beautiFrame.menuItemWithPath("File", "Template", "StarBeast");
		//templateMenu.click();
		// remove menu from screen
		// beautiFrame.menuItemWithPath("File").click();
		robot.clickOn("#File");
		//JTabbedPaneFixture f = beautiFrame.tabbedPane();
		selectTab(robot, "Priors");


		// 1. Load gopher data 26.nex, 29.nex, 47.nex
		warning("1. Load gopher data 26.nex, 29.nex, 47.nex");
		importAlignment(NEXUS_DIR, new File("26.nex"), new File("29.nex"), new File("47.nex"));

		screenshot( PREFIX + "DataPartitions.png");
		printBeautiState();

		
		// 2. Define Taxon sets
		warning("2. Define taxon sets");
		selectTab(robot, "Taxon sets");
		robot.clickOn("Guess");
		//JOptionPaneFixture dialog = new JOptionPaneFixture(robot());
		//DialogFixture dialog = WindowFinder.findDialog("GuessTaxonSets").using(robot());
		robot.clickOn("split on character");
		robot.clickOn("#splitCombo").clickOn("2");
		robot.clickOn("#SplitChar2"); robot.write("_");
		screenshot( PREFIX + "Guess_Taxonsets.png");
		//JButton okButton = dialog.robot.finder().find(JButtonMatcher.withText("OK"));
		//new JButtonFixture(dialog.robot, okButton).click();
		robot.clickOn("#ok");
		printBeautiState();

		// 3. Set site model to HKY + empirical frequencies
		warning("3. Set site model to HKY + empirical frequencies");
		selectTab(robot, "Site Model");
		ListView<?> list = robot.lookup(".list-view").queryAs(ListView.class);
		for (int i = 0; i < 3; i++) {
			list.getSelectionModel().select(i);
			//beautiFrame.list().selectItem(i);
			robot.clickOn("substModel").clickOn("HKY");
			robot.clickOn("frequencies").clickOn("Empirical");
			//beautiFrame.checkBox("mutationRate.isEstimated").check();
		}
		//JCheckBoxFixture fixMeanMutationRate = beautiFrame.checkBox("FixMeanMutationRate");
		//fixMeanMutationRate.check();
		screenshot( PREFIX + "Site_Model.png");
		printBeautiState();
		
		// 4. Inspect clock models
		warning("4. Inspect clock models");
		list = robot.lookup(".list-view").queryAs(ListView.class);
		selectTab(robot, "Clock Model");
		list.getSelectionModel().select(0);
		screenshot( PREFIX + "ClockModel1.png");
		list.getSelectionModel().select(1);
		screenshot( PREFIX + "ClockModel2.png");
		list.getSelectionModel().select(2);
		screenshot( PREFIX + "ClockModel3.png");
		
		// 5. Inspect multispecies coalescent
		warning("5. Inspect multispecies coalescent");
		selectTab(robot, "Multi Species Coalescent");
		robot.clickOn("#treePrior.t:26.editButton");
		robot.clickOn("#treePrior.t:29.editButton");
		robot.clickOn("#treePrior.t:47.editButton");
		robot.clickOn(".comboBox").clickOn("linear_with_constant_root");
		robot.clickOn("#treePrior.t:26.editButton");
		robot.clickOn("#treePrior.t:29.editButton");
		robot.clickOn("#treePrior.t:47.editButton");
		screenshot(PREFIX + "MSP.png");
		
		// 6. Set up MCMC parameters
		warning("6. Set up MCMC parameters");
		selectTab(robot, "MCMC");
		robot.clickOn("#chainLength"); robot.write("5000000");
		robot.clickOn("#speciesTreeLogger.editButton");
		robot.clickOn("#logEvery"); robot.write("1000");
		robot.clickOn("#speciesTreeLogger.editButton");

		robot.clickOn("#screenlog.editButton");
		robot.clickOn("#logEvery"); robot.write("10000");
		robot.clickOn("#speciesTreeLogger.editButton");
		screenshot(PREFIX + "MCMC.png");
		
		makeSureXMLParses();

	}


}
