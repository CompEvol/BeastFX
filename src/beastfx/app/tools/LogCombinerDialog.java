/*
 * LogCombinerDialog.java
 *
 * Copyright (C) 2002-2006 Alexei Drummond and Andrew Rambaut
 *
 * This file is part of BEAST.
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership and licensing.
 *
 * BEAST is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 *  BEAST is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with BEAST; if not, write to the
 * Free Software Foundation, Inc., 51 Franklin St, Fifth Floor,
 * Boston, MA  02110-1301  USA
 */

package beastfx.app.tools;

import java.io.IOException;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Optional;

import beast.base.core.Log;
import beast.pkgmgmt.BEASTVersion;
import beastfx.app.beauti.ThemeProvider;
import beastfx.app.util.Console;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;

public class LogCombinerDialog extends Console {

	@Override
        protected void createDialog() {

        Dialog<String> dialog = new Dialog<>();
            dialog.setTitle("LogCombiner " + BEASTVersion.INSTANCE.getVersion());
            FXMLLoader fl = new FXMLLoader();
            fl.setClassLoader(getClass().getClassLoader());
            fl.setLocation(LogCombiner.class.getResource("logcombiner.fxml"));
            DialogPane root = null;
            try {
                    Object o = fl.load();
                    root = (DialogPane) o;
            } catch (IOException e) {
                    e.printStackTrace();
            }
            dialog.getDialogPane();
            dialog.setDialogPane(root);
            ThemeProvider.loadStyleSheet(root.getScene());        

            Object o = fl.getController();
            LogCombinerController controller = null;
            if (o != null) {
            	controller = (LogCombinerController) o;
            } else {
            	System.exit(0);
            }
                    
            // Showing the dialog on clicking the button
            Optional<String> result = dialog.showAndWait();
            if (result.toString().toLowerCase().contains("cancel")) {
            	System.exit(0);
            }
            
            LogCombiner combiner = new LogCombiner();
            combiner.m_bIsTreeLog = controller.isTreeFiles();
            combiner.m_bUseDecimalFormat = controller.convertToDecimal();
            if (combiner.m_bUseDecimalFormat) {
                combiner.format = new DecimalFormat("#.############", new DecimalFormatSymbols(Locale.US));
            }
            if (!controller.renumberOutputStates()) {
                combiner.m_nSampleInterval = -1;
            }
            if (controller.isResampling()) {
                combiner.m_nResample = controller.getResampleFrequency();
            }

            String[] inputFiles = controller.getFileNames();
            int[] burnins = controller.getBurnins();

            combiner.m_sFileOut = controller.getOutputFileName();

            try {
                if (combiner.m_sFileOut == null) {
                	Log.warning.println("No output file specified");
                } else {
                    combiner.m_out = new PrintStream(combiner.m_sFileOut);
                }

                combiner.combineLogs(inputFiles, burnins);

            } catch (Exception ex) {
            	Log.warning.println("Exception: " + ex.getMessage());
                ex.printStackTrace();
            }
            System.out.println("Finished - Quit program to exit.");
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            
        }
}
