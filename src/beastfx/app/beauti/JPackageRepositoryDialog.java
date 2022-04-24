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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.plaf.multi.MultiMenuItemUI;
import javax.swing.table.AbstractTableModel;

import beast.pkgmgmt.PackageManager;
import beastfx.app.util.Alert;
import javafx.geometry.Dimension2D;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

/**
 * @author Tim Vaughan <tgvaughan@gmail.com>
 */
public class JPackageRepositoryDialog extends DialogPane {

	private static final long serialVersionUID = 1L;

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

        // Assemble table
        VBox pane = new VBox();
        final RepoTableModel repoTableModel = new RepoTableModel(urls);
        final ListView<URL> repoTable = new ListView<>();
        pane.getChildren().add(repoTable);
        repoTable.getItems().addAll(urls);
//		int size = repoTable.getFont().getSize();
//		repoTable.setRowHeight(20 * size/13);
//        repoTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//        ScrollPane scrollPane = new ScrollPane();
//        scrollPane.setContent(repoTable);
//        getContentPane().add(scrollPane, BorderLayout.CENTER);
        
        // Add buttons
        HBox box = new HBox();
        
        // ADD URL
        Button addURLButton = new Button("Add URL");
        addURLButton.setOnAction(e -> {
            String newURLString = (String)Alert.showInputDialog(frame,
                    "Enter package repository URL",
                    "Add repository URL",Alert.PLAIN_MESSAGE, null, null, "http://");

            if (newURLString == null)
                return; // User canceled

            URL newURL = null;
            try {
                newURL = new URL(newURLString);
            } catch (MalformedURLException exception) {
                Alert.showMessageDialog(frame, "Invalid URL.");
                return;
            }

            if (repoTableModel.urls.contains(newURL)) {
                Alert.showMessageDialog(frame, "Repository already exists!");
                return;
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
            repoTableModel.urls.add(newURL);
            repoTableModel.fireTableDataChanged();
        });
        box.getChildren().add(addURLButton);
        
        // DELETE URL
        Button deleteURLButton = new Button("Delete selected URL");
        deleteURLButton.setOnAction(e -> {
            if (Alert.showConfirmDialog(dlg.getDialogPane(), "Really delete this repository?", "Delete?", Alert.YES_NO_OPTION) ==Alert.YES_OPTION) {
                repoTableModel.urls.remove(repoTable.getSelectionModel().getSelectedIndex());
                repoTableModel.fireTableDataChanged();
            }
        });
        deleteURLButton.setDisable(true);
        box.getChildren().add(deleteURLButton);
        
        // DONE
        Button OKButton = new Button("Done");
        OKButton.setOnAction(e -> {
            PackageManager.saveRepositoryURLs(repoTableModel.urls);
            setVisible(false);
        });
        box.getChildren().add(OKButton);
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
        
        dlg.getDialogPane().setContent(pane);
        dlg.showAndWait();

    }

    /**
     * Class of tables containing the current list of package repositories.
     */
    class RepoTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 1L;
		
		public List<URL> urls;

        public RepoTableModel(List<URL> urls) {
            this.urls = urls;
        }

        @Override
        public int getRowCount() {
            return urls.size();
        }

        @Override
        public int getColumnCount() {
            return 1;
        }

        @Override
        public String getColumnName(int column) {
            return "Package repository URLs";
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return urls.get(rowIndex);
        }
    }

}
