package beastfx.app.beauti;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.Box;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import beastfx.app.inputeditor.BEASTObjectInputEditor;
import beastfx.app.inputeditor.BeautiDoc;
import beastfx.app.inputeditor.InputEditor;
import beastfx.app.inputeditor.ListInputEditor;
import beastfx.app.inputeditor.InputEditor.ButtonStatus;
import beastfx.app.inputeditor.InputEditor.ExpandOption;
import beast.base.core.BEASTInterface;
import beast.base.core.Input;
import beast.base.inference.Operator;
import beast.base.inference.StateNode;



public class OperatorListInputEditor extends ListInputEditor {
    List<TextField> textFields = new ArrayList<>();
    List<Operator> operators = new ArrayList<>();

	public OperatorListInputEditor(BeautiDoc doc) {
		super(doc);
	}

    public OperatorListInputEditor() {
		super();
	}

	@Override
    public Class<?> type() {
        return List.class;
    }

    @Override
    public Class<?> baseType() {
        return Operator.class;
    }

    @Override
    public void init(Input<?> input, BEASTInterface beastObject, int itemNr, ExpandOption isExpandOption, boolean addButtons) {
    	HBox box = new HBox();
    	pane = new BorderPane();
    	//box.getChildren().add(new Separator());
    	box.getChildren().add(new Label("Operator"));
    	//box.getChildren().add(new Separator());
    	box.getChildren().add(new Label("Weight"));
    	//box.getChildren().add(new Separator());
    	((BorderPane)pane).setTop(box);
    	

    	m_buttonStatus = ButtonStatus.NONE;
    	super.init(input, beastObject, itemNr, isExpandOption, addButtons);
    	
    	BEASTObjectInputEditor osEditor = new BEASTObjectInputEditor(doc);
    	osEditor.init(((BEASTInterface) doc.mcmc.get()).getInput("operatorschedule"), (BEASTInterface) doc.mcmc.get(), -1, isExpandOption, addButtons);
    	((BorderPane)pane).setBottom(osEditor);
    	//getChildren().add(pane); <- already done by super class
    }
    
    @Override
    protected InputEditor addPluginItem(Pane itemBox, BEASTInterface beastObject) {
        Operator operator = (Operator) beastObject;

        TextField entry = new TextField(" " + getLabel(operator));
        entry.setMinSize(200, 16);
        //entry.setMaxSize(new Dimension(200, 20));
        m_entries.add(entry);
        entry.setBackground(getBackground());
        entry.setBorder(null);
        itemBox.getChildren().add(new Separator());//Box.createRigidArea(new Dimension(5, 1)));
        itemBox.getChildren().add(entry);
        entry.setEditable(false);

//        Label label = new Label(getLabel(operator));
//        label.setBackground(Color.WHITE);
//        m_labels.add(label);
//        m_entries.add(null);
//        itemBox.add(label);


        itemBox.getChildren().add(new Separator());
        TextField weightEntry = new TextField();
        weightEntry.setTooltip(new Tooltip(operator.m_pWeight.getHTMLTipText()));
        weightEntry.setText(operator.m_pWeight.get() + "");
        weightEntry.setOnKeyReleased(e->new OperatorDocumentListener(operator, weightEntry));
        // weightEntry.getDocument().addDocumentListener(new OperatorDocumentListener(operator, weightEntry));
        Dimension size = new Dimension(50, 25);
        weightEntry.setMinSize(size.getWidth(), size.getHeight());
        weightEntry.setPrefSize(size.getWidth(), size.getHeight());
        //int fontsize = weightEntry.getFont().getSize();
        //weightEntry.setMaxSize(new Dimension(50 * fontsize/13, 50 * fontsize/13));
        itemBox.getChildren().add(weightEntry);

        return this;
    }


    /**
     * class to set weight-input on an operator when it changes in the list *
     */
    class OperatorDocumentListener implements DocumentListener {
        Operator m_operator;
        TextField m_weightEntry;

        OperatorDocumentListener(Operator operator, TextField weightEntry) {
            m_operator = operator;
            m_weightEntry = weightEntry;
            textFields.add(weightEntry);
            operators.add(operator);
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            processEntry();
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            processEntry();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            processEntry();
        }

        void processEntry() {
            try {
                Double weight = Double.parseDouble(m_weightEntry.getText());
                m_operator.m_pWeight.setValue(weight, m_operator);
            } catch (Exception e) {
                // ignore
            }
            validateInput();
        }
    }

    ;

    @Override
    public void updateState() {
        super.updateState();
        for (int i = 0; i < textFields.size(); i++) {
            textFields.get(i).setText(operators.get(i).m_pWeight.get() + "");
            //m_labels.get(i).setText(getLabel(m_operators.get(i)));
            m_entries.get(i).setText(getLabel(operators.get(i)));
        }
    }

    String getLabel(Operator operator) {
        String name = operator.getClass().getName();
        name = name.substring(name.lastIndexOf('.') + 1);
        name = name.replaceAll("Operator", "");
        if (name.matches(".*[A-Z].*")) {
            name = name.replaceAll("(.)([A-Z])", "$1 $2");
        }
        name += ": ";
        try {
            for (BEASTInterface beastObject2 : operator.listActiveBEASTObjects()) {
                if (beastObject2 instanceof StateNode && ((StateNode) beastObject2).isEstimatedInput.get()) {
                    name += beastObject2.getID() + " ";
                }
                // issue https://github.com/CompEvol/beast2/issues/661
                if (name.length() > 100) {
                	name += "... ";
                	break;
                }
            }
        } catch (Exception e) {
            // ignore
        }
        String tipText = getDoc().tipTextMap.get(operator.getID());
        if (tipText != null) {
            name += " " + tipText;
        }
        return name;
    }
} // OperatorListInputEditor
