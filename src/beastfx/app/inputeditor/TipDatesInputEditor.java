package beastfx.app.inputeditor;


import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import beast.base.core.BEASTInterface;
import beast.base.core.Input;
import beast.base.core.Log;
import beast.base.evolution.alignment.Taxon;
import beast.base.evolution.tree.TraitSet;
import beast.base.evolution.tree.Tree;
import beastfx.app.util.Alert;
import javafx.geometry.Dimension2D;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.text.DateFormat;
import java.time.format.DateTimeParseException;
import java.util.EventObject;
import java.util.List;
import java.util.Map;

public class TipDatesInputEditor extends BEASTObjectInputEditor {

    private static String DATE_FORMAT_HELP_MESSAGE =
            "If the radio button on the second line is selected, this format string " +
                    "will be used to convert the entries in the second column of the table " +
                    "into heights (ages in years before the most recent sample) in the " +
                    "second column.\n" +
                    "\n" +
                    "The format string may be any combination of the following special characters " +
                    "and other delimiting characters such as '-' or '/'.\n" +
                    "\n" +
                    "  Symbol  Meaning                     Examples\n" +
                    "  ------  -------                     --------\n" +
                    "   G       era                         AD; Anno Domini; A\n" +
                    "   u       year                        2004; 04\n" +
                    "   y       year-of-era                 2004; 04\n" +
                    "   D       day-of-year                 189\n" +
                    "   M/L     month-of-year               7; 07; Jul; July; J\n" +
                    "   d       day-of-month                10\n" +
                    "\n" +
                    "   Q/q     quarter-of-year             3; 03; Q3; 3rd quarter\n" +
                    "   Y       week-based-year             1996; 96\n" +
                    "   w       week-of-week-based-year     27\n" +
                    "   W       week-of-month               4\n" +
                    "   E       day-of-week                 Tue; Tuesday; T\n" +
                    "   e/c     localized day-of-week       2; 02; Tue; Tuesday; T\n" +
                    "   F       week-of-month               3\n" +
                    "\n" +
                    "(The table above is an extract from the documentation for the DateTimeFormatter " +
                    "class which is used by BEAST to parse dates.)\n" +
                    "\n" +
                    "Symbols in the above table representing numeric quantities (e.g. day-of-month " +
                    "and month-of-year) may be repeated to allow for zero-padding.  For instance, " +
                    "the format string \"d/m/y\" will match dates such as \"1/3/2004\" while \"dd/mm/y\" " +
                    "will match \"01/03/2004\".  In addition, use \"yy\" to match the short form of the year.\n" +
                    "\n" +
                    "Be aware that in all cases it is necessary that the format chosen be able to pinpoint a " +
                    "single day in the calendar. For instance, \"d/m\" is not a valid format string as it " +
                    "doesn't allow days to be uniquely identified.";


    public TipDatesInputEditor() {
    	super();
    }    
    public TipDatesInputEditor(BeautiDoc doc) {
        super(doc);
    }
    private static final long serialVersionUID = 1L;

    DateFormat dateFormat = DateFormat.getDateInstance();

    @Override
    public Class<?> type() {
        return Tree.class;
    }
    Tree tree;
    TraitSet traitSet;
    ComboBox<TraitSet.Units> unitsComboBox;
    ComboBox<String> relativeToComboBox;
    List<String> taxa;
    Object[][] tableData;
    boolean[] recordValid;
    JTable table;
    String m_sPattern = ".*(\\d\\d\\d\\d).*";
    ScrollPane scrollPane;
    List<Taxon> taxonsets;

    RadioButton numericRadioButton, formattedDateRadioButton;
    ToggleGroup radioButtonGroup;
    ComboBox<String> dateFormatComboBox;

    @Override
    public void init(Input<?> input, BEASTInterface beastObject, int itemNr, ExpandOption isExpandOption, boolean addButtons) {
        m_bAddButtons = addButtons;
        this.itemNr = itemNr;
        if (itemNr >= 0) {
            tree = (Tree) ((List<?>) input.get()).get(itemNr);
        } else {
            tree = (Tree) input.get();
        }
        if (tree != null) {
            try {
                m_input = ((BEASTInterface) tree).getInput("trait");
            } catch (Exception e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            m_beastObject = tree;
            traitSet = tree.getDateTrait();

            VBox box = new VBox();

            CheckBox useTipDates = new CheckBox("Use tip dates");
            useTipDates.setSelected(traitSet != null);
            useTipDates.setOnAction(e -> {
            	CheckBox checkBox = (CheckBox) e.getSource();
                    try {
                        if (checkBox.isSelected()) {
                            if (traitSet == null) {
                                traitSet = new TraitSet();
                                traitSet.initByName("traitname", "date",
                                        "taxa", tree.getTaxonset(),
                                        "value", "");
                                traitSet.setID("dateTrait.t:" + BeautiDoc.parsePartition(tree.getID()));
                            }
                            tree.setDateTrait(traitSet);
                        } else {
                            tree.setDateTrait(null);
                        }

                        refreshPanel();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                });
            HBox box2 = new HBox();
            box2.getChildren().add(useTipDates);
            box2.getChildren().add(new Separator());
            box.getChildren().add(box2);

            if (traitSet != null) {
                box.getChildren().add(createButtonBox());
                box.getChildren().add(createListBox());
            }
            pane.getChildren().add(box);
        }
    } // init

    private Node createListBox() {
        taxa = traitSet.taxaInput.get().asStringList();
        String[] columnData = new String[]{"Name", "Date (raw value)", "Height"};
        tableData = new Object[taxa.size()][3];
        recordValid = new boolean[taxa.size()];
        convertTraitToTableData();
        // set up table.
        // special features: background shading of rows
        // custom editor allowing only Date column to be edited.
        table = new JTable(tableData, columnData) {
            private static final long serialVersionUID = 1L;

            // method that induces table row shading
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int Index_row, int Index_col) {
                Component comp = super.prepareRenderer(renderer, Index_row, Index_col);
                //even index, selected or not selected
                comp.setForeground(Color.black);
                if (isCellSelected(Index_row, Index_col)) {
                    comp.setBackground(Color.lightGray);
                } else if (!recordValid[Index_row]) {
                    comp.setForeground(Color.WHITE);
                    comp.setBackground(Color.RED);
                } else if (Index_row % 2 == 0 && !isCellSelected(Index_row, Index_col)) {
                    comp.setBackground(new Color(237, 243, 255));
                } else {
                    comp.setBackground(Color.white);
                }
                return comp;
            }
        };

        // set up editor that makes sure only doubles are accepted as entry
        // and only the Date column is editable.
        table.setDefaultEditor(Object.class, new TableCellEditor() {
            TextField m_textField = new TextField();
            int m_iRow,
                    m_iCol;

            @Override
            public boolean stopCellEditing() {
                table.removeEditor();
                String text = m_textField.getText();

                tableData[m_iRow][m_iCol] = text;
                convertTableDataToTrait();
                convertTraitToTableData();
                return true;
            }

            @Override
            public boolean isCellEditable(EventObject anEvent) {
                return table.getSelectedColumn() == 1;
            }

            @Override
            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int rowNr, int colNr) {
                if (!isSelected) {
                    return null;
                }
                m_iRow = rowNr;
                m_iCol = colNr;
                m_textField.setText((String) value);
                return m_textField;
            }

            @Override
            public boolean shouldSelectCell(EventObject anEvent) {
                return false;
            }

            @Override
            public void removeCellEditorListener(CellEditorListener l) {
            }

            @Override
            public Object getCellEditorValue() {
                return null;
            }

            @Override
            public void cancelCellEditing() {
            }

            @Override
            public void addCellEditorListener(CellEditorListener l) {
            }
        });
        int fontsize = table.getFont().getSize();
        table.setRowHeight(24 * fontsize / 13);
        scrollPane = new ScrollPane();
        scrollPane.setContent(table);

        return scrollPane;
    } // createListBox

    private void clearTable(boolean heightsOnly) {
         for (int i = 0; i < tableData.length; i++) {
            tableData[i][0] = taxa.get(i);
            if (!heightsOnly)
                tableData[i][1] = "0";
             tableData[i][2] = "0";
        }
    }

    /* synchronise table with data from traitSet BEASTObject */
    private void convertTraitToTableData() {
        clearTable(false);

        String[] traits = traitSet.traitsInput.get().split(",");
        for (String trait : traits) {
            trait = trait.replaceAll("\\s+", " ");
            String[] strs = trait.split("=");
            if (strs.length != 2) {
                break;
            }

            String taxonID = normalize(strs[0]);
            int taxonIndex = taxa.indexOf(taxonID);

            if (taxonIndex >= 0) {
                tableData[taxonIndex][1] = normalize(strs[1]);
                tableData[taxonIndex][0] = taxonID;
            } else {
            	Log.warning.println("WARNING: File contains taxon " + taxonID + " that cannot be found in alignment");
            }
        }

        boolean numericParseError = false;
        boolean dateParseError = false;
        double [] convertedValues = new double[taxa.size()];

        for (int i=0; i<tableData.length; i++) {
            recordValid[i] = true;

            try {
                convertedValues[i] = traitSet.convertValueToDouble((String) tableData[i][1]);
            } catch (DateTimeParseException ex) {
                dateParseError = true;
                recordValid[i] = false;
            } catch (IllegalArgumentException ex) {
                numericParseError = true;
                recordValid[i] = false;
            }
        }

        if (dateParseError) {
            Alert.showMessageDialog(this,
                    "<html>Error interpreting one or more trait values as a formatted date or numeric value.<br><br>" +
                            "Problem taxa will be highlighted in table.</html>",
                    "Date parsing error",
                    Alert.ERROR_MESSAGE);
            clearTable(true);
        } else if (numericParseError) {
            Alert.showMessageDialog(this,
                    "Error interpreting one or more trait values as a numeric value.<br><br>" +
                            "Problem taxa will be highlighted in table.</html>",
                    "Date parsing error",
                    Alert.ERROR_MESSAGE);
            clearTable(true);
        } else {
            if (traitSet.traitNameInput.get().equals(TraitSet.DATE_BACKWARD_TRAIT)) {
                Double minDate = Double.MAX_VALUE;
                for (int i = 0; i < tableData.length; i++) {
                    minDate = Math.min(minDate, convertedValues[i]);
                }
                for (int i = 0; i < tableData.length; i++) {
                    tableData[i][2] = convertedValues[i] - minDate;
                }
            } else {
                Double maxDate = 0.0;
                for (int i = 0; i < tableData.length; i++) {
                    maxDate = Math.max(maxDate, convertedValues[i]);
                }
                for (int i = 0; i < tableData.length; i++) {
                    tableData[i][2] = maxDate - convertedValues[i];
                }
            }
        }

        if (table != null) {
            for (int i = 0; i < tableData.length; i++) {
                table.setValueAt(tableData[i][1], i, 1);
                table.setValueAt(tableData[i][2], i, 2);
            }
        }
    } // convertTraitToTableData

    private String normalize(String str) {
        if (str.charAt(0) == ' ') {
            str = str.substring(1);
        }
        if (str.endsWith(" ")) {
            str = str.substring(0, str.length() - 1);
        }
        return str;
    }

    /**
     * synchronise traitSet BEAST object with table data
     */
    private void convertTableDataToTrait() {
        String trait = "";
        for (int i = 0; i < tableData.length; i++) {
            trait += taxa.get(i) + "=" + tableData[i][1];
            if (i < tableData.length - 1) {
                trait += ",\n";
            }
        }
        try {
            traitSet.traitsInput.setValue(trait, traitSet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * create box with comboboxes for selectin units and trait name *
     */
    private HBox createButtonBox() {
        HBox buttonBox = new HBox();

        Label label = new Label("Dates specified: ");
        label.setAlignmentY(Component.TOP_ALIGNMENT);
        label.setMaxSize(label.getPreferredSize());
        buttonBox.add(label);

        VBox formatBox = new VBox();
        HBox formatBoxFirstLine = new HBox();
        HBox formatBoxSecondLine = new HBox();

        radioButtonGroup = new ToggleGroup();
        numericRadioButton = new RadioButton("numerically as");
        numericRadioButton.setTooltip(new Tooltip("Interpret values as numerical times."));

        if (traitSet.dateTimeFormatInput.get() == null)
            numericRadioButton.setSelected(true);

        numericRadioButton.setOnAction(e -> {
            traitSet.dateTimeFormatInput.setValue(null, traitSet);
            refreshPanel();
        });

        numericRadioButton.setToggleGroup(radioButtonGroup);
        formatBoxFirstLine.getChildren().add(numericRadioButton);

        unitsComboBox = new ComboBox<>(TraitSet.Units.values());
        unitsComboBox.setValue(traitSet.unitsInput.get());
        unitsComboBox.setOnAction(e -> {
                String selected = unitsComboBox.getValue().toString();
                try {
                    traitSet.unitsInput.setValue(selected, traitSet);
                    //System.err.println("Traitset is now: " + m_traitSet.m_sUnits.get());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
        Dimension2D d = unitsComboBox.getPrefSize();
        unitsComboBox.setMaxSize(d.getWidth(), d.getHeight());
        unitsComboBox.setPrefSize(d.getWidth(), d.getHeight());
        unitsComboBox.setDisable(!numericRadioButton.isSelected());
        formatBoxFirstLine.getChildren().add(unitsComboBox);

        relativeToComboBox = new ComboBox<>(new String[]{"Since some time in the past", "Before the present"});
        relativeToComboBox.setTooltip(new Tooltip("Whether dates go forward or backward"));
        if (traitSet.traitNameInput.get().equals(TraitSet.DATE_BACKWARD_TRAIT)) {
            relativeToComboBox.getSelectionModel().select(1);
        } else {
            relativeToComboBox.getSelectionModel().select(0);
        }
        relativeToComboBox.setOnAction(e -> {
                String selected = TraitSet.DATE_BACKWARD_TRAIT;
                if (relativeToComboBox.getSelectionModel().getSelectedIndex() == 0) {
                    selected = TraitSet.DATE_FORWARD_TRAIT;
                }
                try {
                    traitSet.traitNameInput.setValue(selected, traitSet);
                    Log.warning.println("Relative position is now: " + traitSet.traitNameInput.get());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                convertTraitToTableData();
            });
        relativeToComboBox.setMaxSize(relativeToComboBox.getPreferredSize());
        relativeToComboBox.setEnabled(numericRadioButton.isSelected());
        formatBoxFirstLine.getChildren().add(relativeToComboBox);
        formatBoxFirstLine.getChildren().add(new Separator());
        formatBox.getChildren().add(formatBoxFirstLine);

        formattedDateRadioButton = new RadioButton("as dates with format");
        formattedDateRadioButton.setTooltip(new Tooltip("Interpret values as dates with the given format."));

        if (traitSet.dateTimeFormatInput.get() != null)
            formattedDateRadioButton.setSelected(true);

        formattedDateRadioButton.setOnAction(e -> {
            traitSet.dateTimeFormatInput.setValue(dateFormatComboBox.getSelectedItem(), traitSet);
            refreshPanel();
        });
        formattedDateRadioButton.setToggleGroup(radioButtonGroup);
        formatBoxSecondLine.getChildren().add(formattedDateRadioButton);

        String[] dateFormatExamples = {
                "dd/M/yyyy",
                "M/dd/yyyy",
                "yyyy/M/dd",
                "dd-M-yyyy",
                "M-dd-yyyy",
                "yyyy-M-dd"};

        dateFormatComboBox = new ComboBox<>(dateFormatExamples);
        dateFormatComboBox.setTooltip(new Tooltip("Set format used to parse date values"));
        dateFormatComboBox.setEditable(true);
        if (traitSet.dateTimeFormatInput.get() != null)
            dateFormatComboBox.setValue(traitSet.dateTimeFormatInput.get());
        else
            dateFormatComboBox.setValue(dateFormatExamples[0]);
        dateFormatComboBox.setMaxSize(dateFormatComboBox.getPrefSize());
        dateFormatComboBox.setDisabe(!formattedDateRadioButton.isSelected());
        dateFormatComboBox.setOnAction(e -> {
            traitSet.dateTimeFormatInput.setValue(dateFormatComboBox.getValue(), traitSet);
            refreshPanel();
        });
        formatBoxSecondLine.getChildren().add(dateFormatComboBox);

        Button dateFormatHelpButton = new Button("?");
        dateFormatHelpButton.setOnAction(e ->
                WrappedOptionPane.showWrappedMessageDialog(this,
                        DATE_FORMAT_HELP_MESSAGE, Font.MONOSPACED));
        formatBoxSecondLine.getChildren().add(dateFormatHelpButton);

        formatBoxSecondLine.getChildren().add(new Separator());

        formatBox.getChildren().add(formatBoxSecondLine);
        formatBox.setAlignmentY(TOP_ALIGNMENT);
        buttonBox.getChildren().add(formatBox);

        buttonBox.getChildren().add(new Separator());

        Button guessButton = new Button("Auto-configure");
        guessButton.setAlignmentY(TOP_ALIGNMENT);
        guessButton.setTooltip(new Tooltip("Automatically configure dates based on taxon names"));
        guessButton.setId("Guess");
        guessButton.setOnAction(e -> {
                GuessPatternDialog dlg = new GuessPatternDialog(null, m_sPattern);
                dlg.allowAddingValues();
                StringBuilder traitBuilder = new StringBuilder();
                switch (dlg.showDialog("Guess dates")) {
                    case canceled:
                        return;

                    case trait:
                        Map<String,String> traitMap = dlg.getTraitMap();
                        for (String taxon : taxa) {
                            if (!traitMap.containsKey(taxon))
                                continue;

                            if (traitBuilder.length()>0) {
                                traitBuilder.append(",");
                            }

                            traitBuilder.append(taxon).append("=")
                                    .append(traitMap.get(taxon));
                        }
                        break;

                    case pattern:
                        for (String taxon : taxa) {
                            String match = dlg.match(taxon);
                            if (match == null || match.isEmpty()) {
                                return;
                            }
                            if (traitBuilder.length() > 0) {
                                traitBuilder.append(",");
                            }
                            traitBuilder.append(taxon).append("=").append(match);
                        }
                        break;
                }
                try {
                    traitSet.traitsInput.setValue(traitBuilder.toString(), traitSet);
                } catch (Exception ex) {
                    // TODO: handle exception
                }
                refreshPanel();
            });
        buttonBox.getChildren().add(guessButton);


        Button clearButton = new Button("Clear");
        clearButton.setAlignmentY(TOP_ALIGNMENT);
        clearButton.setTooltip(new Tooltip("Set all dates to zero"));
        clearButton.setOnAction(e -> {
                try {
                    traitSet.traitsInput.setValue("", traitSet);
                } catch (Exception ex) {
                    // TODO: handle exception
                }
                refreshPanel();
            });
        buttonBox.getChildren().add(clearButton);

        return buttonBox;
    } // createButtonBox
}
