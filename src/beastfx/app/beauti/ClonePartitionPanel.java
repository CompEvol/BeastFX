package beastfx.app.beauti;

import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javax.swing.JPanel;


public class ClonePartitionPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    final BeautiPanel beautiPanel;
    final ComboBox<Object> cloneFromComboBox;
    final Button okButton = new Button("OK");

	public ClonePartitionPanel(BeautiPanel beautiPanel) {
        this.beautiPanel = beautiPanel;

        DefaultListModel<String> listModel = beautiPanel.listModel;
        Object[] models = new Object[listModel.getSize()];
        for(int i=0; i < listModel.getSize(); i++){
            models[i] = listModel.getElementAt(i);
        }

        cloneFromComboBox = new ComboBox<>(models);
        // has to be editable
        cloneFromComboBox.setEditable(true);
        // change the editor's document
        new S11InitialSelection(cloneFromComboBox);

        init();
    }


    public void init() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JPanel jPanel = new JPanel(new FlowLayout());

        Label label = new Label("Clone from");
        jPanel.add(label);

        cloneFromComboBox.setMaximumRowCount(10);
        jPanel.add(cloneFromComboBox);

        add(Box.createRigidArea(new Dimension(0, 10)));
        add(jPanel);
        add(Box.createVerticalGlue());
        add(Box.createVerticalStrut(5));

        okButton.setName("ok");
        okButton.setToolTipText("Click to clone configuration from the above selected partition " +
                "into all selected partitions on the left.");
        okButton.addActionListener(e -> {
                clonePartitions();
            });
        add(okButton);

    } // init

    protected void clonePartitions() {
        String sourceId = cloneFromComboBox.getSelectedItem().toString();

        for (Object targetId : beautiPanel.listOfPartitions.getSelectedValuesList()) {
             beautiPanel.cloneFrom(sourceId, targetId.toString());
        }
    }
}
