package beastfx.app.beauti;

import java.awt.Dimension;
import java.awt.Font;
import java.util.Arrays;
import java.util.List;

import javax.swing.Box;

import javafx.geometry.Dimension2D;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;

import javax.swing.JPanel;
import javax.swing.text.StyledEditorKit.FontSizeAction;

import beastfx.app.inputeditor.BEASTObjectDialog;
import beastfx.app.inputeditor.BeautiDoc;
import beastfx.app.inputeditor.BeautiSubTemplate;
import beastfx.app.inputeditor.InputEditor;
import beastfx.app.inputeditor.InputEditor.Base;
import beastfx.app.inputeditor.InputEditor.ExpandOption;
import beast.base.core.BEASTInterface;
import beast.base.core.Input;
import beast.base.inference.distribution.Prior;
import beast.base.inference.parameter.IntegerParameter;
import beast.base.inference.parameter.RealParameter;
import beast.base.parser.PartitionContext;

public class PriorInputEditor extends InputEditor.Base {
	private static final long serialVersionUID = 1L;

	public PriorInputEditor(BeautiDoc doc) {
		super(doc);
	}

	public PriorInputEditor() {
		super();
	}

	@Override
	public Class<?> type() {
		return Prior.class;
	}

	@Override
	public void init(Input<?> input, BEASTInterface beastObject, int listItemNr, ExpandOption isExpandOption, boolean addButtons) {
        m_bAddButtons = addButtons;
        m_input = input;
        m_beastObject = beastObject;
        this.itemNr= listItemNr;
		
        HBox itemBox = new HBox();

        Prior prior = (Prior) beastObject;
        String text = prior.getParameterName();
        Label label = new Label(text);
        Font font = label.getFont();
        Dimension2D size = new Dimension2D(font.getSize() * 200 / 13, font.getSize() * 25/13);
        label.setMinSize(size.getWidth(), size.getHeight());
        label.setPrefSize(size.getWidth(), size.getHeight());
        itemBox.getChildren().add(label);

        List<BeautiSubTemplate> availableBEASTObjects = doc.getInputEditorFactory().getAvailableTemplates(prior.distInput, prior, null, doc);
        ComboBox<BeautiSubTemplate> comboBox = new ComboBox<BeautiSubTemplate>(availableBEASTObjects.toArray(new BeautiSubTemplate[]{}));
        comboBox.setId(text+".distr");

        String id = prior.distInput.get().getID();
        //Log.warning.println("id=" + id);
        id = id.substring(0, id.indexOf('.'));
        for (BeautiSubTemplate template : availableBEASTObjects) {
            if (template.classInput.get() != null && template.shortClassName.equals(id)) {
                comboBox.setValue(template);
            }
        }
        comboBox.setOnAction(e -> {
            @SuppressWarnings("unchecked")
			ComboBox<BeautiSubTemplate> comboBox1 = (ComboBox<BeautiSubTemplate>) e.getSource();

            List<?> list = (List<?>) m_input.get();

            BeautiSubTemplate template = (BeautiSubTemplate) comboBox1.getValue();
            //String id = ((BEASTObject) list.get(item)).getID();
            //String partition = BeautiDoc.parsePartition(id);
            PartitionContext context = doc.getContextFor((BEASTInterface) list.get(itemNr));
            Prior prior1 = (Prior) list.get(itemNr);
            try {
                template.createSubNet(context, prior1, prior1.distInput, true);
            } catch (Exception e1) {
                e1.printStackTrace();
            }

            sync();
            refreshPanel();
        });
        JPanel panel = new JPanel();
        panel.add(comboBox);
        panel.setMaxSize(size);
        itemBox.add(panel);
        
        if (prior.m_x.get() instanceof RealParameter) {
            // add range button for real parameters
            RealParameter p = (RealParameter) prior.m_x.get();
            Button rangeButton = new Button(paramToString(p));
            rangeButton.setOnAction(e -> {
                Button rangeButton1 = (Button) e.getSource();

                List<?> list = (List<?>) m_input.get();
                Prior prior1 = (Prior) list.get(itemNr);
                RealParameter p1 = (RealParameter) prior1.m_x.get();
                BEASTObjectDialog dlg = new BEASTObjectDialog(p1, RealParameter.class, doc);
                if (dlg.showDialog()) {
                    dlg.accept(p1, doc);
                    rangeButton1.setText(paramToString(p1));
                    refreshPanel();
                }
            });
            itemBox.getChildren().add(new Separator());
            itemBox.getChildren().add(rangeButton);
        } else if (prior.m_x.get() instanceof IntegerParameter) {
            // add range button for real parameters
            IntegerParameter p = (IntegerParameter) prior.m_x.get();
            Button rangeButton = new Button(paramToString(p));
            rangeButton.setOnAction(e -> {
                Button rangeButton1 = (Button) e.getSource();

                List<?> list = (List<?>) m_input.get();
                Prior prior1 = (Prior) list.get(itemNr);
                IntegerParameter p1 = (IntegerParameter) prior1.m_x.get();
                BEASTObjectDialog dlg = new BEASTObjectDialog(p1, IntegerParameter.class, doc);
                if (dlg.showDialog()) {
                    dlg.accept(p1, doc);
                    rangeButton1.setText(paramToString(p1));
                    refreshPanel();
                }
            });
            itemBox.getChildren().add(new Separator());
            itemBox.getChildren().add(rangeButton);
        }
        int fontsize = comboBox.getFont().getSize();
        comboBox.setMaxSize(1024 * fontsize / 13, 24 * fontsize / 13);

        String tipText = getDoc().tipTextMap.get(beastObject.getID());
        //System.out.println(beastObject.getID());
        if (tipText != null) {
            Label tipTextLabel = new Label(" " + tipText);
            itemBox.getChildren().add(tipTextLabel);
        }
        itemBox.getChildren().add(new Separator());

        pane.getChildren().add(itemBox);
	}

    String paramToString(RealParameter p) {
        Double lower = p.lowerValueInput.get();
        Double upper = p.upperValueInput.get();
        return "initial = " + Arrays.toString(p.valuesInput.get().toArray()) +
                " [" + (lower == null ? "-\u221E" : lower + "") +
                "," + (upper == null ? "\u221E" : upper + "") + "]";
    }

    String paramToString(IntegerParameter p) {
        Integer lower = p.lowerValueInput.get();
        Integer upper = p.upperValueInput.get();
        return "initial = " + Arrays.toString(p.valuesInput.get().toArray()) +
                " [" + (lower == null ? "-\u221E" : lower + "") +
                "," + (upper == null ? "\u221E" : upper + "") + "]";
    }
}
