package beastfx.app.beauti;



import java.util.List;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;

import javax.swing.SwingUtilities;

import beastfx.app.inputeditor.BeautiDoc;
import beastfx.app.inputeditor.BeautiPanel;
import beastfx.app.inputeditor.BeautiPanelConfig;
import beastfx.app.inputeditor.BeautiSubTemplate;
import beastfx.app.inputeditor.BeautiTabPane;
import beastfx.app.inputeditor.InputEditor;
import beastfx.app.inputeditor.SmallLabel;
import beastfx.app.util.FXUtils;
import beast.base.core.BEASTInterface;
import beast.base.core.Input;
import beast.base.evolution.tree.Tree;
import beast.base.evolution.tree.TreeDistribution;
import beast.base.parser.PartitionContext;

public class TreeDistributionInputEditor extends InputEditor.Base {

    public TreeDistributionInputEditor(BeautiDoc doc) {
        super(doc);
    }

    public TreeDistributionInputEditor() {
		super();
	}

	@Override
    public Class<?> type() {
        return TreeDistribution.class;
    }
//	@Override
//	public Class<?>[] types() {
//		ArrayList<Class> types = new ArrayList<>();
//		types.add(TreeDistribution.class);
//		types.add(BirthDeathGernhard08Model.class);
//		types.add(YuleModel.class);
//		types.add(Coalescent.class);
//		types.add(BayesianSkyline.class);
//		return types.toArray(new Class[0]);
//	}
    ActionEvent m_e;

    @Override
    public void init(Input<?> input, BEASTInterface beastObject, int listItemNr, ExpandOption isExpandOption, boolean addButtons) {
        m_bAddButtons = addButtons;
        m_input = input;
        m_beastObject = beastObject;
        this.itemNr = listItemNr;

        pane = FXUtils.newVBox();
        // pane = new VBox();
        HBox itemBox = FXUtils.newHBox();

        TreeDistribution distr = (TreeDistribution) beastObject;
        String text = ""/* beastObject.getID() + ": " */;
        if (distr.treeInput.get() != null) {
            text += distr.treeInput.get().getID();
        } else {
            text += distr.treeIntervalsInput.get().treeInput.get().getID();
        }
        Label label = new Label(text);
        label.setMinSize(LABEL_SIZE.getWidth(), LABEL_SIZE.getHeight());
        label.setPrefSize(LABEL_SIZE.getWidth(), LABEL_SIZE.getHeight());
        itemBox.getChildren().add(label);
        // List<String> availableBEASTObjects =
        // PluginPanel.getAvailablePlugins(m_input, m_beastObject, null);

        List<BeautiSubTemplate> availableBEASTObjects = doc.getInputEditorFactory().getAvailableTemplates(m_input, m_beastObject,
                null, doc); 
        // make sure we are dealing with a TreeDistribution
        for (int i = availableBEASTObjects.size() - 1; i >= 0; i--) {
        	BeautiSubTemplate t = availableBEASTObjects.get(i);
        	Class<?> c = t._class;
        	if (!(TreeDistribution.class.isAssignableFrom(c))) {
        		availableBEASTObjects.remove(i);
        	}
        }
        
        ComboBox<BeautiSubTemplate> comboBox = new ComboBox<>();
        comboBox.getItems().addAll(availableBEASTObjects.toArray(new BeautiSubTemplate[]{}));
        comboBox.setId("TreeDistribution");

        for (int i = availableBEASTObjects.size() - 1; i >= 0; i--) {
            if (!TreeDistribution.class.isAssignableFrom(availableBEASTObjects.get(i)._class)) {
                availableBEASTObjects.remove(i);
            }
        }

        String id = distr.getID();
        try {
            // id = BeautiDoc.parsePartition(id);
            id = id.substring(0, id.indexOf('.'));
        } catch (Exception e) {
            throw new RuntimeException("Improperly formatted ID: " + distr.getID());
        }
        for (BeautiSubTemplate template : availableBEASTObjects) {
            if (template.matchesName(id)) { // getMainID().replaceAll(".\\$\\(n\\)",
                // "").equals(id)) {
                comboBox.setValue(template);
                comboBox.getSelectionModel().select(template);
            }
        }

        comboBox.setOnAction(e -> {
                m_e = e;
                SwingUtilities.invokeLater(new Runnable() {
					@Override
                    public void run() {
						@SuppressWarnings("unchecked")
						ComboBox<BeautiSubTemplate> currentComboBox = (ComboBox<BeautiSubTemplate>) m_e.getSource();
                        @SuppressWarnings("unchecked")
						List<BEASTInterface> list = (List<BEASTInterface>) m_input.get();
                        BeautiSubTemplate template = (BeautiSubTemplate) currentComboBox.getValue();
                        PartitionContext partitionContext = doc.getContextFor(list.get(itemNr));
                        try {
                            template.createSubNet(partitionContext, list, itemNr, true);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        hardSync();
                        refreshPanel();
                    }
                });
            });
        itemBox.getChildren().add(comboBox);
        FXUtils.createHMCButton(itemBox, m_beastObject, m_input);
        // itemBox.getChildren().add(new Separator());

        m_validateLabel = new SmallLabel("x", "red");
        m_validateLabel.setVisible(false);
        validateInput();
        itemBox.getChildren().add(m_validateLabel);
        pane.getChildren().add(itemBox);
        getChildren().add(pane);
    }
    


    @Override
    public void validateInput() {
        TreeDistribution distr = (TreeDistribution) m_beastObject;
        // TODO: robustify for the case the tree is not a simple binary tree
        Tree tree = (Tree) distr.treeInput.get();
        if (tree == null) {
            tree = distr.treeIntervalsInput.get().treeInput.get();
        }
        if (tree.hasDateTrait()) {
            if (!distr.canHandleTipDates()) {
                m_validateLabel.setTooltip(new Tooltip("This tree prior cannot handle dated tips. Choose another tree prior."));
                m_validateLabel.setColor("red");
                m_validateLabel.setVisible(true);
                return;
            }
        }

        super.validateInput();
    }
}
