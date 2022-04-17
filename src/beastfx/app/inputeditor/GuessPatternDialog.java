package beastfx.app.inputeditor;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.ButtonGroup;

import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;

import javax.swing.JDialog;
import javafx.scene.control.Label;
import beastfx.app.util.Alert;
import javax.swing.JPanel;
import javafx.scene.control.RadioButton;
import javax.swing.JSeparator;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import beastfx.app.beauti.Beauti;
import beast.app.util.Utils;
import beast.base.core.Log;
import beast.base.core.ProgramStatus;


public class GuessPatternDialog extends Dialog {
    private static final long serialVersionUID = 1L;

    private static String TRAIT_FILE_HELP_MESSAGE =
            "This option allows trait values (such as species, tip dates and sample locations) " +
                    "to be specified  using a file which links each taxon " +
                    "with a trait value.  The file must contain one row per " +
                    "taxon, with each row containing the taxon name and the " +
                    "trait value separated by a TAB (not a space!) character.\n" +
                    "\n" +
                    "For instance, a file specifying the ages of three taxa named " +
                    "taxonA, taxonB and taxonC should contain the following:\n" +
                    "\n" +
                    "taxonA 0.0\n" +
                    "taxonB 0.1\n" +
                    "taxonC 0.2\n" +
                    "\n" +
                    "where the gap between each taxon name and its corresponding " +
                    "trait value must be a TAB.";



    public enum Status {
        canceled, pattern, trait
    };

    public Map<String,String> traitMap;

    public Map<String,String> getTraitMap() {
        return traitMap;
    }

    /**
     * Constructs and returns a string containing a comma-delimited list of
     * taxonName=traitValue pairs, as used by the TraitSet initializer.
     *
     * This method is deprecated: use getTraitMap instead.
     *
     * @return constructed string
     */
    @Deprecated
    public String getTrait() {
       StringBuilder sb = new StringBuilder();

       boolean isFirst = true;
       for (String taxonName : traitMap.keySet()) {
           if (isFirst)
               isFirst = false;
           else
               sb.append(",");

           sb.append(taxonName).append("=").append(traitMap.get(taxonName));
       }

       return sb.toString();
    }

    Parent m_parent;
    GridPane guessPanel;
    ButtonGroup group;
    RadioButton useEverything = new RadioButton("use everything");
    RadioButton isSplitOnChar = new RadioButton("split on character");
    RadioButton useRegexp = new RadioButton("use regular expression");
    RadioButton readFromFile = new RadioButton("read from file");

    int m_location = 0;
    int m_splitlocation = 0;
    String m_sDelimiter = ".";
    TextField textRegExp;
    ComboBox<String> combo;
    ComboBox<String> combo_1;
    String pattern;

    public String getPattern() {
        return pattern;
    }

    private TextField txtFile;
    private TextField textSplitChar;
    private TextField textSplitChar2;
    private TextField textAddValue;
    private TextField textUnlessLessThan;
    private TextField textThenAdd;
    CheckBox chckbxAddFixedValue;
    CheckBox chckbxUnlessLessThan;
    Label lblThenAdd;
    Label lblAndTakeGroups;
    Button btnBrowse;
    private JSeparator separator_2;
    private JSeparator separator_3;
    private JSeparator separator_4;
    private JSeparator separator_5;

    public GuessPatternDialog(Parent parent, String pattern) {
        m_parent = parent;
        this.pattern = pattern;
        guessPanel = new GridPane();
        GridBagLayout gbl_guessPanel = new GridBagLayout();
        gbl_guessPanel.rowHeights = new int[]{0, 0, 0, 20, 0, 0, 20, 0, 0, 20, 0, 29, 0, 0, 0, 0};
        gbl_guessPanel.columnWidths = new int[]{0, 0, 0, 0, 0, 0};
        gbl_guessPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
        gbl_guessPanel.columnWeights = new double[] { 1.0, 1.0, 1.0, 0.0, 1.0, 0.0 };
        guessPanel.setLayout(gbl_guessPanel);

        group = new ButtonGroup();
        group.add(useEverything);
        group.add(isSplitOnChar);
        group.add(useRegexp);
        group.add(readFromFile);
        group.setSelected(useEverything.getModel(), true);
        useEverything.setOnAction(e -> {
                updateFields();
            });
        useEverything.setId(useEverything.getText());
        isSplitOnChar.setOnAction(e -> {
                updateFields();
            });
        isSplitOnChar.setId(isSplitOnChar.getText());
        useRegexp.setOnAction(e -> {
                updateFields();
            });
        useRegexp.setId(useRegexp.getText());
        readFromFile.setOnAction(e -> {
                updateFields();
            });
        readFromFile.setId(readFromFile.getText());

        createDelimiterBox(useEverything);
        createSplitBox(isSplitOnChar);
        createRegExtpBox(useRegexp);

        textRegExp = new TextField();
        textRegExp.setText(pattern);
        textRegExp.setColumns(10);
        textRegExp.setTooltip(new Tooltip("Enter regular expression to match taxa"));
        int fontsize = textRegExp.getFont().getSize();
        textRegExp.setMaxSize(new Dimension(1024 * fontsize/13, 25 * fontsize/13));
        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.insets = new Insets(0, 0, 5, 5);
        gbc2.anchor = GridBagConstraints.WEST;
        gbc2.gridwidth = 4;
        gbc2.gridx = 1;
        gbc2.gridy = 7;
        guessPanel.add(textRegExp, gbc2);
        textRegExp.setOnKeyPressed(e ->useRegexp.setSelected(true));
//        textRegExp.getDocument().addDocumentListener(new DocumentListener() {
//            @Override
//            public void removeUpdate(DocumentEvent e) {
//                useRegexp.setSelected(true);
//            }
//
//            @Override
//            public void insertUpdate(DocumentEvent e) {
//                useRegexp.setSelected(true);
//            }
//
//            @Override
//            public void changedUpdate(DocumentEvent e) {
//                useRegexp.setSelected(true);
//            }
//        });

        separator_4 = new JSeparator();
        separator_4.setPrefSize(new Dimension(5,1));
        GridBagConstraints gbc_separator_4 = new GridBagConstraints();
        gbc_separator_4.gridwidth = 5;
        gbc_separator_4.insets = new Insets(5, 0, 15, 5);
        gbc_separator_4.gridx = 0;
        gbc_separator_4.gridy = 8;
        gbc_separator_4.fill = GridBagConstraints.HORIZONTAL;
        guessPanel.add(separator_4, gbc_separator_4);

        GridBagConstraints gbc_rdbtnReadFromFile = new GridBagConstraints();
        gbc_rdbtnReadFromFile.anchor = GridBagConstraints.WEST;
        gbc_rdbtnReadFromFile.insets = new Insets(0, 0, 5, 5);
        gbc_rdbtnReadFromFile.gridx = 0;
        gbc_rdbtnReadFromFile.gridy = 10;
        guessPanel.add(readFromFile, gbc_rdbtnReadFromFile);

        btnBrowse = new Button("Browse");
        btnBrowse.setOnAction(e -> {
                File file = Utils.getLoadFile("Load trait from file", new File(ProgramStatus.g_sDir), "Select trait file", "dat","txt");
                if (file != null) {
                    txtFile.setText(file.getPath());
                    readFromFile.setSelected(true);
                    updateFields();
                }
            });

        txtFile = new TextField();
        txtFile.setText("File");
        GridBagConstraints gbc_txtFile = new GridBagConstraints();
        gbc_txtFile.gridwidth = 2;
        gbc_txtFile.insets = new Insets(0, 0, 5, 5);
        gbc_txtFile.fill = GridBagConstraints.HORIZONTAL;
        gbc_txtFile.gridx = 1;
        gbc_txtFile.gridy = 10;
        guessPanel.add(txtFile, gbc_txtFile);
        txtFile.setColumns(10);
        GridBagConstraints gbc_btnReadFromFile = new GridBagConstraints();
        gbc_btnReadFromFile.insets = new Insets(0, 0, 5, 5);
        gbc_btnReadFromFile.gridx = 3;
        gbc_btnReadFromFile.gridy = 10;
        guessPanel.add(btnBrowse, gbc_btnReadFromFile);

        Button btnHelp = new Button("?");
        btnHelp.setTooltip(new Tooltip("Show format of trait file"));
        btnHelp.setOnAction(e -> WrappedOptionPane.showWrappedMessageDialog(
                guessPanel, TRAIT_FILE_HELP_MESSAGE));
        GridBagConstraints gbc_btnHelp = new GridBagConstraints();
        gbc_btnHelp.insets = new Insets(0, 0, 5, 5);
        gbc_btnHelp.gridx = 4;
        gbc_btnHelp.gridy = 10;
        guessPanel.add(btnHelp, gbc_btnHelp);


        chckbxAddFixedValue = new CheckBox("Add fixed value");
        chckbxAddFixedValue.setId("Add fixed value");
        chckbxAddFixedValue.setOnAction(e -> updateFields());

        separator_5 = new JSeparator();
        separator_5.setPrefSize(new Dimension(5,1));
        GridBagConstraints gbc_separator_5 = new GridBagConstraints();
        gbc_separator_5.gridwidth = 5;
        gbc_separator_5.insets = new Insets(5, 0, 15, 5);
        gbc_separator_5.gridx = 0;
        gbc_separator_5.gridy = 12;
        gbc_separator_5.fill = GridBagConstraints.HORIZONTAL;
        guessPanel.add(separator_5, gbc_separator_5);
        GridBagConstraints gbc_chckbxAddFixedValue = new GridBagConstraints();
        gbc_chckbxAddFixedValue.anchor = GridBagConstraints.WEST;
        gbc_chckbxAddFixedValue.insets = new Insets(0, 0, 5, 5);
        gbc_chckbxAddFixedValue.gridx = 0;
        gbc_chckbxAddFixedValue.gridy = 13;
        guessPanel.add(chckbxAddFixedValue, gbc_chckbxAddFixedValue);

        textAddValue = new TextField("1900");
        GridBagConstraints gbc_textField = new GridBagConstraints();
        gbc_textField.gridwidth = 2;
        gbc_textField.insets = new Insets(0, 0, 5, 5);
        gbc_textField.fill = GridBagConstraints.HORIZONTAL;
        gbc_textField.gridx = 1;
        gbc_textField.gridy = 13;
        guessPanel.add(textAddValue, gbc_textField);
        textAddValue.setColumns(10);

        chckbxUnlessLessThan = new CheckBox("Unless less than...");
        chckbxUnlessLessThan.setId("Unless less than");
        chckbxUnlessLessThan.setOnAction(e -> {
                updateFields();
            });
        GridBagConstraints gbc_chckbxUnlessLargerThan = new GridBagConstraints();
        gbc_chckbxUnlessLargerThan.anchor = GridBagConstraints.WEST;
        gbc_chckbxUnlessLargerThan.insets = new Insets(0, 0, 5, 5);
        gbc_chckbxUnlessLargerThan.gridx = 0;
        gbc_chckbxUnlessLargerThan.gridy = 14;
        guessPanel.add(chckbxUnlessLessThan, gbc_chckbxUnlessLargerThan);

        textUnlessLessThan = new TextField("13");
        GridBagConstraints gbc_textField_1 = new GridBagConstraints();
        gbc_textField_1.gridwidth = 2;
        gbc_textField_1.insets = new Insets(0, 0, 5, 5);
        gbc_textField_1.fill = GridBagConstraints.HORIZONTAL;
        gbc_textField_1.gridx = 1;
        gbc_textField_1.gridy = 14;
        guessPanel.add(textUnlessLessThan, gbc_textField_1);
        textUnlessLessThan.setColumns(10);

        lblThenAdd = new Label("...then add");
        GridBagConstraints gbc_lblThenAdd = new GridBagConstraints();
        gbc_lblThenAdd.anchor = GridBagConstraints.EAST;
        gbc_lblThenAdd.insets = new Insets(0, 0, 0, 5);
        gbc_lblThenAdd.gridx = 0;
        gbc_lblThenAdd.gridy = 15;
        guessPanel.add(lblThenAdd, gbc_lblThenAdd);

        textThenAdd = new TextField("2000");
        GridBagConstraints gbc_textField_2 = new GridBagConstraints();
        gbc_textField_2.gridwidth = 2;
        gbc_textField_2.insets = new Insets(0, 0, 0, 5);
        gbc_textField_2.fill = GridBagConstraints.HORIZONTAL;
        gbc_textField_2.gridx = 1;
        gbc_textField_2.gridy = 15;
        guessPanel.add(textThenAdd, gbc_textField_2);
        textThenAdd.setColumns(10);


        chckbxAddFixedValue.setVisible(false);
        textAddValue.setVisible(false);
        chckbxUnlessLessThan.setVisible(false);
        lblThenAdd.setVisible(false);
        chckbxUnlessLessThan.setVisible(false);
        textUnlessLessThan.setVisible(false);
        textThenAdd.setVisible(false);
    }

    public void allowAddingValues() {
        chckbxAddFixedValue.setVisible(true);
        textAddValue.setVisible(true);
        chckbxUnlessLessThan.setVisible(true);
        lblThenAdd.setVisible(true);
        chckbxUnlessLessThan.setVisible(true);
        textUnlessLessThan.setVisible(true);
        textThenAdd.setVisible(true);
    }

    protected void updateFields() {
        if (chckbxAddFixedValue.isSelected()) {
            textAddValue.setDisable(false);
            chckbxUnlessLessThan.setDisable(false);
            lblThenAdd.setDisable(false);
            if (chckbxUnlessLessThan.isSelected()) {
                textUnlessLessThan.setDisable(false);
                textThenAdd.setDisable(false);
            } else {
                textUnlessLessThan.setDisable(true);
                textThenAdd.setDisable(true);
            }
        } else {
            textAddValue.setDisable(true);
            chckbxUnlessLessThan.setDisable(true);
            lblThenAdd.setDisable(true);
            textUnlessLessThan.setDisable(true);
            textThenAdd.setDisable(true);
        }

        txtFile.setDisable(true);
        textSplitChar.setDisable(true);
        textSplitChar2.setDisable(true);
        textRegExp.setDisable(true);
        combo.setDisable(true);
        combo_1.setDisable(true);
        lblAndTakeGroups.setDisable(true);
        btnBrowse.setDisable(true);
        if (useEverything.isSelected()) {
            textSplitChar.setDisable(false);
            combo.setDisable(false);
        }
        if (isSplitOnChar.isSelected()) {
            textSplitChar2.setDisable(false);
            combo_1.setDisable(false);
            lblAndTakeGroups.setDisable(false);
        }
        if (useRegexp.isSelected()) {
            textRegExp.setDisable(false);
        }
        if (readFromFile.isSelected()) {
            btnBrowse.setDisable(false);
            txtFile.setDisable(false);
        }
    }

    private void createDelimiterBox(RadioButton b) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
        gbc.gridy = 1;
        guessPanel.add(b, gbc);

        combo = new ComboBox<>(new String[] { "after first", "after last", "before first", "before last" });
        combo.setId("delimiterCombo");
        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.anchor = GridBagConstraints.WEST;
        gbc2.gridwidth = 2;
        gbc2.insets = new Insets(0, 0, 5, 5);
        gbc2.gridx = 1;
        gbc2.gridy = 1;
        guessPanel.add(combo, gbc2);
        combo.setOnAction(e -> {
                @SuppressWarnings("unchecked")
				ComboBox<String> combo = (ComboBox<String>) e.getSource();
                m_location = combo.getSelectedIndex();
                useEverything.setSelected(true);
                updateFields();
            });
    }

    private void createSplitBox(RadioButton b) {

        textSplitChar = new TextField("_");
        textSplitChar.setId("SplitChar");
        GridBagConstraints gbc_textField = new GridBagConstraints();
        gbc_textField.anchor = GridBagConstraints.WEST;
        gbc_textField.insets = new Insets(0, 0, 5, 5);
        gbc_textField.gridx = 3;
        gbc_textField.gridy = 1;
        guessPanel.add(textSplitChar, gbc_textField);
        textSplitChar.setColumns(2);

        separator_2 = new JSeparator();
        separator_2.setPrefSize(new Dimension(5,1));
        GridBagConstraints gbc_separator_2 = new GridBagConstraints();
        gbc_separator_2.gridwidth = 5;
        gbc_separator_2.insets = new Insets(5, 0, 15, 5);
        gbc_separator_2.gridx = 0;
        gbc_separator_2.gridy = 2;
        gbc_separator_2.fill = GridBagConstraints.HORIZONTAL;
        guessPanel.add(separator_2, gbc_separator_2);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
        gbc.gridy = 4;
        guessPanel.add(b, gbc);
    }

    public void createRegExtpBox(RadioButton b) {

        textSplitChar2 = new TextField("_");
        textSplitChar2.setId("SplitChar2");
        GridBagConstraints gbc_textField_1 = new GridBagConstraints();
        gbc_textField_1.anchor = GridBagConstraints.WEST;
        gbc_textField_1.insets = new Insets(0, 0, 5, 5);
        gbc_textField_1.gridx = 1;
        gbc_textField_1.gridy = 4;
        guessPanel.add(textSplitChar2, gbc_textField_1);
        textSplitChar2.setColumns(2);

        lblAndTakeGroups = new Label("and take group(s):");
        GridBagConstraints gbc_lblAndTakeGroups = new GridBagConstraints();
        gbc_lblAndTakeGroups.gridwidth = 2;
        gbc_lblAndTakeGroups.insets = new Insets(0, 0, 5, 5);
        gbc_lblAndTakeGroups.gridx = 2;
        gbc_lblAndTakeGroups.gridy = 4;
        guessPanel.add(lblAndTakeGroups, gbc_lblAndTakeGroups);

        combo_1 = new ComboBox<>(new String[] { "1", "2", "3", "4", "1-2", "2-3", "3-4", "1-3", "2-4" });
        combo_1.setID("splitCombo");
        GridBagConstraints gbc_combo_1 = new GridBagConstraints();
        gbc_combo_1.anchor = GridBagConstraints.WEST;
        gbc_combo_1.insets = new Insets(0, 0, 5, 5);
        gbc_combo_1.gridx = 4;
        gbc_combo_1.gridy = 4;
        guessPanel.add(combo_1, gbc_combo_1);
        combo_1.setOnAction(e -> {
                @SuppressWarnings("unchecked")
				ComboBox<String> combo = (ComboBox<String>) e.getSource();
                m_splitlocation = combo.getSelectionModel().getSelectedIndex();
                isSplitOnChar.setSelected(true);
                updateFields();
            });

        separator_3 = new JSeparator();
        separator_3.setPrefSize(new Dimension(5,1));
        GridBagConstraints gbc_separator_3 = new GridBagConstraints();
        gbc_separator_3.gridwidth = 5;
        gbc_separator_3.insets = new Insets(5, 0, 15, 5);
        gbc_separator_3.gridx = 0;
        gbc_separator_3.gridy = 5;
        gbc_separator_3.fill = GridBagConstraints.HORIZONTAL;
        guessPanel.add(separator_3, gbc_separator_3);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
        gbc.gridy = 7;
        guessPanel.add(b, gbc);
    }

    public Status showDialog(String title) {

        Alert optionPane = new Alert(guessPanel, Alert.PLAIN_MESSAGE, Alert.OK_CANCEL_OPTION,
                null, new String[] { "Cancel", "OK" }, "OK");
        optionPane.setBorder(new EmptyBorder(12, 12, 12, 12));

        final JDialog dialog = optionPane.createDialog(m_parent, title);
        dialog.setID("GuessTaxonSets");
        // dialog.setResizable(true);
        dialog.pack();
        updateFields();
        dialog.setVisible(true);

        if (optionPane.getValue() == null || !optionPane.getValue().equals("OK")) {
            return Status.canceled;
        }

        if (useEverything.getModel() == group.getSelection()) {
            String delimiter = normalise(textSplitChar.getText());
            switch (m_location) {
                case 0: // "after first",
                    pattern = "^[^" + delimiter + "]+" + delimiter + "(.*)$";
                    break;
                case 1: // "after last",
                    pattern = "^.*" + delimiter + "(.*)$";
                    break;
                case 2: // "before first",
                    pattern = "^([^" + delimiter + "]+)" + delimiter + ".*$";
                    break;
                case 3: // "before last"
                    pattern = "^(.*)" + delimiter + ".*$";
                    break;
            }
        }
        if (isSplitOnChar.getModel() == group.getSelection()) {
            String delimiter = normalise(textSplitChar2.getText());
            switch (m_splitlocation) {
                case 0: // "1"
                    pattern = "^([^" + delimiter + "]*)" + ".*$";
                    break;
                case 1: // "2"
                    pattern = "^[^" + delimiter + "]*" + delimiter + "([^" + delimiter + "]*)" + ".*$";
                    break;
                case 2: // "3"
                    pattern = "^[^" + delimiter + "]*" + delimiter + "[^" + delimiter + "]*" + delimiter + "([^"
                            + delimiter + "]*)" + ".*$";
                    break;
                case 3: // "4"
                    pattern = "^[^" + delimiter + "]*" + delimiter + "[^" + delimiter + "]*" + delimiter + "[^"
                            + delimiter + "]*" + delimiter + "([^" + delimiter + "]*)" + ".*$";
                    break;
                case 4: // "1-2"
                    pattern = "^([^" + delimiter + "]*" + delimiter + "[^" + delimiter + "]*)" + ".*$";
                    break;
                case 5: // "2-3"
                    pattern = "^[^" + delimiter + "]*" + delimiter + "([^" + delimiter + "]*" + delimiter + "[^"
                            + delimiter + "]*)" + ".*$";
                    break;
                case 6: // "3-4"
                    pattern = "^[^" + delimiter + "]*" + delimiter + "[^" + delimiter + "]*" + delimiter + "([^"
                            + delimiter + "]*" + delimiter + "[^" + delimiter + "]*)" + ".*$";
                    break;
                case 7: // "1-3"
                    pattern = "^([^" + delimiter + "]*" + delimiter + "[^" + delimiter + "]*" + delimiter + "[^"
                            + delimiter + "]*)" + ".*$";
                    break;
                case 8: // "2-4"
                    pattern = "^[^" + delimiter + "]*" + delimiter + "([^" + delimiter + "]*" + delimiter + "[^"
                            + delimiter + "]*" + delimiter + "[^" + delimiter + "]*)" + ".*$";
            }
        }
        if (useRegexp.getModel() == group.getSelection()) {
            pattern = textRegExp.getText();
        }
        if (readFromFile.getModel() == group.getSelection()) {
            traitMap = new HashMap<>();
            try {
                BufferedReader fin = new BufferedReader(new FileReader(txtFile.getText()));
                while (fin.ready()) {
                    String[] strArray = fin.readLine().trim().split("\t");
                    // only add entries that are non-empty
                    if (strArray.length >= 2) {
                    	String str = strArray[1];
                    	int k = 2;
                    	while (k < strArray.length) {
                    		str += "\t" + strArray[k++];
                    	}
                        traitMap.put(strArray[0], str);
                    }
                }
                fin.close();

               if (traitMap.isEmpty()) {
            	   Alert.showMessageDialog(m_parent, "Could not find trait information in the file. " +
            			   "Perhaps this is not a tab-delimited but space file?");
               }
            } catch (Exception e) {
                Alert.showMessageDialog(m_parent, "Loading trait from file failed:" + e.getMessage());
                return Status.canceled;
            }
            return Status.trait;
        }

        // sanity check
        try {
            pattern.matches(pattern);
        } catch (PatternSyntaxException e) {
            Alert.showMessageDialog(this, "This is not a valid regular expression");
            return Status.canceled;
        }

        if (optionPane.getValue() != null && optionPane.getValue().equals("OK")) {
        	Log.warning.println("Pattern = " + pattern);
            return Status.pattern;
        } else {
            return Status.canceled;
        }
    }

    /**
     * Converts the first character of delimiter into a substring suitable for
     * inclusion in a regexp. This is done by expressing the character as an
     * octal escape.
     *
     * @param delimiter first character of this string to be used as delimiter
     * @return escaped octal representation of character
     */
    private String normalise(String delimiter) {

        if (delimiter.length() == 0) {
            return ".";
        }

        return String.format("\\0%o", (int)delimiter.charAt(0));
    }

    public String match(String s) {
        Pattern _pattern = Pattern.compile(pattern);
        Matcher matcher = _pattern.matcher(s);
        if (matcher.find()) {
            String match = matcher.group(1);
            if (chckbxAddFixedValue.isSelected()) {
                try {
                    Double value = Double.parseDouble(match);
                    Double addValue = Double.parseDouble(textAddValue.getText());
                    if (chckbxUnlessLessThan.isSelected()) {
                        Double threshold = Double.parseDouble(textUnlessLessThan.getText());
                        Double addValue2 = Double.parseDouble(textThenAdd.getText());
                        if (value < threshold) {
                            value += addValue2;
                        } else {
                            value += addValue;
                        }
                    } else {
                        value += addValue;
                    }
                    return value + "";
                } catch (Exception e) {
                    // ignore
                }
            }
            return match;
        }
        return null;
    }
}
