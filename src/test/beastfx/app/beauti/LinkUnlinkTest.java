package test.beastfx.app.beauti;




import java.io.File;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import beast.app.util.Utils;
import beastfx.app.beauti.BeautiTabPane;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
public class LinkUnlinkTest extends BeautiBase {

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
	
	
	protected void clickOn(FxRobot robot, String buttonText) {
//		robot.clickOn(".button[text=\""+buttonText+"\"]");
		Node node = (Node) robot.lookup(target -> {
			if (target instanceof Button) {
				return buttonText.equals(((Button)target).getText());
			}
		    return false;
		}).queryAs(Node.class);
		
////		Node node = robot.lookup(new BaseMatcher<Node>() {
////			  @Override
////			  public boolean matches(Object target)
////			  {
////				if (target instanceof Button) {
////					return buttonText.equals(((Button)target).getText());
////				}
////			    return false;
////			  }
////
////			@Override
////			public void describeTo(Description arg0) {
////			}
////		}).query();
//
		robot.clickOn(node);
	}

	@Test
	public void simpleLinkUnlinkTwoAlignmentTest(FxRobot robot) throws Exception {
		warning("Load gopher data 26.nex, 47.nex");
		importAlignment(NEXUS_DIR, new File("26.nex"), new File("47.nex"));
		
		// JTabbedPaneFixture f = beautiFrame.tabbedPane();
		printBeautiState();

		selectPartitions(robot, 0, 1);
		
		warning("Link site models");
		selectTab(robot, "Partitions");
		clickOn(robot, "Link Site Models");
		printBeautiState();

		warning("Unlink site models");
		selectTab(robot, "Partitions");
		clickOn(robot, "Unlink Site Models");
		printBeautiState();
		assertStateEquals("Tree.t:26", "birthRate.t:26", "Tree.t:47", "clockRate.c:47", "birthRate.t:47");
		assertOperatorsEqual("YuleBirthRateScaler.t:26", "YuleModelTreeScaler.t:26", "YuleModelTreeRootScaler.t:26", "YuleModelUniformOperator.t:26", "YuleModelSubtreeSlide.t:26", "YuleModelNarrow.t:26", "YuleModelWide.t:26", "YuleModelWilsonBalding.t:26", "StrictClockRateScaler.c:47", "YuleBirthRateScaler.t:47", "YuleModelTreeScaler.t:47", "YuleModelTreeRootScaler.t:47", "YuleModelUniformOperator.t:47", "YuleModelSubtreeSlide.t:47", "YuleModelNarrow.t:47", "YuleModelWide.t:47", "YuleModelWilsonBalding.t:47", "strictClockUpDownOperator.c:47");
		assertPriorsEqual("YuleModel.t:26", "YuleBirthRatePrior.t:26", "YuleModel.t:47", "ClockPrior.c:47", "YuleBirthRatePrior.t:47");
		assertTraceLogEqual("posterior", "likelihood", "prior", "treeLikelihood.26", "TreeHeight.t:26", "YuleModel.t:26", "birthRate.t:26", "treeLikelihood.47", "TreeHeight.t:47", "clockRate.c:47", "YuleModel.t:47", "birthRate.t:47");
		
		warning("Link clock models");
		selectTab(robot, "Partitions");
		clickOn(robot, "Link Clock Models");
		printBeautiState();

		warning("Unlink clock models");
		selectTab(robot, "Partitions");
		clickOn(robot, "Unlink Clock Models");
		printBeautiState();
		assertStateEquals("Tree.t:26", "birthRate.t:26", "Tree.t:47", "clockRate.c:47", "birthRate.t:47");
		assertOperatorsEqual("YuleBirthRateScaler.t:26", "YuleModelTreeScaler.t:26", "YuleModelTreeRootScaler.t:26", "YuleModelUniformOperator.t:26", "YuleModelSubtreeSlide.t:26", "YuleModelNarrow.t:26", "YuleModelWide.t:26", "YuleModelWilsonBalding.t:26", "StrictClockRateScaler.c:47", "YuleBirthRateScaler.t:47", "YuleModelTreeScaler.t:47", "YuleModelTreeRootScaler.t:47", "YuleModelUniformOperator.t:47", "YuleModelSubtreeSlide.t:47", "YuleModelNarrow.t:47", "YuleModelWide.t:47", "YuleModelWilsonBalding.t:47", "strictClockUpDownOperator.c:47");
		assertPriorsEqual("YuleModel.t:26", "YuleBirthRatePrior.t:26", "YuleModel.t:47", "ClockPrior.c:47", "YuleBirthRatePrior.t:47");
		assertTraceLogEqual("posterior", "likelihood", "prior", "treeLikelihood.26", "TreeHeight.t:26", "YuleModel.t:26", "birthRate.t:26", "treeLikelihood.47", "TreeHeight.t:47", "clockRate.c:47", "YuleModel.t:47", "birthRate.t:47");

		warning("Link trees");
		selectTab(robot, "Partitions");
		clickOn(robot, "Link Trees");
		printBeautiState();

		warning("Unlink trees");
		selectTab(robot, "Partitions");
		clickOn(robot, "Unlink Trees");
		printBeautiState();
		assertStateEquals("Tree.t:26", "birthRate.t:26", "Tree.t:47", "clockRate.c:47", "birthRate.t:47");
		assertOperatorsEqual("YuleBirthRateScaler.t:26", "YuleModelTreeScaler.t:26", "YuleModelTreeRootScaler.t:26", "YuleModelUniformOperator.t:26", "YuleModelSubtreeSlide.t:26", "YuleModelNarrow.t:26", "YuleModelWide.t:26", "YuleModelWilsonBalding.t:26", "StrictClockRateScaler.c:47", "YuleBirthRateScaler.t:47", "YuleModelTreeScaler.t:47", "YuleModelTreeRootScaler.t:47", "YuleModelUniformOperator.t:47", "YuleModelSubtreeSlide.t:47", "YuleModelNarrow.t:47", "YuleModelWide.t:47", "YuleModelWilsonBalding.t:47", "strictClockUpDownOperator.c:47");
		assertPriorsEqual("YuleModel.t:26", "YuleBirthRatePrior.t:26", "YuleModel.t:47", "ClockPrior.c:47", "YuleBirthRatePrior.t:47");
		assertTraceLogEqual("posterior", "likelihood", "prior", "treeLikelihood.26", "TreeHeight.t:26", "YuleModel.t:26", "birthRate.t:26", "treeLikelihood.47", "TreeHeight.t:47", "clockRate.c:47", "YuleModel.t:47", "birthRate.t:47");

		makeSureXMLParses();
	}

	@Test
	public void simpleLinkUnlinkThreeAlignmentsTest(FxRobot robot) throws Exception {
		warning("Load gopher data 26.nex, 47.nex, 59.nex");
		importAlignment(NEXUS_DIR, new File("26.nex"), new File("47.nex"), new File("59.nex"));

		// JTabbedPaneFixture f = beautiFrame.tabbedPane();
		printBeautiState();
		assertStateEquals("Tree.t:26", "birthRate.t:26", "Tree.t:47", "clockRate.c:47", "birthRate.t:47", "Tree.t:59", "clockRate.c:59", "birthRate.t:59");
		assertOperatorsEqual("YuleBirthRateScaler.t:26", "YuleModelTreeScaler.t:26", "YuleModelTreeRootScaler.t:26", "YuleModelUniformOperator.t:26", "YuleModelSubtreeSlide.t:26", "YuleModelNarrow.t:26", "YuleModelWide.t:26", "YuleModelWilsonBalding.t:26", "StrictClockRateScaler.c:47", "YuleBirthRateScaler.t:47", "YuleModelTreeScaler.t:47", "YuleModelTreeRootScaler.t:47", "YuleModelUniformOperator.t:47", "YuleModelSubtreeSlide.t:47", "YuleModelNarrow.t:47", "YuleModelWide.t:47", "YuleModelWilsonBalding.t:47", "strictClockUpDownOperator.c:47", "StrictClockRateScaler.c:59", "YuleBirthRateScaler.t:59", "YuleModelTreeScaler.t:59", "YuleModelTreeRootScaler.t:59", "YuleModelUniformOperator.t:59", "YuleModelSubtreeSlide.t:59", "YuleModelNarrow.t:59", "YuleModelWide.t:59", "YuleModelWilsonBalding.t:59", "strictClockUpDownOperator.c:59");
		assertPriorsEqual("YuleModel.t:26", "YuleBirthRatePrior.t:26", "YuleModel.t:47", "ClockPrior.c:47", "YuleBirthRatePrior.t:47", "YuleModel.t:59", "ClockPrior.c:59", "YuleBirthRatePrior.t:59");
		assertTraceLogEqual("posterior", "likelihood", "prior", "treeLikelihood.26", "TreeHeight.t:26", "YuleModel.t:26", "birthRate.t:26", "treeLikelihood.47", "TreeHeight.t:47", "clockRate.c:47", "YuleModel.t:47", "birthRate.t:47", "treeLikelihood.59", "TreeHeight.t:59", "clockRate.c:59", "YuleModel.t:59", "birthRate.t:59");

		selectPartitions(robot, 0, 1, 2);

		warning("Link site models");
		selectTab(robot, "Partitions");
		clickOn(robot, "Link Site Models");
		printBeautiState();

		warning("Unlink site models");
		selectTab(robot, "Partitions");
		clickOn(robot, "Unlink Site Models");
		printBeautiState();
		assertStateEquals("Tree.t:26", "birthRate.t:26", "Tree.t:47", "clockRate.c:47", "birthRate.t:47", "Tree.t:59", "clockRate.c:59", "birthRate.t:59");
		assertOperatorsEqual("YuleBirthRateScaler.t:26", "YuleModelTreeScaler.t:26", "YuleModelTreeRootScaler.t:26", "YuleModelUniformOperator.t:26", "YuleModelSubtreeSlide.t:26", "YuleModelNarrow.t:26", "YuleModelWide.t:26", "YuleModelWilsonBalding.t:26", "StrictClockRateScaler.c:47", "YuleBirthRateScaler.t:47", "YuleModelTreeScaler.t:47", "YuleModelTreeRootScaler.t:47", "YuleModelUniformOperator.t:47", "YuleModelSubtreeSlide.t:47", "YuleModelNarrow.t:47", "YuleModelWide.t:47", "YuleModelWilsonBalding.t:47", "strictClockUpDownOperator.c:47", "StrictClockRateScaler.c:59", "YuleBirthRateScaler.t:59", "YuleModelTreeScaler.t:59", "YuleModelTreeRootScaler.t:59", "YuleModelUniformOperator.t:59", "YuleModelSubtreeSlide.t:59", "YuleModelNarrow.t:59", "YuleModelWide.t:59", "YuleModelWilsonBalding.t:59", "strictClockUpDownOperator.c:59");
		assertPriorsEqual("YuleModel.t:26", "YuleBirthRatePrior.t:26", "YuleModel.t:47", "ClockPrior.c:47", "YuleBirthRatePrior.t:47", "YuleModel.t:59", "ClockPrior.c:59", "YuleBirthRatePrior.t:59");
		assertTraceLogEqual("posterior", "likelihood", "prior", "treeLikelihood.26", "TreeHeight.t:26", "YuleModel.t:26", "birthRate.t:26", "treeLikelihood.47", "TreeHeight.t:47", "clockRate.c:47", "YuleModel.t:47", "birthRate.t:47", "treeLikelihood.59", "TreeHeight.t:59", "clockRate.c:59", "YuleModel.t:59", "birthRate.t:59");
		
		warning("Link clock models");
		selectTab(robot, "Partitions");
		clickOn(robot, "Link Clock Models");
		printBeautiState();

		warning("Unlink clock models");
		selectTab(robot, "Partitions");
		clickOn(robot, "Unlink Clock Models");
		printBeautiState();
		assertStateEquals("Tree.t:26", "birthRate.t:26", "Tree.t:47", "clockRate.c:47", "birthRate.t:47", "Tree.t:59", "clockRate.c:59", "birthRate.t:59");
		assertOperatorsEqual("YuleBirthRateScaler.t:26", "YuleModelTreeScaler.t:26", "YuleModelTreeRootScaler.t:26", "YuleModelUniformOperator.t:26", "YuleModelSubtreeSlide.t:26", "YuleModelNarrow.t:26", "YuleModelWide.t:26", "YuleModelWilsonBalding.t:26", "StrictClockRateScaler.c:47", "YuleBirthRateScaler.t:47", "YuleModelTreeScaler.t:47", "YuleModelTreeRootScaler.t:47", "YuleModelUniformOperator.t:47", "YuleModelSubtreeSlide.t:47", "YuleModelNarrow.t:47", "YuleModelWide.t:47", "YuleModelWilsonBalding.t:47", "strictClockUpDownOperator.c:47", "StrictClockRateScaler.c:59", "YuleBirthRateScaler.t:59", "YuleModelTreeScaler.t:59", "YuleModelTreeRootScaler.t:59", "YuleModelUniformOperator.t:59", "YuleModelSubtreeSlide.t:59", "YuleModelNarrow.t:59", "YuleModelWide.t:59", "YuleModelWilsonBalding.t:59", "strictClockUpDownOperator.c:59");
		assertPriorsEqual("YuleModel.t:26", "YuleBirthRatePrior.t:26", "YuleModel.t:47", "ClockPrior.c:47", "YuleBirthRatePrior.t:47", "YuleModel.t:59", "ClockPrior.c:59", "YuleBirthRatePrior.t:59");
		assertTraceLogEqual("posterior", "likelihood", "prior", "treeLikelihood.26", "TreeHeight.t:26", "YuleModel.t:26", "birthRate.t:26", "treeLikelihood.47", "TreeHeight.t:47", "clockRate.c:47", "YuleModel.t:47", "birthRate.t:47", "treeLikelihood.59", "TreeHeight.t:59", "clockRate.c:59", "YuleModel.t:59", "birthRate.t:59");

		warning("Link trees");
		selectTab(robot, "Partitions");
		clickOn(robot, "Link Trees");
		printBeautiState();

		warning("Unlink trees");
		selectTab(robot, "Partitions");
		clickOn(robot, "Unlink Trees");
		printBeautiState();
		assertStateEquals("Tree.t:26", "birthRate.t:26", "Tree.t:47", "clockRate.c:47", "birthRate.t:47", "Tree.t:59", "clockRate.c:59", "birthRate.t:59");
		assertPriorsEqual("YuleModel.t:26", "YuleBirthRatePrior.t:26", "YuleModel.t:47", "ClockPrior.c:47", "YuleBirthRatePrior.t:47", "YuleModel.t:59", "ClockPrior.c:59", "YuleBirthRatePrior.t:59");
		assertOperatorsEqual("YuleBirthRateScaler.t:26", "YuleModelTreeScaler.t:26", "YuleModelTreeRootScaler.t:26", "YuleModelUniformOperator.t:26", "YuleModelSubtreeSlide.t:26", "YuleModelNarrow.t:26", "YuleModelWide.t:26", "YuleModelWilsonBalding.t:26", "StrictClockRateScaler.c:47", "YuleBirthRateScaler.t:47", "YuleModelTreeScaler.t:47", "YuleModelTreeRootScaler.t:47", "YuleModelUniformOperator.t:47", "YuleModelSubtreeSlide.t:47", "YuleModelNarrow.t:47", "YuleModelWide.t:47", "YuleModelWilsonBalding.t:47", "strictClockUpDownOperator.c:47", "StrictClockRateScaler.c:59", "YuleBirthRateScaler.t:59", "YuleModelTreeScaler.t:59", "YuleModelTreeRootScaler.t:59", "YuleModelUniformOperator.t:59", "YuleModelSubtreeSlide.t:59", "YuleModelNarrow.t:59", "YuleModelWide.t:59", "YuleModelWilsonBalding.t:59", "strictClockUpDownOperator.c:59");
		assertTraceLogEqual("posterior", "likelihood", "prior", "treeLikelihood.26", "TreeHeight.t:26", "YuleModel.t:26", "birthRate.t:26", "treeLikelihood.47", "TreeHeight.t:47", "clockRate.c:47", "YuleModel.t:47", "birthRate.t:47", "treeLikelihood.59", "TreeHeight.t:59", "clockRate.c:59", "YuleModel.t:59", "birthRate.t:59");

		makeSureXMLParses();
	}

	
	@Test
	public void linkTreesAndDeleteTest2a(FxRobot robot) throws Exception {
		warning("Load gopher data 26.nex, 47.nex");
		importAlignment(NEXUS_DIR, new File("26.nex"), new File("47.nex"));

		// JTabbedPaneFixture f = beautiFrame.tabbedPane();
		printBeautiState();

		selectPartitions(robot, 0, 1);

		warning("Link trees");
		selectTab(robot, "Partitions");
		clickOn(robot, "Link Trees");
		printBeautiState();

		warning("Delete second partition");
		selectTab(robot, "Partitions");
		selectPartitions(robot, 1);
		clickOn(robot, "-");
		printBeautiState();
		assertPriorsEqual("YuleModel.t:26", "YuleBirthRatePrior.t:26");
		
		makeSureXMLParses();
	}
	
	@Test
	public void linkTreesAndDeleteTest2b(FxRobot robot) throws Exception {
		warning("Load gopher data 26.nex, 47.nex");
		importAlignment(NEXUS_DIR, new File("26.nex"), new File("47.nex"));

		// JTabbedPaneFixture f = beautiFrame.tabbedPane();
		printBeautiState();

		selectPartitions(robot, 1, 0);

		warning("Link trees");
		selectTab(robot, "Partitions");
		clickOn(robot, "Link Trees");
		printBeautiState();
		assertPriorsEqual("YuleModel.t:26", "YuleBirthRatePrior.t:26", "ClockPrior.c:47");

		warning("Delete first partition");
		selectTab(robot, "Partitions");
		selectPartitions(robot, 0);
		clickOn(robot, "-");
		printBeautiState();
		assertPriorsEqual("YuleModel.t:26", "YuleBirthRatePrior.t:26");
		
		makeSureXMLParses();
	}
	
	@Test
	public void linkTreesAndDeleteTest3(FxRobot robot) throws Exception {
		warning("Load gopher data 26.nex, 47.nex, 59.nex");
		importAlignment(NEXUS_DIR, new File("26.nex"), new File("47.nex"), new File("59.nex"));

		// JTabbedPaneFixture f = beautiFrame.tabbedPane();
		printBeautiState();
		assertStateEquals("Tree.t:26", "birthRate.t:26", "Tree.t:47", "clockRate.c:47", "birthRate.t:47", "Tree.t:59", "clockRate.c:59", "birthRate.t:59");
		assertOperatorsEqual("YuleBirthRateScaler.t:26", "YuleModelTreeScaler.t:26", "YuleModelTreeRootScaler.t:26", "YuleModelUniformOperator.t:26", "YuleModelSubtreeSlide.t:26", "YuleModelNarrow.t:26", "YuleModelWide.t:26", "YuleModelWilsonBalding.t:26", "StrictClockRateScaler.c:47", "YuleBirthRateScaler.t:47", "YuleModelTreeScaler.t:47", "YuleModelTreeRootScaler.t:47", "YuleModelUniformOperator.t:47", "YuleModelSubtreeSlide.t:47", "YuleModelNarrow.t:47", "YuleModelWide.t:47", "YuleModelWilsonBalding.t:47", "strictClockUpDownOperator.c:47", "StrictClockRateScaler.c:59", "YuleBirthRateScaler.t:59", "YuleModelTreeScaler.t:59", "YuleModelTreeRootScaler.t:59", "YuleModelUniformOperator.t:59", "YuleModelSubtreeSlide.t:59", "YuleModelNarrow.t:59", "YuleModelWide.t:59", "YuleModelWilsonBalding.t:59", "strictClockUpDownOperator.c:59");
		assertPriorsEqual("YuleModel.t:26", "YuleBirthRatePrior.t:26", "YuleModel.t:47", "ClockPrior.c:47", "YuleBirthRatePrior.t:47", "YuleModel.t:59", "ClockPrior.c:59", "YuleBirthRatePrior.t:59");
		assertTraceLogEqual("posterior", "likelihood", "prior", "treeLikelihood.26", "TreeHeight.t:26", "YuleModel.t:26", "birthRate.t:26", "treeLikelihood.47", "TreeHeight.t:47", "clockRate.c:47", "YuleModel.t:47", "birthRate.t:47", "treeLikelihood.59", "TreeHeight.t:59", "clockRate.c:59", "YuleModel.t:59", "birthRate.t:59");

		selectPartitions(robot, 2, 1, 0);

		warning("Link trees");
		selectTab(robot, "Partitions");
		clickOn(robot, "Link Trees");
		printBeautiState();
		assertPriorsEqual("YuleModel.t:26", "YuleBirthRatePrior.t:26", "ClockPrior.c:47", "ClockPrior.c:59");
		makeSureXMLParses();

		warning("Delete second partition (47)");
		selectTab(robot, "Partitions");
		selectPartitions(robot, 1);
		clickOn(robot, "-");
		printBeautiState();
		assertPriorsEqual("YuleModel.t:26", "YuleBirthRatePrior.t:26", "ClockPrior.c:59");
		makeSureXMLParses();

		warning("Delete first partition (26)");
		selectTab(robot, "Partitions");
		selectPartitions(robot, 0);
		clickOn(robot, "-");
		printBeautiState();
		assertPriorsEqual("YuleModel.t:26", "YuleBirthRatePrior.t:26");

		makeSureXMLParses();
	}


	@Test
	public void linkTreesAndClocksAndDeleteTest(FxRobot robot) throws Exception {
		warning("Load gopher data 26.nex, 47.nex, 59.nex");
		importAlignment(NEXUS_DIR, new File("26.nex"), new File("47.nex"), new File("59.nex"));

		// JTabbedPaneFixture f = beautiFrame.tabbedPane();
		printBeautiState();
		assertStateEquals("Tree.t:26", "birthRate.t:26", "Tree.t:47", "clockRate.c:47", "birthRate.t:47", "Tree.t:59", "clockRate.c:59", "birthRate.t:59");
		assertOperatorsEqual("YuleBirthRateScaler.t:26", "YuleModelTreeScaler.t:26", "YuleModelTreeRootScaler.t:26", "YuleModelUniformOperator.t:26", "YuleModelSubtreeSlide.t:26", "YuleModelNarrow.t:26", "YuleModelWide.t:26", "YuleModelWilsonBalding.t:26", "StrictClockRateScaler.c:47", "YuleBirthRateScaler.t:47", "YuleModelTreeScaler.t:47", "YuleModelTreeRootScaler.t:47", "YuleModelUniformOperator.t:47", "YuleModelSubtreeSlide.t:47", "YuleModelNarrow.t:47", "YuleModelWide.t:47", "YuleModelWilsonBalding.t:47", "strictClockUpDownOperator.c:47", "StrictClockRateScaler.c:59", "YuleBirthRateScaler.t:59", "YuleModelTreeScaler.t:59", "YuleModelTreeRootScaler.t:59", "YuleModelUniformOperator.t:59", "YuleModelSubtreeSlide.t:59", "YuleModelNarrow.t:59", "YuleModelWide.t:59", "YuleModelWilsonBalding.t:59", "strictClockUpDownOperator.c:59");
		assertPriorsEqual("YuleModel.t:26", "YuleBirthRatePrior.t:26", "YuleModel.t:47", "ClockPrior.c:47", "YuleBirthRatePrior.t:47", "YuleModel.t:59", "ClockPrior.c:59", "YuleBirthRatePrior.t:59");
		assertTraceLogEqual("posterior", "likelihood", "prior", "treeLikelihood.26", "TreeHeight.t:26", "YuleModel.t:26", "birthRate.t:26", "treeLikelihood.47", "TreeHeight.t:47", "clockRate.c:47", "YuleModel.t:47", "birthRate.t:47", "treeLikelihood.59", "TreeHeight.t:59", "clockRate.c:59", "YuleModel.t:59", "birthRate.t:59");

		selectPartitions(robot, 0, 1, 2);

		warning("Link trees");
		selectTab(robot, "Partitions");
		clickOn(robot, "Link Trees");
		printBeautiState();

		warning("Link clocks");
		selectPartitions(robot, 0, 1, 2);
		selectTab(robot, "Partitions");
		clickOn(robot, "Link Clock Models");
		printBeautiState();

		warning("Delete second partition");
		selectTab(robot, "Partitions");
		selectPartitions(robot, 1);
		clickOn(robot, "-");
		printBeautiState();

		warning("Delete first partition");
		selectTab(robot, "Partitions");
		selectPartitions(robot, 0, 1);
		clickOn(robot, "Link Clock Models");
		selectPartitions(robot, 0);
		clickOn(robot, "-");
		printBeautiState();
		assertPriorsEqual("YuleModel.t:26", "YuleBirthRatePrior.t:26");

		makeSureXMLParses();
	}

	@Test
	public void linkSiteModelsAndDeleteTest(FxRobot robot) throws Exception {
		warning("Load gopher data 26.nex, 47.nex, 59.nex");
		importAlignment(NEXUS_DIR, new File("26.nex"), new File("47.nex"), new File("59.nex"));

		// JTabbedPaneFixture f = beautiFrame.tabbedPane();
		printBeautiState();
		assertStateEquals("Tree.t:26", "birthRate.t:26", "Tree.t:47", "clockRate.c:47", "birthRate.t:47", "Tree.t:59", "clockRate.c:59", "birthRate.t:59");
		assertOperatorsEqual("YuleBirthRateScaler.t:26", "YuleModelTreeScaler.t:26", "YuleModelTreeRootScaler.t:26", "YuleModelUniformOperator.t:26", "YuleModelSubtreeSlide.t:26", "YuleModelNarrow.t:26", "YuleModelWide.t:26", "YuleModelWilsonBalding.t:26", "StrictClockRateScaler.c:47", "YuleBirthRateScaler.t:47", "YuleModelTreeScaler.t:47", "YuleModelTreeRootScaler.t:47", "YuleModelUniformOperator.t:47", "YuleModelSubtreeSlide.t:47", "YuleModelNarrow.t:47", "YuleModelWide.t:47", "YuleModelWilsonBalding.t:47", "strictClockUpDownOperator.c:47", "StrictClockRateScaler.c:59", "YuleBirthRateScaler.t:59", "YuleModelTreeScaler.t:59", "YuleModelTreeRootScaler.t:59", "YuleModelUniformOperator.t:59", "YuleModelSubtreeSlide.t:59", "YuleModelNarrow.t:59", "YuleModelWide.t:59", "YuleModelWilsonBalding.t:59", "strictClockUpDownOperator.c:59");
		assertPriorsEqual("YuleModel.t:26", "YuleBirthRatePrior.t:26", "YuleModel.t:47", "ClockPrior.c:47", "YuleBirthRatePrior.t:47", "YuleModel.t:59", "ClockPrior.c:59", "YuleBirthRatePrior.t:59");
		assertTraceLogEqual("posterior", "likelihood", "prior", "treeLikelihood.26", "TreeHeight.t:26", "YuleModel.t:26", "birthRate.t:26", "treeLikelihood.47", "TreeHeight.t:47", "clockRate.c:47", "YuleModel.t:47", "birthRate.t:47", "treeLikelihood.59", "TreeHeight.t:59", "clockRate.c:59", "YuleModel.t:59", "birthRate.t:59");

		assertParameterCountInPriorIs(5);		

		selectPartitions(robot, 0, 1, 2);

		warning("Link trees");
		selectTab(robot, "Partitions");
		clickOn(robot, "Link Trees");
		printBeautiState();

		assertParameterCountInPriorIs(3);
		
		selectTab(robot, "Site Model");
		robot.clickOn("#substModel").clickOn("HKY");
        //JComboBoxFixture substModel = beautiFrame.comboBox("substModel");
        //substModel.selectItem("HKY");
		assertParameterCountInPriorIs(7);		
		
		selectTab(robot, "Partitions");
		warning("Link site models");
		selectPartitions(robot, 0, 1, 2);
		clickOn(robot, "Link Site Models");
		printBeautiState();

		assertParameterCountInPriorIs(7);		
		clickOn(robot, "Unlink Site Models");

		printBeautiState();
		assertParameterCountInPriorIs(15);		

		warning("Delete second partition");
		selectTab(robot, "Partitions");
		selectPartitions(robot, 1);
		clickOn(robot, "-");
		printBeautiState();

		assertParameterCountInPriorIs(10);		

		warning("Delete first partition");
		selectTab(robot, "Partitions");
		selectPartitions(robot, 0, 1);
		clickOn(robot, "Link Clock Models");
		selectPartitions(robot, 0);
		clickOn(robot, "-");
		printBeautiState();
		assertPriorsEqual("YuleModel.t:26", "YuleBirthRatePrior.t:26", "KappaPrior.s:59", "FrequenciesPrior.s:59");		
		assertParameterCountInPriorIs(5);
		
		makeSureXMLParses();
	}
	



	@Test
	public void linkUnlinkTreesAndSetTreePriorTest1(FxRobot robot) throws Exception {
		warning("Load gopher data 26.nex, 47.nex");
		importAlignment(NEXUS_DIR, new File("26.nex"));

		// JTabbedPaneFixture f = beautiFrame.tabbedPane();
		printBeautiState();

		selectTab(robot, "Priors");
		
		warning("Change to Coalescent - constant population");
		//beautiFrame.comboBox("TreeDistribution").selectItem("Coalescent Constant Population");
		robot.clickOn("#TreeDistribution").clickOn("Coalescent Constant Population");

		printBeautiState();
		assertPriorsEqual("CoalescentConstant.t:26", "PopSizePrior.t:26");
		importAlignment(NEXUS_DIR, new File("47.nex"));

		warning("Link trees");
		selectTab(robot, "Partitions");
		selectPartitions(robot, 1, 0);
		clickOn(robot, "Link Trees");
		printBeautiState();
		assertPriorsEqual("CoalescentConstant.t:26", "ClockPrior.c:47", "PopSizePrior.t:26");

		warning("Unlink trees");
		clickOn(robot, "Unlink Trees");
		// should have PopSizePrior.t:47 as well?
		assertPriorsEqual("CoalescentConstant.t:26", "CoalescentConstant.t:47", "ClockPrior.c:47", "PopSizePrior.t:26", "PopSizePrior.t:47");
		
//		warning("Delete partition");
//		selectTab(robot, "Partitions");
//		selectPartitions(robot, 1);
//		clickOn(robot, "-");
//		printBeautiState();
//		assertPriorsEqual("CoalescentConstant.t:26", "PopSizePrior.t:26");

		warning("Delete partition");
		selectTab(robot, "Partitions");
		selectPartitions(robot, 0);
		clickOn(robot, "-");
		printBeautiState();
		assertPriorsEqual("CoalescentConstant.t:47", "PopSizePrior.t:47");

		makeSureXMLParses();
	}

	
	@Test
	public void linkClocksAndDeleteTest(FxRobot robot) throws Exception {
		warning("Load gopher data 26.nex, 47.nex, 59.nex");
		importAlignment(NEXUS_DIR, new File("26.nex"), new File("47.nex"), new File("59.nex"));

		// JTabbedPaneFixture f = beautiFrame.tabbedPane();
		printBeautiState();

		selectPartitions(robot, 0, 1, 2);

		warning("Link clocks");
		selectTab(robot, "Partitions");
		clickOn(robot, "Link Clock Models");
		printBeautiState();

		warning("Delete second partition");
		selectTab(robot, "Partitions");
		selectPartitions(robot, 1);
		clickOn(robot, "-");
		printBeautiState();
		assertPriorsEqual("YuleModel.t:26", "YuleBirthRatePrior.t:26", "YuleModel.t:59", "YuleBirthRatePrior.t:59");

		// JTableFixture t = beautiFrame.table();
		Assertions.assertThat(getPartitionCount(robot)).isEqualTo(2);
		
		makeSureXMLParses();
	}

	@Test
	public void linkSiteModelssAndDeleteTest(FxRobot robot) throws Exception {
		warning("Load gopher data 26.nex, 47.nex, 59.nex");
		importAlignment(NEXUS_DIR, new File("26.nex"), new File("47.nex"), new File("59.nex"));

		// JTabbedPaneFixture f = beautiFrame.tabbedPane();
		printBeautiState();

		selectPartitions(robot, 0, 1, 2);

		warning("Link clocks");
		selectTab(robot, "Partitions");
		clickOn(robot, "Link Site Models");
		printBeautiState();

		warning("Delete second partition");
		selectTab(robot, "Partitions");
		selectPartitions(robot, 1);
		clickOn(robot, "-");
		printBeautiState();
		assertPriorsEqual("YuleModel.t:26", "YuleBirthRatePrior.t:26", "YuleModel.t:59", "ClockPrior.c:59", "YuleBirthRatePrior.t:59");

		// JTableFixture t = beautiFrame.table();
		Assertions.assertThat(getPartitionCount(robot)).isEqualTo(2);
		
		makeSureXMLParses();
	}

	@Test
	public void linkClocksSitesAndDeleteTest(FxRobot robot) throws Exception {
		warning("Load gopher data 26.nex, 47.nex, 59.nex");
		importAlignment(NEXUS_DIR, new File("26.nex"), new File("47.nex"), new File("59.nex"));

		// JTabbedPaneFixture f = beautiFrame.tabbedPane();
		printBeautiState();

		selectPartitions(robot, 0, 1, 2);

		warning("Link clocks");
		selectTab(robot, "Partitions");
		clickOn(robot, "Link Clock Models");

		warning("Link site models");
		selectTab(robot, "Partitions");
		clickOn(robot, "Link Site Models");
		printBeautiState();

		warning("Delete second partition");
		selectTab(robot, "Partitions");
		selectPartitions(robot, 1);
		clickOn(robot, "-");
		printBeautiState();
		assertPriorsEqual("YuleModel.t:26", "YuleBirthRatePrior.t:26", "YuleModel.t:59", "YuleBirthRatePrior.t:59");

		// JTableFixture t = beautiFrame.table();
		Assertions.assertThat(getPartitionCount(robot)).isEqualTo(2);
		
		makeSureXMLParses();
	}

	@Test
	public void linkClocksSitesTreesAndDeleteTest(FxRobot robot) throws Exception {
		warning("Load gopher data 26.nex, 47.nex, 59.nex");
		importAlignment(NEXUS_DIR, new File("26.nex"), new File("47.nex"), new File("59.nex"));

		// JTabbedPaneFixture f = beautiFrame.tabbedPane();
		printBeautiState();

		selectPartitions(robot, 0, 1, 2);

		warning("Link clocks");
		selectTab(robot, "Partitions");
		clickOn(robot, "Link Clock Models");

		warning("Link site models");
		selectTab(robot, "Partitions");
		clickOn(robot, "Link Site Models");
		printBeautiState();

		warning("Link trees");
		selectTab(robot, "Partitions");
		clickOn(robot, "Link Trees");
		printBeautiState();

		warning("Delete second partition");
		selectTab(robot, "Partitions");
		selectPartitions(robot, 1);
		clickOn(robot, "-");
		printBeautiState();
		assertPriorsEqual("YuleModel.t:26", "YuleBirthRatePrior.t:26");

		// JTableFixture t = beautiFrame.table();
		Assertions.assertThat(getPartitionCount(robot)).isEqualTo(2);
		
		makeSureXMLParses();
	}
	
	
	@Test // issue #413
	public void starBeastLinkTreesAndDeleteTest(FxRobot robot) throws Exception {
		warning("Select StarBeast template");
		if (!Utils.isMac()) {
			//robot.clickOn(".menu[text=\"File\"]").clickOn(".menu[text=\"Template\"]").clickOn(".menu[text=\"StarBeast\"]");
			robot.clickOn("#File").clickOn("#Template").clickOn("#StarBeast");
			// robot.menuItemWithPath("File", "Template", "StarBeast").click();
		} else {
//			execute(new GuiTask() {
//		        @Override
//				protected void executeInEDT() {
		        	try {
		    			doc.loadNewTemplate(TEMPLATE_DIR +"/StarBeast.xml");
		    			doc.beauti.refreshPanel();
		        	} catch (Exception e) {
						e.printStackTrace();
					}
//		        }
//		    });
		}

		warning("Load gopher data 26.nex, 47.nex");
		importAlignment(NEXUS_DIR, new File("26.nex"), new File("47.nex"));

		// JTabbedPaneFixture f = beautiFrame.tabbedPane();
		printBeautiState();

		Thread.sleep(500);
		robot.clickOn(robot.lookup(".table-view").queryAs(TableView.class));
		selectPartitions(robot, 0, 1);

		warning("Link trees");
		selectTab(robot, "Partitions");
		clickOn(robot, "Link Trees");
		printBeautiState();

		warning("Delete second partition");
		selectTab(robot, "Partitions");
		selectPartitions(robot, 1);
		clickOn(robot, "-");
		printBeautiState();
//		assertPriorsEqual("YuleModel.t:26", "YuleBirthRatePrior.t:26");
		
		// JTableFixture t = beautiFrame.table();
		Assertions.assertThat(getPartitionCount(robot)).isEqualTo(1);

		// does not parse unless taxon set is specified
		//makeSureXMLParses();
	}	
	
	@Test // issue #414
	public void linkClocksDeleteAllTest(FxRobot robot) throws Exception {
		warning("Load gopher data 26.nex, 47.nex, 59.nex");
		importAlignment(NEXUS_DIR, new File("26.nex"), new File("47.nex"), new File("59.nex"));

		// JTabbedPaneFixture f = beautiFrame.tabbedPane();
		printBeautiState();

		selectPartitions(robot, 0, 1, 2);

		warning("Link clocks");
		selectTab(robot, "Partitions");
		clickOn(robot, "Link Clock Models");
		
		clickOn(robot, "-");

		// JTableFixture t = beautiFrame.table();
		Assertions.assertThat(getPartitionCount(robot)).isEqualTo(0);
	}
}
