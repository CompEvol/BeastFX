package beastfx.app.methodsection;




import java.awt.Component;
import java.awt.Desktop;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import beastfx.app.inputeditor.AlignmentListInputEditor;
import beastfx.app.inputeditor.BeautiDoc;
import beastfx.app.inputeditor.BeautiPanelConfig;
import beastfx.app.inputeditor.BeautiSubTemplate;
import beastfx.app.beauti.MRCAPriorProvider;
import beastfx.app.beauti.PriorListInputEditor;
import beastfx.app.beauti.PriorProvider;
import beastfx.app.inputeditor.TipDatesInputEditor;
import beastfx.app.inputeditor.BEASTObjectDialog;
import beastfx.app.inputeditor.BEASTObjectPanel;
import beastfx.app.inputeditor.InputEditor;
import beastfx.app.inputeditor.InputEditorFactory;
import beastfx.app.inputeditor.InputEditor.ButtonStatus;
import beastfx.app.inputeditor.InputEditor.ExpandOption;
import beast.base.core.BEASTInterface;
import beast.base.core.BEASTObject;
import beast.base.inference.Distribution;
import beast.base.core.Input;
import beast.base.inference.parameter.RealParameter;
import beast.base.inference.CompoundDistribution;
import beast.pkgmgmt.BEASTClassLoader;
import beast.pkgmgmt.PackageManager;
import javafx.embed.swing.SwingNode;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextArea;

public class ModelEditor extends BEASTObject {
	
	boolean useSwingThreads;
	
	public ModelEditor(boolean useSwingThreads) {
		this.useSwingThreads = useSwingThreads;
	}
	boolean refresh = false;
	
	public boolean handleCmd(String cmd, BeautiDoc doc, Component w) {
		String c = getAttribute("cmd", cmd);
		if (c == null) {
			return false;
		}
		System.out.println(c);
		switch (c) {
		case "PartitionEditor": return editPartition(cmd, doc, w);
		case "CitationPhrase": 
		case "Ref": return showCitation(cmd, doc, w);
		case "Text": return handleTextField(cmd, doc, w);
		case "RealParameter": return editRealParameter(cmd, doc, w);
		case "Select": return handleComboBox(cmd, doc, w);
		case "TipDates": return handleTipDates(cmd, doc, w);
		case "AddPrior": return handleAddPrior(cmd, doc, w);
		case "EditObject": return handleObject(cmd, doc, w);
		case "SetValue": return handleSetValue(cmd, doc, w);
		}
		
		return false;
	}

	
	

    private boolean handleSetValue(String cmd, BeautiDoc doc, Component w) {
		String src = getAttribute("source", cmd);
		String id = src.substring(0, src.lastIndexOf(' '));
		String inputName = src.substring(src.lastIndexOf(' ') + 1);
		BEASTInterface o = doc.pluginmap.get(id);
		Input<?> input = o.getInput(inputName);
		String value = getAttribute("value", cmd);
		input.setValue(value, o);
		return false;
	}




	class FlexibleInput<T> extends Input<T> {
        FlexibleInput() {
            // sets name to something non-trivial This is used by canSetValue()
            super("xx", "");
        }

        public FlexibleInput(T arrayList) {
            super("xx", "", arrayList);
        }

        @Override
		public void setType(Class<?> type) {
            theClass = type;
        }
    }

    FlexibleInput<?> _input = new FlexibleInput<>(new ArrayList<>());
	InputEditor editor = null;

	private boolean handleObject(String cmd, BeautiDoc doc, Component w) {
		String id = getAttribute("source", cmd);
		BEASTInterface o = doc.pluginmap.get(id);
		_input.setType(Object.class);
	    ((List<?>)_input.get()).clear();
	    ((List)_input.get()).add(o);
	    
	    editor = null;
		try {
			editor = doc.getInputEditorFactory().createInputEditor(_input, 0, o, true, 
					ExpandOption.TRUE, 
					ButtonStatus.ALL, null, doc);
		} catch (NoSuchMethodException | SecurityException | ClassNotFoundException | InstantiationException
				| IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
			return false;
		}

		
		if (useSwingThreads) {
	        JOptionPane optionPane = new JOptionPane(editor,
	                JOptionPane.PLAIN_MESSAGE,
	                JOptionPane.OK_CANCEL_OPTION,
	                null,
	                new String[]{"OK"},
	                "OK");
	        optionPane.setBorder(new EmptyBorder(12, 12, 12, 12));
			final JDialog dialog = optionPane.createDialog(w, "Edit " + o.getClass().getSimpleName());
			dialog.setResizable(true);
			dialog.pack();
			dialog.setAlwaysOnTop(true);
			dialog.setVisible(true);
		} else {
			final SwingNode swingNode = new SwingNode() {
				@Override
				public boolean isResizable() {
					return false;
				}
			};
			SwingUtilities.invokeLater(new Runnable() {
	            @Override
	            public void run() {
	                swingNode.setContent((JComponent) editor);
	            }
	        });
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle("Edit " + o.getClass().getSimpleName());
			alert.setHeaderText("Edit " + o.getClass().getSimpleName());
			alert.setContentText(null);
	
			DialogPane pane = alert.getDialogPane();
			pane.setExpandableContent(swingNode);
			pane.setExpanded(true);
			pane.setMinHeight(500);
			pane.setMinWidth(1024);
	
			alert.showAndWait();
		}		
		return true;
	}

	private boolean handleAddPrior(String cmd, BeautiDoc doc, Component w) {
		pluginSelector(doc);
		return false;
	}

    static private List<PriorProvider> priorProviders = null;
    
    private void initProviders(BeautiDoc doc) {
    	priorProviders = new ArrayList<>();
    	priorProviders.add(new MRCAPriorProvider());
    	
        // build up list of data types
        List<String> importerClasses = PackageManager.find(PriorProvider.class, new String[]{"beast.app"});
        for (String _class: importerClasses) {
        	try {
        		if (!_class.startsWith(this.getClass().getName())) {
        			PriorProvider priorProvider = (PriorProvider) BEASTClassLoader.forName(_class).getDeclaredConstructor().newInstance();
					priorProviders.add(priorProvider);
        		}
			} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }

    }
    
	PriorProvider priorProvider = null;
	
	protected void pluginSelector(BeautiDoc doc) {
    	if (priorProviders == null) {
    		initProviders(doc);
    	}
    	priorProvider = priorProviders.get(0);
    	if (priorProviders.size() > 1) {
			// let user choose a PriorProvider
			List<String> descriptions = new ArrayList<>();
			List<PriorProvider> availableProviders = new ArrayList<>();
			for (PriorProvider i : priorProviders) {
				if (i.canProvidePrior(doc)) {
					descriptions.add(i.getDescription());
					availableProviders.add(i);
				}
			}

			List<String> priorProviderStrings = new ArrayList<>();
			for (PriorProvider p : priorProviders) {
				priorProviderStrings.add(p.getDescription());
			}
			
			if (useSwingThreads) {
				JComboBox<String> optionBox = new JComboBox<>(priorProviderStrings.toArray(new String[]{}));
		        optionBox.setSelectedItem("All");
		        JOptionPane optionPane = new JOptionPane(optionBox,
		                JOptionPane.PLAIN_MESSAGE,
		                JOptionPane.OK_OPTION,
		                null,
		                new String[]{"OK"},
		                "OK");
		        optionPane.setBorder(new EmptyBorder(12, 12, 12, 12));
				final JDialog dialog = optionPane.createDialog(null, "Choose one:");
				dialog.setResizable(true);
				dialog.pack();
				dialog.setAlwaysOnTop(true);
				dialog.setVisible(true);
				int i = optionBox.getSelectedIndex();
				System.out.println("Your choice: " + priorProviderStrings.get(i));
				priorProvider = priorProviders.get(i);
			} else {
				ChoiceDialog<String> dialog = new ChoiceDialog<>(priorProviders.get(0).getDescription(), priorProviderStrings);
				dialog.setTitle("Add Prior Dialog");
				dialog.setHeaderText("Add Extra Prior");
				dialog.setContentText("Choose prior:");
	
				// Traditional way to get the response value.
				Optional<String> result = dialog.showAndWait();
				if (!result.isPresent()){
					return;
				}
				System.out.println("Your choice: " + result.get());
				int i = priorProviderStrings.indexOf(result.get());
				priorProvider = priorProviders.get(i);
			}
    	}
    	
//        List<BEASTInterface> selectedPlugins = new ArrayList<>();
        
        SwingUtilities.invokeLater( ()-> {
        		List<Distribution> distrs = priorProvider.createDistribution(doc);
        		CompoundDistribution prior = (CompoundDistribution) doc.pluginmap.get("prior");
        		prior.pDistributions.get().addAll(distrs);
        		for (Distribution distr : distrs) {
        			distr.getOutputs().add(prior);
        		}
                if (distrs != null) {
                	
                }
        	}
        );
//        if (distrs == null) {
//        	return null;
//        }
//        for (Distribution distr : distrs) {
//        	selectedPlugins.add(distr);
//        }
//        return selectedPlugins;
    }
 
	
	
	private boolean handleTipDates(String cmd, BeautiDoc doc, Component w) {
		BeautiPanelConfig config = new BeautiPanelConfig();
		config.initByName("path","tree",
				"panelname", "Partitions", "tiptext", "Data Partitions",
	            "hasPartitions", "Tree", "forceExpansion", "TRUE"
				);
		final Input<?> input = config.resolveInput(doc, 0);
		
		TipDatesInputEditor ie = new TipDatesInputEditor(doc);
        ((JComponent) ie).setBorder(BorderFactory.createEmptyBorder());
		ie.init(input, config, -1, ExpandOption.FALSE, false);
        ie.getComponent().setVisible(true);

		if (useSwingThreads) {
	        JOptionPane optionPane = new JOptionPane(ie,
	                JOptionPane.PLAIN_MESSAGE,
	                JOptionPane.OK_CANCEL_OPTION,
	                null,
	                new String[]{"OK"},
	                "OK");
	        optionPane.setBorder(new EmptyBorder(12, 12, 12, 12));
			final JDialog dialog = optionPane.createDialog(w, "Partition panel");
			dialog.setResizable(true);
			dialog.pack();
			dialog.setAlwaysOnTop(true);
			dialog.setVisible(true);
		} else {
			final SwingNode swingNode = new SwingNode() {
				@Override
				public boolean isResizable() {
					return false;
				}
			};

			SwingUtilities.invokeLater(new Runnable() {
	            @Override
	            public void run() {
	                swingNode.setContent(ie);
	            }
	        });

			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle("Tip dates Dialog");
			alert.setHeaderText("Edit tip dates");
			alert.setContentText(null);

			DialogPane pane = alert.getDialogPane();
			pane.setExpandableContent(swingNode);
			pane.setExpanded(true);
			pane.setMinHeight(500);
			pane.setMinWidth(1024);

			alert.showAndWait();
			
		}
        
        try {
			return true;
		} catch (Exception e1) {
			e1.printStackTrace();
		}		
    	return false;
	}

	public boolean showCitation(String cmd, BeautiDoc doc, Component w) {
		int counter = Integer.parseInt(getAttribute("counter", cmd));
		CitationPhrase citation = null;
		for (CitationPhrase p : CitationPhrase.citations.values()) {
			if (p.counter == counter) {
				citation = p;
				break;
			}
		}
		try {
			if (useSwingThreads) {
		    	JTextArea textArea = new JTextArea(citation.toReference());
		    	textArea.setLineWrap(true);
		    	textArea.setRows(5);
		    	textArea.setColumns(50);
		    	textArea.setEditable(true);
		    	JScrollPane scroller = new JScrollPane(textArea);
		        JOptionPane optionPane = new JOptionPane(scroller,
		                JOptionPane.PLAIN_MESSAGE,
		                JOptionPane.OK_CANCEL_OPTION,
		                null,
		                new String[]{"OK"},
		                "OK");
		        optionPane.setBorder(new EmptyBorder(12, 12, 12, 12));
				final JDialog dialog = optionPane.createDialog(w, "Citation");
				dialog.setResizable(true);
				dialog.pack();
				dialog.setAlwaysOnTop(true);
				dialog.setVisible(true);
				dialog.setFocusable(true);
				dialog.requestFocus();

		    	//JOptionPane.showMessageDialog(w, scroller);
			} else {
				Alert alert = new Alert(AlertType.CONFIRMATION);
				alert.setTitle("Reference Dialog");
				alert.setHeaderText("Open paper in browser?");
				alert.setContentText(citation.DOI);
	
	
				TextArea textArea = new TextArea(citation.toReference());
				textArea.setEditable(false);
				textArea.setWrapText(true);
	
				textArea.setMaxWidth(Double.MAX_VALUE);
				textArea.setMaxHeight(Double.MAX_VALUE);

				alert.getDialogPane().setExpandableContent(textArea);
				alert.getDialogPane().setExpanded(true);
	
				if (alert.showAndWait().get() == ButtonType.OK) {
					if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
						String url = citation.DOI;
						if (!url.toLowerCase().startsWith("http")) {
							url = "http://doi.org/" + url;
						}
					    Desktop.getDesktop().browse(new URI(url));
					}
//					StringSelection stringSelection = new StringSelection(citation.toReference());
//					Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
//					clipboard.setContents(stringSelection, null);				
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
    	return false;
	}


	private boolean editRealParameter(String cmd, BeautiDoc doc, Component w) {
		String id = getAttribute("id", cmd);
		RealParameter param = (RealParameter) doc.pluginmap.get(id);
		
		Set<Phrase> set = new LinkedHashSet<>(); 
		set.addAll(MethodsText.partitionGroupMap.get(param));
		if (set.size() > 1) {
			String [] options = new String[set.size() + 1];
			int i = 0;
			options[i++] = "All";
			for (Phrase p : set) {
				options[i++] = ((RealParameter) p.source).getID();
			}
			if (useSwingThreads) {
		        JComboBox<String> optionBox = new JComboBox<>(options);
		        optionBox.setSelectedItem("All");
		        JOptionPane optionPane = new JOptionPane(optionBox,
		                JOptionPane.PLAIN_MESSAGE,
		                JOptionPane.OK_OPTION,
		                null,
		                new String[]{"OK"},
		                "OK");
		        optionPane.setBorder(new EmptyBorder(12, 12, 12, 12));
				final JDialog dialog = optionPane.createDialog(optionBox, "Which parameter?");
				dialog.setResizable(true);
				dialog.pack();
				dialog.setAlwaysOnTop(true);
				dialog.setVisible(true);
				i = optionBox.getSelectedIndex();
				//i = JOptionPane.showOptionDialog(w, "Which parameter?", null, JOptionPane.OK_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, "All");
			} else {
				List<ButtonType> optionsB = new ArrayList<>();
				for (String option : options) {
					optionsB.add(new ButtonType(option));
				}
				Alert alert = new Alert(AlertType.CONFIRMATION);
				alert.setTitle("Confirmation Dialog with Custom Actions");
				alert.setHeaderText("Look, a Confirmation Dialog with Custom Actions");
				alert.setContentText("Choose your option.");
				alert.getButtonTypes().setAll(optionsB.toArray(new ButtonType[]{}));
				Optional<ButtonType> result = alert.showAndWait();
				i = optionsB.indexOf(result);
			}
			if (i > 0) {
				Phrase x = (Phrase) set.toArray()[i-1];
				set.clear();
				set.add(x);
				param = (RealParameter) doc.pluginmap.get(options[i]);
			}
		}
		if (useSwingThreads) {
	        BEASTObjectDialog dlg = new BEASTObjectDialog(param, RealParameter.class, doc);
	        dlg.setAlwaysOnTop(true);
	        if (dlg.showDialog()) {
	        	for (Phrase p : set) {
	        		param = (RealParameter) p.source;
	                String id2 = param.getID();
	        		dlg.accept((BEASTInterface) param, doc);
	        		param.setID(id2);
	        	}
	            return true;
	        }		
		} else {
	        // BEASTObjectDialog dlg = new BEASTObjectDialog(param, RealParameter.class, doc);
			BEASTObjectPanel panel = new BEASTObjectPanel(param, RealParameter.class, doc);
			final SwingNode swingNode = new SwingNode();

			SwingUtilities.invokeLater(new Runnable() {
	            @Override
	            public void run() {
	                swingNode.setContent(panel);
	            }
	        });

			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle("Parameter Dialog");
			alert.setHeaderText("Edit parameters");
			alert.setContentText(null);

			DialogPane pane = alert.getDialogPane();
			pane.setExpandableContent(swingNode);
			pane.setExpanded(true);
			pane.setMinHeight(500);
			pane.setMinWidth(500);


			if (alert.showAndWait().get() == ButtonType.OK) {
	        	for (Phrase p : set) {
	        		param = (RealParameter) p.source;
	                String id2 = param.getID();

	                for (Input<?> input : panel.m_beastObject.listInputs()) {
	                	if (input.get() != null && (input.get() instanceof List)) {
	                        // setInpuValue (below) on lists does not lead to expected result
	                		// it appends values to the list instead, so we have to clear it first
	                        List<?> list = (List<?>)param.getInput(input.getName()).get();
	                        list.clear();
	                	}
	                	param.setInputValue(input.getName(), input.get());
	                }
	                param.setID(panel.m_beastObject.getID());
	                if (doc != null) {
	                	doc.addPlugin(param);
	                }
	        		param.setID(id2);
	        	}
				return true;
			}
		}
    	return false;
	}


	private boolean editPartition(String cmd, BeautiDoc doc, Component w) {
		BeautiPanelConfig config = new BeautiPanelConfig();
		config.initByName("path","distribution/distribution[id=\"likelihood\"]/distribution/data",
				"panelname", "Partitions", "tiptext", "Data Partitions",
	            "hasPartitions", "none", "forceExpansion", "FALSE",
	            "type", "beast.base.evolution.alignment.Alignment"    				
				);
		final Input<?> input = config.resolveInput(doc, 0);    		
		AlignmentListInputEditor ie = new AlignmentListInputEditor(doc);
		ie.init(input, config, -1, ExpandOption.FALSE, false);
        ((JComponent) ie).setBorder(BorderFactory.createEmptyBorder());
        ie.getComponent().setVisible(true);

		if (useSwingThreads) {
	        JOptionPane optionPane = new JOptionPane(ie,
	                JOptionPane.PLAIN_MESSAGE,
	                JOptionPane.OK_CANCEL_OPTION,
	                null,
	                new String[]{"OK"},
	                "OK");
	        optionPane.setBorder(new EmptyBorder(12, 12, 12, 12));
			final JDialog dialog = optionPane.createDialog(w, "Partition panel");
			dialog.setResizable(true);
			dialog.pack();
			dialog.setAlwaysOnTop(true);
			dialog.setVisible(true);
		} else {
			final SwingNode swingNode = new SwingNode() {
				@Override
				public boolean isResizable() {
					return false;
				}
			};

			SwingUtilities.invokeLater(new Runnable() {
	            @Override
	            public void run() {
	                swingNode.setContent(ie);
	            }
	        });

			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle("Partition Dialog");
			alert.setHeaderText("Edit partitions");
			alert.setContentText(null);

			DialogPane pane = alert.getDialogPane();
			pane.setExpandableContent(swingNode);
			pane.setExpanded(true);
			pane.setMinHeight(500);
			pane.setMinWidth(1024);

			alert.showAndWait();
			
		}
        
        try {
			return true;
		} catch (Exception e1) {
			e1.printStackTrace();
		}		
    	return false;
	}


	private boolean handleComboBox(String cmd, BeautiDoc doc, Component w) {
		//JComboBox<String> b = (JComboBox<String>) e.getSource();
		String pid = getAttribute("source", cmd);
		String inputName = getAttribute("input", cmd);
		
		String selectedValue = getAttribute("value", cmd); 
		System.out.println("You selected " + selectedValue + " for " + cmd);
		BEASTInterface m_beastObject = doc.pluginmap.get(pid);
		Input<?> input = m_beastObject.getInput(inputName);
		
        InputEditorFactory inputEditorFactory = doc.getInputEditorFactory();
        List<BeautiSubTemplate> plugins = inputEditorFactory.getAvailableTemplates(input, m_beastObject, null, doc);

        BeautiSubTemplate selected = null;
        for (BeautiSubTemplate template : plugins) {
        	if (template.toString().equals(selectedValue)) {
        		selected = template;
        		break;
        	}
        }
        
        
        String id = getAttribute("object", cmd);
        BEASTInterface oldBeastObject = doc.pluginmap.get(id); // (BEASTInterface) input.get();
        BEASTInterface newBeastObject = null;
        //String id = beastObject.getID();
        String partition = id.indexOf('.') >= 0 ? 
        		id.substring(id.indexOf('.') + 1) : "";
        if (partition.indexOf(':') >= 0) {
        	partition = id.substring(id.indexOf(':') + 1);
        }
        if (selected.equals(InputEditor.NO_VALUE)) {
            newBeastObject = null;
        } else {
            try {
                if (input.get() instanceof  List) {
                	List objects = ((List)input.get());
                	int i = objects.indexOf(oldBeastObject);
                	newBeastObject = selected.createSubNet(doc.getContextFor(oldBeastObject), objects, i, true);
                } else {
                	newBeastObject = selected.createSubNet(doc.getContextFor(oldBeastObject), m_beastObject, input, true);
                }
            } catch (Exception ex) {
            	messageDialog("Could not select beastObject: " +
                        ex.getClass().getName() + " " +
                        ex.getMessage());
            }
        }

        try {
            if (newBeastObject == null) {
                //b.setSelectedItem(InputEditor.NO_VALUE);
            } else {
                if (!input.canSetValue(newBeastObject, m_beastObject)) {
                    throw new IllegalArgumentException("Cannot set input to this value");
                }
            }

            if (!(input.get() instanceof  List)) {
            	input.setValue(newBeastObject, m_beastObject);
            } else {
            	newBeastObject.getOutputs().add(m_beastObject);
            }

            return true;
        } catch (Exception ex) {
            id = ((BEASTInterface) input.get()).getID();
            //b.setSelectedItem(id);
            ex.printStackTrace();
            messageDialog("Could not change beastObject: " +
                    ex.getClass().getName() + " " +
                    ex.getMessage() 
            );
        }
    	return false;
	}


	private void messageDialog(String string) {
    	if (useSwingThreads) {
	        JOptionPane optionPane = new JOptionPane("Message: ",
	                JOptionPane.PLAIN_MESSAGE,
	                JOptionPane.OK_OPTION,
	                null,
	                new String[]{"OK"},
	                "OK");
	        optionPane.setBorder(new EmptyBorder(12, 12, 12, 12));
			final JDialog dialog = optionPane.createDialog(string);
			dialog.setResizable(true);
			dialog.pack();
			dialog.setAlwaysOnTop(true);
			dialog.setVisible(true);
        } else {
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Information");
			alert.setHeaderText("You got a new message:");
			alert.setContentText(string);
			alert.showAndWait();
		}		
	}

	private boolean handleTextField(String cmd, BeautiDoc doc, Component w) {
		//JTextField b = (JTextField) e.getSource();
		String source = getAttribute("source", cmd);
		int k = source.lastIndexOf(' ');
		String id = source.substring(0, k);
		String inputName = source.substring(k + 1);
		String value = getAttribute("value", cmd);
		BEASTInterface o = doc.pluginmap.get(id);
		Input<?> input = o.getInput(inputName);
		if (input.canSetValue(value, o)) {
			try {
				input.setValue(value, o);
			} catch (RuntimeException ex) {
				// could not set the value after all...
			}
		}
		System.out.println(id + "." + input.getName() + " set to " + input.get().toString());
    	return false;
	}


	public static String getAttribute(String attr, String cmd) {
		cmd = cmd.replaceAll("%20", " ");
		int i = cmd.indexOf(attr + "=");
		if (i < 0) {
			return null;
		}
		String value = cmd.substring(i + attr.length() + 1);
		if (value.indexOf('=') > 0) {
			value = value.substring(0, value.indexOf('='));
		}
		if (value.charAt(0) == '"') {
			value = value.substring(1, value.indexOf('"', 1));
		} else if (value.charAt(0) == '\'') {
			value = value.substring(1, value.indexOf('\'', 1));
		} else if (value.indexOf(' ') > 0) {
			value = value.substring(0, value.lastIndexOf(' '));
		}
		return value;
	}

	@Override
	public void initAndValidate() {
	}

}
