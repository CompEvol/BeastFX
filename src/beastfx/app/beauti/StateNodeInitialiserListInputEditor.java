package beastfx.app.beauti;


import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.Pane;

import javax.swing.SwingUtilities;

import beastfx.app.inputeditor.BeautiDoc;
import beastfx.app.inputeditor.BeautiSubTemplate;
import beastfx.app.inputeditor.InputEditor;
import beastfx.app.inputeditor.ListInputEditor;
import beast.base.core.BEASTInterface;
import beast.base.core.Input;
import beast.base.evolution.tree.Tree;
import beast.base.inference.MCMC;
import beast.base.inference.State;
import beast.base.inference.StateNode;
import beast.base.inference.StateNodeInitialiser;
import beast.base.parser.PartitionContext;

public class StateNodeInitialiserListInputEditor extends ListInputEditor {
	private static final long serialVersionUID = 1L;

	public StateNodeInitialiserListInputEditor(BeautiDoc doc) {
		super(doc);
	}

	public StateNodeInitialiserListInputEditor() {
		super();
	}

	@Override
	public Class<?> type() {
		return List.class;
	}

	@Override
	public Class<?> baseType() {
		return StateNodeInitialiser.class;
	}

	@Override
	public void init(Input<?> input, BEASTInterface beastObject, int itemNr, ExpandOption isExpandOption,
			boolean addButtons) {
		super.init(input, beastObject, itemNr, isExpandOption, addButtons);
	}

	@Override
	protected InputEditor addPluginItem(Pane itemBox, BEASTInterface beastObject) {
		final StateNodeInitialiser currentInitialiser = (StateNodeInitialiser) beastObject;
		Input initialInput = beastObject.getInput("initial");
		List<BeautiSubTemplate> sAvailablePlugins = doc.getInputEditorFactory().getAvailableTemplates(initialInput,
				(BEASTInterface) beastObject, null, doc);

		ComboBox<BeautiSubTemplate> comboBox = null;

		if (sAvailablePlugins.size() > 0) {
			sAvailablePlugins.remove(sAvailablePlugins.size() - 1);

			comboBox = new ComboBox<>();
			comboBox.getItems().addAll(sAvailablePlugins);
			String sID = beastObject.getID();
			try {
				sID = sID.substring(0, sID.indexOf('.'));
			} catch (Exception e) {
				throw new RuntimeException("Improperly formatted ID: " + sID);
			}
			for (BeautiSubTemplate template : sAvailablePlugins) {
				if (template.matchesName(sID)) {
					comboBox.setValue(template);
				}
			}
			comboBox.setId("Initialiser");

			comboBox.setOnAction(e->{
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							ComboBox<?> currentComboBox = (ComboBox<?>) e.getSource();
							BeautiSubTemplate template = (BeautiSubTemplate) currentComboBox.getValue();
							PartitionContext partitionContext;
							partitionContext = doc.getContextFor(beastObject);

							try {
								Object o = template.createSubNet(partitionContext, true);
								StateNodeInitialiser newInitialiser = (StateNodeInitialiser) o;
								List<StateNodeInitialiser> inits = (List<StateNodeInitialiser>) m_input.get();
								int i = inits.indexOf(currentInitialiser);
								inits.set(i, newInitialiser);
								System.out.println(inits.size());
							} catch (Exception ex) {
								ex.printStackTrace();
							}
							sync();
							refreshPanel();
						}
					});
			});

		}

		String name = beastObject.getID();
		Object o = beastObject.getInput("initial").get();
		if (o instanceof BEASTInterface) {
			name = ((BEASTInterface) o).getID();
		}

		if (name == null || name.length() == 0) {
			name = beastObject.getClass().getName();
			name = name.substring(name.lastIndexOf('.') + 1);
		}
		Label label = new Label("Initial " + name + ":");

		itemBox.getChildren().add(new Separator());// Box.createRigidArea(new Dimension(5, 1)));
		itemBox.getChildren().add(label);
		if (comboBox != null) {
			itemBox.getChildren().add(comboBox);
		}
		itemBox.getChildren().add(new Separator());
		return this;
	}

    public static boolean customConnector(BeautiDoc doc) {
        // scrub Tree initialisers
    	
        // 0. collect state node info
        List<StateNodeInitialiser> inits = ((MCMC)doc.mcmc.get()).initialisersInput.get();
        State state = ((MCMC)doc.mcmc.get()).startStateInput.get();
        List<StateNode> stateNodes = state.stateNodeInput.get();
        List<Tree> trees = new ArrayList<>();
        for (StateNode s: stateNodes) {
        	if (s instanceof Tree) {
        		trees.add((Tree) s);
        	}
        }        
        List<List<StateNode>> initStateNodes = new ArrayList<>();
        for (StateNodeInitialiser init : inits) {
        	List<StateNode> initStateNodes0 = new ArrayList<>();
        	init.getInitialisedStateNodes(initStateNodes0);
        	for (int i = initStateNodes0.size() - 1; i >= 0; i--) {
        		if (!(initStateNodes0.get(i) instanceof Tree)) {
        			initStateNodes0.remove(i);
        		}
        	}
        	initStateNodes.add(initStateNodes0);
        }
        // 1. remove initialisers that have no stateNode in state
        for (int i = inits.size() - 1; i >= 0; i--) {
        	boolean found = false;
        	for (StateNode stateNode : initStateNodes.get(i)) {
        		if (trees.contains(stateNode)) {
        			found = true;
        			break;
        		}
        	}
        	if (!found) {
        		inits.remove(i);
        		initStateNodes.remove(i);
        	}
        }
        // 2. remove initialisers that share stateNodes
        for (int i = inits.size() - 1; i >= 0; i--) {
            for (int j = i - 1; j >= 0; j--) {
            	boolean found = false;
            	for (StateNode stateNode : initStateNodes.get(i)) {
            		if (initStateNodes.get(j).contains(stateNode)) {
            			found = true;
            			break;
            		}
            	}
            	if (found) {
            		inits.remove(i);
            		initStateNodes.remove(i);
            	}
            }
        }

        // 3. add RandomTree for those trees not having a stateNodeInitialiser
        boolean [] hasInitialiser = new boolean[trees.size()];
        for (int i = inits.size() - 1; i >= 0; i--) {
        	for (StateNode stateNode : initStateNodes.get(i)) {
        		int k = trees.indexOf(stateNode);
        		if (k >= 0) {
        			hasInitialiser[k] = true;
        			break;
        		}
        	}
        }
        for (int i = 0; i < hasInitialiser.length; i++) {
        	if (!hasInitialiser[i]) {
        		for (BeautiSubTemplate tmp : doc.beautiConfig.subTemplates) {
        			if (tmp.getID().equals("RandomTree")) {
        				PartitionContext partition = doc.getContextFor(trees.get(i));
        				Object o = tmp.createSubNet(partition, false);
        				inits.add((StateNodeInitialiser) o);
        			}
        		}
        	}
        }
    	return true;
    }
}
