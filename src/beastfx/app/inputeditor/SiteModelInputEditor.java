package beastfx.app.inputeditor;


//import javax.swing.event.DocumentEvent;
//import javax.swing.event.DocumentListener;


import beast.base.core.BEASTInterface;
import beast.base.core.Input;
import beast.base.evolution.alignment.Alignment;
import beast.base.evolution.likelihood.GenericTreeLikelihood;
import beast.base.evolution.sitemodel.SiteModel;
import beast.base.evolution.sitemodel.SiteModelInterface;
import beast.base.inference.*;
import beast.base.inference.operator.DeltaExchangeOperator;
import beast.base.inference.parameter.IntegerParameter;
import beast.base.inference.parameter.RealParameter;
import beastfx.app.util.FXUtils;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class SiteModelInputEditor extends BEASTObjectInputEditor {

    IntegerInputEditor categoryCountEditor;
    TextField categoryCountEntry;
    InputEditor gammaShapeEditor;
    ParameterInputEditor inVarEditor;

    // vars for dealing with mean-rate delta exchange operator
    CheckBox fixMeanRatesCheckBox;
    DeltaExchangeOperator operator;
    protected SmallLabel fixMeanRatesValidateLabel;

	public SiteModelInputEditor() {
		super();
	}
	public SiteModelInputEditor(BeautiDoc doc) {
		super(doc);
	}

    @Override
    public Class<?> type() {
        return SiteModelInterface.Base.class;
    }
    
    @Override
    public void init(Input<?> input, BEASTInterface beastObject, int itemNr,
    		ExpandOption isExpandOption, boolean addButtons) {
    	fixMeanRatesCheckBox = new CheckBox("Fix mean substitution rate");
    	fixMeanRatesCheckBox.setId("FixMeanMutationRate");
    	fixMeanRatesCheckBox.setDisable(doc.autoUpdateFixMeanSubstRate);
    	super.init(input, beastObject, itemNr, isExpandOption, addButtons);

		List<Operator> operators = ((MCMC) doc.mcmc.get()).operatorsInput.get();
    	fixMeanRatesCheckBox.setOnAction(e -> {
    			CheckBox averageRatesBox = (CheckBox) e.getSource();
				doFixMeanRates(averageRatesBox.isSelected());
				if (averageRatesBox.isSelected())
					// set up relative weights
					setUpOperator();
			});
    	operator = (DeltaExchangeOperator) doc.pluginmap.get("FixMeanMutationRatesOperator");
    	if (operator == null) {
    		operator = new DeltaExchangeOperator();
    		try {
    			operator.setID("FixMeanMutationRatesOperator");
				operator.initByName("weight", 2.0, "delta", 0.75);
			} catch (Throwable e1) {
				// ignore initAndValidate exception
			}
    		doc.addPlugin(operator);
    	}
		fixMeanRatesCheckBox.setSelected(operators.contains(operator));
		
		HBox box = FXUtils.newHBox();
		box.getChildren().add(fixMeanRatesCheckBox);
		// box.getChildren().add(new Separator());
		fixMeanRatesValidateLabel = new SmallLabel("x", "green");
		fixMeanRatesValidateLabel.setVisible(false);
		box.getChildren().add(fixMeanRatesValidateLabel);
		
        BeautiPanel.resizeList.clear();
        BeautiPanel.resizeList.add(pane);

    	int offset = 20;
        try {
        	if (doc.beauti.getCurrentPanel().listModel != null
        			&& doc.beauti.getCurrentPanel().listModel.size() > 1) {
        		offset += doc.beauti.getCurrentPanel().listOfPartitions.getWidth();
        	}
        	if (!Double.isNaN(doc.beauti.frame.getWidth())) {
        		pane.setMinSize(doc.beauti.frame.getWidth() - offset, doc.beauti.frame.getHeight()-100);
        	} else {
        		pane.setMinSize(1024 - offset, 768 - 80);
        	}
        } catch (NullPointerException e) {
        	// ignore
        	pane.setMinSize(1024 - offset, 768 - 80);
        }


    	if (doc.alignments.size() >= 1 && operator != null) {
    		VBox vbox = new VBox();
    		vbox.getChildren().addAll(pane, box);
    		pane = vbox;
    		getChildren().add(vbox);
        	//Pane component = (Pane) getChildren().get(0);
    		//component.getChildren().add(box);
    	}
		setUpOperator();
    }
    
//	@Override
//    public Class<?> [] types() {
//		Class<?>[] types = {SiteModel.class, SiteModel.Base.class}; 
//		return types;
//    }

	private void doFixMeanRates(boolean averageRates) {
		List<Operator> operators = ((MCMC) doc.mcmc.get()).operatorsInput.get();
		if (averageRates) {
			// connect DeltaExchangeOperator
			if (!operators.contains(operator)) {
				operators.add(operator);
			}
		} else {
			operators.remove(operator);
			fixMeanRatesValidateLabel.setVisible(false);
			repaint();
		}
	}

    public InputEditor createMutationRateEditor() {
    	SiteModel sitemodel = ((SiteModel) m_input.get()); 
        final Input<?> input = sitemodel.muParameterInput;
        ParameterInputEditor mutationRateEditor = new ParameterInputEditor(doc);
        mutationRateEditor.init(input, sitemodel, -1, ExpandOption.FALSE, true);
        mutationRateEditor.getEntry().setDisable(doc.autoUpdateFixMeanSubstRate);
        mutationRateEditor.m_isEstimatedBox.setOnAction(e -> {
        	mutationRateEditor.toggleEstimate();
        	setUpOperator();
        });
        
        return mutationRateEditor;
    }
	
	public InputEditor createGammaCategoryCountEditor() {
    	SiteModel sitemodel = ((SiteModel) m_input.get()); 
        final Input<?> input = sitemodel.gammaCategoryCount;
        categoryCountEditor = new IntegerInputEditor(doc) {
			@Override
			public void validateInput() {
        		super.validateInput();
            	SiteModel sitemodel = (SiteModel) m_beastObject; 
                if (sitemodel.gammaCategoryCount.get() < 2 && sitemodel.shapeParameterInput.get().isEstimatedInput.get()) {
                	m_validateLabel.setColor("orange");
                	m_validateLabel.setTooltip(new Tooltip("shape parameter is estimated, but not used"));
                	m_validateLabel.setVisible(true);
                }
        	};
        };
        
        categoryCountEditor.init(input, sitemodel, -1, ExpandOption.FALSE, true);
        categoryCountEntry = categoryCountEditor.getEntry();
        categoryCountEntry.setOnKeyReleased(e -> processEntry2());
//        categoryCountEntry.getDocument().addDocumentListener(new DocumentListener() {
//            @Override
//            public void removeUpdate(DocumentEvent e) {
//                processEntry2();
//            }
//
//            @Override
//            public void insertUpdate(DocumentEvent e) {
//                processEntry2();
//            }
//
//            @Override
//            public void changedUpdate(DocumentEvent e) {
//                processEntry2();
//            }
//        });
        
       	categoryCountEditor.validateInput();
        return categoryCountEditor;
    }

    void processEntry2() {
        String categories = categoryCountEntry.getText();
        try {
            int categoryCount = Integer.parseInt(categories);
            SiteModel s = (SiteModel) m_input.get();
            s.getInput("gammaCategoryCount").setValue(categoryCount, s);
        	RealParameter shapeParameter = ((SiteModel) m_input.get()).shapeParameterInput.get();
            if (!gammaShapeEditor.getComponent().isVisible() && categoryCount >= 2) {
            	// we are flipping from no gamma to gamma heterogeneity accross sites
            	// so set the estimate flag on the shape parameter
            	shapeParameter.isEstimatedInput.setValue(true, shapeParameter);            	
            } else if (gammaShapeEditor.getComponent().isVisible() && categoryCount < 2) {
            	// we are flipping from with gamma to no gamma heterogeneity accross sites
            	// so unset the estimate flag on the shape parameter
            	shapeParameter.isEstimatedInput.setValue(false, shapeParameter);            	
            }
            Object o = ((ParameterInputEditor)gammaShapeEditor).getComponent();
            if (o instanceof ParameterInputEditor) {
	            ParameterInputEditor e = (ParameterInputEditor) o;
	            e.m_isEstimatedBox.setSelected(shapeParameter.isEstimatedInput.get());
            }
            gammaShapeEditor.getComponent().setVisible(categoryCount >= 2);
            gammaShapeEditor.getComponent().setManaged(categoryCount >= 2);
            ((Node)gammaShapeEditor.getComponent()).prefHeight(categoryCount >= 2 ? USE_COMPUTED_SIZE : 0);
            repaint();
        } catch (java.lang.NumberFormatException e) {
            // ignore.
        }
    }

    public InputEditor createShapeEditor() throws NoSuchMethodException, SecurityException, ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        final Input<?> input = ((SiteModel) m_input.get()).shapeParameterInput;
        gammaShapeEditor = doc.getInputEditorFactory().createInputEditor(input, (BEASTInterface) m_input.get(), doc);
        boolean b = ((SiteModel) m_input.get()).gammaCategoryCount.get() >= 2;
        gammaShapeEditor.getComponent().setVisible(b);
        gammaShapeEditor.getComponent().setManaged(b);
        ((Node)gammaShapeEditor.getComponent()).prefHeight(b ? USE_COMPUTED_SIZE : 0);
        return gammaShapeEditor;
    }

    public InputEditor createProportionInvariantEditor() {
        final Input<?> input = ((SiteModel) m_input.get()).invarParameterInput;
        inVarEditor = new ParameterInputEditor(doc) {

			@Override
            public void validateInput() {
				RealParameter p = (RealParameter) m_input.get();
				if (p.isEstimatedInput.get() && p.valuesInput.get().get(0) <= 0.0) {
                    m_validateLabel.setVisible(true);
                    m_validateLabel.setTooltip(new Tooltip("<html><p>Proportion invariant should be non-zero when estimating</p></html>"));
                    return;
				}
				if (p.valuesInput.get().get(0) < 0.0 || p.valuesInput.get().get(0) >= 1.0) {
                    m_validateLabel.setVisible(true);
                    m_validateLabel.setTooltip(new Tooltip("<html><p>Proportion invariant should be from 0 to 1 (exclusive 1)</p></html>"));
                    return;
				}
            	super.validateInput();
            }
        };
        inVarEditor.init(input, (BEASTInterface) m_input.get(), -1, ExpandOption.FALSE, true);
        inVarEditor.addValidationListener(this);
        return inVarEditor;
    }

    public static boolean customConnector(BeautiDoc doc) {
 		try {
 	        DeltaExchangeOperator operator = (DeltaExchangeOperator) doc.pluginmap.get("FixMeanMutationRatesOperator");
 	        if (operator == null) {
 	        	return false;
 	        }

 	       	List<RealParameter> parameters = operator.parameterInput.get();
 	    	parameters.clear();
		   	//String weights = "";
		    CompoundDistribution likelihood = (CompoundDistribution) doc.pluginmap.get("likelihood");
		    boolean hasOneEstimatedRate = false;
		    List<String> rateIDs = new ArrayList<>();
		    List<Integer> weights = new ArrayList<>();
			for (Distribution d : likelihood.pDistributions.get()) {
				GenericTreeLikelihood treelikelihood = (GenericTreeLikelihood) d;
	    		Alignment data = treelikelihood.dataInput.get(); 
	    		int weight = data.getSiteCount();
	    		if (data.isAscertained) {
	    			weight -= data.getExcludedPatternCount();
	    		}
	    		if (treelikelihood.siteModelInput.get() instanceof SiteModel) {
		    		SiteModel siteModel = (SiteModel) treelikelihood.siteModelInput.get();
		    		RealParameter mutationRate = siteModel.muParameterInput.get();
		    		//clockRate.m_bIsEstimated.setValue(true, clockRate);
		    		if (mutationRate.isEstimatedInput.get()) {
		    			hasOneEstimatedRate = true;
		    			if (rateIDs.indexOf(mutationRate.getID()) == -1) {
			    			parameters.add(mutationRate);
			    			weights.add(weight);
			    			rateIDs.add(mutationRate.getID());
		    			} else {
		    				int k = rateIDs.indexOf(mutationRate.getID());
			    			weights.set(k,  weights.get(k) + weight);
		    			}
		    		}
	    		}
	    	}
			
			
		    IntegerParameter weightParameter;
			if (weights.size() == 0) {
		    	weightParameter = new IntegerParameter();
			} else {
				String weightString = "";
				for (int k : weights) {
					weightString += k + " ";
				}
		    	weightParameter = new IntegerParameter(weightString);
				weightParameter.setID("weightparameter");
				
			}
			weightParameter.isEstimatedInput.setValue(false, weightParameter);
	    	operator.parameterWeightsInput.setValue(weightParameter, operator);
	    	return hasOneEstimatedRate;
		} catch (Exception e) {
			
		}
		return false;
    }
    
    /** set up relative weights and parameter input **/
    public void setUpOperator() {
    	boolean isAllClocksAreEqual = true;
    	try {
    		boolean hasOneEstimatedRate = customConnector(doc);
		    if (doc.autoUpdateFixMeanSubstRate) {
		    	fixMeanRatesCheckBox.setSelected(hasOneEstimatedRate);
		    	doFixMeanRates(hasOneEstimatedRate);
		    }


     		try {
     	    	double commonClockRate = -1;
    		    CompoundDistribution likelihood = (CompoundDistribution) doc.pluginmap.get("likelihood");
    			for (Distribution d : likelihood.pDistributions.get()) {
    				GenericTreeLikelihood treelikelihood = (GenericTreeLikelihood) d;
    	    		if (treelikelihood.siteModelInput.get() instanceof SiteModel) {
    		    		SiteModel siteModel = (SiteModel) treelikelihood.siteModelInput.get();
    		    		RealParameter mutationRate = siteModel.muParameterInput.get();
    		    		//clockRate.m_bIsEstimated.setValue(true, clockRate);
    		    		if (mutationRate.isEstimatedInput.get()) {
    		    			if (commonClockRate < 0) {
    		    				commonClockRate = mutationRate.valuesInput.get().get(0);
    		    			} else {
    		    				if (Math.abs(commonClockRate - mutationRate.valuesInput.get().get(0)) > 1e-10) {
    		    					isAllClocksAreEqual = false;
    		    				}
    		    			}
    		    		}
    	    		}
    	    	}

    		} catch (Exception e) {
    			
    		}
   		
    		List<RealParameter> parameters = operator.parameterInput.get();
	    	if (!fixMeanRatesCheckBox.isSelected()) {
	    		fixMeanRatesValidateLabel.setVisible(false);
				repaint();
	    		return;
	    	}
	    	if (parameters.size() == 0) {
	    		fixMeanRatesValidateLabel.setVisible(true);
	    		fixMeanRatesValidateLabel.setColor("red");
	    		fixMeanRatesValidateLabel.setTooltip(new Tooltip("The model is invalid: At least one substitution rate should be estimated."));
				repaint();
	    		return;
	    	}
	    	if (!isAllClocksAreEqual) {
	    		fixMeanRatesValidateLabel.setVisible(true);
	    		fixMeanRatesValidateLabel.setColor("orange");
	    		fixMeanRatesValidateLabel.setTooltip(new Tooltip("Not all substitution rates are equal. Are you sure this is what you want?"));
	    	} else if (parameters.size() == 1) {
	    		fixMeanRatesValidateLabel.setVisible(true);
	    		fixMeanRatesValidateLabel.setColor("orange");
	    		fixMeanRatesValidateLabel.setTooltip(new Tooltip("At least 2 clock models should have their rate estimated"));
	    	} else if (parameters.size() < doc.getPartitions("SiteModel").size()) {
	    		fixMeanRatesValidateLabel.setVisible(true);
	    		fixMeanRatesValidateLabel.setColor("orange");
	    		fixMeanRatesValidateLabel.setTooltip(new Tooltip("Not all partitions have their rate estimated"));
	    	} else {
	    		fixMeanRatesValidateLabel.setVisible(false);
	    	}
			repaint();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
