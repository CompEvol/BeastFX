package beastfx.app.inputeditor;

import javax.swing.Box;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Separator;
import beast.base.core.BEASTInterface;
import beast.base.core.Input;
import beast.base.core.Log;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;



public class BooleanInputEditor extends InputEditor.Base {
    public BooleanInputEditor(BeautiDoc doc) {
		super(doc);
	}
    
	public BooleanInputEditor() {
		super();
	}

	private static final long serialVersionUID = 1L;
    CheckBox m_entry;


    @Override
    public Class<?> type() {
        return Boolean.class;
    }

    /**
     * create input editor containing a check box *
     */
    @Override
    public void init(Input<?> input, BEASTInterface beastObject, int itemNr, ExpandOption isExpandOption, boolean addButtons) {
        m_bAddButtons = addButtons;
        m_beastObject = beastObject;
        m_input = input;
		this.itemNr = itemNr;
        m_entry = new CheckBox(formatName(m_input.getName()));
        if (input.get() != null) {
            m_entry.setSelected((Boolean) input.get());
        }
        m_entry.setTooltip(new Tooltip(input.getHTMLTipText()));
        m_entry.setOnAction(e -> {
                try {
                	setValue(m_entry.isSelected());
                	refreshPanel();
                    //validateInput();
                    //m_input.setValue(m_entry.isSelected(), m_beastObject);
                } catch (Exception ex) {
                    Log.err.println("BooleanInputEditor " + ex.getMessage());
                }
            });
        pane = new HBox();
        pane.getChildren().add(m_entry);
        //getChildren().add(Box.createHorizontalGlue());
        pane.getChildren().add(new Separator());
        getChildren().add(pane);
    } // c'tor

} // class BooleanInputEditor
