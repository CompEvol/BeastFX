package beastfx.app.inputeditor;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;

import javax.swing.JDialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;

import javax.swing.JList;
import beastfx.app.util.Alert;
import javax.swing.JScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import beast.base.evolution.alignment.Taxon;
import beast.base.evolution.alignment.TaxonSet;



public class TaxonSetDialog extends DialogPane {
    private static final long serialVersionUID = 1L;
    public boolean isOK = false;
    public TaxonSet taxonSet;
    String id;
    List<Taxon> _candidates;

    TextField idEntry;

    TextField filterEntry;

    JList<Taxon> listOfTaxonCandidates;
    DefaultListModel<Taxon> listModel1;
    JList<Taxon> listOfTaxonSet;
    DefaultListModel<Taxon> listModel2;


    VBox box;
    BeautiDoc doc;
    
    public TaxonSetDialog(TaxonSet taxonSet, Set<Taxon> candidates, BeautiDoc doc) {
        // initialize state
        this.taxonSet = taxonSet;
        this.doc = doc;
        id = taxonSet.getID();
        // create components
        box = new VBox();
        box.getChildren().add(createIDBox());
        box.getChildren().add(createFilterBox());
        box.getChildren().add(createTaxonSelector());
        box.getChildren().add(new Separator());
        //box.add(createCancelOKButtons());

        // initialise lists
        List<Taxon> taxonset = taxonSet.taxonsetInput.get();
        Comparator<Taxon> comparator = (o1, o2) -> o1.getID().compareTo(o2.getID());
        Collections.sort(taxonset, comparator);
        _candidates = new ArrayList<>();
        _candidates.addAll(candidates);
        Collections.sort(_candidates, comparator);

        for (Taxon taxon : taxonset) {
            listModel2.addElement(taxon);
        }
        for (Taxon taxon : _candidates) {
            listModel1.addElement(taxon);
        }
        for (int i = 0; i < listModel2.size(); i++) {
            listModel1.removeElement(listModel2.get(i));
        }

        getChildren().add(box);
        int size = UIManager.getFont("Label.font").getSize();
        setPrefSize(400 * size / 13, 600 * size / 13);
        //setModal(true);
    } // c'tor
    
    public boolean showDialog() {
        Alert optionPane = new Alert(box,
                Alert.QUESTION_MESSAGE,
                Alert.OK_CANCEL_OPTION,
                null,
                null,
                null);
        optionPane.setBorder(new EmptyBorder(12, 12, 12, 12));

        Frame frame = (doc != null ? doc.getFrame(): Frame.getFrames()[0]);
        final JDialog dialog = optionPane.createDialog(frame, "Taxon set editor");
        dialog.pack();

        dialog.setVisible(true);

        ButtonType result = Alert.CANCEL_OPTION;
        Integer value = (Integer) optionPane.getValue();
        if (value != null && value != -1) {
            result = value;
        }
        isOK =  (result != Alert.CANCEL_OPTION);
        if (isOK) {
            taxonSet.setID(id);
            List<Taxon> taxa = taxonSet.taxonsetInput.get();
            while (taxa.size() > 0) {
                taxa.remove(0);
            }
            for (int i = 0; i < listModel2.size(); i++) {
                taxa.add(listModel2.get(i));
            }
            isOK = true;
            dispose();
        }
        return isOK;
    }

    

    private Pane createFilterBox() {
        HBox box = new HBox();
        Label label = new Label("Filter:");
        box.getChildren().add(label);
        filterEntry = new TextField();
        filterEntry.setPrefColumnCount(17);
        //Dimension size = new Dimension(100, 20);
        //filterEntry.setMinSize(size);
        //filterEntry.setPrefSize(size);
        //filterEntry.setSize(size);
        filterEntry.setTooltip(new Tooltip("Enter regular expression to match taxa"));
        //int fontsize = filterEntry.getFont().getSize();
        //filterEntry.setMaxSize(new Dimension(1024 * fontsize / 13, 50 * fontsize / 13));
        box.getChildren().add(filterEntry);
        box.getChildren().add(new Separator());
        filterEntry.setOnKeyReleased(e -> processEntry());
        
//        filterEntry.getDocument().addDocumentListener(new DocumentListener() {
//            @Override
//            public void removeUpdate(DocumentEvent e) {
//                processEntry();
//            }
//
//            @Override
//            public void insertUpdate(DocumentEvent e) {
//                processEntry();
//            }
//
//            @Override
//            public void changedUpdate(DocumentEvent e) {
//                processEntry();
//            }
//        });
        return box;
    }

    private void processEntry() {
        String filter = ".*" + filterEntry.getText() + ".*";

        listModel1.clear();
        for (Taxon taxon : _candidates) {
            if (taxon.getID().matches(filter)) {
                listModel1.addElement(taxon);
            }
        }
        for (int i = 0; i < listModel2.size(); i++) {
            listModel1.removeElement(listModel2.get(i));
        }
    }

    Pane createIDBox() {
        HBox box = new HBox();
        box.getChildren().add(new Label("Taxon set label:"));
        idEntry = new TextField();
        idEntry.setId("idEntry");
        idEntry.setText(id);
        box.getChildren().add(idEntry);
        idEntry.setOnKeyReleased(e->{id = idEntry.getText();});
//        idEntry.getDocument().addDocumentListener(new DocumentListener() {
//            @Override
//            public void removeUpdate(DocumentEvent e) {
//                id = idEntry.getText();
//            }
//
//            @Override
//            public void insertUpdate(DocumentEvent e) {
//                id = idEntry.getText();
//            }
//
//            @Override
//            public void changedUpdate(DocumentEvent e) {
//                id = idEntry.getText();
//            }
//        });

//        int fontsize = idEntry.getFont().getSize();
//        box.setMaxSize(new Dimension(400 * fontsize / 13, 100 * fontsize / 13));
        return box;
    }
    
    class TaxonCellRenderer extends DefaultListCellRenderer {
		private static final long serialVersionUID = 1L;

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			Label label = (Label)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			label.setText(((Taxon)value).getID());
			return label;
		}
	}
    
    Pane createTaxonSelector() {
        HBox box = new HBox();

        // list of taxa to select from
        listModel1 = new DefaultListModel<>();
        listOfTaxonCandidates = new JList<>(listModel1);
        listOfTaxonCandidates.setId("listOfTaxonCandidates");
        listOfTaxonCandidates.setBorder(BorderFactory.createEtchedBorder());
        listOfTaxonCandidates.setCellRenderer(new TaxonCellRenderer());
        
        ScrollPane scroller = new ScrollPane();
        scroller.setContent(listOfTaxonCandidates);
        box.getChildren().add(scroller);

        // add buttons to select/deselect taxa
        VBox buttonBox = new VBox();
        buttonBox.getChildren().add(new Separator());
        Button selectButton = new Button(">>");
        selectButton.setId(">>");
        selectButton.setOnAction(e -> {
                int[] selected = listOfTaxonCandidates.getSelectedIndices();
                for (int i : selected) {
                    listModel2.addElement(listModel1.get(i));
                }
                for (int i = 0; i < listModel2.size(); i++) {
                    listModel1.removeElement(listModel2.get(i));
                }
            });
        buttonBox.getChildren().add(selectButton);
        Button deselectButton = new Button("<<");
        deselectButton.setId("<<");
        deselectButton.setOnAction(e -> {
                int[] selected = listOfTaxonSet.getSelectedIndices();
                for (int i : selected) {
                    listModel1.addElement(listModel2.get(i));
                }
                for (int i = 0; i < listModel1.size(); i++) {
                    listModel2.removeElement(listModel1.get(i));
                }
            });
        buttonBox.getChildren().add(deselectButton);
        buttonBox.getChildren().add(new Separator());
        box.getChildren().add(buttonBox);

        // list of taxa in taxon set
        listModel2 = new DefaultListModel<>();
        listOfTaxonSet = new JList<>(listModel2);
        listOfTaxonSet.setBorder(BorderFactory.createEtchedBorder());
        listOfTaxonSet.setCellRenderer(new TaxonCellRenderer());

        ScrollPane scroller2 = new ScrollPane();
        scroller2.setConent(listOfTaxonSet);
        box.getChildren().add(scroller2);
        return box;
    } // createTaxonSelector

    Pane createCancelOKButtons() {
        HBox cancelOkBox = new HBox();
        cancelOkBox.setBorder(new EtchedBorder());
        Button okButton = new Button("Ok");
        okButton.setId("OK");
        okButton.setOnAction(e -> {
                taxonSet.setID(id);
                List<Taxon> taxa = taxonSet.taxonsetInput.get();
                while (taxa.size() > 0) {
                    taxa.remove(0);
                }
                for (int i = 0; i < listModel2.size(); i++) {
                    taxa.add(listModel2.get(i));
                }
                isOK = true;
                dispose();
            });
        Button cancelButton = new Button("Cancel");
        cancelButton.setId("Cancel");
        cancelButton.setOnAction(e -> {
                dispose();
            });
        cancelOkBox.getChildren().add(new Separator());
        cancelOkBox.getChildren().add(okButton);
        cancelOkBox.getChildren().add(new Separator());
        cancelOkBox.getChildren().add(cancelButton);
        cancelOkBox.getChildren().add(new Separator());
        return cancelOkBox;
    } // createCancelOKButtons


} // class TaxonSetDialog
