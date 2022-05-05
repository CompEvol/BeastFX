package beastfx.app.inputeditor;



// import java.awt.Dimension;
import java.util.List;

//import javax.swing.Box;
//import javafx.scene.control.ComboBox;
//import javax.swing.JComponent;
//import javafx.scene.control.Label;
//import beastfx.app.util.Alert;
//import javax.swing.border.EtchedBorder;

import beast.base.core.BEASTInterface;
import beast.base.core.Input;
import beastfx.app.util.Alert;
import beastfx.app.util.FXUtils;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class BEASTObjectInputEditor extends InputEditor.Base {
    ComboBox<Object> m_selectBEASTObjectBox;
    SmallButton m_editBEASTObjectButton;

    BEASTObjectInputEditor _this;

    public BEASTObjectInputEditor() {    
    	_this = this;
    }
    
    public BEASTObjectInputEditor(BeautiDoc doc) {
        super(doc);
        _this = this;
    }

    @Override
    public Class<?> type() {
        return BEASTInterface.class;
    }

    /**
     * construct an editor consisting of
     * o a label
     * o a combo box for selecting another plug-in
     * o a button for editing the plug-in
     * o validation label -- optional, if input is not valid
     */
    @Override
    public void init(Input<?> input, BEASTInterface beastObject, int itemNr, ExpandOption isExpandOption, boolean addButtons) {
    	//box.setAlignmentY(LEFT_ALIGNMENT);
    	
        m_bAddButtons = addButtons;
        m_input = input;
        m_beastObject = beastObject;
		this.itemNr = itemNr;
    	pane = new HBox();

        if (isExpandOption == ExpandOption.FALSE) {
            simpleInit(input, beastObject);
        } else {
            expandedInit(input, beastObject);
        }
        getChildren().add(pane);
    } // init

    /**
     * add combobox with available beastObjects
     * a button to edit that beastObject +
     * a validation icon
     * *
     */
    void simpleInit(Input<?> input, BEASTInterface beastObject) {

        addInputLabel();

        addComboBox(pane, input, beastObject);

        if (m_bAddButtons) {
            if (BEASTObjectPanel.countInputs(m_input.get(), doc) > 0) {
                m_editBEASTObjectButton = new SmallButton("e", true);
                if (input.get() == null) {
                    m_editBEASTObjectButton.setDisable(true);
                }
                m_editBEASTObjectButton.setTooltip(new Tooltip("Edit " + m_inputLabel.getText()));

                m_editBEASTObjectButton.setOnAction(e -> {
                    BEASTObjectDialog dlg = new BEASTObjectDialog((BEASTInterface) m_input.get(), m_input.getType(), doc);
                    if (dlg.showDialog()) {
                        try {
                            dlg.accept((BEASTInterface) m_input.get(), doc);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                    refresh();
                    validateInput();
                    refreshPanel();
                });
                pane.getChildren().add(m_editBEASTObjectButton);
            }
        }
        addValidationLabel();
    } // init

    void refresh() {
    	if (m_selectBEASTObjectBox != null) {
	        String oldID = (String) m_selectBEASTObjectBox.getValue();
	        String id = ((BEASTInterface) m_input.get()).getID();
	        if (!id.equals(oldID)) {
	            m_selectBEASTObjectBox.getItems().add(id);
	            m_selectBEASTObjectBox.setValue(id);
	            m_selectBEASTObjectBox.getItems().remove(oldID);
	        }
    	}
        super.refreshPanel();
//        Component c = this;
//        while (((Component) c).getParent() != null) {
//        	c = ((Component) c).getParent();
//        	if (c instanceof ListSelectionListener) {
//        		((ListSelectionListener) c).valueChanged(null);
//        	}
//        }
    }

    void initSelectPluginBox() {
        List<String> availableBEASTObjects = doc.getInputEditorFactory().getAvailablePlugins(m_input, m_beastObject, null, doc);
        if (availableBEASTObjects.size() > 0) {
            availableBEASTObjects.add(NO_VALUE);
            for (int i = 0; i < availableBEASTObjects.size(); i++) {
                String beastObjectName = availableBEASTObjects.get(i);
                if (beastObjectName.startsWith("new ")) {
                    beastObjectName = beastObjectName.substring(beastObjectName.lastIndexOf('.'));
                    availableBEASTObjects.set(i, beastObjectName);
                }

            }
            m_selectBEASTObjectBox.getItems().remove(0, m_selectBEASTObjectBox.getItems().size()-1);
            for (String str : availableBEASTObjects.toArray(new String[0])) {
                m_selectBEASTObjectBox.getItems().add(str);
            }
            m_selectBEASTObjectBox.setValue(m_beastObject.getID());
        }
    }

    VBox m_expansionBox = null;

    void expandedInit(Input<?> input, BEASTInterface beastObject) {
        addInputLabel();
        VBox box = new VBox();
        // add horizontal box with combobox of BEASTObjects to select from
        HBox combobox = new HBox();
        addComboBox(combobox, input, beastObject);
        box.getChildren().add(combobox);

        doc.getInputEditorFactory().addInputs(box, (BEASTInterface) input.get(), this, this, doc);

        // box.setBorder(new EtchedBorder());
        //box.setBorder(BorderFactory.createLineBorder(Color.BLUE));
        
        pane.getChildren().add(box);
        m_expansionBox = box;
    } // expandedInit


    /**
     * add combobox with BEASTObjects to choose from
     * On choosing a new value, create beastObject (if is not already an object)
     * Furthermore, if expanded, update expanded inputs
     */
    protected void addComboBox(Pane box, Input<?> input, BEASTInterface beastObject0) {
    	if (itemNr >= 0) {
    		box.getChildren().add(new Label(beastObject0.getID()));
    		// box.add(Box.createGlue());
    		return;
    	}
    	
        List<BeautiSubTemplate> availableTemplates = doc.getInputEditorFactory().getAvailableTemplates(m_input, m_beastObject, null, doc);
        if (availableTemplates.size() > 0) {
//        	if (m_input.getRule() != Validate.REQUIRED || beastObject == null) {
//        		availableBEASTObjects.add(NO_VALUE);
//        	}
//        	for (int i = 0; i < availableBEASTObjects.size(); i++) {
//        		String beastObjectName = availableBEASTObjects.get(i);
//        		if (beastObjectName.startsWith("new ")) {
//        			beastObjectName = beastObjectName.substring(beastObjectName.lastIndexOf('.'));
//        			availableBEASTObjects.set(i, beastObjectName);
//        		}
//
//        	}
            m_selectBEASTObjectBox = new ComboBox<>();
            m_selectBEASTObjectBox.getItems().addAll(availableTemplates.toArray());
            m_selectBEASTObjectBox.setId(input.getName());

            Object o = input.get();
            if (itemNr >= 0) {
            	o = ((List<?>)o).get(itemNr);
            }
            String id2;
            if (o == null) {
                id2 = beastObject0.getID();
            } else if (o instanceof BEASTInterface) {
                id2 = ((BEASTInterface) o).getID();
            } else {
            	id2 = input.getName();
            }
            if (id2.indexOf('.')>=0) {
            	id2 = id2.substring(0, id2.indexOf('.'));
            }
            for (BeautiSubTemplate template : availableTemplates) {
                if (template.matchesName(id2)) {
                    m_selectBEASTObjectBox.setValue(template);
                }
            }

            m_selectBEASTObjectBox.setOnAction(e -> {

//            	SwingUtilities.invokeLater(new Runnable() {
//					
//					@Override
//					public void run() {

                // get a handle of the selected beastObject
            	BeautiSubTemplate selected = (BeautiSubTemplate) m_selectBEASTObjectBox.getValue();
                BEASTInterface beastObject = (BEASTInterface) m_input.get();
                String id = beastObject==null ? "no_id" : beastObject.getID();
                String partition = id.indexOf('.') >= 0 ? 
                		id.substring(id.indexOf('.') + 1) : "";
                if (partition.indexOf(':') >= 0) {
                	partition = id.substring(id.indexOf(':') + 1);
                }
                //String newID = selected.getMainID().replaceAll("\\$\\(n\\)", partition);

                if (selected.equals(NO_VALUE)) {
                    beastObject = null;
//                } else if (PluginPanel.g_plugins.containsKey(newID)) {
//                	beastObject = PluginPanel.g_plugins.get(newID);
                } else {
                    try {
                        beastObject = selected.createSubNet(doc.getContextFor(beastObject), m_beastObject, m_input, true);
                        //PluginPanel.addPluginToMap(beastObject);
                        // tricky: try to connect up new inputs with old inputs of existing name
//                        beastObject oldPlugin = (beastObject) m_input.get();
//                        for (Input<?> oldInput: oldPlugin.listInputs()) {
//                        	String name = oldInput.getName();
//                        	try {
//                        		Input<?> newInput = beastObject.getInput(name);
//                        		if (newInput.get() instanceof List) {
//                        			List<?> values = (List<?>) oldInput.get();
//                        			for (Object value: values) {
//                            			newInput.setValue(value, beastObject);
//                        			}
//                        		} else {
//                        			newInput.setValue(oldInput.get(), beastObject);
//                        		}
//                        	} catch (Exception ex) {
//								// ignore
//							}
//                        }
                    } catch (Exception ex) {
                        Alert.showMessageDialog(null, "Could not select beastObject: " +
                                ex.getClass().getName() + " " +
                                ex.getMessage()
                        );
                    }
                }


                try {
                    if (beastObject == null) {
                        m_selectBEASTObjectBox.setValue(NO_VALUE);
                        // is this input expanded?
                        if (m_expansionBox != null) {
                            // remove items from Expansion Box, if any
                            for (int i = 1; i < m_expansionBox.getChildren().size(); i++) {
                                m_expansionBox.getChildren().remove(i);
                            }
                        } else { // not expanded
                            if (m_bAddButtons && m_editBEASTObjectButton != null) {
                                m_editBEASTObjectButton.setDisable(true);
                            }
                        }
                    } else {
                        if (!m_input.canSetValue(beastObject, m_beastObject)) {
                            throw new IllegalArgumentException("Cannot set input to this value");
                        }
//                    	// get handle on ID of the beastObject, and add to combobox if necessary
//                        String id = beastObject.getID();
//                        // TODO RRB: have to remove ID first, then add it
//                        // The addition is necessary to make the items in the expansionBox scale and show up
//                        // Is there another way?
//                        m_selectPluginBox.removeItem(id);
//                        m_selectPluginBox.addItem(id);
//                        m_selectPluginBox.setSelectedItem(id);
                        id = beastObject.getID();
                        if (id.indexOf('.') != -1) {
                        	id = id.substring(0,  id.indexOf('.'));
                        }
                         for (int i = 0; i < m_selectBEASTObjectBox.getItems().size(); i++) {
                            BeautiSubTemplate template = (BeautiSubTemplate) m_selectBEASTObjectBox.getItems().get(i);
                            if (template.getMainID().replaceAll(".\\$\\(n\\)", "").equals(id) ||
                            		template.getMainID().replaceAll(".s:\\$\\(n\\)", "").equals(id) || 
                            		template.getMainID().replaceAll(".c:\\$\\(n\\)", "").equals(id) || 
                            		template.getMainID().replaceAll(".t:\\$\\(n\\)", "").equals(id)) {
                                m_selectBEASTObjectBox.setValue(template);
                            }
                        }
                    }

                    setValue(beastObject);
                    //m_input.setValue(beastObject, m_beastObject);

                    if (m_expansionBox != null) {
                        // remove items from Expansion Box
                        while (m_expansionBox.getChildren().size()>1) {
                            m_expansionBox.getChildren().remove(m_expansionBox.getChildren().size()-1);
                        }
                        // add new items to Expansion Box
                        if (beastObject != null) {
                        	doc.getInputEditorFactory().addInputs(m_expansionBox, beastObject, _this, _this, doc);
                        }
                    } else {
                        // it is not expanded, enable the edit button
                        if (m_bAddButtons && m_editBEASTObjectButton != null) {
                            m_editBEASTObjectButton.setDisable(false);
                        }
                        validateInput();
                    }
                    sync();
                    refreshPanel();
                } catch (Exception ex) {
                    id = ((BEASTInterface) m_input.get()).getID();
                    m_selectBEASTObjectBox.setValue(id);
                    ex.printStackTrace();
                    Alert.showMessageDialog(null, "Could not change beastObject: " +
                            ex.getClass().getName() + " " +
                            ex.getMessage() 
                    );
                }
            });

            m_selectBEASTObjectBox.setTooltip(new Tooltip(input.getHTMLTipText()));
            //int fontsize = m_selectBEASTObjectBox.getFont().getSize();
            //m_selectBEASTObjectBox.setMaxSize(1024, 200 * fontsize / 13);
            box.getChildren().add(m_selectBEASTObjectBox);
        }
    }

//    protected void addComboBox2(Box box, Input <?> input, BEASTObject beastObject) {
//        List<String> availableBEASTObjects = PluginPanel.getAvailablePlugins(m_input, m_beastObject, null);
//        if (availableBEASTObjects.size() > 0) {
//        	if (m_input.getRule() != Validate.REQUIRED || beastObject == null) {
//        		availableBEASTObjects.add(NO_VALUE);
//        	}
//        	for (int i = 0; i < availableBEASTObjects.size(); i++) {
//        		String beastObjectName = availableBEASTObjects.get(i);
//        		if (beastObjectName.startsWith("new ")) {
//        			beastObjectName = beastObjectName.substring(beastObjectName.lastIndexOf('.'));
//        			availableBEASTObjects.set(i, beastObjectName);
//        		}
//
//        	}
//            m_selectPluginBox = new ComboBox(availableBEASTObjects.toArray(new String[0]));
//            String selectString = NO_VALUE;
//            if (input.get() != null) {
//                selectString = ((BEASTObject) input.get()).getID();
//            }
//            m_selectPluginBox.setSelectedItem(selectString);
//
//            m_selectPluginBox.setOnAction(new ActionListener() {
//                // implements ActionListener
//                public void actionPerformed(ActionEvent e) {
//                	
//                	// get a handle of the selected beastObject
//                    String selected = (String) m_selectPluginBox.getValue();
//                    BEASTObject beastObject = (BEASTObject) m_input.get();
//                    if (selected.equals(NO_VALUE)) {
//                        beastObject = null;
//                    } else if (!selected.startsWith(".")) {
//                        beastObject = PluginPanel.g_plugins.get(selected);
//                    } else {
//                        List<String> availableBEASTObjects = PluginPanel.getAvailablePlugins(m_input, m_beastObject, null);
//                        int i = 0;                     
//                        while (!availableBEASTObjects.get(i).matches(".*\\"+selected+"$")) {
//                        	i++;
//                        }
//                    	selected = availableBEASTObjects.get(i);                       
//                        /* create new beastObject */
//                        try {
//                            beastObject = (BEASTObject) Class.forName(selected.substring(4)).newInstance();
//                            PluginPanel.addPluginToMap(beastObject);
//                            // tricky: try to connect up new inputs with old inputs of existing name
//                            BEASTObject oldPlugin = (BEASTObject) m_input.get();
//                            for (Input<?> oldInput: oldPlugin.listInputs()) {
//                            	String name = oldInput.getName();
//                            	try {
//                            		Input<?> newInput = beastObject.getInput(name);
//                            		if (newInput.get() instanceof List) {
//                            			List<?> values = (List<?>) oldInput.get();
//                            			for (Object value: values) {
//                                			newInput.setValue(value, beastObject);
//                            			}
//                            		} else {
//                            			newInput.setValue(oldInput.get(), beastObject);
//                            		}
//                            	} catch (Exception ex) {
//									// ignore
//								}
//                            }
//                            
//                        } catch (Exception ex) {
//                            Alert.showMessageDialog(null, "Could not select beastObject: " +
//                                    ex.getClass().getName() + " " +
//                                    ex.getMessage()
//                            );
//                        }
//                    }
//
//                    
//                    try {
//                        if (beastObject == null) {
//                            m_selectPluginBox.setSelectedItem(NO_VALUE);
//                            // is this input expanded?
//                            if (m_expansionBox != null) {
//                            	// remove items from Expansion Box, if any
//                            	for (int i = 1; i < m_expansionBox.getComponentCount(); i++) {
//                            		m_expansionBox.remove(i);
//                            	}
//                            } else { // not expanded
//                            	if (m_bAddButtons) {
//                            		m_editPluginButton.setEnabled(false);
//                            	}
//                            }
//                        } else {
//                            if (!m_input.canSetValue(beastObject, m_beastObject)) {
//                            	throw new Exception("Cannot set input to this value");
//                            }
//                        	// get handle on ID of the beastObject, and add to combobox if necessary
//                            String id = beastObject.getID();
//                            // TODO RRB: have to remove ID first, then add it
//                            // The addition is necessary to make the items in the expansionBox scale and show up
//                            // Is there another way?
//                            m_selectPluginBox.removeItem(id);
//                            m_selectPluginBox.addItem(id);
//                            m_selectPluginBox.setSelectedItem(id);
//                        }
//                        
//                        m_input.setValue(beastObject, m_beastObject);
//                        
//                        if (m_expansionBox != null) {
//                        	// remove items from Expansion Box
//                        	for (int i = 1; i < m_expansionBox.getComponentCount(); ) {
//                        		m_expansionBox.remove(i);
//                        	}
//                        	// add new items to Expansion Box
//                        	if (beastObject != null) {
//                        		PluginPanel.addInputs(m_expansionBox, beastObject, _this, _this);
//                        	}
//                        } else {
//                        	// it is not expanded, enable the edit button
//                        	if (m_bAddButtons) {
//                        		m_editPluginButton.setEnabled(true);
//                        	}
//                            checkValidation();
//                        }
//                        
//                    } catch (Exception ex) {
//                        String id = ((BEASTObject)m_input.get()).getID();
//                        m_selectPluginBox.setSelectedItem(id);
//                    	//ex.printStackTrace();
//                        Alert.showMessageDialog(null, "Could not change beastObject: " +
//                                ex.getClass().getName() + " " +
//                                ex.getMessage()
//                        );
//                    }
//                }
//            });
//            m_selectPluginBox.setTooltip(new Tooltip(input.getTipText()));
//            m_selectPluginBox.setMaxSize(new Dimension(1024, 20));
//            box.add(m_selectPluginBox);
//        }
//    }

//    String[] getAvailablePlugins() {
//        List<String> beastObjectNames = PackageManager.find(m_input.getType(), "beast");
//        return beastObjectNames.toArray(new String[0]);
//    } // getAvailablePlugins

} // class PluginInputEditor
