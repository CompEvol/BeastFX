/*
 * Copyright (C) 2014 Tim Vaughan <tgvaughan@gmail.com>.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package beastfx.app.beauti;


import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import beast.pkgmgmt.Package;
import beast.pkgmgmt.PackageManager;
import beastfx.app.beauti.JPackageDialog.Package0;
import beastfx.app.util.Alert;
import beastfx.app.util.FXUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

/**
 * @author Tim Vaughan <tgvaughan@gmail.com>
 */
public class JPackageRepositoryDialog extends DialogPane {
    public class URL0 {
    	URL url;
    	public URL0(URL url) {
    		this.url = url;
    	}
    	
    	public String getURL() {
    		return url.getPath();
    	}
    	public void setURL(String URL) {
    		// ignore
    	}
    }

    private ObservableList<URL0> urls0;

    
	public JPackageRepositoryDialog(final Pane frame) {
        // super(frame);

        //setModal(true);
		Dialog dlg = new Dialog();
        dlg.setTitle("BEAST 2 Package Repository Manager");
        // Get current list of URLs:
        List<URL> urls;
        try {
            urls = PackageManager.getRepositoryURLs();
        } catch (MalformedURLException exception) {
            urls = new ArrayList<>();
            try {
                urls.add(new URL(PackageManager.PACKAGES_XML));
            } catch (MalformedURLException e) {
                // Hard-coded URL is broken. Should never happen!
                e.printStackTrace();
            }
        }
        
        List<URL0> urls0 = new ArrayList<>();
        for (URL url : urls) {
        	urls0.add(new URL0(url));
        }
        this.urls0 = FXCollections.observableArrayList(urls0);
        
        // Assemble table
        VBox pane = FXUtils.newVBox();
        //final RepoTableModel repoTableModel = new RepoTableModel(urls);
        
        final TableView<URL0> repoTable = new TableView<>();
        pane.getChildren().add(repoTable);
        
		TableColumn<URL0, String> col1 = new TableColumn<>("Package repository URL");
		col1.setPrefWidth(400);
		col1.setCellValueFactory(new PropertyValueFactory<URL0, String>("URL"));
		repoTable.getColumns().add(col1);

		repoTable.setItems(this.urls0);
//		int size = repoTable.getFont().getSize();
//		repoTable.setRowHeight(20 * size/13);
//        repoTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//        ScrollPane scrollPane = new ScrollPane();
//        scrollPane.setContent(repoTable);
//        getContentPane().add(scrollPane, BorderLayout.CENTER);
        
        // Add buttons
        HBox box = FXUtils.newHBox();
        
        // ADD URL
        Button addURLButton = new Button("Add URL");
        addURLButton.setOnAction(e -> {
            String newURLString = (String)Alert.showInputDialog(frame,
                    "Enter package repository URL",
                    "Add repository URL",Alert.PLAIN_MESSAGE, "http://");

            if (newURLString == null)
                return; // User canceled

            URL newURL = null;
            try {
                newURL = new URL(newURLString);
            } catch (MalformedURLException exception) {
                Alert.showMessageDialog(frame, "Invalid URL.");
                return;
            }

            for (URL0 url : this.urls0) {
	            if (url.getURL().equals(newURL.getPath())) {
	                Alert.showMessageDialog(frame, "Repository already exists!");
	                return;
	            }
            }

            try {
                if (newURL.getHost() == null)
                    return;

                InputStream is = newURL.openStream();
                is.close();

            } catch (IOException ex) {
                Alert.showMessageDialog(frame, "Could not access URL.");
                return;
            }

            // Add to table:
            this.urls0.add(new URL0(newURL));
            save();
        });
        box.getChildren().add(addURLButton);
        
        // DELETE URL
        Button deleteURLButton = new Button("Delete selected URL");
        deleteURLButton.setOnAction(e -> {
            if (Alert.showConfirmDialog(dlg.getDialogPane(), "Really delete this repository?", "Delete?", Alert.YES_NO_OPTION) ==Alert.YES_OPTION) {
                this.urls0.remove(repoTable.getSelectionModel().getSelectedIndex());
                save();
            }
        });
        deleteURLButton.setDisable(true);
        box.getChildren().add(deleteURLButton);
        
        // DONE
//        Button OKButton = new Button("Done");
//        OKButton.setOnAction(e -> {
//            PackageManager.saveRepositoryURLs(repoTableModel.urls);
//            dlg.close();
//        });
//        box.getChildren().add(OKButton);
        // getContentPane().add(box, BorderLayout.PAGE_END);

        // Action listeners to disable/enable delete button
        repoTable.getSelectionModel().selectedIndexProperty().
    	addListener(e -> {
            if (repoTable.getSelectionModel().getSelectedIndex() == 0)
                deleteURLButton.setDisable(true);
            else
                deleteURLButton.setDisable(false);
        });

        // Set size and location of dialog
//        Dimension2D dim = scrollPane.getPreferredSize();
//        Dimension2D dim2 = box.getPreferredSize();
//        pane.setPrefSize(dim.getWidth() + 30, dim.getHeight() + dim2.getHeight() + 30);
//        Point frameLocation = frame.getLocation();
//        Dimension frameSize = frame.getSize();
//        setLocation(frameLocation.x + frameSize.width / 2 - dim.getWidth() / 2,
//                frameLocation.y + frameSize.height / 2 - dim.getWidth() / 2);
        
        
        pane.getChildren().add(box);
        pane.setPrefWidth(400);
        pane.setPrefHeight(400);
        
        dlg.getDialogPane().setContent(pane);
        dlg.getDialogPane().getButtonTypes().add(Alert.CLOSED_OPTION);
        dlg.setResizable(true);
    	FXUtils.loadStyleSheet(pane.getScene());
        dlg.showAndWait();

    }

	private void save() {
		List<URL> urls = new ArrayList<>();
		for (URL0 url0 : this.urls0) {
			urls.add(url0.url);
		}
		PackageManager.saveRepositoryURLs(urls);
	}
	
    /**
     * Class of tables containing the current list of package repositories.
     */
//    class RepoTableModel extends AbstractTableModel {
//		private static final long serialVersionUID = 1L;
//		
//		public List<URL> urls;
//
//        public RepoTableModel(List<URL> urls) {
//            this.urls = urls;
//        }
//
//        @Override
//        public int getRowCount() {
//            return urls.size();
//        }
//
//        @Override
//        public int getColumnCount() {
//            return 1;
//        }
//
//        @Override
//        public String getColumnName(int column) {
//            return "Package repository URLs";
//        }
//
//        @Override
//        public Object getValueAt(int rowIndex, int columnIndex) {
//            return urls.get(rowIndex);
//        }
//    }

}
