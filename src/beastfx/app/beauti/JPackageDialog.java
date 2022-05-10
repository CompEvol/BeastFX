package beastfx.app.beauti;








import beast.base.core.Description;
import beast.pkgmgmt.Package;
import beast.pkgmgmt.PackageManager;
import beast.pkgmgmt.PackageVersion;
import beastfx.app.inputeditor.BEASTObjectDialog;
import beastfx.app.util.Alert;
import beastfx.app.util.FXUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Control;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import static beast.pkgmgmt.PackageManager.*;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * dialog for managing Package.
 * List, install and uninstall Package
 *
 * @author  Remco Bouckaert
 * @author  Walter Xie
 */
@Description("BEAUti package manager")
public class JPackageDialog extends DialogPane {
    // JScrollPane scrollPane;
    Label jLabel;
    HBox buttonBox;
    // JFrame frame;
    TableView<Package0> dataTable = null;
    private List<Package> packageList = null;
    private ObservableList<Package0> packages;

    public class Package0 {
    	Package pkg;
    	public Package0(Package pkg) {
    		this.pkg = pkg;
    	}
    	
    	public String getName() {
    		return pkg.getName();
    	}
    	
    	public String getInstalledVersion() {
    		if (pkg.getInstalledVersion() != null) {
    			return pkg.getInstalledVersion().getVersion();
    		}
    		return "";
    	}
    	
    	public String getLatestVersion() {
    		if (pkg.getLatestVersion() != null) {
    			return pkg.getLatestVersion().getVersion();
    		}
    		return "";
    	}
    	
        public String getDependencies() {
            return pkg.getDependenciesString();
        }
        
        public String getProjectURL() {
        	if (pkg.getProjectURL() != null)
                return pkg.getProjectURL().toString();
        	return "";
        }
        
        public String getDescription() {
            return pkg.getDescription();
        }
    }
    
    boolean useLatestVersion = true;

    TreeMap<String, Package> packageMap = new TreeMap<>((s1,s2)->{
    	if (s1.equals(PackageManager.BEAST_PACKAGE_NAME)) {
    		if (s2.equals(PackageManager.BEAST_PACKAGE_NAME)) {
    			return 0;
    		}
    		return -1;
    	}
    	if (s2.equals(PackageManager.BEAST_PACKAGE_NAME)) {
    		return 1;
    	}
    	return s1.compareToIgnoreCase(s2);
    });


    boolean isRunning;
    Thread t;
    VBox pane;
    
    public JPackageDialog() {
        jLabel = new Label("List of available packages for BEAST v" + beastVersion.getMajorVersion() + ".*");
        jLabel.setMinWidth(400);
        //frame = (JFrame) SwingUtilities.getWindowAncestor(this);
        //setLayout(new BorderLayout());
        pane = FXUtils.newVBox();
        pane.getChildren().add(jLabel);

        dataTable = createTable();
        // update packages using a 30 second time out
        isRunning = true;
        t = new Thread() {
        	@Override
			public void run() {
                resetPackages();
                // dataTable.updateWidths();
        		isRunning = false;
        	}
        };
        t.start();
    	Thread t2 = new Thread() {
    		@Override
			public void run() {
    			try {
    				// wait 30 seconds
					sleep(30000);
	    			if (isRunning) {
	    				t.interrupt();
	    				Alert.showMessageDialog(null, "<html>Download of file " +
	    						PackageManager.PACKAGES_XML + " timed out.<br>" +
	    								"Perhaps this is due to lack of internet access</br>" +
	    								"or some security settings not allowing internet access.</html>"
	    						);
	    			}
				} catch (InterruptedException e) {
				}
    		}
    	};
    	t2.start();
        
        try {
			t.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        
        pane.getChildren().add(dataTable);

        buttonBox = createButtonBox();
        pane.getChildren().add(buttonBox);

        setPrefSize(dataTable.getPrefWidth() + 0, dataTable.getPrefHeight() + buttonBox.getPrefHeight() + 60);
        
        dlgPane = this;
        getChildren().add(pane);
    }


    private TableView<Package0> createTable() {
        dataTable = new TableView<>();
                
        final int linkColumn = 4;

        
		String[] columnNames = {"Name", "Installed", "Latest", "Dependencies", "Link", "Detail"};
		int[] columnWidth = {200, 60, 50, 200, 40, 1200};
		String[] names = {"Name", "InstalledVersion", "LatestVersion", "Dependencies", "ProjectURL", "Description"};

		for (int i = 0; i < columnNames.length; i++) {
			TableColumn<Package0, String> col1 = new TableColumn<>(columnNames[i]);
			col1.setPrefWidth(columnWidth[i]);
			if (i != linkColumn) {
				col1.setCellValueFactory(new PropertyValueFactory<Package0, String>(names[i]));
			} else {
				col1.setCellFactory(e->{
					TableCell cell = new TableCell();
					ImageView imageview = FXUtils.getIcon(BEASTObjectDialog.ICONPATH + "link.png");
					cell.setGraphic(imageview);
				    return cell;
				});
			}
			dataTable.getColumns().add(col1);
		}

		dataTable.setPrefWidth(200+50+50+200+30+400);
		dataTable.setPrefHeight(600);
		dataTable.setMinSize(930, 600);
		dataTable.setPadding(new Insets(7));
        
//        dataTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

        
        
        
		dataTable.setItems(packages);
        
        dataTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        dataTable.setOnMouseClicked(e -> {
                if (dataTable.getSelectionModel().getSelectedCells().get(0).getColumn() == linkColumn) {
                    URL url = getSelectedPackage(dataTable.getSelectionModel().getSelectedIndex()).getProjectURL();
                    if (url != null) {
                        try {
                            Desktop.getDesktop().browse(url.toURI());
                        } catch (IOException | URISyntaxException e1) {
                            e1.printStackTrace();
                        }
                    }

                } else {
                    if (e.getClickCount() == 2) {
                        Package selPackage = getSelectedPackage(dataTable.getSelectionModel().getSelectedIndex());
                        showDetail(selPackage);
                    }
                }
        });

        dataTable.setOnMouseMoved(e -> {
                // super.mouseMoved(e);
        		TablePosition<Package, String> tc = dataTable.getFocusModel().getFocusedCell();
                int row = tc.getRow();//dataTable.rowAtPoint(e.getPoint());
                int col = tc.getColumn();//dataTable.columnAtPoint(e.getPoint());

                Cursor currentCursorType = dataTable.getCursor();

                if (col != linkColumn) {
                    if (currentCursorType == Cursor.HAND)
                        dataTable.setCursor(Cursor.DEFAULT);

                    return;
                }

                Package thisPkg = getSelectedPackage(row);

                if (thisPkg.getProjectURL() == null) {
                    if (currentCursorType == Cursor.HAND)
                        dataTable.setCursor(Cursor.DEFAULT);

                    return;
                }

                dataTable.setCursor(Cursor.HAND);
        });

//		int size = dataTable.getFont().getSize();
//		dataTable.setRowHeight(20 * size/13);
		return dataTable;
    }


    private void resetPackages() {
        packageMap.clear();
        try {
            addAvailablePackages(packageMap);
            addInstalledPackages(packageMap);

            // Create list of packages excluding beast2
            packageList = new ArrayList<>();
            List<Package0> list = new ArrayList<>();
            for (Package pkg : packageMap.values()) {
                if (!pkg.getName().equals("beast2")) {
                    packageList.add(pkg);
                    list.add(new Package0(pkg));
                } else {
                	list.add(0, new Package0(pkg));
                }
            }
            packages = FXCollections.observableArrayList(list);
            if (dataTable != null) {
            	dataTable.setItems(packages);
            	dataTable.refresh();
            }
        } catch (PackageManager.PackageListRetrievalException e) {
        	StringBuilder msgBuilder = new StringBuilder("<html>" + e.getMessage() + "<br>");
            if (e.getCause() instanceof IOException)
                msgBuilder.append(NO_CONNECTION_MESSAGE.replaceAll("\\.", ".<br>"));
            msgBuilder.append("</html>");

        	try {
        		Alert.showMessageDialog(null, msgBuilder.toString());
        	} catch (Exception e0) {
        		e0.printStackTrace();
        	}
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Package getSelectedPackage(int selectedRow) {
        if (packageList.size() <= selectedRow)
            throw new IllegalArgumentException("Incorrect row " + selectedRow +
                    " is selected from package list, size = " + packageMap.size());
        return packageList.get(selectedRow);
    }

    private void showDetail(Package aPackage) {
        //custom title, no icon
        Alert.showMessageDialog(null,
                aPackage.toHTML(),
                aPackage.getName(),
                Alert.PLAIN_MESSAGE);
    }

    private HBox createButtonBox() {
        HBox box = FXUtils.newHBox();
        final CheckBox latestVersionCheckBox = new CheckBox("Latest");
        latestVersionCheckBox.setTooltip(new Tooltip("If selected, only the latest version is installed when hitting the Install/Upgrade button. "
        		+ "Otherwise, you can select from a list of available versions."));
        box.getChildren().add(latestVersionCheckBox);
        latestVersionCheckBox.setOnAction(e -> {
        	CheckBox checkBox = (CheckBox) e.getSource();
        	useLatestVersion = checkBox.isSelected();
        });
        latestVersionCheckBox.setSelected(useLatestVersion);
        Button installButton = new Button("Install/Upgrade");
        installButton.setOnAction(e -> {
            // first get rid of existing packages
            List<Integer> selectedRows = dataTable.getSelectionModel().getSelectedIndices();
            String installedPackageNames = "";

            setCursor(Cursor.WAIT);

            Map<Package, PackageVersion> packagesToInstall = new HashMap<>();
            PackageManager.useArchive(!useLatestVersion);
            for (int selRow : selectedRows) {
                Package selPackage = getSelectedPackage(selRow);
                if (selPackage != null) {
                	if (useLatestVersion) {
                		packagesToInstall.put(selPackage, selPackage.getLatestVersion());
                	} else {
                		PackageVersion version = (PackageVersion) Alert.showInputDialog( null, "Select Version for " + selPackage.getName(), 
                				"Select version", 
                				Alert.QUESTION_MESSAGE, null, 
                				selPackage.getAvailableVersions().toArray(), selPackage.getAvailableVersions().toArray()[0]);
                		if (version == null) {
                			return;
                		}
                		packagesToInstall.put(selPackage, version);
                	}
                }
            }

            try {
                populatePackagesToInstall(packageMap, packagesToInstall);

                prepareForInstall(packagesToInstall, false, null);

                if (getToDeleteListFile().exists()) {
                    Alert.showMessageDialog(box,
                            "<html><body><p style='width: 200px'>Upgrading packages on your machine requires BEAUti " +
                                    "to restart. Shutting down now.</p></body></html>");
                    System.exit(0);
                }

                installPackages(packagesToInstall, false, null);

                // Refresh classes:
                loadExternalJars();

                installedPackageNames = String.join(",",
                        packagesToInstall.keySet().stream()
                                .map(Package::toString)
                                .collect(Collectors.toList()));

                setCursor(Cursor.DEFAULT);

            } catch (DependencyResolutionException | IOException ex) {
                Alert.showMessageDialog(null, "Install failed because: " + ex.getMessage());
                setCursor(Cursor.DEFAULT);
            }

            resetPackages();
            dataTable.getSelectionModel().select(selectedRows.get(0));

            if (installedPackageNames.length()>0)
                Alert.showMessageDialog(null, "Package(s) "
                        + installedPackageNames + " installed. "
                        + "Note that any changes to the BEAUti "
                        + "interface will\n not appear until a "
                        + "new document is created or BEAUti is "
                        + "restarted.");
        });
        box.getChildren().add(installButton);

        Button uninstallButton = new Button("Uninstall");
        uninstallButton.setOnAction(e -> {
            StringBuilder removedPackageNames = new StringBuilder();
            List<Integer> selectedRows = dataTable.getSelectionModel().getSelectedIndices();

            for (int selRow : selectedRows) {
                Package selPackage = getSelectedPackage(selRow);
                if (selPackage != null) {
                    try {
                        if (selPackage.isInstalled()) {
                            setCursor(Cursor.WAIT);
                            List<String> deps = getInstalledDependencyNames(selPackage, packageMap);

                            if (deps.isEmpty()) {
                                String result = uninstallPackage(selPackage, selPackage.getInstalledVersion(), false, null);

                                if (result != null) {
                                    if (removedPackageNames.length() > 0)
                                        removedPackageNames.append(", ");
                                    removedPackageNames.append("'")
                                            .append(selPackage.getName())
                                            .append(" v")
                                            .append(selPackage.getInstalledVersion())
                                            .append("'");
                                }
                            } else {
                                throw new DependencyResolutionException("package " + selPackage
                                        + " is used by the following packages: "
                                + String.join(", ", deps) + "\n"
                                + "Remove those packages first.");
                            }

                            setCursor(Cursor.DEFAULT);
                        }

                        resetPackages();
                        dataTable.getSelectionModel().select(selectedRows.get(0));
                    } catch (IOException | DependencyResolutionException ex) {
                        Alert.showMessageDialog(null, "Uninstall failed because: " + ex.getMessage());
                        setCursor(Cursor.DEFAULT);
                    }
                }
            }

            if (getToDeleteListFile().exists()) {
                Alert.showMessageDialog(dataTable,
                        "<html><body><p style='width: 200px'>Removing packages on your machine requires BEAUti " +
                                "to restart. Shutting down now.</p></body></html>");
                System.exit(0);
            }

            if (removedPackageNames.length()>0)
                Alert.showMessageDialog(null, "Package(s) "
                        + removedPackageNames.toString() + " removed. "
                        + "Note that any changes to the BEAUti "
                        + "interface will\n not appear until a "
                        + "new document is created or BEAUti is "
                        + "restarted.");
        });
        box.getChildren().add(uninstallButton);

//        box.getChildren().add(new Separator());

        Button packageRepoButton = new Button("Package repositories");
        packageRepoButton.setOnAction(e -> {
                JPackageRepositoryDialog dlg = new JPackageRepositoryDialog(dlgPane);
                dlg.setVisible(true);
                resetPackages();
            });
        box.getChildren().add(packageRepoButton);

//        box.getChildren().add(new Separator());

//        Button closeButton = new Button("Close");
//        closeButton.setOnAction(e -> {
//            	if (dlgPane != null) {
//            		// dialog.close();
//            	} else {
//            		setVisible(false);
//            	}
//            });
//        box.getChildren().add(closeButton);

        Button button = new Button("?");
        button.setTooltip(new Tooltip(getPackageUserDir() + " " + getPackageSystemDir()));
        button.setOnAction(e -> {
                Alert.showMessageDialog(dataTable, "<html>By default, packages are installed in <br><br><em>" + getPackageUserDir() +
                        "</em><br><br>and are available only to you.<br>" +
                        "<br>Packages can also be moved manually to <br><br><em>" + getPackageSystemDir() +
                        "</em><br><br>which makes them available to all users<br>"
                        + "on your system.</html>", "Repository directory", null);
            });
        box.getChildren().add(button);
        for (Node n : box.getChildren()) {
        	if (n instanceof Control) {
        		((Control)n).setMinWidth(150);
        		((Control)n).setPadding(new Insets(5));
        	}
        }
        return box;
    }

//	class DataTableModel extends AbstractTableModel {
//		private static final long serialVersionUID = 1L;
//
//		String[] columnNames = {"Name", "Installed", "Latest", "Dependencies", "Link", "Detail"};
//
//		ImageView linkIcon = FXUtils.getIcon(BeautiPanel.ICONPATH + "link.png");
//
//        @Override
//		public int getColumnCount() {
//            return columnNames.length;
//        }
//
//        @Override
//		public int getRowCount() {
//            return packageList.size();
//        }
//
//        @Override
//		public Object getValueAt(int row, int col) {
//            Package aPackage = packageList.get(row);
//            switch (col) {
//                case 0:
//                    return aPackage.getName();
//                case 1:
//                    return aPackage.getInstalledVersion();
//                case 2:
//                    return aPackage.getLatestVersion();
//                case 3:
//                    return aPackage.getDependenciesString();
//                case 4:
//                    return aPackage.getProjectURL() != null ? linkIcon : null ;
//                case 5:
//                    return aPackage.getDescription();
//                default:
//                    throw new IllegalArgumentException("unknown column, " + col);
//            }
//        }
//
//        @Override
//		public String getColumnName(int column) {
//            return columnNames[column];
//        }
//
//        @Override
//		public String toString() {
//            StringBuffer buffer = new StringBuffer();
//
//            buffer.append(getColumnName(0));
//            for (int j = 1; j < getColumnCount(); j++) {
//                buffer.append("\t");
//                buffer.append(getColumnName(j));
//            }
//            buffer.append("\n");
//
//            for (int i = 0; i < getRowCount(); i++) {
//                buffer.append(getValueAt(i, 0));
//                for (int j = 1; j < getColumnCount(); j++) {
//                    buffer.append("\t");
//                    buffer.append(getValueAt(i, j));
//                }
//                buffer.append("\n");
//            }
//
//            return buffer.toString();
//        }
//    }
//


	public static Dialog<Package> asDialog(Parent pane) {
		Dialog<Package> dlg = new Dialog<>();
		dlg.setDialogPane(new JPackageDialog());
		dlg.setTitle("BEAST 2 Package Manager");
		dlg.getDialogPane().getButtonTypes().add(Alert.CLOSED_OPTION);
		
		Window stage = null;
		
		if (pane == null) {
			stage = dlg.getDialogPane().getScene().getWindow();
		} else {
			stage = pane.getScene().getWindow();
		}
//        int size = UIManager.getFont("Label.font").getSize();
//        dlg.setSize(690 * size / 13, 430 * size / 13);
        
        dlg.setX(stage.getX() + stage.getWidth() / 2 - dlg.getWidth() / 2);
        dlg.setY(stage.getY() + stage.getHeight() / 2 - dlg.getHeight() / 2);
        
        if (pane != null) {
        	pane.setCursor(Cursor.DEFAULT);
        }
        return dlg;
	}


	DialogPane dlgPane = null;

//    class PackageTable extends TableView<Package> {
//		private static final long serialVersionUID = 1L;
//
//        Map<Package, PackageVersion> packagesToInstall = new HashMap<>();
//
//        public PackageTable(TableModel dm) {
//            super(dm);
//        }
//
//        @Override
//        public Class<?> getColumnClass(int column) {
//            if (column != ((DataTableModel)getModel()).linkColumn)
//                return String.class;
//            else
//                return ImageIcon.class;
//        }
//
//        @Override
//        public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
//            Component c =  super.prepareRenderer(renderer, row, column);
//
//            Font font = c.getFont();
//            font.getFamily();
//            Font boldFont = Font.font(font.getName(), FontWeight.BOLD, FontPosture.ITALIC, font.getSize());
//
//            Package pkg = packageList.get(row);
//
//            if (! isRowSelected(row)) {
//                if (pkg.newVersionAvailable()) {
//                    if (pkg.isInstalled())
//                        c.setFont(boldFont);
//
//                    if (column == 2) {
//                        packagesToInstall.clear();
//                        packagesToInstall.put(pkg, pkg.getLatestVersion());
//                        try {
//                            populatePackagesToInstall(packageMap, packagesToInstall);
//                            c.setForeground(new Color(0, 150, 0));
//                        } catch (DependencyResolutionException ex) {
//                            c.setForeground(new Color(150, 0, 0));
//                        }
//                    } else {
//                        c.setForeground(Color.BLACK);
//                    }
//                } else {
//                    c.setForeground(Color.BLACK);
//                }
//            }
//
//            return c;
//        }
//
//        /**
//         *  Calculate the width based on the widest cell renderer for the
//         *  given column.
//         *
//         * @param cIdx column index
//         * @return maximum width.
//         */
//        private int getColumnDataWidth(int cIdx)
//        {
//            int preferredWidth = 0;
//            int maxWidth = getColumnModel().getColumn(cIdx).getMaxWidth();
//
//            for (int row = 0; row < getRowCount(); row++)
//            {
//                preferredWidth = Math.max(preferredWidth, getCellDataWidth(row, cIdx));
//
//                //  We've exceeded the maximum width, no need to check other rows
//
//                if (preferredWidth >= maxWidth)
//                    break;
//            }
//
//            preferredWidth = Math.max(preferredWidth, getHeaderWidth(cIdx));
//
//            return preferredWidth;
//        }
//
//        /*
//         *  Get the preferred width for the specified cell
//         */
//        private int getCellDataWidth(int row, int column)
//        {
//            //  Inovke the renderer for the cell to calculate the preferred width
//
//            TableCellRenderer cellRenderer = getCellRenderer(row, column);
//            Component c = prepareRenderer(cellRenderer, row, column);
//
//            return c.getPreferredSize().width + 2*getIntercellSpacing().width;
//        }
//
//        /*
//         *  Get the preferred width for the specified header
//         */
//        private int getHeaderWidth(int cIdx)
//        {
//            //  Inovke the renderer for the cell to calculate the preferred width
//
//            TableColumn column = getColumnModel().getColumn(cIdx);
//            TableCellRenderer cellRenderer = getDefaultRenderer(String.class);
//            Component c = cellRenderer.getTableCellRendererComponent(this, column.getHeaderValue(), false, false, -1, cIdx);
//
//            return c.getPreferredSize().width + 2*getIntercellSpacing().width;
//        }
//
//
//        void updateWidths() {
//            for (int cIdx = 0; cIdx < getColumnCount(); cIdx++) {
//                int width = getColumnDataWidth(cIdx);
//
//                TableColumn column = getColumnModel().getColumn(cIdx);
//                getTableHeader().setResizingColumn(column);
//                column.setWidth(width);
//            }
//        }
//    }
}
