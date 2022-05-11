package beastfx.app.inputeditor;


import java.io.File;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.params.shadow.com.univocity.parsers.common.processor.ConcurrentRowProcessor;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import beastfx.app.inputeditor.AlignmentListInputEditor.Partition0;
import beastfx.app.util.Alert;
import beastfx.app.util.FXUtils;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.Clipboard;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.util.Callback;
import beast.app.util.FileDrop;
import beast.app.util.PartitionContextUtil;
import beast.base.core.BEASTInterface;
import beast.base.core.Input;
import beast.base.core.Log;
import beast.base.evolution.alignment.Alignment;
import beast.base.evolution.alignment.FilteredAlignment;
import beast.base.evolution.alignment.Taxon;
import beast.base.evolution.branchratemodel.BranchRateModel;
import beast.base.evolution.likelihood.GenericTreeLikelihood;
import beast.base.evolution.likelihood.TreeLikelihood;
import beast.base.evolution.sitemodel.SiteModel;
import beast.base.evolution.sitemodel.SiteModelInterface;
import beast.base.evolution.tree.Node;
import beast.base.evolution.tree.TreeInterface;
import beast.base.inference.CompoundDistribution;
import beast.base.inference.MCMC;
import beast.base.inference.State;
import beast.base.inference.StateNode;
import beast.base.parser.PartitionContext;
import beast.pkgmgmt.Package;

// TODO: add useAmbiguities flag 
// TODO: add warning if useAmbiguities=false and nr of patterns=1 (happens when all data is ambiguous)

public class AlignmentListInputEditor extends ListInputEditor {

	final static int NAME_COLUMN = 0;
	final static int FILE_COLUMN = 1;
	final static int TAXA_COLUMN = 2;
	final static int SITES_COLUMN = 3;
	final static int TYPE_COLUMN = 4;
	final static int SITEMODEL_COLUMN = 5;
	final static int CLOCKMODEL_COLUMN = 6;
	final static int TREE_COLUMN = 7;
	final static int USE_AMBIGUITIES_COLUMN = 8;
	
	final static int NR_OF_COLUMNS = 9;

    final static int STRUT_SIZE = 5;

	/**
	 * alignments that form a partition. These can be FilteredAlignments *
	 */
	List<Alignment> alignments;
	int partitionCount;
	GenericTreeLikelihood[] likelihoods;
	Object[][] tableData;
	ObservableList<Partition0> tableEntries;
	TableView<Partition0> table;
	TextField nameEditor;
	List<Button> linkButtons;
	List<Button> unlinkButtons;
	Button splitButton;

    /**
     * The button for deleting an alignment in the alignment list.
     */
    Button delButton;
    protected SmallButton replaceButton;

	private ScrollPane scrollPane;

	public AlignmentListInputEditor(BeautiDoc doc) {
		super(doc);
	}

	public AlignmentListInputEditor() {
		super();
	}

	@Override
	public Class<?> type() {
		return List.class;
	}

	@Override
	public Class<?> baseType() {
		return Alignment.class;
	}

	@Override
	public Class<?>[] types() {
		Class<?>[] types = new Class[2];
		types[0] = List.class;
		types[1] = Alignment.class;
		return types;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void init(Input<?> input, BEASTInterface beastObject, int itemNr, ExpandOption isExpandOption, boolean addButtons) {
		this.itemNr = itemNr;
		if (input.get() instanceof List) {
			alignments = (List<Alignment>) input.get();
		} else {
			// we just have a single Alignment
			alignments = new ArrayList<>();
			alignments.add((Alignment) input.get());
		}
		linkButtons = new ArrayList<>();
		unlinkButtons = new ArrayList<>();
		partitionCount = alignments.size();

        // override BoxLayout in superclass
        // setLayout(new BorderLayout());
		BorderPane bpane = new BorderPane();
		pane = bpane;

        bpane.setTop(createLinkButtons());
        bpane.setCenter(createListBox());

        //VBox box = FXUtils.newVBox();
		//box.add(Box.createVerticalStrut(STRUT_SIZE));
		//box.add(createLinkButtons());
		//box.add(Box.createVerticalStrut(STRUT_SIZE));
		//box.add(createListBox());
        //box.add(Box.createVerticalStrut(STRUT_SIZE));
        //box.add(Box.createVerticalGlue());
		//add(box, BorderLayout.CENTER);

//        Color focusColor = UIManager.getColor("Focus.color");
//        Border focusBorder = BorderFactory.createMatteBorder(2, 2, 2, 2, focusColor);
//        new FileDrop(null, scrollPane, focusBorder, new FileDrop.Listener() {
//            @Override
//			public void filesDropped(java.io.File[] files) {
//            	SwingUtilities.invokeLater(new Runnable() {
//					@Override
//					public void run() {
//						addItem(files);
//					}
//				});
//            }   // end filesDropped
//        }); // end FileDrop.Listener

        bpane.setOnDragDropped(e -> {
        	List<File> files = Clipboard.getSystemClipboard().getFiles();
        	addItem(files.toArray(new File[]{}));
        });
        
        
        // this should place the add/remove/split buttons at the bottom of the window.
        bpane.setBottom(createAddRemoveSplitButtons());
        getChildren().add(pane);
        updateStatus();
	}

    /**
     * Creates the link/unlink button component
     * @return a box containing three link/unlink button pairs.
     */
	private Pane createLinkButtons() {
        HBox box = FXUtils.newHBox();
		addLinkUnlinkPair(box, "Site Models");
		//Separator separator = new Separator();
		//separator.setStyle("-fx-width:5;");
        //box.getChildren().add(separator);
        addLinkUnlinkPair(box, "Clock Models");
        //box.getChildren().add(separator);
        addLinkUnlinkPair(box, "Trees");
        // box.getChildren().add(new Separator());
		return box;
	}

    private Pane createAddRemoveSplitButtons() {
        HBox buttonBox = FXUtils.newHBox();

        addButton = new SmallButton("+", true, SmallButton.ButtonType.square);
        addButton.setId("+");
        addButton.setTooltip(new Tooltip("Add item to the list"));
        addButton.setOnAction(e -> addItem());
        //Separator separator = new Separator();
        //separator.setPrefWidth(STRUT_SIZE);
        
        // buttonBox.getChildren().add(separator);
        buttonBox.getChildren().add(addButton);
        // buttonBox.getChildren().add(separator);

        delButton = new SmallButton("-", true, SmallButton.ButtonType.square);
        delButton.setId("-");
        delButton.setTooltip(new Tooltip("Delete selected items from the list"));
        delButton.setOnAction(e -> {
            if (doc.hasLinkedAtLeastOnce) {
                Alert.showMessageDialog(null, "Cannot delete partition while parameters are linked");
                return;
            }
            delItem();
        });
        buttonBox.getChildren().add(delButton);
        //buttonBox.getChildren().add(separator);

        replaceButton = new SmallButton("r", true, SmallButton.ButtonType.square);
        replaceButton.setId("r");
        replaceButton.setTooltip(new Tooltip("Replace alignment by one loaded from file"));
        replaceButton.setOnAction(e -> replaceItem());
        //buttonBox.getChildren().add(separator);
        buttonBox.getChildren().add(replaceButton);
        //buttonBox.getChildren().add(separator);

        
        splitButton = new Button("Split");
        splitButton.setId("Split");
        splitButton.setTooltip(new Tooltip("Split alignment into partitions, for example, codon positions"));
        splitButton.setOnAction(e -> splitItem());
        buttonBox.getChildren().add(splitButton);

        // buttonBox.getChildren().add(new Separator());

        return buttonBox;
    }

	/**
     * This method just adds the two buttons (with add()) and does not add any glue or struts before or after.
     * @param box
     * @param label
     */
	private void addLinkUnlinkPair(HBox box, String label) {

        //Label label = new Label(label+":");
        //box.add(label);
        Button linkSModelButton = new Button("Link " + label);
		linkSModelButton.setId("Link " + label);
		linkSModelButton.setOnAction(e -> {
            Button button = (Button) e.getSource();
            link(columnLabelToNr(button.getText()));
//            table.repaint();
    		initTableData();
        });
		box.getChildren().add(linkSModelButton);
		linkSModelButton.setDisable(getDoc().hasLinkedAtLeastOnce);
		Button unlinkSModelButton = new Button("Unlink " + label);
		unlinkSModelButton.setId("Unlink " + label);
		unlinkSModelButton.setOnAction(e -> {
            Button button = (Button) e.getSource();
            unlink(columnLabelToNr(button.getText()));
//            table.repaint();
    		initTableData();
        });
		box.getChildren().add(unlinkSModelButton);
		unlinkSModelButton.setDisable(getDoc().hasLinkedAtLeastOnce);

		linkButtons.add(linkSModelButton);
		unlinkButtons.add(unlinkSModelButton);
	}

	private int columnLabelToNr(String column) {
		int columnNr;
		if (column.contains("Tree")) {
			columnNr = TREE_COLUMN;
		} else if (column.contains("Clock")) {
			columnNr = CLOCKMODEL_COLUMN;
		} else {
			columnNr = SITEMODEL_COLUMN;
		}
		return columnNr;
	}

	private void link(int columnNr) {
		List<Integer> selected = getTableRowSelection();
		// do the actual linking
		for (int i = 1; i < selected.size(); i++) {
			int rowNr = selected.get(i);
			link(columnNr, rowNr, selected.get(0));
		}
	}
	
	/** links partition in row "rowToLink" with partition in "rowToLinkWith" so that
	 * after linking there is only one partition for context "columnNr", namely that
	 * of "rowToLinkWith"
	 */
	private void link(int columnNr, int rowToLink, int rowToLinkWith) {
		Object old = tableData[rowToLink][columnNr];
		tableData[rowToLink][columnNr] = tableData[rowToLinkWith][columnNr];
		try {
			updateModel(columnNr, rowToLink);
		} catch (Exception ex) {
			Log.warning.println(ex.getMessage());
			// unlink if we could not link
			tableData[rowToLink][columnNr] = old;
			try {
				updateModel(columnNr, rowToLink);
			} catch (Exception ex2) {
				// ignore
			}
		}
		MRCAPriorInputEditor.customConnector(doc);
	}

	
	private void unlink(int columnNr) {
		List<Integer> selected = getTableRowSelection();
		for (int i = 1; i < selected.size(); i++) {
			int rowNr = selected.get(i);
			tableData[rowNr][columnNr] = getDoc().partitionNames.get(rowNr).partition;
			try {
				updateModel(columnNr, rowNr);
			} catch (Exception ex) {
				Log.err.println(ex.getMessage());
				ex.printStackTrace();
			}
		}
	}


    List<Integer> getTableRowSelection() {
        return table.getSelectionModel().getSelectedIndices();
	}

	/** set partition of type columnNr to partition model nr rowNr **/
	void updateModel(int columnNr, int rowNr) {
		Log.warning.println("updateModel: " + rowNr + " " + columnNr + " " +  
				+ table.getSelectionModel().getSelectedCells().get(0).getRow() + " "
				+ table.getSelectionModel().getSelectedCells().get(0).getColumn() );
		for (int i = 0; i < partitionCount; i++) {
			Log.warning.println(i + " " + tableData[i][0] + " " + tableData[i][SITEMODEL_COLUMN] + " "
					+ tableData[i][CLOCKMODEL_COLUMN] + " " + tableData[i][TREE_COLUMN]);
		}

		getDoc();
		String partition = (String) tableData[rowNr][columnNr];

		// check if partition needs renaming
		String oldName = null;
		boolean isRenaming = false;
		try {
			switch (columnNr) {
			case SITEMODEL_COLUMN:
				if (!doc.pluginmap.containsKey("SiteModel.s:" + partition)) {
					String id = ((BEASTInterface)likelihoods[rowNr].siteModelInput.get()).getID();
					oldName = BeautiDoc.parsePartition(id);
					doc.renamePartition(BeautiDoc.SITEMODEL_PARTITION, oldName, partition);
					isRenaming = true;
				}
				break;
			case CLOCKMODEL_COLUMN: {
				String id = likelihoods[rowNr].branchRateModelInput.get().getID();
				String clockModelName = id.substring(0, id.indexOf('.')) + ".c:" + partition;
				if (!doc.pluginmap.containsKey(clockModelName)) {
					oldName = BeautiDoc.parsePartition(id);
					doc.renamePartition(BeautiDoc.CLOCKMODEL_PARTITION, oldName, partition);
					isRenaming = true;
				}
			}
				break;
			case TREE_COLUMN:
				if (!doc.pluginmap.containsKey("Tree.t:" + partition)) {
					String id = likelihoods[rowNr].treeInput.get().getID();
					oldName = BeautiDoc.parsePartition(id);
					doc.renamePartition(BeautiDoc.TREEMODEL_PARTITION, oldName, partition);
					isRenaming = true;
				}
				break;
			}
		} catch (Exception e) {
			Alert.showMessageDialog(this, "Cannot rename item: " + e.getMessage());
			setTableEntry(rowNr, columnNr, oldName);
			return;
		}
		if (isRenaming) {
			doc.determinePartitions();
			initTableData();
			setUpComboBoxes();
//			table.repaint();
			return;
		}
		
		int partitionID = BeautiDoc.ALIGNMENT_PARTITION;
		switch (columnNr) {
		case SITEMODEL_COLUMN:
			partitionID = BeautiDoc.SITEMODEL_PARTITION;
			break;
		case CLOCKMODEL_COLUMN:
			partitionID = BeautiDoc.CLOCKMODEL_PARTITION;
			break;
		case TREE_COLUMN:
			partitionID = BeautiDoc.TREEMODEL_PARTITION;
			break;
		}
		int partitionNr = doc.getPartitionNr(partition, partitionID);
		GenericTreeLikelihood treeLikelihood = null;
		if (partitionNr >= 0) {
			// we ar linking
			treeLikelihood = likelihoods[partitionNr];
		}
		// (TreeLikelihood) doc.pluginmap.get("treeLikelihood." +
		// tableData[rowNr][NAME_COLUMN]);

		boolean needsRePartition = false;
		
		PartitionContext oldContext = PartitionContextUtil.newPartitionContext(this.likelihoods[rowNr]);

		switch (columnNr) {
		case SITEMODEL_COLUMN: {
			SiteModelInterface siteModel = null;
			if (treeLikelihood != null) { // getDoc().getPartitionNr(partition,
											// BeautiDoc.SITEMODEL_PARTITION) !=
											// rowNr) {
				siteModel = treeLikelihood.siteModelInput.get();
			} else {
				siteModel = (SiteModel) doc.pluginmap.get("SiteModel.s:" + partition);
				if (siteModel != likelihoods[rowNr].siteModelInput.get()) {
					PartitionContext context = getPartitionContext(rowNr);
					try {
					siteModel = (SiteModel.Base) BeautiDoc.deepCopyPlugin((BEASTInterface) likelihoods[rowNr].siteModelInput.get(),
							likelihoods[rowNr], (MCMC) doc.mcmc.get(), oldContext, context, doc, null);
					} catch (RuntimeException e) {
						Alert.showMessageDialog(this, "Could not clone site model: " + e.getMessage());
						return;
					}
				}
			}
			SiteModelInterface target = this.likelihoods[rowNr].siteModelInput.get();
			if (target instanceof SiteModel.Base && siteModel instanceof SiteModel.Base) {
				if (!((SiteModel.Base)target).substModelInput.canSetValue(((SiteModel.Base)siteModel).substModelInput.get(), (SiteModel.Base) target)) {
					throw new IllegalArgumentException("Cannot link site model: substitution models (" + 
							((SiteModel.Base)target).substModelInput.get().getClass().toString() + " and " +
							((SiteModel.Base)siteModel).substModelInput.get().getClass().toString() +
							") are incompatible");
				}
			} else {
				throw new IllegalArgumentException("Don't know how to link this site model");
			}
			needsRePartition = (this.likelihoods[rowNr].siteModelInput.get() != siteModel);
			this.likelihoods[rowNr].siteModelInput.setValue(siteModel, this.likelihoods[rowNr]);

			partition = ((BEASTInterface)likelihoods[rowNr].siteModelInput.get()).getID();
			partition = BeautiDoc.parsePartition(partition);
			getDoc().setCurrentPartition(BeautiDoc.SITEMODEL_PARTITION, rowNr, partition);
		}
			break;
		case CLOCKMODEL_COLUMN: {
			BranchRateModel clockModel = null;
			if (treeLikelihood != null) { // getDoc().getPartitionNr(partition,
											// BeautiDoc.CLOCKMODEL_PARTITION)
											// != rowNr) {
				clockModel = treeLikelihood.branchRateModelInput.get();
			} else {
				clockModel = getDoc().getClockModel(partition);
				if (clockModel != likelihoods[rowNr].branchRateModelInput.get()) {
					PartitionContext context = getPartitionContext(rowNr);
					try {
						clockModel = (BranchRateModel) BeautiDoc.deepCopyPlugin(likelihoods[rowNr].branchRateModelInput.get(),
							likelihoods[rowNr], (MCMC) doc.mcmc.get(), oldContext, context, doc, null);
					} catch (RuntimeException e) {
						Alert.showMessageDialog(this, "Could not clone clock model: " + e.getMessage());
						return;
					}
				}
			}
			// make sure that *if* the clock model has a tree as input, it is
			// the same as
			// for the likelihood
			TreeInterface tree = null;
			for (Input<?> input : ((BEASTInterface) clockModel).listInputs()) {
				if (input.getName().equals("tree")) {
					tree = (TreeInterface) input.get();
				}

			}
			if (tree != null && tree != this.likelihoods[rowNr].treeInput.get()) {
				Alert.showMessageDialog(this, "Cannot link clock model with different trees");
				throw new IllegalArgumentException("Cannot link clock model with different trees");
			}

			needsRePartition = (this.likelihoods[rowNr].branchRateModelInput.get() != clockModel);
			this.likelihoods[rowNr].branchRateModelInput.setValue(clockModel, this.likelihoods[rowNr]);
			partition = likelihoods[rowNr].branchRateModelInput.get().getID();
			partition = BeautiDoc.parsePartition(partition);
			getDoc().setCurrentPartition(BeautiDoc.CLOCKMODEL_PARTITION, rowNr, partition);
		}
			break;
		case TREE_COLUMN: {
			TreeInterface tree = null;
			if (treeLikelihood != null) { // getDoc().getPartitionNr(partition,
											// BeautiDoc.TREEMODEL_PARTITION) !=
											// rowNr) {
				tree = treeLikelihood.treeInput.get();
			} else {
				tree = (TreeInterface) doc.pluginmap.get("Tree.t:" + partition);
				if (tree != likelihoods[rowNr].treeInput.get()) {
					PartitionContext context = getPartitionContext(rowNr);
					try {
						tree = (TreeInterface) BeautiDoc.deepCopyPlugin((BEASTInterface) likelihoods[rowNr].treeInput.get(), likelihoods[rowNr],
							(MCMC) doc.mcmc.get(), oldContext, context, doc, null);
					} catch (RuntimeException e) {
						Alert.showMessageDialog(this, "Could not clone tree model: " + e.getMessage());
						return;
					}
					
					State state = ((MCMC) doc.mcmc.get()).startStateInput.get();
					List<StateNode> stateNodes = new ArrayList<>();
					stateNodes.addAll(state.stateNodeInput.get());
					for (StateNode s : stateNodes) {
						if (s.getID().endsWith(".t:" + oldContext.tree) && !(s instanceof TreeInterface)) {
							try {
								@SuppressWarnings("unused")
								StateNode copy = (StateNode) BeautiDoc.deepCopyPlugin(s, likelihoods[rowNr], (MCMC) doc.mcmc.get(), oldContext, context, doc, null);
							} catch (RuntimeException e) {
								Alert.showMessageDialog(this, "Could not clone tree model: " + e.getMessage());
								return;
							}

						}
					}
				}
			}
			// sanity check: make sure taxon sets are compatible
			Taxon.assertSameTaxa(tree.getID(), tree.getTaxonset().getTaxaNames(),
					likelihoods[rowNr].dataInput.get().getID(), likelihoods[rowNr].dataInput.get().getTaxaNames());

			needsRePartition = (this.likelihoods[rowNr].treeInput.get() != tree);
			Log.warning.println("needsRePartition = " + needsRePartition);			
			if (needsRePartition) {
				TreeInterface oldTree = this.likelihoods[rowNr].treeInput.get();
				List<TreeInterface> tModels = new ArrayList<>();
				for (GenericTreeLikelihood likelihood : likelihoods) {
					if (likelihood.treeInput.get() == oldTree) {
						tModels.add(likelihood.treeInput.get());
					}
				}
				if (tModels.size() == 1) {
					// remove old tree from model
					((BEASTInterface)oldTree).setInputValue("estimate", false);
                	// use toArray to prevent ConcurrentModificationException
					for (Object beastObject : BEASTInterface.getOutputs(oldTree).toArray()) { //.toArray(new BEASTInterface[0])) {
						for (Input<?> input : ((BEASTInterface)beastObject).listInputs()) {
							try {
							if (input.get() == oldTree) {
								if (input.getRule() != Input.Validate.REQUIRED) {
									input.setValue(tree/*null*/, (BEASTInterface) beastObject);
								//} else {
									//input.setValue(tree, (BEASTInterface) beastObject);
								}
							} else if (input.get() instanceof List) {
								@SuppressWarnings("unchecked")
								List<TreeInterface> list = (List<TreeInterface>) input.get();
								if (list.contains(oldTree)) { // && input.getRule() != Validate.REQUIRED) {
									list.remove(oldTree);
									if (!list.contains(tree)) {
										list.add(tree);
									}
								}
							}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
			likelihoods[rowNr].treeInput.setValue(tree, likelihoods[rowNr]);
			// TreeDistribution d = getDoc().getTreePrior(partition);
			// CompoundDistribution prior = (CompoundDistribution)
			// doc.pluginmap.get("prior");
			// if (!getDoc().posteriorPredecessors.contains(d)) {
			// prior.pDistributions.setValue(d, prior);
			// }
			partition = likelihoods[rowNr].treeInput.get().getID();
			partition = BeautiDoc.parsePartition(partition);
			getDoc().setCurrentPartition(BeautiDoc.TREEMODEL_PARTITION, rowNr, partition);
		}
		}
		tableData[rowNr][columnNr] = partition;
		if (needsRePartition) {
			List<BeautiSubTemplate> templates = new ArrayList<>();
			templates.add(doc.beautiConfig.partitionTemplate.get());
			templates.addAll(doc.beautiConfig.subTemplates);
			// keep applying rules till model does not change
			doc.setUpActivePlugins();
			int n;
			do {
				n = doc.posteriorPredecessors.size();
				doc.applyBeautiRules(templates, false, oldContext);
				doc.setUpActivePlugins();
			} while (n != doc.posteriorPredecessors.size());
			doc.determinePartitions();
		}
		if (treeLikelihood == null) {
			initTableData();
			setUpComboBoxes();
		}
		
		updateStatus();
	}

	private void setTableEntry(int rowNr, int columnNr, Object o) {
		tableData[rowNr][columnNr] = o;
		if (o instanceof BEASTInterface) {
			switch (columnNr) {
			case NAME_COLUMN: tableEntries.get(rowNr).setName(((BEASTInterface)o).getID());break;
			case FILE_COLUMN: tableEntries.get(rowNr).setFile(((BEASTInterface)o).getID());break;
			case TAXA_COLUMN: tableEntries.get(rowNr).setTaxa(((BEASTInterface)o).getID());break;
			case SITES_COLUMN: tableEntries.get(rowNr).setSites(((BEASTInterface)o).getID());break;
			case TYPE_COLUMN: tableEntries.get(rowNr).setDataType(((BEASTInterface)o).getID());break;
			case SITEMODEL_COLUMN: tableEntries.get(rowNr).setSiteModel(((BEASTInterface)o).getID());break;
			case CLOCKMODEL_COLUMN: tableEntries.get(rowNr).setClockModel(((BEASTInterface)o).getID());break;
			case TREE_COLUMN: tableEntries.get(rowNr).setTree(((BEASTInterface)o).getID());break;
			// case USE_AMBIGUITIES_COLUMN: tableEntries.get(rowNr).setAmbiguities(((BEASTInterface)o).getID());break;
			}
		} else {
			switch (columnNr) {
			case NAME_COLUMN: tableEntries.get(rowNr).setName(o.toString());break;
			case FILE_COLUMN: tableEntries.get(rowNr).setFile(o.toString());break;
			case TAXA_COLUMN: tableEntries.get(rowNr).setTaxa(o.toString());break;
			case SITES_COLUMN: tableEntries.get(rowNr).setSites(o.toString());break;
			case TYPE_COLUMN: tableEntries.get(rowNr).setDataType(o.toString());break;
			case SITEMODEL_COLUMN: tableEntries.get(rowNr).setSiteModel(o.toString());break;
			case CLOCKMODEL_COLUMN: tableEntries.get(rowNr).setClockModel(o.toString());break;
			case TREE_COLUMN: tableEntries.get(rowNr).setTree(o.toString());break;
			// case USE_AMBIGUITIES_COLUMN: tableEntries.get(rowNr).setAmbiguities(o.toString());break;
			}
		}
	}

	private PartitionContext getPartitionContext(int rowNr) {
		PartitionContext context = new PartitionContext(
				tableData[rowNr][NAME_COLUMN].toString(),
				tableData[rowNr][SITEMODEL_COLUMN].toString(),
				tableData[rowNr][CLOCKMODEL_COLUMN].toString(),
				tableData[rowNr][TREE_COLUMN].toString());
		return context;
	}

	@Override
	protected void addInputLabel() {
	}

	void initTableData() {
		this.likelihoods = new GenericTreeLikelihood[partitionCount];
		if (tableData == null) {
			tableData = new Object[partitionCount][NR_OF_COLUMNS];
		}
		CompoundDistribution likelihoods = (CompoundDistribution) doc.pluginmap.get("likelihood");

		List<Partition0> list = new ArrayList<>();
		for (int i = 0; i < partitionCount; i++) {
			Alignment data = alignments.get(i);
			// partition name
			//setTableEntry(i, NAME_COLUMN, data);
			tableData[i][NAME_COLUMN] = data;

			// alignment name
			if (data instanceof FilteredAlignment) {
				tableData[i][FILE_COLUMN] = ((FilteredAlignment) data).alignmentInput.get();
			} else {
				tableData[i][FILE_COLUMN] = data;
			}
			// # taxa
			tableData[i][TAXA_COLUMN] = data.getTaxonCount();
			// # sites
			tableData[i][SITES_COLUMN] = data.getSiteCount();
			// Data type
			tableData[i][TYPE_COLUMN] = data.getDataType();
			// site model
			GenericTreeLikelihood likelihood = (GenericTreeLikelihood) likelihoods.pDistributions.get().get(i);
			assert (likelihood != null);
			this.likelihoods[i] = likelihood;
			tableData[i][SITEMODEL_COLUMN] = getPartition(likelihood.siteModelInput);
			// clock model
			tableData[i][CLOCKMODEL_COLUMN] = getPartition(likelihood.branchRateModelInput);
			// tree
			tableData[i][TREE_COLUMN] = getPartition(likelihood.treeInput);
			// useAmbiguities
			tableData[i][USE_AMBIGUITIES_COLUMN] = null;
			try {
				if (hasUseAmbiguitiesInput(i)) {
					tableData[i][USE_AMBIGUITIES_COLUMN] = likelihood.getInputValue("useAmbiguities");
				}
			} catch (Exception e) {
				// ignore
			}
			list.add(new Partition0(likelihood));
		}
		tableEntries = FXCollections.observableArrayList(list);
		table.setItems(tableEntries);
	}
	
//	void initTableEntries() {
//		if (tableData == null) {
//			throw new RuntimeException("Programmer error: tableData should be initialised");
//		}
//		CompoundDistribution likelihoods = (CompoundDistribution) doc.pluginmap.get("likelihood");
//
//		List<Partition0> list = new ArrayList<>();
//		for (int i = 0; i < partitionCount; i++) {
//			GenericTreeLikelihood likelihood = (GenericTreeLikelihood) likelihoods.pDistributions.get().get(i);
//			assert (likelihood != null);
//			list.add(new Partition0(likelihood));
//		}
//		tableEntries = FXCollections.observableArrayList(list);
//	}
	
	private boolean hasUseAmbiguitiesInput(int i) {
		try {
			for (Input<?> input : likelihoods[i].listInputs()) {
				if (input.getName().equals("useAmbiguities")) {
					return true;
				}
			}
		} catch (Exception e) {
			// ignore
		}
		return false;
	}

	private String getPartition(Input<?> input) {
		BEASTInterface beastObject = (BEASTInterface) input.get();
		String id = beastObject.getID();
		String partition = BeautiDoc.parsePartition(id);
		return partition;
	}

	
	
	public class Partition0 {
		private SimpleStringProperty name, file, taxa, sites, dataType, siteModel, clockModel, tree;
		private SimpleBooleanProperty ambiguities;
		
		public Partition0(GenericTreeLikelihood likelihood)  {
			Alignment data = likelihood.dataInput.get();
			name = new SimpleStringProperty(likelihood.getID());
			if (data instanceof FilteredAlignment) {
				file = new SimpleStringProperty(((FilteredAlignment) data).alignmentInput.get().getID());
			} else {
				file = new SimpleStringProperty(data.getID());
			}
			// # taxa
			taxa = new SimpleStringProperty(data.getTaxonCount() + "");
			// # sites
			sites = new SimpleStringProperty(data.getSiteCount() + "");
			// Data type
			dataType = new SimpleStringProperty(data.getDataType().toString());
			// site model
			// this.likelihoods[i] = likelihood;
			siteModel = new SimpleStringProperty(getPartition(likelihood.siteModelInput));
			// clock model
			clockModel = new SimpleStringProperty(getPartition(likelihood.branchRateModelInput));
			// tree
			tree = new SimpleStringProperty(getPartition(likelihood.treeInput));
			// useAmbiguities
			ambiguities = new SimpleBooleanProperty();
			try {
				ambiguities.set((Boolean) likelihood.getInputValue("useAmbiguities"));
			} catch (Exception e) {
				// ignore
			}
			ambiguities.addListener((obs,  wasSelected,  isSelected) -> {
			    if (likelihood.getInputs().containsKey("useAmbiguities")) {
			        Input<?> input = likelihood.getInput("useAmbiguities");
			        input.setValue(isSelected, likelihood);
			    }
			});	
		}
		
		public String getName() {
			return name.get();
		}

		public void setName(String name) {
			this.name.set(name);
		}

		public String getFile() {
			return file.get();
		}

		public void setFile(String file) {
			this.file.set(file);
		}

		public String getTaxa() {
			return taxa.get();
		}

		public void setTaxa(String taxa) {
			this.taxa.set(taxa);
		}

		public String getSites() {
			return sites.get();
		}

		public void setSites(String sites) {
			this.sites.set(sites);
		}

		public String getDataType() {
			return dataType.get();
		}

		public void setDataType(String dataType) {
			this.dataType.set(dataType);
		}

		public String getSiteModel() {
			return siteModel.get();
		}

		public void setSiteModel(String siteModel) {
			this.siteModel.set(siteModel);
		}

		public String getClockModel() {
			return clockModel.get();
		}

		public void setClockModel(String clockModel) {
			this.clockModel.set(clockModel);
		}

		public String getTree() {
			return tree.get();
		}

		public void setTree(String tree) {
			this.tree.set(tree);
		}

		public Boolean getAmbiguities() {
			return ambiguities.getValue();
		}

		public void setAmbiguities(Boolean ambiguities) {
			this.ambiguities.setValue(ambiguities);
		}
		
//		public StringProperty nameProperty() {return name;}
//		public StringProperty fileProperty() {return file;}
//		public StringProperty taxaProperty() {return taxa;}
//		public StringProperty sitesProperty() {return sites;}
//		public StringProperty dataTypeProperty() {return dataType;}
		public StringProperty siteModelProperty() {return siteModel;}
		public StringProperty clockModelProperty() {return clockModel;}
		public StringProperty treeProperty() {return tree;}
		public BooleanProperty ambiguitiesProperty() { return ambiguities; }

	}
	
	protected TableView<Partition0> createListBox() {
		String[] columnData = new String[] { "Name", "File", "Taxa", "Sites", "Data Type", "Site Model", "Clock Model",
				"Tree", "Ambiguities"};

		// set up table.
		// special features: background shading of rows
		// custom editor allowing only Date column to be edited.
		table = new TableView<Partition0>();
		table.setPlaceholder(new Label("No partitions loaded yet"));

		for (int i = 0; i < columnData.length; i++) {
			TableColumn<Partition0, String> col1 = new TableColumn<>(columnData[i]);
			String str = columnData[i].substring(0,1).toLowerCase() + columnData[i].substring(1);
			str = str.replaceAll(" ","");
			if (i != 5 && i!= 6 && i != 7)
				col1.setCellValueFactory(new PropertyValueFactory<>(str));
			table.getColumns().add(col1);
		}

		initTableData();
		// table.setItems(tableEntries);
		
//		table = new JTable(tableData, columnData) {
//
//			// method that induces table row shading
//			@Override
//			public Component prepareRenderer(TableCellRenderer renderer, int Index_row, int Index_col) {
//				Component comp = super.prepareRenderer(renderer, Index_row, Index_col);
//				// even index, selected or not selected
//				if (isCellSelected(Index_row, Index_col)) {
//					comp.setBackground(Color.gray);
//				} else if (Index_row % 2 == 0 && !isCellSelected(Index_row, Index_col)) {
//					comp.setBackground(new Color(237, 243, 255));
//				} else {
//					comp.setBackground(Color.white);
//				}
//			    JComponent jcomp = (JComponent) comp;
//		    	switch (Index_col) {
//		    	case NAME_COLUMN:			    		
//	    		case CLOCKMODEL_COLUMN: 
//	    		case TREE_COLUMN: 
//	    		case SITEMODEL_COLUMN: 
//			        jcomp.setTooltip(new Tooltip("Set " + table.getColumnName(Index_col).toLowerCase() + " for this partition"));
//					break;
//	    		case FILE_COLUMN:
//	    		case TAXA_COLUMN:
//	    		case SITES_COLUMN:
//	    		case TYPE_COLUMN:
//			        jcomp.setTooltip(new Tooltip("Report " + table.getColumnName(Index_col).toLowerCase() + " for this partition"));
//					break;
//	    		case USE_AMBIGUITIES_COLUMN: 
//					jcomp.setToolTipText("<html>Flag whether to use ambiguities.<br>" +
//							"If not set, the treelikelihood will treat ambiguities in the<br>" +
//							"data as unknowns<br>" +
//							"If set, the treelikelihood will use ambiguities as equally<br>" +
//							"likely values for the tips.<br>" +
//							"This will make the computation twice as slow.</html>");
//					break;
//				default:
//			        jcomp.setTooltip(new Tooltip(null));
//		    	}
//				updateStatus();
//				return comp;
//			}
//		};
		//int size = table.getFont().getSize();
		//table.setRowHeight(25 * size/13);
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        table.getSelectionModel().selectedItemProperty().addListener(
        	    (observable, oldValue, newValue) -> {
        	    	updateStatus();
        	    }
        		);
		//table.setColumnSelectionAllowed(false);
		//table.setRowSelectionAllowed(true);
		table.setId("alignmenttable");

		setUpComboBoxes();
		
		// make columns 0, 5, 6 and 7 editable
		for (int i : new int[] {0, 5, 6, 7}) {
			TableColumn<Partition0, ?> tblColID = table.getColumns().get(i);
			((TableColumn)tblColID).setCellFactory(TextFieldTableCell.forTableColumn());
			tblColID.setOnEditCommit(e->{
				processPartitionName(e);
			});
		}
		
		// make column 9 use checkboxes
		if (table.getColumns().size() > 8) {
		TableColumn<Partition0, Boolean> ambiguitiesColumn = (TableColumn<Partition0, Boolean>) table.getColumns().get(8);
		ambiguitiesColumn.setCellValueFactory( f -> f.getValue().ambiguitiesProperty());
		ambiguitiesColumn.setCellFactory( tc -> new CheckBoxTableCell<>());
		
//		ambiguitiesColumn.setOnEditCommit(e -> {
//			CheckBox checkBox = (CheckBox) e.getSource();
//			ObservableList<TablePosition> list = table.getSelectionModel().getSelectedCells();
//			TablePosition pos = list.get(0);
//			if (pos.getRow() >= 0 && pos.getColumn() >= 0) {
//				Log.warning.println(" " + tableData[pos.getRow()][pos.getColumn()]);
//			}
//			try {
//				int row = pos.getRow();
//				if (hasUseAmbiguitiesInput(row)) {
//					likelihoods[row].setInputValue("useAmbiguities", checkBox.isSelected());
//					tableData[row][USE_AMBIGUITIES_COLUMN] = checkBox.isSelected();
//				} else {
//					if (checkBox.isSelected()) {
//						checkBox.setSelected(false);
//					}
//				}
//			} catch (Exception ex) {
//				// TODO: handle exception
//			}
//	
//		});  
		}
		
//		TableColumn col = table.getColumnModel().getColumn(NAME_COLUMN);
//		nameEditor = new TextField();
//		nameEditor.getDocument().addDocumentListener(new DocumentListener() {
//			@Override
//			public void removeUpdate(DocumentEvent e) {
//				processPartitionName();
//			}
//
//			@Override
//			public void insertUpdate(DocumentEvent e) {
//				processPartitionName();
//			}
//
//			@Override
//			public void changedUpdate(DocumentEvent e) {
//				processPartitionName();
//			}
//		});
//
//		col.setCellEditor(new DefaultCellEditor(nameEditor));

		// // set up editor that makes sure only doubles are accepted as entry
		// // and only the Date column is editable.
//		table.setDefaultEditor(Object.class, new TableCellEditor() {
//			TextField m_textField = new TextField();
//			int m_iRow, m_iCol;
//
//			@Override
//			public boolean stopCellEditing() {
//				//Log.warning.println("stopCellEditing()");
//				table.removeEditor();
//				String text = m_textField.getText();
//				try {
//					Double.parseDouble(text);
//				} catch (Exception e) {
//					return false;
//				}
//				tableData[m_iRow][m_iCol] = text;
//				return true;
//			}
//
//			@Override
//			public boolean isCellEditable(EventObject anEvent) {
//				//Log.warning.println("isCellEditable()");
//				return table.getSelectedColumn() == 0;
//			}
//
//			@Override
//			public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int rowNr,
//					int colNr) {
//				return null;
//			}
//
//			@Override
//			public boolean shouldSelectCell(EventObject anEvent) {
//				return false;
//			}
//
//			@Override
//			public void removeCellEditorListener(CellEditorListener l) {
//			}
//
//			@Override
//			public Object getCellEditorValue() {
//				return null;
//			}
//
//			@Override
//			public void cancelCellEditing() {
//			}
//
//			@Override
//			public void addCellEditorListener(CellEditorListener l) {
//			}
//
//		});

		// show alignment viewer when double clicking a row
		table.setOnMouseClicked(e->{
			if (e.getClickCount() > 1) {
				try {
					int alignmemt = table.getSelectionModel().getSelectedIndex();//table.rowAtPoint(e.getPoint());
					Alignment alignment = alignments.get(alignmemt);
					int best = 0;
					BeautiAlignmentProvider provider = null;
					for (BeautiAlignmentProvider provider2 : doc.beautiConfig.alignmentProvider) {
						int match = provider2.matches(alignment);
						if (match > best) {
							best = match;
							provider = provider2;
						}
					}
					provider.editAlignment(alignment, doc);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				updateStatus();
			} else if (e.isMiddleButtonDown()) {
				int alignmemt = table.getSelectionModel().getSelectedIndex();// table.rowAtPoint(e.getPoint());
				Alignment alignment = alignments.get(alignmemt);
				ButtonType result = Alert.showConfirmDialog(null, "Do you want to replace alignment " + alignment.getID(), "Replace alignment?", Alert.YES_NO_OPTION);
				if (result == Alert.YES_OPTION) {
					replaceItem(alignment);
				}
			}			
		});
		
//		table.addMouseListener(new MouseListener() {
//
//			@Override
//			public void mouseReleased(MouseEvent e) {
//			}
//
//			@Override
//			public void mousePressed(MouseEvent e) {
//			}
//
//			@Override
//			public void mouseExited(MouseEvent e) {
//			}
//
//			@Override
//			public void mouseEntered(MouseEvent e) {
//			}
//
//			@Override
//			public void mouseClicked(MouseEvent e) {
//				if (e.getClickCount() > 1) {
//					try {
//						int alignmemt = table.rowAtPoint(e.getPoint());
//						Alignment alignment = alignments.get(alignmemt);
//						int best = 0;
//						BeautiAlignmentProvider provider = null;
//						for (BeautiAlignmentProvider provider2 : doc.beautiConfig.alignmentProvider) {
//							int match = provider2.matches(alignment);
//							if (match > best) {
//								best = match;
//								provider = provider2;
//							}
//						}
//						provider.editAlignment(alignment, doc);
//					} catch (Exception e1) {
//						e1.printStackTrace();
//					}
//					updateStatus();
//				} else if (e.getButton() == e.BUTTON3) {
//					int alignmemt = table.rowAtPoint(e.getPoint());
//					Alignment alignment = alignments.get(alignmemt);
//					ButtonType result = Alert.showConfirmDialog(null, "Do you want to replace alignment " + alignment.getID(), "Replace alignment?", Alert.YES_NO_OPTION);
//					if (result == Alert.YES_OPTION) {
//						replaceItem(alignment);
//					}
//				}
//			}
//		});

//		scrollPane = new ScrollPane();
//		scrollPane.setContent(table);
//
//        int rowsToDisplay = 3;
//        Dimension d = table.getPreferredSize();
//        scrollPane.setPrefSize(
//                d.width,table.getRowHeight()*rowsToDisplay+table.getTableHeader().getHeight());
//
//		return scrollPane;
		return table;
	} // createListBox
	

	private void processPartitionName(CellEditEvent<Partition0, ?> e) {
		String newId = (String) e.getNewValue();
		int row = e.getTablePosition().getRow();
		int col = e.getTablePosition().getColumn();
		processPartitionName(row, col, newId);
	}

	void setUpComboBoxes() {
		// set up comboboxes
		@SuppressWarnings("unchecked")
		Set<String>[] partitionNames = new HashSet[3];
		for (int i = 0; i < 3; i++) {
			partitionNames[i] = new HashSet<>();
		}
		for (int i = 0; i < partitionCount; i++) {
			partitionNames[0].add(((BEASTInterface) likelihoods[i].siteModelInput.get()).getID());
			partitionNames[1].add(likelihoods[i].branchRateModelInput.get().getID());
			partitionNames[2].add(likelihoods[i].treeInput.get().getID());
		}
		String[][] partitionNameStrings = new String[3][];
		for (int i = 0; i < 3; i++) {
			partitionNameStrings[i] = partitionNames[i].toArray(new String[0]);
		}
		for (int j = 0; j < 3; j++) {
			for (int i = 0; i < partitionNameStrings[j].length; i++) {
				partitionNameStrings[j][i] = BeautiDoc.parsePartition(partitionNameStrings[j][i]);
			}
		}
		
        table.setEditable(true);

		
		String [] colName = {"Site Model", "Clock Model", "Tree"};
		int [] colPos = {SITEMODEL_COLUMN, CLOCKMODEL_COLUMN, TREE_COLUMN};
		
		for (int k = 0; k < 3; k++) {
			TableColumn<Partition0, String> column = new TableColumn<>(colName[k]);
	        switch (k) {
	        case 0: 
	        	column.setCellValueFactory(cellData -> cellData.getValue().siteModelProperty());
	        	column.setCellFactory(ComboBoxTableCell.forTableColumn(partitionNameStrings[0]));
	        	column.setCellFactory(col -> {
	        		ComboBoxTableCell cell = new ComboBoxTableCell(partitionNameStrings[0]);
	        		cell.setId("cell-" + 0 + "-" + SITEMODEL_COLUMN);
	        		return cell;
	        	});

//	        	column.setCellValueFactory(i -> {
//			        final StringProperty value = i.getValue().siteModelProperty();
//			        return Bindings.createObjectBinding(() -> value);
//			    });
//			    column.setCellFactory(col -> {
//			        TableCell<Partition0, StringProperty> c = new TableCell<>();
//			        final ComboBox<String> comboBox = new ComboBox<>();
//			        comboBox.getItems().addAll(partitionNameStrings[0]);
//			        comboBox.setEditable(true);
//			        c.itemProperty().addListener((observable, oldValue, newValue) -> {
//			        	int row = c.getTableRow().getIndex();
//			            if (oldValue != null) {
//			                comboBox.valueProperty().unbindBidirectional(oldValue);
//			            }
//			            if (newValue != null) {
//			                comboBox.valueProperty().bindBidirectional(newValue);
//			            }
//			        });
//			        c.graphicProperty().bind(comboBox);
//			        		//Bindings.when(c.emptyProperty()).then((Node) null).otherwise(comboBox));
//			        return c;
//			    });
			    
			    break;
	    	case 1: 
	        	column.setCellValueFactory(cellData -> cellData.getValue().clockModelProperty());
	        	column.setCellFactory(ComboBoxTableCell.forTableColumn(partitionNameStrings[1]));
	        	column.setCellFactory(col -> {
	        		ComboBoxTableCell cell = new ComboBoxTableCell(partitionNameStrings[1]);
	        		cell.setId("cell-" + 0 + "-" + CLOCKMODEL_COLUMN);
	        		return cell;
	        	});
//			    column.setCellValueFactory(i -> {
//			        final StringProperty value = i.getValue().clockModelProperty();
//			        return Bindings.createObjectBinding(() -> value);
//			    });
//			    column.setCellFactory(col -> {
//			        TableCell<Partition0, StringProperty> c = new TableCell<>();
//			        final ComboBox<String> comboBox = new ComboBox<>();
//			        comboBox.getItems().addAll(partitionNameStrings[1]);
//			        comboBox.setEditable(true);
//			        c.itemProperty().addListener((observable, oldValue, newValue) -> {
//			        	int row = c.getTableRow().getIndex();
//			            if (oldValue != null) {
//			                comboBox.valueProperty().unbindBidirectional(oldValue);
//			            }
//			            if (newValue != null) {
//			                comboBox.valueProperty().bindBidirectional(newValue);
//			            }
//			        });
//			        // c.graphicProperty().bind(comboBox);
//			        		//Bindings.when(c.emptyProperty()).then((Node) null).otherwise(comboBox));
//			        return c;
//			    });
			    break;
	    	case 2: 
	        	column.setCellValueFactory(cellData -> cellData.getValue().treeProperty());
	        	column.setCellFactory(ComboBoxTableCell.forTableColumn(partitionNameStrings[2]));
	        	column.setCellFactory(col -> {
	        		ComboBoxTableCell cell = new ComboBoxTableCell(partitionNameStrings[2]);
	        		cell.setId("cell-" + 0 + "-" + TREE_COLUMN);
	        		return cell;
	        	});
//			    column.setCellValueFactory(i -> {
//			        final StringProperty value = i.getValue().treeProperty();
//			        return Bindings.createObjectBinding(() -> value);
//			    });
//			    column.setCellFactory(col -> {
//			        TableCell<Partition0, StringProperty> c = new TableCell<>();
//			        final ComboBox<String> comboBox = new ComboBox<>();
//			        comboBox.getItems().addAll(partitionNameStrings[2]);
//			        comboBox.setEditable(true);
//			        c.itemProperty().addListener((observable, oldValue, newValue) -> {
//			        	int row = c.getTableRow().getIndex();
//			            if (oldValue != null) {
//			                comboBox.valueProperty().unbindBidirectional(oldValue);
//			            }
//			            if (newValue != null) {
//			                comboBox.valueProperty().bindBidirectional(newValue);
//			            }
//			        });
//			        // c.graphicProperty().bind(comboBox);
//			        		//Bindings.when(c.emptyProperty()).then((Node) null).otherwise(comboBox));
//			        return c;
//			    });
			}
		    
			
			table.getColumns().set(colPos[k], column);
		}
		
		
		
		
		
		
		
		
		
		
//		TableColumn<Partition0,StringProperty> col = (TableColumn<Partition0,String>) table.getColumns().get(SITEMODEL_COLUMN);
//		col.setCellFactory(ComboBoxTableCell.forTableColumn(partitionNameStrings[0]));
//
//		ComboBoxTableCell<Partition0, String> siteModelComboBox = new ComboBoxTableCell<>();
//
//		siteModelComboBox.getItems().addAll(partitionNameStrings[0]);
//		siteModelComboBox.setEditable(true);
//		siteModelComboBox.setComboBoxEditable(true);
//		siteModelComboBox.setOnAction(e->new ComboActionListener(SITEMODEL_COLUMN));
//		col.setCellEditor(siteModelComboBox);
//		
//		col.setCellEditor(new DefaultCellEditor(siteModelComboBox));
//		// If the cell should appear like a combobox in its
//		// non-editing state, also set the combobox renderer
//		col.setCellRenderer(new MyComboBoxRenderer(partitionNameStrings[0]));
//
//		col = (TableColumn<Partition0,String>)table.getColumns().get(CLOCKMODEL_COLUMN);
//		ComboBoxTableCell<Partition0, String> clockModelComboBox = new ComboBoxTableCell<>();
//		clockModelComboBox.getItems().addAll(partitionNameStrings[1]);
//		clockModelComboBox.setEditable(true);
//		clockModelComboBox.setComboBoxEditable(true);
//		clockModelComboBox.setOnAction(e->new ComboActionListener(CLOCKMODEL_COLUMN));
//
//		col.setCellEditor(new DefaultCellEditor(clockModelComboBox));
//		col.setCellRenderer(new MyComboBoxRenderer(partitionNameStrings[1]));
//
//		col = (	TableColumn<Partition0,String>) table.getColumns().get(TREE_COLUMN);
//		ComboBoxTableCell<Partition0, String> treeComboBox = new ComboBoxTableCell<>();
//		treeComboBox.getItems().addAll(partitionNameStrings[2]);
//		treeComboBox.setEditable(true);
//		treeComboBox.setComboBoxEditable(true);
//		treeComboBox.setOnAction(a->new ComboActionListener(TREE_COLUMN));
//		col.setCellEditor(new DefaultCellEditor(treeComboBox));
//		col.setCellRenderer(new MyComboBoxRenderer(partitionNameStrings[2]));
//		col = table.getColumnModel().getColumn(TAXA_COLUMN);
//		col.setPreferredWidth(30);
//		col = table.getColumnModel().getColumn(SITES_COLUMN);
//		col.setPreferredWidth(30);
//		
//		col = table.getColumns().get(USE_AMBIGUITIES_COLUMN);
//		CheckBox checkBox = new CheckBox();
//		checkBox.setOnAction(e -> {
//				if (table.getSelectedRow() >= 0 && table.getSelectedColumn() >= 0) {
//					Log.warning.println(" " + table.getValueAt(table.getSelectedRow(), table.getSelectedColumn()));
//				}
//				try {
//					int row = table.getSelectedRow();
//					if (hasUseAmbiguitiesInput(row)) {
//						likelihoods[row].setInputValue("useAmbiguities", checkBox.isSelected());
//						tableData[row][USE_AMBIGUITIES_COLUMN] = checkBox.isSelected();
//					} else {
//						if (checkBox.isSelected()) {
//							checkBox.setSelected(false);
//						}
//					}
//				} catch (Exception ex) {
//					// TODO: handle exception
//				}
//		
//			});
//		col.setCellEditor(new DefaultCellEditor(checkBox));
//		col.setCellRenderer(new MyCheckBoxRenderer());
//		col.setPreferredWidth(20);
//		col.setMaxWidth(20);
	}

	void processPartitionName(int row, int col, String newName) {
		Log.warning.println("processPartitionName");
		Log.warning.println(col + " " + row);
		String oldName = tableData[row][col].toString();
		// String newName = nameEditor.getText();
		if (!oldName.equals(newName) && newName.indexOf(".") >= 0) {
			// prevent full stops to be used in IDs
			newName = newName.replaceAll("\\.", "");
			//table.setValueAt(newName, row, col);
			//table.repaint();
		}
		if (!oldName.equals(newName)) {
			try {
				int partitionID = -2;
				switch (col) {
				case NAME_COLUMN:
					partitionID = BeautiDoc.ALIGNMENT_PARTITION;
					tableEntries.get(row).setName(newName);
					break;
				case SITEMODEL_COLUMN:
					partitionID = BeautiDoc.SITEMODEL_PARTITION;
					tableEntries.get(row).setSiteModel(newName);
					break;
				case CLOCKMODEL_COLUMN:
					partitionID = BeautiDoc.CLOCKMODEL_PARTITION;
					tableEntries.get(row).setClockModel(newName);
					break;
				case TREE_COLUMN:
					partitionID = BeautiDoc.TREEMODEL_PARTITION;
					tableEntries.get(row).setTree(newName);
					break;
				default:
					throw new IllegalArgumentException("Cannot rename item in column");
				}
				getDoc().renamePartition(partitionID, oldName, newName);
				// table.setValueAt(newName, row, col);
				setUpComboBoxes();
			} catch (Exception e) {
				Alert.showMessageDialog(null, "Renaming failed: " + e.getMessage());
			}
		}
		// debugging code:
		//for (int i = 0; i < partitionCount; i++) {
		//	Log.warning.println(i + " " + tableData[i][0]);
		//}
	}

//	class ComboActionListener implements ActionListener {
//		int m_nColumn;
//
//		public ComboActionListener(int columnNr) {
//			m_nColumn = columnNr;
//		}
//
//		@Override
//		public void actionPerformed(ActionEvent e) {
////			 SwingUtilities.invokeLater(new Runnable() {
////			 @Override
////			 public void run() {
//			Log.warning.println("actionPerformed ");
//			Log.warning.println(table.getSelectedRow() + " " + table.getSelectedColumn());
//			if (table.getSelectedRow() >= 0 && table.getSelectedColumn() >= 0) {
//				Log.warning.println(" " + table.getValueAt(table.getSelectedRow(), table.getSelectedColumn()));
//			}
//			for (int i = 0; i < partitionCount; i++) {
//				try {
//					updateModel(m_nColumn, i);
//				} catch (Exception ex) {
//					Log.warning.println(ex.getMessage());
//				}
//			}
////		    }});
//		}
//	}

//	public class MyComboBoxRenderer extends ComboBox<String> implements TableCellRenderer {
//
//		public MyComboBoxRenderer(String[] items) {
//			super(items);
//			setOpaque(true);
//		}
//
//		@Override
//		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
//				boolean hasFocus, int row, int column) {
//			if (isSelected) {
//				// setForeground(table.getSelectionForeground());
//				super.setBackground(table.getSelectionBackground());
//			} else {
//				setForeground(table.getForeground());
//				setBackground(table.getBackground());
//			}
//
//			// Select the current value
//			setSelectedItem(value);
//			return this;
//		}
//	}

//	public class MyCheckBoxRenderer extends Button implements TableCellRenderer {
//
//		public MyCheckBoxRenderer() {
//			super();
//			setOpaque(true);
//		}
//
//		@Override
//		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
//				boolean hasFocus, int row, int column) {
//			if (hasUseAmbiguitiesInput(row)) {
//				if (isSelected) {
//					// setForeground(table.getSelectionForeground());
//					super.setBackground(table.getSelectionBackground());
//				} else {
//					setForeground(table.getForeground());
//					setBackground(table.getBackground());
//				}
//				setDisable(false);
//				setSelected((Boolean) value);
//			} else {
//				setDisable(true);
//			}
//			return this;
//		}
//	}

	@Override
	protected void addSingleItem(BEASTInterface beastObject) {
		initTableData();
		repaint();
	}

	@Override
	protected void addItem() {
		addItem(null);
	} // addItem

	private void addItem(File[] fileArray) {
		List<BEASTInterface> beastObjects = doc.beautiConfig.selectAlignments(doc, this, fileArray);

		if (beastObjects != null) {
			for (BEASTInterface b : beastObjects) {
				GenericTreeLikelihood likelihood = null;
				for (BEASTInterface o : b.getOutputs()) {
					if (o instanceof GenericTreeLikelihood) {
						likelihood = (GenericTreeLikelihood) o;
						break;
					}
				}
//				Partition0 p = new Partition0(likelihood);
//				tableEntries.add(p);
				alignments.add(likelihood.dataInput.get());
			}
			partitionCount = alignments.size();
			tableData = null;
			initTableData();
//			table.setItems(tableEntries);
	    	table.refresh();
			refreshPanel();
		}
	}

	void delItem() {
		List<Integer> selected = getTableRowSelection();
		if (selected.size() == 0) {
			Alert.showMessageDialog(this, "Select partitions to delete, before hitting the delete button");
		}
		// do the actual deleting
		for (int i = selected.size() - 1; i >= 0; i--) {
			int rowNr = selected.get(i);
			
			// before deleting, unlink site model, clock model and tree
			
			// check whether any of the models are linked
			BranchRateModel.Base clockModel = likelihoods[rowNr].branchRateModelInput.get();
			SiteModelInterface siteModel = likelihoods[rowNr].siteModelInput.get();
			TreeInterface tree = likelihoods[rowNr].treeInput.get();
			List<GenericTreeLikelihood> cModels = new ArrayList<>();
			List<GenericTreeLikelihood> models = new ArrayList<>();
			List<GenericTreeLikelihood> tModels = new ArrayList<>();
			for (GenericTreeLikelihood likelihood : likelihoods) {
				if (likelihood != likelihoods[rowNr]) {
				if (likelihood.branchRateModelInput.get() == clockModel) {
					cModels.add(likelihood);
				}
				if (likelihood.siteModelInput.get() == siteModel) {
					models.add(likelihood);
				}
				if (likelihood.treeInput.get() == tree) {
					tModels.add(likelihood);
				}
				}
			}
			
			try {
				if (cModels.size() > 0) {
					// clock model is linked, so we need to unlink
					if (doc.getPartitionNr(clockModel) != rowNr) {
						tableData[rowNr][CLOCKMODEL_COLUMN] = getDoc().partitionNames.get(rowNr).partition;
					} else {
						int freePartition = doc.getPartitionNr(cModels.get(0));
						tableData[rowNr][CLOCKMODEL_COLUMN] = getDoc().partitionNames.get(freePartition).partition;
					}
					updateModel(CLOCKMODEL_COLUMN, rowNr);
				}
				
				if (models.size() > 0) {
					// site model is linked, so we need to unlink
					if (doc.getPartitionNr((BEASTInterface) siteModel) != rowNr) {
						tableData[rowNr][SITEMODEL_COLUMN] = getDoc().partitionNames.get(rowNr).partition;
					} else {
						int freePartition = doc.getPartitionNr(models.get(0));
						tableData[rowNr][SITEMODEL_COLUMN] = getDoc().partitionNames.get(freePartition).partition;
					}
					updateModel(SITEMODEL_COLUMN, rowNr);
				}
				
				if (tModels.size() > 0) {
					// tree is linked, so we need to unlink
					if (doc.getPartitionNr((BEASTInterface) tree) != rowNr) {
						tableData[rowNr][TREE_COLUMN] = getDoc().partitionNames.get(rowNr).partition;
					} else {
						int freePartition = doc.getPartitionNr(tModels.get(0));
						tableData[rowNr][TREE_COLUMN] = getDoc().partitionNames.get(freePartition).partition;
					}
					updateModel(TREE_COLUMN, rowNr);
				}
				getDoc().delAlignmentWithSubnet(alignments.get(rowNr));
				alignments.remove(rowNr);
			    // remove deleted likelihood from likelihoods array
				GenericTreeLikelihood[] tmp = new GenericTreeLikelihood[likelihoods.length - 1];
				int k = 0;
				for (int j = 0; j < likelihoods.length; j++) {
					if (j != rowNr) {
						tmp[k] = likelihoods[j];
						k++;
					}
				}
				likelihoods = tmp;
				partitionCount--;
			} catch (Exception e) {
				Alert.showMessageDialog(null, "Deletion failed: " + e.getMessage());
				e.printStackTrace();
			}
		}
		MRCAPriorInputEditor.customConnector(doc);
		refreshPanel();
	} // delItem

	@Override
    public void refreshPanel() {
		initTableData();
    }
	
	void replaceItem() {
		List<Integer> selected = getTableRowSelection();
		if (selected.size() != 1) {
			// don't know how to replace multiple alignments at the same time
			// should never get here (button is disabled)
			return;
		}
		Alignment alignment = alignments.get(selected.get(0));
		replaceItem(alignment);
	}
	
	private void replaceItem(Alignment alignment) {
		BeautiAlignmentProvider provider = new BeautiAlignmentProvider();
		List<BEASTInterface> list = provider.getAlignments(doc);
		List<Alignment> alignments = new ArrayList<>();
		for (BEASTInterface o : list) {
			if (o instanceof Alignment) {
				alignments.add((Alignment) o);
			}
		}
		Alignment replacement = null;
		if (alignments.size() > 1) {
			ComboBox<Alignment> jcb = new ComboBox<Alignment>();
			jcb.getItems().addAll(alignments.toArray(new Alignment[]{}));
			Alert.showMessageDialog( null, jcb, "Select a replacement alignment", Alert.QUESTION_MESSAGE);
			replacement = (Alignment) jcb.getValue();
		} else if (alignments.size() == 1) {
			replacement = alignments.get(0);
		}
		if (replacement != null) {
			if (!replacement.getDataType().getClass().getName().equals(alignment.getDataType().getClass().getName())) {
				Alert.showMessageDialog(null, "Data types do not match, so alignment cannot be replaced: " + 
						replacement.getID() + " " + replacement.getDataType().getClass().getName() + " != " + 
						alignment.getID() + " " + alignment.getDataType().getClass().getName());
				return;
			}
			// replace alignment
			Set<BEASTInterface> outputs = new LinkedHashSet<>();
			outputs.addAll(alignment.getOutputs());
			for (BEASTInterface o : outputs) {
				for (Input<?> input : o.listInputs()) {
					if (input.get() == alignment) {
						input.setValue(replacement, o);
						replacement.getOutputs().add(o);
					} else if (input.get() instanceof List) {
						@SuppressWarnings("rawtypes")
						List inputlist = (List) input.get();
						int i = inputlist.indexOf(alignment);
						if (i >= 0) {
							inputlist.set(i, replacement);
							replacement.getOutputs().add(o);
						}
					}
				}
			}
			int i = doc.alignments.indexOf(alignment);
			doc.alignments.set(i, replacement);
			refreshPanel();
		}
	} // replaceItem
	
	void splitItem() {
		List<Integer> selected = getTableRowSelection();
		if (selected.size() == 0) {
			Alert.showMessageDialog(this, "Select partitions to split, before hitting the split button");
			return;
		}
		String[] options = { "{1,2} + 3", "{1,2} + 3 frame 2", "{1,2} + 3 frame 3", "1 + 2 + 3", "1 + 2 + 3 frame 2", "1 + 2 + 3 frame 3"};

		String option = (String)Alert.showInputDialog(null, "Split selected alignments into partitions", "Option",
		                    Alert.WARNING_MESSAGE, null, options, "1 + 2 + 3");
		if (option == null) {
			return;
		}
		
		String[] filters = null;
		String[] ids = null;
		if (option.equals(options[0])) {
			filters = new String[] { "1::3,2::3", "3::3" };
			ids = new String[] { "_1,2", "_3" };
		} else if (option.equals(options[1])) {
			filters = new String[] { "1::3,3::3", "2::3" };
			ids = new String[] { "_1,2", "_3" };
		} else if (option.equals(options[2])) {
			filters = new String[] { "2::3,3::3", "1::3" };
			ids = new String[] { "_1,2", "_3" };
		} else if (option.equals(options[3])) {
			filters = new String[] { "1::3", "2::3", "3::3" };
			ids = new String[] { "_1", "_2", "_3" };
		} else if (option.equals(options[4])) {
			filters = new String[] { "2::3", "3::3", "1::3" };
			ids = new String[] { "_1", "_2", "_3" };
		} else if (option.equals(options[5])) {
			filters = new String[] { "3::3", "1::3", "2::3" };
			ids = new String[] { "_1", "_2", "_3" };
		} else {
			return;
		}

		for (int i = selected.size() - 1; i >= 0; i--) {
			int rowNr = selected.get(i);
			Alignment alignment = alignments.remove(rowNr);
			getDoc().delAlignmentWithSubnet(alignment);
			try {
				List<Alignment> newAlignments = new ArrayList<>();
				for (int j = 0; j < filters.length; j++) {
					FilteredAlignment f = new FilteredAlignment();
					f.initByName("data", alignment, "filter", filters[j], "dataType", alignment.dataTypeInput.get());
					f.setID(alignment.getID() + ids[j]);
					getDoc().addAlignmentWithSubnet(f, getDoc().beautiConfig.partitionTemplate.get());
					newAlignments.add(f);
				}
				alignments.addAll(newAlignments);
				partitionCount = alignments.size();
				tableData = null; 
				initTableData();
				if (newAlignments.size() == 2) {
					link(TREE_COLUMN, alignments.size() - 1, alignments.size() - 2);
				} else {
					link(TREE_COLUMN, alignments.size() - 2, alignments.size() - 3);
					tableData = null; 
					initTableData();			
					link(TREE_COLUMN, alignments.size() - 1, alignments.size() - 2);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		refreshPanel();
	} // splitItem

	/** enable/disable buttons, etc **/
	void updateStatus() {
		boolean status = (alignments.size() > 1);
		if (alignments.size() >= 2 && getTableRowSelection().size() == 0) {
			status = false;
		}
		for (Button button : linkButtons) {
			button.setDisable(!status);
		}
		for (Button button : unlinkButtons) {
			button.setDisable(!status);
		}
		status = (getTableRowSelection().size() > 0);
		splitButton.setDisable(!status);
		delButton.setDisable(!status);
		replaceButton.setDisable(getTableRowSelection().size() != 1);
	}
	
} // class AlignmentListInputEditor

