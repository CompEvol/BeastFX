package beastfx.app.methodsection;


import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;

import beast.base.inference.MCMC;
import beast.base.parser.ClassToPackageMap;
import beast.pkgmgmt.PackageManager;
import beastfx.app.beauti.BeautiHelpAction;
import beastfx.app.inputeditor.BeautiDoc;
import beastfx.app.methodsection.implementation.BEASTObjectMethodsText;


/**
 * Custom menu for BEAUti, which appears as "Help => Methods Section" menu item
 **/
public class MethodsSectionHelpMenu extends BeautiHelpAction {

	BeautiDoc doc;
	MCMC mcmc;

	public MethodsSectionHelpMenu(BeautiDoc doc) {
		super("Methods section", "Attempts to convert model into a methods section", "methods", -1);
		this.doc = doc;
		BEASTObjectMethodsText.setBeautiCFG(doc.beautiConfig);
		setOnAction(a->actionPerformed());
	}
	 
	public void actionPerformed() {
		beast.base.inference.Runnable runnable = doc.mcmc.get();
		if (runnable instanceof MCMC) {
			mcmc = (MCMC) runnable;
			javax.swing.SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					JFrame frame = new JFrame("Methods text");

					MethodsWindow methodsWindow = new MethodsWindow();
					methodsWindow.setOpaque(true);
					frame.setContentPane(methodsWindow);

					frame.pack();
					frame.setVisible(true);
					methodsWindow.actionPerformed(null);
					}
			});
		}
	}

	public class MethodsWindow extends JPanel implements ActionListener, PropertyChangeListener {

		private static final long serialVersionUID = 1L;

		private JProgressBar progressBar;
		private JComboBox<CitationPhrase.mode> citationMode;
		private JTextArea textArea;
		private Task task;

		class Task extends SwingWorker<Void, Void> {
			/*
			 * Main task. Executed in background thread.
			 */
			@Override
			public Void doInBackground() {
				setProgress(0);
				
				MethodsText.initNameMap();
				
		    	if (ClassToPackageMap.getRawClassToPackageMap().size() == 0) {
		    		List<String> dirs = PackageManager.getBeastDirectories();
		    		int k = 0;
		            for (String jarDirName : dirs) {
		            	ClassToPackageMap.initPackageMap(jarDirName);
		            	k++;
						setProgress(90 * k / dirs.size());
		            }
		    	}
				
				XML2Text xml2textProducer = new XML2Text(doc);
				try {
					xml2textProducer.initialise(mcmc);
				} catch (Exception e) {
					e.printStackTrace();
				}
				setProgress(95);
				List<Phrase> m = xml2textProducer.getPhrases();
				String text = Phrase.toText(doc, m);
				setProgress(100);
				textArea.setText(text);
				return null;
			}

			@Override
			public void done() {
				citationMode.setEnabled(true);
				progressBar.setVisible(false);
				setCursor(null); // turn off the wait cursor
			}

			public void setProgress_(int percentage) {
				super.setProgress(percentage);
			}
		}

		
		public MethodsWindow() {
			super(new BorderLayout());

			// Create the demo's UI.
			citationMode = new JComboBox<>(CitationPhrase.mode.values());
			citationMode.setActionCommand("start");
			citationMode.addActionListener(this);

			progressBar = new JProgressBar(0, 100);
			progressBar.setValue(0);
			progressBar.setStringPainted(true);

			textArea = new JTextArea(40, 50);
			textArea.setWrapStyleWord(true);
			textArea.setLineWrap(true);
			textArea.setMargin(new Insets(5, 5, 5, 5));
			textArea.setEditable(false);

			JButton editButton = new JButton("Edit interactively");
			editButton.addActionListener(e -> {
				XML2HTMLPaneFX.launchForDoc(doc);
			});
			add(editButton, BorderLayout.SOUTH);
			
			JPanel panel = new JPanel();
			panel.add(new JLabel("Citation mode:"));
			panel.add(citationMode);
			panel.add(progressBar);

			add(panel, BorderLayout.PAGE_START);
			add(new JScrollPane(textArea), BorderLayout.CENTER);
			setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

		}

		public void actionPerformed(ActionEvent evt) {
			citationMode.setEnabled(false);
			progressBar.setVisible(true);
			CitationPhrase.CitationMode = (CitationPhrase.mode) citationMode.getSelectedItem();
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			task = new Task();
			task.addPropertyChangeListener(this);
			task.execute();
		}

		public void propertyChange(PropertyChangeEvent evt) {
			if ("progress" == evt.getPropertyName()) {
				int progress = (Integer) evt.getNewValue();
				progressBar.setValue(progress);
				if (task.getProgress() < 100) {
					textArea.append(task.getProgress() + " completed.\n");
				}
			}
		}

	}

	
}
